import type { OpGgRankedChampion } from "@/shared/lib/opgg-draft-suggestions";

export type DraftLaneKey = "TOP" | "JUNGLE" | "MID" | "ADC" | "SUPPORT";

export type LaneFilterTab = "ALL" | DraftLaneKey;

/** Nomes de rota na API OP.GG → filtro do draft. */
function opggLaneNameToDraftLane(name: string): DraftLaneKey | null {
  switch (name) {
    case "TOP":
      return "TOP";
    case "JUNGLE":
      return "JUNGLE";
    case "MIDDLE":
      return "MID";
    case "BOTTOM":
      return "ADC";
    case "UTILITY":
    case "SUPPORT":
      return "SUPPORT";
    default:
      return null;
  }
}

/**
 * Mapa id numérico do campeão (mesmo que `numericKey` do Data Dragon) → rotas em que aparece no meta ranked.
 * Fonte: OP.GG via `/api/opgg/champions/ranked` (evita CORS e alinha id com o cliente).
 */
export async function fetchChampionPositionsMap(): Promise<Record<string, DraftLaneKey[]>> {
  const res = await fetch("/api/opgg/champions/ranked", {
    headers: { Accept: "application/json" },
  });
  if (!res.ok) {
    throw new Error(`opgg positions ${res.status}`);
  }
  const json = (await res.json()) as { data: OpGgRankedChampion[] };
  const out: Record<string, DraftLaneKey[]> = {};
  for (const row of json.data ?? []) {
    const lanes: DraftLaneKey[] = [];
    for (const p of row.positions ?? []) {
      const lane = opggLaneNameToDraftLane(p.name);
      if (lane && !lanes.includes(lane)) lanes.push(lane);
    }
    if (lanes.length > 0) {
      out[String(row.id)] = lanes;
    }
  }
  return out;
}

/**
 * @param numericKey — `DDragonChampionRow.numericKey` (id Riot)
 */
export function championVisibleForLaneFilter(
  numericKey: string,
  tab: LaneFilterTab,
  positionsByNumericId: Record<string, DraftLaneKey[]> | undefined
): boolean {
  if (tab === "ALL") return true;
  if (!positionsByNumericId) return true;
  const lanes = positionsByNumericId[numericKey];
  if (!lanes || lanes.length === 0) return true;
  return lanes.includes(tab);
}
