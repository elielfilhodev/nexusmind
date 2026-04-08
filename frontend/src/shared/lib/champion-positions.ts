import type { OpGgRankedChampion } from "@/shared/lib/opgg-draft-suggestions";

export type DraftLaneKey = "TOP" | "JUNGLE" | "MID" | "ADC" | "SUPPORT";

export type LaneFilterTab = "ALL" | DraftLaneKey;

/** Nomes em {@code positions[].name} (OP.GG) → rota do draft. */
function opggLaneNameToDraftLane(name: string): DraftLaneKey | null {
  switch (name) {
    case "TOP":
      return "TOP";
    case "JUNGLE":
      return "JUNGLE";
    case "MID":
    case "MIDDLE":
      return "MID";
    case "ADC":
    case "BOTTOM":
      return "ADC";
    case "SUPPORT":
    case "UTILITY":
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
  // Enquanto o mapa OP.GG não carregou, não dá para filtrar com rigor.
  if (!positionsByNumericId) return true;
  const lanes = positionsByNumericId[numericKey];
  if (!lanes || lanes.length === 0) return false;
  return lanes.includes(tab);
}
