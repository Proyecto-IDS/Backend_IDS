-- Migration: Add AI support fields to meeting table
-- Date: 2025-11-30
-- Description: Adds checklist and incident context JSON fields to support AI assistant features

ALTER TABLE meeting 
ADD COLUMN IF NOT EXISTS checklist TEXT,
ADD COLUMN IF NOT EXISTS incident_context TEXT;

-- Create AI system user if not exists (using ADMIN role since SYSTEM is not allowed)
INSERT INTO _user (email, name, role, password)
SELECT 'ai-assistant@system', 'Asistente IA', 'ADMIN', 'N/A'
WHERE NOT EXISTS (
    SELECT 1 FROM _user WHERE email = 'ai-assistant@system'
);

COMMENT ON COLUMN meeting.checklist IS 'JSON array of checklist items with id, label, done status';
COMMENT ON COLUMN meeting.incident_context IS 'JSON with attack type, severity, and probability for AI context';
