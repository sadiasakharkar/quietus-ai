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

CREATE TABLE analysis_records (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  input_text TEXT NOT NULL,
  source VARCHAR(32) NOT NULL,
  predicted_label VARCHAR(16) NOT NULL,
  confidence NUMERIC(5,4) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
  model_version VARCHAR(64) NOT NULL,
  ai_processing_ms INTEGER NOT NULL CHECK (ai_processing_ms >= 0),
  submitted_at TIMESTAMPTZ NOT NULL,
  analyzed_at TIMESTAMPTZ NOT NULL,
  request_id UUID UNIQUE NOT NULL
);

CREATE TABLE video_sessions (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  stream_session_id VARCHAR(128) UNIQUE NOT NULL,
  session_type VARCHAR(32) NOT NULL,
  client_platform VARCHAR(32) NOT NULL,
  status VARCHAR(16) NOT NULL,
  started_at TIMESTAMPTZ NOT NULL,
  ended_at TIMESTAMPTZ NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE video_analysis_events (
  id UUID PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES video_sessions(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  event_at TIMESTAMPTZ NOT NULL,
  risk_level VARCHAR(16) NOT NULL,
  confidence NUMERIC(5,4) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
  signals JSONB NOT NULL,
  model_version VARCHAR(64) NOT NULL,
  latency_ms INTEGER NOT NULL CHECK (latency_ms >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
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

CREATE INDEX idx_analysis_user_analyzed_at ON analysis_records (user_id, analyzed_at DESC);
CREATE INDEX idx_analysis_label_analyzed_at ON analysis_records (predicted_label, analyzed_at);
CREATE INDEX idx_analysis_analyzed_at ON analysis_records (analyzed_at);

CREATE INDEX idx_video_sessions_user_started_at ON video_sessions (user_id, started_at DESC);
CREATE INDEX idx_video_sessions_status_started_at ON video_sessions (status, started_at);

CREATE INDEX idx_video_events_session_event_at ON video_analysis_events (session_id, event_at DESC);
CREATE INDEX idx_video_events_risk_event_at ON video_analysis_events (risk_level, event_at);
CREATE INDEX idx_video_events_event_at ON video_analysis_events (event_at);

CREATE INDEX idx_audit_user_created_at ON audit_logs (user_id, created_at DESC);
CREATE INDEX idx_audit_created_at ON audit_logs (created_at DESC);

INSERT INTO roles(name) VALUES ('USER'), ('ADMIN');
