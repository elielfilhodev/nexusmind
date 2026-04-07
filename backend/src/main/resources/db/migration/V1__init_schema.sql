CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE patch_versions (
    id          BIGSERIAL PRIMARY KEY,
    version     VARCHAR(32)  NOT NULL UNIQUE,
    is_current  BOOLEAN      NOT NULL DEFAULT FALSE,
    released_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE champions (
    id         BIGSERIAL PRIMARY KEY,
    patch_id   BIGINT       NOT NULL REFERENCES patch_versions (id) ON DELETE CASCADE,
    riot_key   VARCHAR(64)  NOT NULL,
    name       VARCHAR(128) NOT NULL,
    title      VARCHAR(256),
    lanes_json JSONB        NOT NULL DEFAULT '[]'::jsonb,
    tags_json  JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (patch_id, riot_key)
);

CREATE INDEX idx_champions_patch ON champions (patch_id);

CREATE TABLE items (
    id         BIGSERIAL PRIMARY KEY,
    patch_id   BIGINT       NOT NULL REFERENCES patch_versions (id) ON DELETE CASCADE,
    riot_key   VARCHAR(32)  NOT NULL,
    name       VARCHAR(256) NOT NULL,
    description TEXT,
    gold_cost  INT,
    stats_json JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (patch_id, riot_key)
);

CREATE INDEX idx_items_patch ON items (patch_id);

CREATE TABLE rune_trees (
    id         BIGSERIAL PRIMARY KEY,
    patch_id   BIGINT       NOT NULL REFERENCES patch_versions (id) ON DELETE CASCADE,
    tree_key   VARCHAR(32)  NOT NULL,
    name       VARCHAR(128) NOT NULL,
    slots_json JSONB        NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (patch_id, tree_key)
);

CREATE INDEX idx_rune_trees_patch ON rune_trees (patch_id);

CREATE TABLE summoner_spells (
    id         BIGSERIAL PRIMARY KEY,
    patch_id   BIGINT       NOT NULL REFERENCES patch_versions (id) ON DELETE CASCADE,
    riot_key   VARCHAR(32)  NOT NULL,
    name       VARCHAR(128) NOT NULL,
    cooldown_sec INT,
    modes_json JSONB        NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (patch_id, riot_key)
);

CREATE INDEX idx_spells_patch ON summoner_spells (patch_id);

CREATE TABLE casual_analysis_reports (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    elo                    VARCHAR(32) NOT NULL,
    lane                   VARCHAR(16) NOT NULL,
    playstyle              VARCHAR(32) NOT NULL,
    region                 VARCHAR(32),
    favorite_champion_key  VARCHAR(64),
    structured_payload     JSONB       NOT NULL,
    summary_text           TEXT
);

CREATE INDEX idx_casual_reports_created ON casual_analysis_reports (created_at DESC);

CREATE TABLE draft_analysis_reports (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    side                VARCHAR(8),
    context_type        VARCHAR(32),
    strategic_focus     VARCHAR(32),
    draft_payload       JSONB       NOT NULL,
    structured_payload  JSONB       NOT NULL,
    summary_text        TEXT
);

CREATE INDEX idx_draft_reports_created ON draft_analysis_reports (created_at DESC);

CREATE TABLE report_sections (
    id          BIGSERIAL PRIMARY KEY,
    report_kind VARCHAR(16)  NOT NULL CHECK (report_kind IN ('CASUAL', 'DRAFT')),
    report_id   UUID         NOT NULL,
    section_key VARCHAR(64)  NOT NULL,
    title       VARCHAR(256),
    body        JSONB        NOT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    UNIQUE (report_kind, report_id, section_key)
);

CREATE INDEX idx_report_sections_lookup ON report_sections (report_kind, report_id);

CREATE TABLE recommendation_cache (
    id         BIGSERIAL PRIMARY KEY,
    cache_key  VARCHAR(512) NOT NULL UNIQUE,
    payload    JSONB        NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rec_cache_expires ON recommendation_cache (expires_at);
