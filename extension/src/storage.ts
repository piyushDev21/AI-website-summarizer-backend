export const DEFAULT_BACKEND_URL = "http://localhost:8080";

export interface Settings {
  backendUrl: string;
  notes: string;
}

export async function loadSettings(): Promise<Settings> {
  const stored = await chrome.storage.local.get(["backendUrl", "researchNotes"]);
  return {
    backendUrl: stored.backendUrl || DEFAULT_BACKEND_URL,
    notes: stored.researchNotes ?? "",
  };
}

export function saveBackendUrl(backendUrl: string): Promise<void> {
  return chrome.storage.local.set({ backendUrl });
}

export function saveNotes(notes: string): Promise<void> {
  return chrome.storage.local.set({ researchNotes: notes });
}
