// Gemini handles long inputs, but very large pages slow responses down without adding much.
const MAX_CHARS = 30_000;

export interface PageText {
  text: string;
  title: string;
  source: "selection" | "page";
  truncated: boolean;
}

/** Reads the highlighted text in the active tab, or the page's main content if nothing is selected. */
export async function readActiveTab(): Promise<PageText> {
  const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
  if (!tab?.id || !tab.url || !/^https?:/.test(tab.url)) {
    throw new Error("Open a regular web page to analyse it. Browser pages like chrome:// can't be read.");
  }

  const [injection] = await chrome.scripting.executeScript({
    target: { tabId: tab.id },
    func: () => {
      const selection = window.getSelection()?.toString().trim() ?? "";
      if (selection) {
        return { text: selection, source: "selection" as const };
      }
      const main = document.querySelector<HTMLElement>("article, main, [role='main']") ?? document.body;
      return { text: main.innerText, source: "page" as const };
    },
  });

  const result = injection?.result;
  const text = result?.text.replace(/\n{3,}/g, "\n\n").trim() ?? "";
  if (!result || !text) {
    throw new Error("This page has no readable text.");
  }

  return {
    text: text.slice(0, MAX_CHARS),
    title: tab.title ?? "this page",
    source: result.source,
    truncated: text.length > MAX_CHARS,
  };
}
