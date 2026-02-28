async function postMedia(path, file, fields) {
  const form = new FormData();
  Object.entries(fields).forEach(([key, value]) => form.append(key, value));
  form.append(path.includes("vision") ? "video_file" : "audio_file", file);

  const res = await fetch(path, {
    method: "POST",
    body: form
  });

  if (!res.ok) {
    let detail = `Service call failed (${res.status})`;
    try {
      const error = await res.json();
      detail = error.detail || detail;
    } catch (_err) {
      // ignore parse error
    }
    throw new Error(detail);
  }

  return res.json();
}

export const modalityClients = {
  analyzeVision({ sessionId, chunkId, videoBlob }) {
    const file = new File([videoBlob], "chunk.mp4", { type: "video/mp4" });
    return postMedia("/vision-api/v1/vision/analyze", file, {
      session_id: sessionId,
      chunk_id: chunkId
    });
  },
  analyzeAudio({ sessionId, chunkId, audioBlob }) {
    const file = new File([audioBlob], "chunk.webm", { type: "audio/webm" });
    return postMedia("/audio-api/v1/audio/analyze", file, {
      session_id: sessionId,
      chunk_id: chunkId
    });
  }
};
