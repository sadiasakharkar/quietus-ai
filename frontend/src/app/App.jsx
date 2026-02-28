import { useMemo, useState } from "react";

import { AuthPanel } from "../features/auth/AuthPanel";
import { CapturePanel } from "../features/capture/CapturePanel";
import { ResultsPanel } from "../features/results/ResultsPanel";
import { SessionPanel } from "../features/session/SessionPanel";
import { apiClient } from "../shared/api/client";
import { modalityClients } from "../shared/api/modalityClients";
import { createUuid } from "../shared/utils/id";

export function App() {
  const [auth, setAuth] = useState(() => {
    const token = localStorage.getItem("quietus_token");
    const userRaw = localStorage.getItem("quietus_user");
    return token && userRaw ? { token, user: JSON.parse(userRaw) } : null;
  });

  const [session, setSession] = useState(null);
  const [capture, setCapture] = useState(null);
  const [fusionResult, setFusionResult] = useState(null);
  const [explanation, setExplanation] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  const token = auth?.token;

  const sessionActive = useMemo(() => session?.status === "ACTIVE", [session]);

  function handleAuthenticated(login) {
    const next = { token: login.accessToken, user: login.user };
    setAuth(next);
    localStorage.setItem("quietus_token", next.token);
    localStorage.setItem("quietus_user", JSON.stringify(next.user));
  }

  function logout() {
    setAuth(null);
    setSession(null);
    setCapture(null);
    setFusionResult(null);
    setExplanation(null);
    localStorage.removeItem("quietus_token");
    localStorage.removeItem("quietus_user");
  }

  async function runPipeline() {
    if (!token || !session?.sessionId || !capture?.videoBlob || !capture?.audioBlob) {
      setError("Capture video and audio in an active session first.");
      return;
    }

    setBusy(true);
    setError("");
    setFusionResult(null);
    setExplanation(null);

    const chunkId = createUuid();

    try {
      const [visionData, audioData] = await Promise.all([
        modalityClients.analyzeVision({
          sessionId: session.sessionId,
          chunkId,
          videoBlob: capture.videoBlob
        }),
        modalityClients.analyzeAudio({
          sessionId: session.sessionId,
          chunkId,
          audioBlob: capture.audioBlob
        })
      ]);

      await apiClient.upsertVision(token, session.sessionId, chunkId, {
        emotion: visionData.top_emotion,
        confidence: visionData.top_emotion_confidence,
        distressScore: visionData.distress_score,
        framesProcessed: visionData.frames_processed
      });

      await apiClient.upsertAudio(token, session.sessionId, chunkId, {
        emotion: audioData.emotion,
        stressScore: audioData.stress_score,
        confidence: audioData.confidence
      });

      const fused = await apiClient.fuse(token, session.sessionId, chunkId);
      const explained = await apiClient.explain(token, session.sessionId, chunkId);

      setFusionResult(fused);
      setExplanation(explained);
    } catch (err) {
      setError(err.message || "Pipeline execution failed");
    } finally {
      setBusy(false);
    }
  }

  if (!auth) {
    return (
      <main className="flex min-h-screen items-center justify-center px-4 py-10">
        <AuthPanel onAuthenticated={handleAuthenticated} />
      </main>
    );
  }

  return (
    <main className="mx-auto w-full max-w-7xl px-4 py-8">
      <header className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">Quietus AI Console</h1>
          <p className="mt-1 text-sm text-slate-300">Vision + Audio Distress Pipeline</p>
        </div>
        <div className="flex items-center gap-3">
          <span className="rounded-lg border border-slate-600 bg-panelAlt px-3 py-2 text-xs">
            {auth.user.email}
          </span>
          <button
            className="rounded-lg border border-slate-600 px-3 py-2 text-sm hover:border-accent"
            onClick={logout}
            type="button"
          >
            Logout
          </button>
        </div>
      </header>

      {error ? <p className="mb-4 rounded-lg border border-danger/40 bg-danger/10 p-3 text-sm text-danger">{error}</p> : null}

      <section className="grid gap-5 lg:grid-cols-3">
        <div className="space-y-5 lg:col-span-2">
          <SessionPanel token={token} session={session} setSession={setSession} setError={setError} />
          <CapturePanel sessionActive={sessionActive} onCaptureReady={setCapture} setError={setError} />

          <div className="rounded-2xl border border-slate-700/60 bg-panel/80 p-5">
            <h2 className="text-lg font-semibold">Run Analysis</h2>
            <p className="mt-1 text-sm text-slate-300">
              Sends media to vision/audio services, upserts predictions, computes fusion, and requests LLM explanation.
            </p>
            <button
              className="mt-4 rounded-lg bg-accent px-5 py-2 text-sm font-semibold text-slate-950 disabled:opacity-70"
              onClick={runPipeline}
              disabled={!sessionActive || !capture || busy}
              type="button"
            >
              {busy ? "Processing..." : "Analyze Current Chunk"}
            </button>
          </div>
        </div>

        <ResultsPanel fusionResult={fusionResult} explanation={explanation} />
      </section>
    </main>
  );
}
