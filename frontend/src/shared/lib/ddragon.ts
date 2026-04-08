/** CDN oficial do League — usado para ícones de campeões, itens e runas. */

export const DDRAGON_BASE = "https://ddragon.leagueoflegends.com";

export const DDRAGON_LOCALES = ["pt_BR", "en_US"] as const;

/** Se o patch do banco não existir mais no CDN, usa o primeiro de `versions.json`. */
export async function ensureCdnVersion(preferred: string): Promise<string> {
  const probe = `${DDRAGON_BASE}/cdn/${preferred}/data/en_US/champion.json`;
  try {
    const res = await fetch(probe);
    if (res.ok) return preferred;
  } catch {
    /* rede */
  }
  try {
    const r = await fetch(`${DDRAGON_BASE}/api/versions.json`);
    if (!r.ok) return preferred;
    const arr = (await r.json()) as string[];
    return arr[0] ?? preferred;
  } catch {
    return preferred;
  }
}

export function championIconUrl(version: string, championId: string): string {
  return `${DDRAGON_BASE}/cdn/${version}/img/champion/${championId}.png`;
}

export function itemIconUrl(version: string, itemId: string | number): string {
  return `${DDRAGON_BASE}/cdn/${version}/img/item/${itemId}.png`;
}

/** `icon` vem do JSON de runas (ex.: `/lol/perks/Styles/7200_Domination.png`). */
export function perkIconUrl(iconPath: string): string {
  if (!iconPath) return "";
  if (iconPath.startsWith("http")) return iconPath;
  const p = iconPath.startsWith("/") ? iconPath : `/${iconPath}`;
  return `${DDRAGON_BASE}/cdn/img${p}`;
}

export type DDragonChampionRow = { riotKey: string; name: string; numericKey: string };

export type DDragonChampionBundle = { cdnVersion: string; champions: DDragonChampionRow[] };

export async function fetchChampionList(version: string): Promise<DDragonChampionBundle> {
  const v = await ensureCdnVersion(version);
  let lastErr: Error | null = null;
  for (const locale of DDRAGON_LOCALES) {
    const url = `${DDRAGON_BASE}/cdn/${v}/data/${locale}/champion.json`;
    try {
      const res = await fetch(url);
      if (!res.ok) continue;
      const json = (await res.json()) as {
        data: Record<string, { id: string; key: string; name: string }>;
      };
      const champions = Object.values(json.data).map((c) => ({
        riotKey: c.id,
        name: c.name,
        numericKey: c.key,
      }));
      champions.sort((a, b) => a.name.localeCompare(b.name, "pt-BR"));
      return { cdnVersion: v, champions };
    } catch (e) {
      lastErr = e instanceof Error ? e : new Error(String(e));
    }
  }
  throw lastErr ?? new Error("champion.json indisponível");
}

export type DDragonRuneTree = {
  id: number;
  key: string;
  name: string;
  icon: string;
  slots: Array<{ runes: Array<{ id: number; key: string; name: string; icon: string; shortDesc: string }> }>;
};

export type DDragonRunesBundle = { cdnVersion: string; trees: DDragonRuneTree[] };

export async function fetchRunesReforged(version: string): Promise<DDragonRunesBundle> {
  const v = await ensureCdnVersion(version);
  let lastErr: Error | null = null;
  for (const locale of DDRAGON_LOCALES) {
    const url = `${DDRAGON_BASE}/cdn/${v}/data/${locale}/runesReforged.json`;
    try {
      const res = await fetch(url);
      if (!res.ok) continue;
      const trees = (await res.json()) as DDragonRuneTree[];
      return { cdnVersion: v, trees };
    } catch (e) {
      lastErr = e instanceof Error ? e : new Error(String(e));
    }
  }
  throw lastErr ?? new Error("runesReforged.json indisponível");
}

export type DDragonItemRow = {
  id: string;
  name: string;
  plaintext: string;
  description: string;
  gold: { total: number; sell: number };
};

export type DDragonItemsBundle = { cdnVersion: string; items: DDragonItemRow[] };

export async function fetchItemList(version: string): Promise<DDragonItemsBundle> {
  const v = await ensureCdnVersion(version);
  let lastErr: Error | null = null;
  for (const locale of DDRAGON_LOCALES) {
    const url = `${DDRAGON_BASE}/cdn/${v}/data/${locale}/item.json`;
    try {
      const res = await fetch(url);
      if (!res.ok) continue;
      const json = (await res.json()) as {
        data: Record<string, { name: string; plaintext: string; description: string; gold: { total: number; sell: number } }>;
      };
      const items: DDragonItemRow[] = Object.entries(json.data).map(([id, item]) => ({
        id,
        name: item.name,
        plaintext: item.plaintext ?? "",
        description: item.description ?? "",
        gold: item.gold ?? { total: 0, sell: 0 },
      }));
      items.sort((a, b) => a.name.localeCompare(b.name, "pt-BR"));
      return { cdnVersion: v, items };
    } catch (e) {
      lastErr = e instanceof Error ? e : new Error(String(e));
    }
  }
  throw lastErr ?? new Error("item.json indisponível");
}
