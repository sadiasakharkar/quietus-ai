/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        bg: "#09121f",
        panel: "#111f33",
        panelAlt: "#1a2b42",
        accent: "#2bc3b6",
        warn: "#ff9f43",
        danger: "#ff5e5b",
        ok: "#5cd67a"
      },
      boxShadow: {
        glow: "0 0 0 1px rgba(43, 195, 182, 0.28), 0 12px 36px rgba(10, 200, 190, 0.14)"
      }
    }
  },
  plugins: []
};
