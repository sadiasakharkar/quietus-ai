# Quietus AI Database Schema (Multimodal)

## 1. Tables

### roles
- id BIGSERIAL PRIMARY KEY
- name VARCHAR(32) UNIQUE NOT NULL
- created_at TIMESTAMPTZ NOT NULL DEFAULT now()

### users
- id UUID PRIMARY KEY
- email VARCHAR(255) UNIQUE NOT NULL
- password_hash VARCHAR(255) NOT NULL
- full_name VARCHAR(120) NOT NULL
- is_active BOOLEAN NOT NULL DEFAULT TRUE
- created_at TIMESTAMPTZ NOT NULL DEFAULT now()
- updated_at TIMESTAMPTZ NOT NULL DEFAULT now()

### user_roles
- user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE
- role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE
- PRIMARY KEY (user_id, role_id)

### sessions
- id UUID PRIMARY KEY
- user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE
- status VARCHAR(16) NOT NULL (`ACTIVE`, `ENDED`)
- started_at TIMESTAMPTZ NOT NULL
- ended_at TIMESTAMPTZ NULL
- created_at TIMESTAMPTZ NOT NULL DEFAULT now()
- updated_at TIMESTAMPTZ NOT NULL DEFAULT now()

### vision_predictions
- id UUID PRIMARY KEY
- session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE
- chunk_id UUID NOT NULL
- emotion VARCHAR(32) NOT NULL
- confidence NUMERIC(5,4) NOT NULL
- distress_score NUMERIC(5,4) NOT NULL
- frames_processed INTEGER NOT NULL
- created_at TIMESTAMPTZ NOT NULL DEFAULT now()
- UNIQUE(session_id, chunk_id)

### audio_predictions
- id UUID PRIMARY KEY
- session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE
- chunk_id UUID NOT NULL
- emotion VARCHAR(32) NOT NULL
- stress_score NUMERIC(5,4) NOT NULL
- confidence NUMERIC(5,4) NOT NULL
- created_at TIMESTAMPTZ NOT NULL DEFAULT now()
- UNIQUE(session_id, chunk_id)

### text_predictions
- id UUID PRIMARY KEY
- session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE
- chunk_id UUID NOT NULL
- transcript TEXT NOT NULL
- risk_level VARCHAR(16) NOT NULL
- distress_probability NUMERIC(5,4) NOT NULL
- confidence NUMERIC(5,4) NOT NULL
- created_at TIMESTAMPTZ NOT NULL DEFAULT now()
- UNIQUE(session_id, chunk_id)

### fusion_results
- id UUID PRIMARY KEY
- session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE
- chunk_id UUID NOT NULL
- final_risk VARCHAR(16) NOT NULL
- final_score NUMERIC(5,4) NOT NULL
- confidence NUMERIC(5,4) NOT NULL
- modalities_used JSONB NOT NULL
- computed_at TIMESTAMPTZ NOT NULL DEFAULT now()
- UNIQUE(session_id, chunk_id)

### llm_explanations
- id UUID PRIMARY KEY
- session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE
- chunk_id UUID NOT NULL
- summary TEXT NOT NULL
- key_signals JSONB NOT NULL
- recommended_action_level VARCHAR(32) NOT NULL
- explanation_confidence NUMERIC(5,4) NOT NULL
- created_at TIMESTAMPTZ NOT NULL DEFAULT now()
- UNIQUE(session_id, chunk_id)

### audit_logs
- id BIGSERIAL PRIMARY KEY
- user_id UUID NULL REFERENCES users(id) ON DELETE SET NULL
- action VARCHAR(64) NOT NULL
- resource_type VARCHAR(64) NOT NULL
- resource_id VARCHAR(128) NULL
- metadata JSONB NULL
- created_at TIMESTAMPTZ NOT NULL DEFAULT now()

## 2. Analytics Rule
All analytics must derive from persisted `vision_predictions`, `audio_predictions`, `text_predictions`, and `fusion_results` records.
