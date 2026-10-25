import { useEffect, useRef, useState } from "react";
import { analyse, OPERATIONS, type Operation } from "./api";
import Markdown from "./Markdown";
import { readActiveTab, type PageText } from "./page";
import { DEFAULT_BACKEND_URL, loadSettings, saveBackendUrl, saveNotes } from "./storage";

type Result =
  | { status: "idle" }
  | { status: "loading" }
  | { status: "error"; message: string }
  | { status: "done"; text: string; page: PageText; operation: Operation };

export default function App() {
  const [operation, setOperation] = useState<Operation>("summarize");
  const [result, setResult] = useState<Result>({ status: "idle" });
  const [backendUrl, setBackendUrl] = useState(DEFAULT_BACKEND_URL);
  const [notes, setNotes] = useState("");
  const [notesSaved, setNotesSaved] = useState(true);
  const [copied, setCopied] = useState(false);
  const loaded = useRef(false);

  useEffect(() => {
    loadSettings().then((settings) => {
      setBackendUrl(settings.backendUrl);
      setNotes(settings.notes);
      loaded.current = true;
    });
  }, []);

  // Autosave notes shortly after the user stops typing.
  useEffect(() => {
    if (!loaded.current) return;
    setNotesSaved(false);
    const timer = setTimeout(() => saveNotes(notes).then(() => setNotesSaved(true)), 500);
    return () => clearTimeout(timer);
  }, [notes]);

  async function run() {
    setResult({ status: "loading" });
    setCopied(false);
    try {
      const page = await readActiveTab();
      const text = await analyse(backendUrl, page.text, operation);
      setResult({ status: "done", text, page, operation });
    } catch (error) {
      setResult({ status: "error", message: error instanceof Error ? error.message : String(error) });
    }
  }

  async function copyResult() {
    if (result.status !== "done") return;
    await navigator.clipboard.writeText(result.text);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  }

  function appendToNotes() {
    if (result.status !== "done") return;
    const heading = `## ${result.page.title}`;
    setNotes((current) => [current.trim(), heading, result.text].filter(Boolean).join("\n\n"));
  }

  const loading = result.status === "loading";

  return (
    <main className="panel">
      <header>
        <h1>Research Assistant</h1>
        <p className="muted">Highlight text to analyse just that, or leave nothing selected to use the whole page.</p>
      </header>

      <div className="segmented" role="radiogroup" aria-label="What to generate">
        {OPERATIONS.map((option) => (
          <button
            key={option.id}
            role="radio"
            aria-checked={operation === option.id}
            className={operation === option.id ? "active" : ""}
            onClick={() => setOperation(option.id)}
            disabled={loading}
          >
            {option.label}
          </button>
        ))}
      </div>

      <button className="primary" onClick={run} disabled={loading}>
        {loading ? "Analysing…" : "Analyse this page"}
      </button>

      {result.status === "error" && <div className="card error">{result.message}</div>}

      {result.status === "done" && (
        <section className="card">
          <div className="card-header">
            <span className="muted">
              {OPERATIONS.find((o) => o.id === result.operation)?.label} of{" "}
              {result.page.source === "selection" ? "your selection" : "the page"}
              {result.page.truncated && " (first 30,000 characters)"}
            </span>
            <div className="card-actions">
              <button className="ghost" onClick={copyResult}>{copied ? "Copied" : "Copy"}</button>
              <button className="ghost" onClick={appendToNotes}>Add to notes</button>
            </div>
          </div>
          <Markdown text={result.text} />
        </section>
      )}

      <section className="notes">
        <div className="notes-header">
          <h2>Notes</h2>
          <span className="muted small">{notesSaved ? "Saved" : "Saving…"}</span>
        </div>
        <textarea
          value={notes}
          onChange={(event) => setNotes(event.target.value)}
          placeholder="Your research notes are saved automatically."
        />
      </section>

      <details className="settings">
        <summary>Settings</summary>
        <label>
          Backend URL
          <input
            type="url"
            value={backendUrl}
            onChange={(event) => setBackendUrl(event.target.value)}
            onBlur={() => saveBackendUrl(backendUrl.trim() || DEFAULT_BACKEND_URL)}
          />
        </label>
      </details>
    </main>
  );
}
