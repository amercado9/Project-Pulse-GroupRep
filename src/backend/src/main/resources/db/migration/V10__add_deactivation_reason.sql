-- V10: Add deactivation reason to users
ALTER TABLE users ADD COLUMN deactivation_reason TEXT;
