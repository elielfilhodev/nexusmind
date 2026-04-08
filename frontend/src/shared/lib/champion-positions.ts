/** Fonte: Meraki Analytics (posições oficiais por campeão, alinhadas ao cliente). */
export const MERAKI_CHAMPIONS_JSON =
  "https://cdn.merakianalytics.com/riot/lol/resources/latest/en-US/champions.json";

export type DraftLaneKey = "TOP" | "JUNGLE" | "MID" | "ADC" | "SUPPORT";

/** Valores de `positions` no JSON Meraki → chave de rota do draft. */
const MERAKI_TO_LANE: Record<string, DraftLaneKey> = {
  TOP: "TOP",
  JUNGLE: "JUNGLE",
  MIDDLE: "MID",
  BOTTOM: "ADC",
  SUPPORT: "SUPPORT",
};

export type LaneFilterTab = "ALL" | DraftLaneKey;

export async function fetchChampionPositionsMap(): Promise<Record<string, DraftLaneKey[]>> {
  const res = await fetch(MERAKI_CHAMPIONS_JSON);
  if (!res.ok) {
    throw new Error(`Meraki champions ${res.status}`);
  }
  const data = (await res.json()) as Record<string, { positions?: string[] } | unknown>;
  const out: Record<string, DraftLaneKey[]> = {};
  for (const [riotKey, v] of Object.entries(data)) {
    if (!v || typeof v !== "object" || !("positions" in v)) continue;
    const positions = (v as { positions?: string[] }).positions;
    if (!Array.isArray(positions)) continue;
    const lanes: DraftLaneKey[] = [];
    for (const p of positions) {
      const lane = MERAKI_TO_LANE[p];
      if (lane && !lanes.includes(lane)) lanes.push(lane);
    }
    if (lanes.length > 0) {
      out[riotKey] = lanes;
    }
  }
  return out;
}

/** Campeão sem dados de posição entra em qualquer filtro (flex / patch novo). */
export function championVisibleForLaneFilter(
  riotKey: string,
  tab: LaneFilterTab,
  positionsMap: Record<string, DraftLaneKey[]> | undefined
): boolean {
  if (tab === "ALL") return true;
  if (!positionsMap) return true;
  const lanes = positionsMap[riotKey];
  if (!lanes || lanes.length === 0) return true;
  return lanes.includes(tab);
}
