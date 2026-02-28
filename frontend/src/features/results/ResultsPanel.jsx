function riskClass(risk) {
  if (risk === "HIGH") return "text-danger";
  if (risk === "MEDIUM") return "text-warn";
  if (risk === "LOW") return "text-ok";
  return "text-slate-300";
}

export function ResultsPanel({ fusionResult, explanation }) {
  return (
    <div className="rounded-2xl border border-slate-700/60 bg-panel/80 p-5">
      <h2 className="text-lg font-semibold">Fusion Output</h2>

      <div className="mt-4 rounded-lg border border-slate-600 bg-panelAlt p-4">
        <p className="text-sm text-slate-300">Final Risk</p>
        <p className={`text-2xl font-bold ${riskClass(fusionResult?.finalRisk)}`}>
          {fusionResult?.finalRisk || "-"}
        </p>
        <p className="mt-2 text-sm text-slate-300">Score: {fusionResult?.finalScore ?? "-"}</p>
        <p className="text-sm text-slate-300">Confidence: {fusionResult?.confidence ?? "-"}</p>
      </div>

      <div className="mt-4 rounded-lg border border-slate-600 bg-panelAlt p-4">
        <p className="text-sm text-slate-300">LLM Explanation</p>
        <p className="mt-2 text-sm">{explanation?.summary || "-"}</p>
        <p className="mt-3 text-xs uppercase tracking-wide text-slate-400">Key Signals</p>
        <ul className="mt-2 space-y-1 text-sm text-slate-200">
          {(explanation?.keySignals || []).map((signal) => (
            <li key={signal}>- {signal}</li>
          ))}
        </ul>
      </div>
    </div>
  );
}
