# Quietus Frontend (Phase 7)

React + Vite + Tailwind frontend for multimodal demo flow.

## Implemented
1. Login/Register screen
2. Session start/end controls
3. Webcam and microphone 20-second chunk capture
4. Calls vision and audio services for analysis
5. Sends modality predictions to backend
6. Triggers fusion and LLM explanation
7. Displays risk and key signals

## Local Dev
1. `npm install`
2. `npm run dev`

Vite proxies:
- `/backend-api` -> `http://localhost:8080`
- `/vision-api` -> `http://localhost:8100`
- `/audio-api` -> `http://localhost:8200`
