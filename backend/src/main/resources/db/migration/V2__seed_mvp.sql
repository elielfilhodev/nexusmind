INSERT INTO patch_versions (version, is_current, released_at)
VALUES ('15.5.1', TRUE, NOW())
ON CONFLICT (version) DO UPDATE SET released_at = EXCLUDED.released_at;

UPDATE patch_versions SET is_current = (version = '15.5.1');

WITH p AS (SELECT id FROM patch_versions WHERE version = '15.5.1' LIMIT 1)
INSERT INTO champions (patch_id, riot_key, name, title, lanes_json, tags_json)
SELECT p.id, x.riot_key, x.cname, x.title, x.lanes::jsonb, x.tags::jsonb
FROM p
CROSS JOIN (VALUES
    ('Ahri', 'Ahri', 'A Raposa de Nove Caudas', '["MID"]', '{"damage":"AP","style":"pick"}'),
    ('LeeSin', 'Lee Sin', 'O Monge Cego', '["JUNGLE"]', '{"damage":"AD","style":"early"}'),
    ('Ornn', 'Ornn', 'O Fogo da Montanha', '["TOP"]', '{"damage":"Tank","style":"engage"}'),
    ('Jinx', 'Jinx', 'A Louca de Zaun', '["BOTTOM"]', '{"damage":"AD","style":"scaling"}'),
    ('Thresh', 'Thresh', 'O Guardião das Correntes', '["UTILITY"]', '{"damage":"Mixed","style":"pick"}')
) AS x(riot_key, cname, title, lanes, tags)
ON CONFLICT (patch_id, riot_key) DO NOTHING;

WITH p AS (SELECT id FROM patch_versions WHERE version = '15.5.1' LIMIT 1)
INSERT INTO items (patch_id, riot_key, name, description, gold_cost, stats_json)
SELECT p.id, x.riot_key, x.iname, x.descr, x.gold, x.stats::jsonb
FROM p
CROSS JOIN (VALUES
    ('3087', 'Statikk Shiv', 'Ataque e limpeza de wave', 3000, '{"as":35,"crit":25}'),
    ('3157', 'Zhonya''s Hourglass', 'Defensivo AP', 3250, '{"ap":120,"armor":50}')
) AS x(riot_key, iname, descr, gold, stats)
ON CONFLICT (patch_id, riot_key) DO NOTHING;

WITH p AS (SELECT id FROM patch_versions WHERE version = '15.5.1' LIMIT 1)
INSERT INTO rune_trees (patch_id, tree_key, name, slots_json)
SELECT p.id, x.tree_key, x.tname, x.slots::jsonb
FROM p
CROSS JOIN (VALUES
    ('domination', 'Domination', '[{"row":0,"runes":["Electrocute","Dark Harvest"]}]'),
    ('precision', 'Precision', '[{"row":0,"runes":["Press the Attack","Lethal Tempo"]}]'),
    ('resolve', 'Resolve', '[{"row":0,"runes":["Grasp of the Undying","Aftershock"]}]')
) AS x(tree_key, tname, slots)
ON CONFLICT (patch_id, tree_key) DO NOTHING;

WITH p AS (SELECT id FROM patch_versions WHERE version = '15.5.1' LIMIT 1)
INSERT INTO summoner_spells (patch_id, riot_key, name, cooldown_sec, modes_json)
SELECT p.id, x.riot_key, x.sname, x.cd, x.modes::jsonb
FROM p
CROSS JOIN (VALUES
    ('SummonerFlash', 'Flash', 300, '["CLASSIC"]'),
    ('SummonerDot', 'Ignite', 180, '["CLASSIC"]'),
    ('SummonerHeal', 'Heal', 240, '["CLASSIC"]'),
    ('SummonerSmite', 'Smite', 15, '["CLASSIC"]')
) AS x(riot_key, sname, cd, modes)
ON CONFLICT (patch_id, riot_key) DO NOTHING;
