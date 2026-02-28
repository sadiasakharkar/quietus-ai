import { useRef, useState } from "react";

const CAPTURE_MS = 20000;

export function CapturePanel({ sessionActive, onCaptureReady, setError }) {
  const [isCapturing, setIsCapturing] = useState(false);
  const [videoReady, setVideoReady] = useState(false);
  const [audioReady, setAudioReady] = useState(false);

  const videoBlobRef = useRef(null);
  const audioBlobRef = useRef(null);

  async function capture() {
    if (!sessionActive) {
      setError("Start an active session first.");
      return;
    }

    setError("");
    setIsCapturing(true);

    try {
      const videoStream = await navigator.mediaDevices.getUserMedia({ video: true });
      const audioStream = await navigator.mediaDevices.getUserMedia({ audio: true });

      const videoChunks = [];
      const audioChunks = [];

      const videoRecorder = new MediaRecorder(videoStream, { mimeType: "video/webm" });
      const audioRecorder = new MediaRecorder(audioStream, { mimeType: "audio/webm" });

      videoRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) videoChunks.push(event.data);
      };
      audioRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) audioChunks.push(event.data);
      };

      videoRecorder.start();
      audioRecorder.start();

      await new Promise((resolve) => setTimeout(resolve, CAPTURE_MS));

      videoRecorder.stop();
      audioRecorder.stop();

      await new Promise((resolve) => {
        let done = 0;
        const markDone = () => {
          done += 1;
          if (done === 2) resolve();
        };

        videoRecorder.onstop = markDone;
        audioRecorder.onstop = markDone;
      });

      videoStream.getTracks().forEach((track) => track.stop());
      audioStream.getTracks().forEach((track) => track.stop());

      videoBlobRef.current = new Blob(videoChunks, { type: "video/webm" });
      audioBlobRef.current = new Blob(audioChunks, { type: "audio/webm" });

      setVideoReady(true);
      setAudioReady(true);

      onCaptureReady({ videoBlob: videoBlobRef.current, audioBlob: audioBlobRef.current });
    } catch (err) {
      setError(err.message || "Media capture failed");
    } finally {
      setIsCapturing(false);
    }
  }

  return (
    <div className="rounded-2xl border border-slate-700/60 bg-panel/80 p-5">
      <h2 className="text-lg font-semibold">Capture (20s)</h2>
      <p className="mt-1 text-sm text-slate-300">Record webcam and microphone chunk for multimodal analysis.</p>

      <button
        className="mt-4 rounded-lg bg-warn px-4 py-2 text-sm font-semibold text-slate-950 disabled:opacity-70"
        onClick={capture}
        disabled={!sessionActive || isCapturing}
        type="button"
      >
        {isCapturing ? "Recording..." : "Record Chunk"}
      </button>

      <div className="mt-4 grid grid-cols-2 gap-3 text-sm">
        <div className="rounded-lg border border-slate-600 bg-panelAlt p-3">
          Video: <span className={videoReady ? "text-ok" : "text-slate-400"}>{videoReady ? "Ready" : "Pending"}</span>
        </div>
        <div className="rounded-lg border border-slate-600 bg-panelAlt p-3">
          Audio: <span className={audioReady ? "text-ok" : "text-slate-400"}>{audioReady ? "Ready" : "Pending"}</span>
        </div>
      </div>
    </div>
  );
}
