-- Fix ML fields column types to support long text content
ALTER TABLE alerts ALTER COLUMN probabilities TYPE TEXT;
ALTER TABLE alerts ALTER COLUMN standard_protocol TYPE TEXT;
