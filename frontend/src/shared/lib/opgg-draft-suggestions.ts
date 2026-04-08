import type { DDragonChampionRow } from "@/shared/hooks/use-ddragon-catalog";

export type OpGgRankedChampion = {
  id: number;
  average_stats: {
    play: number;
    win_rate: number;
    pick_rate: number;
    ban_rate: number;
  };
  positions: Array<{
    name: string;
    stats: {
      play: number;
      win_rate: number;
      pick_rate: number;
      ban_rate: number;
    };
  }>;
};

export type OpGgRankedResponse = { data: OpGgRankedChampion[] };

export const LANES_ORDER = ["TOP", "JUNGLE", "MID", "ADC", "SUPPORT"] as const;
export type DraftLane = (typeof LANES_ORDER)[number];

/**
 * Nomes usados pela OP.GG em {@code positions[].name} (ranked global).
 * A API usa MID / ADC / SUPPORT — não MIDDLE / BOTTOM / UTILITY (nomes antigos/alternativos).
 */
export const OPGG_POSITION_NAMES_BY_DRAFT_LANE: Record<DraftLane, readonly string[]> = {
  TOP: ["TOP"],
  JUNGLE: ["JUNGLE"],
  MID: ["MID", "MIDDLE"],
  ADC: ["ADC", "BOTTOM"],
  SUPPORT: ["SUPPORT", "UTILITY"],
};

function findPositionForDraftLane(
  row: OpGgRankedChampion,
  lane: DraftLane
): (typeof row.positions)[0] | undefined {
  const names = OPGG_POSITION_NAMES_BY_DRAFT_LANE[lane];
  return row.positions?.find((p) => names.includes(p.name));
}

function usedRiotKeys(
  ally: Record<DraftLane, string>,
  enemy: Record<DraftLane, string>
): Set<string> {
  const s = new Set<string>();
  for (const v of Object.values(ally)) {
    if (v) s.add(v);
  }
  for (const v of Object.values(enemy)) {
    if (v) s.add(v);
  }
  return s;
}

function byNumericId(champions: DDragonChampionRow[]): Map<string, DDragonChampionRow> {
  return new Map(champions.map((c) => [c.numericKey, c]));
}

/**
 * Preenche slots vazios do time com o campeão de maior win rate naquela rota (ranked global OP.GG),
 * excluindo picks já usados em qualquer time.
 */
export function suggestTeamByLaneWinRate(
  target: Record<DraftLane, string>,
  ally: Record<DraftLane, string>,
  enemy: Record<DraftLane, string>,
  meta: OpGgRankedChampion[],
  champions: DDragonChampionRow[],
  opts?: { minRoleGames?: number }
): Record<DraftLane, string> {
  const minG = opts?.minRoleGames ?? 50;
  const byId = byNumericId(champions);
  const used = usedRiotKeys(ally, enemy);
  const next = { ...target };

  for (const lane of LANES_ORDER) {
    if (next[lane]) continue;
    let best: { wr: number; riotKey: string } | null = null;

    for (const row of meta) {
      const c = byId.get(String(row.id));
      if (!c || used.has(c.riotKey)) continue;
      const pos = findPositionForDraftLane(row, lane);
      if (!pos) continue;
      if (pos.stats.play < minG) continue;
      const wr = pos.stats.win_rate;
      if (best == null || wr > best.wr) {
        best = { wr, riotKey: c.riotKey };
      }
    }

    if (best) {
      next[lane] = best.riotKey;
      used.add(best.riotKey);
    }
  }

  return next;
}

export type BanSuggestion = {
  riotKey: string;
  name: string;
  banRate: number;
  winRate: number;
};

/** Top N bans por taxa de ban (meta global), excluindo campeões já no draft. */
export function suggestBansByBanRate(
  meta: OpGgRankedChampion[],
  champions: DDragonChampionRow[],
  ally: Record<DraftLane, string>,
  enemy: Record<DraftLane, string>,
  count = 5
): BanSuggestion[] {
  const byId = byNumericId(champions);
  const used = usedRiotKeys(ally, enemy);
  const sorted = [...meta].sort(
    (a, b) => b.average_stats.ban_rate - a.average_stats.ban_rate
  );
  const out: BanSuggestion[] = [];
  for (const row of sorted) {
    const c = byId.get(String(row.id));
    if (!c || used.has(c.riotKey)) continue;
    out.push({
      riotKey: c.riotKey,
      name: c.name,
      banRate: row.average_stats.ban_rate,
      winRate: row.average_stats.win_rate,
    });
    if (out.length >= count) break;
  }
  return out;
}
