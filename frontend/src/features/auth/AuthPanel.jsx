import { useState } from "react";

import { apiClient } from "../../shared/api/client";

export function AuthPanel({ onAuthenticated }) {
  const [mode, setMode] = useState("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [fullName, setFullName] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      if (mode === "register") {
        await apiClient.register({ email, password, fullName });
      }
      const login = await apiClient.login({ email, password });
      onAuthenticated(login);
    } catch (err) {
      setError(err.message || "Authentication failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto w-full max-w-md rounded-2xl border border-slate-700/60 bg-panel/80 p-6 shadow-glow backdrop-blur">
      <h1 className="text-2xl font-bold">Quietus AI</h1>
      <p className="mt-2 text-sm text-slate-300">Multimodal Distress Intelligence Console</p>

      <form className="mt-6 space-y-4" onSubmit={handleSubmit}>
        {mode === "register" ? (
          <label className="block">
            <span className="mb-1 block text-sm text-slate-300">Full Name</span>
            <input
              className="w-full rounded-lg border border-slate-600 bg-panelAlt px-3 py-2 text-sm outline-none focus:border-accent"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              required
            />
          </label>
        ) : null}

        <label className="block">
          <span className="mb-1 block text-sm text-slate-300">Email</span>
          <input
            className="w-full rounded-lg border border-slate-600 bg-panelAlt px-3 py-2 text-sm outline-none focus:border-accent"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </label>

        <label className="block">
          <span className="mb-1 block text-sm text-slate-300">Password</span>
          <input
            className="w-full rounded-lg border border-slate-600 bg-panelAlt px-3 py-2 text-sm outline-none focus:border-accent"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </label>

        {error ? <p className="text-sm text-danger">{error}</p> : null}

        <button
          className="w-full rounded-lg bg-accent px-4 py-2 font-semibold text-slate-950 transition hover:brightness-105 disabled:opacity-70"
          disabled={loading}
          type="submit"
        >
          {loading ? "Please wait..." : mode === "register" ? "Create Account" : "Sign In"}
        </button>
      </form>

      <button
        className="mt-4 text-sm text-slate-300 underline underline-offset-4"
        onClick={() => setMode(mode === "register" ? "login" : "register")}
        type="button"
      >
        {mode === "register" ? "Already have an account? Login" : "New user? Register"}
      </button>
    </div>
  );
}
