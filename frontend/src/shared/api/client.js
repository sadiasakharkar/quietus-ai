const BASE = "/backend-api";

async function request(path, { method = "GET", token, body, headers = {} } = {}) {
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: {
      ...(body ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers
    },
    body: body ? JSON.stringify(body) : undefined
  });

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const error = await res.json();
      message = error.message || message;
    } catch (_err) {
      // ignore parse failure
    }
    throw new Error(message);
  }

  if (res.status === 204) {
    return null;
  }

  return res.json();
}

export const apiClient = {
  register(payload) {
    return request("/api/v1/auth/register", { method: "POST", body: payload });
  },
  login(payload) {
    return request("/api/v1/auth/login", { method: "POST", body: payload });
  },
  startSession(token) {
    return request("/api/v1/sessions", { method: "POST", token });
  },
  endSession(token, sessionId) {
    return request(`/api/v1/sessions/${sessionId}/end`, { method: "POST", token });
  },
  upsertVision(token, sessionId, chunkId, payload) {
    return request(`/api/v1/sessions/${sessionId}/chunks/${chunkId}/vision`, {
      method: "POST",
      token,
      body: payload
    });
  },
  upsertAudio(token, sessionId, chunkId, payload) {
    return request(`/api/v1/sessions/${sessionId}/chunks/${chunkId}/audio`, {
      method: "POST",
      token,
      body: payload
    });
  },
  fuse(token, sessionId, chunkId) {
    return request(`/api/v1/sessions/${sessionId}/chunks/${chunkId}/fuse`, {
      method: "POST",
      token
    });
  },
  explain(token, sessionId, chunkId) {
    return request(`/api/v1/sessions/${sessionId}/chunks/${chunkId}/explain`, {
      method: "POST",
      token
    });
  }
};
