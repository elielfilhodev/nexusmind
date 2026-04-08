-- Isolamento por “sessão de navegador”: cada cliente envia X-Client-Session-Id (UUID em localStorage).
ALTER TABLE casual_analysis_reports
    ADD COLUMN IF NOT EXISTS client_session_id VARCHAR(64) NOT NULL DEFAULT '';

ALTER TABLE draft_analysis_reports
    ADD COLUMN IF NOT EXISTS client_session_id VARCHAR(64) NOT NULL DEFAULT '';

CREATE INDEX IF NOT EXISTS idx_casual_reports_session_created ON casual_analysis_reports (client_session_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_draft_reports_session_created ON draft_analysis_reports (client_session_id, created_at DESC);
