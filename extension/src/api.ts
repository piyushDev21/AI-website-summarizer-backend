export type Operation = "summarize" | "key-points" | "suggest";

export const OPERATIONS: { id: Operation; label: string }[] = [
  { id: "summarize", label: "Summary" },
  { id: "key-points", label: "Key points" },
  { id: "suggest", label: "Related topics" },
];

interface ResearchResponse {
  operation: Operation;
  result: string;
}

export async function analyse(backendUrl: string, content: string, operation: Operation): Promise<string> {
  let response: Response;
  try {
    response = await fetch(`${backendUrl.replace(/\/+$/, "")}/api/research/process`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ content, operation }),
    });
  } catch {
    throw new Error(`Can't reach the backend at ${backendUrl}. Is it running?`);
  }

  if (!response.ok) {
    // The backend answers errors with RFC 9457 problem details.
    const problem = await response.json().catch(() => null);
    throw new Error(problem?.detail ?? `Request failed with status ${response.status}`);
  }

  const body: ResearchResponse = await response.json();
  return body.result;
}
