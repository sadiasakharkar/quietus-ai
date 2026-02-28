from fastapi import APIRouter, HTTPException

router = APIRouter()


@router.get("/health")
def health() -> dict:
    return {"status": "ok", "service": "nlp-service", "ready": False}


@router.post("/v1/text/analyze")
def analyze_text() -> dict:
    raise HTTPException(
        status_code=501,
        detail="NLP inference pipeline is not implemented yet. Planned in next phase.",
    )
