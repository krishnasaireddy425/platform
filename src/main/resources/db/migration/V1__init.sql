-- enable UUID generator
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- organizations
CREATE TABLE organizations (
  id          UUID PRIMARY KEY,
  name        TEXT NOT NULL,
  slug        TEXT UNIQUE,
  status      TEXT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- users
CREATE TABLE users (
  id                     UUID PRIMARY KEY,
  email                  CITEXT UNIQUE NOT NULL,
  password_hash          TEXT NOT NULL,
  temp_password_hash     TEXT,
  must_change_password   BOOLEAN NOT NULL DEFAULT true,
  display_name           TEXT,
  avatar_url             TEXT,
  created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- platform_owners
CREATE TABLE platform_owners (
  id            UUID PRIMARY KEY,
  email         CITEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  display_name  TEXT,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- roles (system-only roles)
CREATE TABLE roles (
  id          UUID PRIMARY KEY,
  org_id      UUID NULL,
  name        TEXT NOT NULL,
  description TEXT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT roles_only_system CHECK (org_id IS NULL),
  CONSTRAINT uniq_role_name UNIQUE (org_id, name)
);

-- org_memberships
CREATE TABLE org_memberships (
  id         UUID PRIMARY KEY,
  org_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role_id    UUID NOT NULL REFERENCES roles(id),
  state      TEXT NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uniq_org_user UNIQUE (org_id, user_id)
);

-- invites
CREATE TABLE invites (
  id                  UUID PRIMARY KEY,
  org_id              UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  email               CITEXT NOT NULL,
  temp_password_hash  TEXT NOT NULL,
  role_id             UUID NOT NULL REFERENCES roles(id),
  expires_at          TIMESTAMPTZ NOT NULL,
  invited_by_user_id  UUID REFERENCES users(id),
  accepted_at         TIMESTAMPTZ,
  status              TEXT NOT NULL DEFAULT 'PENDING'
);

-- audit_logs
CREATE TABLE audit_logs (
  id             BIGSERIAL PRIMARY KEY,
  org_id         UUID NULL REFERENCES organizations(id),
  actor_user_id  UUID NULL REFERENCES users(id),
  action         TEXT NOT NULL,
  target_type    TEXT,
  target_id      TEXT,
  meta           JSONB,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- helpful indexes
CREATE INDEX idx_audit_org_created   ON audit_logs(org_id, created_at);
CREATE INDEX idx_audit_actor_created ON audit_logs(actor_user_id, created_at);
CREATE INDEX idx_invites_email       ON invites(email);
CREATE INDEX idx_invites_expires     ON invites(expires_at);
