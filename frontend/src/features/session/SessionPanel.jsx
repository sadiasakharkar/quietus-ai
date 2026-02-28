import { apiClient } from "../../shared/api/client";

export function SessionPanel({ token, session, setSession, setError }) {
  async function startSession() {
    setError("");
    try {
      const started = await apiClient.startSession(token);
      setSession(started);
    } catch (err) {
      setError(err.message);
    }
  }

  async function endSession() {
    if (!session?.sessionId) return;
    setError("");
    try {
      const ended = await apiClient.endSession(token, session.sessionId);
      setSession(ended);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="rounded-2xl border border-slate-700/60 bg-panel/80 p-5">
      <h2 className="text-lg font-semibold">Session Control</h2>
      <p className="mt-1 text-sm text-slate-300">Create and close distress analysis sessions.</p>

      <div className="mt-4 flex flex-wrap gap-3">
        <button
          className="rounded-lg bg-accent px-4 py-2 text-sm font-semibold text-slate-950 disabled:opacity-70"
          onClick={startSession}
          disabled={session?.status === "ACTIVE"}
          type="button"
        >
          Start Session
        </button>
        <button
          className="rounded-lg bg-danger px-4 py-2 text-sm font-semibold text-white disabled:opacity-70"
          onClick={endSession}
          disabled={!session?.sessionId || session?.status === "ENDED"}
          type="button"
        >
          End Session
        </button>
      </div>

      <div className="mt-4 rounded-lg border border-slate-600 bg-panelAlt p-3 text-sm">
        <p><span className="text-slate-400">Session ID:</span> {session?.sessionId || "-"}</p>
        <p><span className="text-slate-400">Status:</span> {session?.status || "-"}</p>
      </div>
    </div>
  );
}
