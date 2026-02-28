CREATE TABLE roles (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(32) UNIQUE NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(120) NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_roles (
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, role_id)
);

CREATE TABLE sessions (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  status VARCHAR(16) NOT NULL,
  started_at TIMESTAMPTZ NOT NULL,
  ended_at TIMESTAMPTZ NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE vision_predictions (
  id UUID PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  chunk_id UUID NOT NULL,
  emotion VARCHAR(32) NOT NULL,
  confidence NUMERIC(5,4) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
  distress_score NUMERIC(5,4) NOT NULL CHECK (distress_score >= 0 AND distress_score <= 1),
  frames_processed INTEGER NOT NULL CHECK (frames_processed >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (session_id, chunk_id)
);

CREATE TABLE audio_predictions (
  id UUID PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  chunk_id UUID NOT NULL,
  emotion VARCHAR(32) NOT NULL,
  stress_score NUMERIC(5,4) NOT NULL CHECK (stress_score >= 0 AND stress_score <= 1),
  confidence NUMERIC(5,4) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (session_id, chunk_id)
);

CREATE TABLE text_predictions (
  id UUID PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  chunk_id UUID NOT NULL,
  transcript TEXT NOT NULL,
  risk_level VARCHAR(16) NOT NULL,
  distress_probability NUMERIC(5,4) NOT NULL CHECK (distress_probability >= 0 AND distress_probability <= 1),
  confidence NUMERIC(5,4) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (session_id, chunk_id)
);

CREATE TABLE fusion_results (
  id UUID PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  chunk_id UUID NOT NULL,
  final_risk VARCHAR(16) NOT NULL,
  final_score NUMERIC(5,4) NOT NULL CHECK (final_score >= 0 AND final_score <= 1),
  confidence NUMERIC(5,4) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
  modalities_used JSONB NOT NULL,
  computed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (session_id, chunk_id)
);

CREATE TABLE llm_explanations (
  id UUID PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  chunk_id UUID NOT NULL,
  summary TEXT NOT NULL,
  key_signals JSONB NOT NULL,
  recommended_action_level VARCHAR(32) NOT NULL,
  explanation_confidence NUMERIC(5,4) NOT NULL CHECK (explanation_confidence >= 0 AND explanation_confidence <= 1),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (session_id, chunk_id)
);

CREATE TABLE audit_logs (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID NULL REFERENCES users(id) ON DELETE SET NULL,
  action VARCHAR(64) NOT NULL,
  resource_type VARCHAR(64) NOT NULL,
  resource_id VARCHAR(128) NULL,
  metadata JSONB NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_sessions_user_started_at ON sessions (user_id, started_at DESC);
CREATE INDEX idx_sessions_status_started_at ON sessions (status, started_at);

CREATE INDEX idx_vision_session_created_at ON vision_predictions (session_id, created_at DESC);
CREATE INDEX idx_audio_session_created_at ON audio_predictions (session_id, created_at DESC);
CREATE INDEX idx_text_session_created_at ON text_predictions (session_id, created_at DESC);

CREATE INDEX idx_fusion_session_computed_at ON fusion_results (session_id, computed_at DESC);
CREATE INDEX idx_fusion_risk_computed_at ON fusion_results (final_risk, computed_at);

CREATE INDEX idx_llm_session_created_at ON llm_explanations (session_id, created_at DESC);

CREATE INDEX idx_audit_user_created_at ON audit_logs (user_id, created_at DESC);
CREATE INDEX idx_audit_created_at ON audit_logs (created_at DESC);

INSERT INTO roles(name) VALUES ('USER'), ('ADMIN');
