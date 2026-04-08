-- Jogadores profissionais / scouting (lista manual ou sincronizada futuramente)
CREATE TABLE pro_player (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    puuid VARCHAR(78) NOT NULL,
    platform_id VARCHAR(16) NOT NULL,
    game_name VARCHAR(64),
    tag_line VARCHAR(8),
    team_name VARCHAR(128),
    competitive_region VARCHAR(32),
    primary_role VARCHAR(16),
    scouting_tags TEXT,
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_pro_puuid_platform UNIQUE (puuid, platform_id)
);

CREATE INDEX idx_pro_player_active ON pro_player (active) WHERE active = TRUE;

-- Cache persistente de análises IA (match / perfil) para reduzir custo e repetição
CREATE TABLE competitive_ai_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cache_key VARCHAR(256) NOT NULL,
    kind VARCHAR(32) NOT NULL,
    payload_json TEXT NOT NULL,
    model VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_competitive_ai UNIQUE (cache_key, kind)
);

CREATE INDEX idx_competitive_ai_created ON competitive_ai_cache (created_at);
