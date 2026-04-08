"use client";

import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/shared/lib/api-client";
import {
  type DDragonChampionRow,
  type DDragonItemRow,
  type DDragonRuneTree,
  fetchChampionList,
  fetchItemList,
  fetchRunesReforged,
} from "@/shared/lib/ddragon";

export type PatchInfo = { version: string; current: boolean; releasedAt: string | null };

export function useCurrentPatch() {
  return useQuery({
    queryKey: ["patch", "current"],
    queryFn: () => apiGet<PatchInfo>("/api/patch/current"),
    staleTime: 60 * 60 * 1000,
  });
}

export function useDDragonChampions(version: string | undefined) {
  return useQuery({
    queryKey: ["ddragon", "champions", version],
    enabled: Boolean(version),
    queryFn: () => fetchChampionList(version as string),
    staleTime: 6 * 60 * 60 * 1000,
  });
}

export function useDDragonRunes(version: string | undefined) {
  return useQuery({
    queryKey: ["ddragon", "runes", version],
    enabled: Boolean(version),
    queryFn: () => fetchRunesReforged(version as string),
    staleTime: 6 * 60 * 60 * 1000,
  });
}

export function useDDragonItems(version: string | undefined) {
  return useQuery({
    queryKey: ["ddragon", "items", version],
    enabled: Boolean(version),
    queryFn: () => fetchItemList(version as string),
    staleTime: 6 * 60 * 60 * 1000,
  });
}

export type { DDragonChampionRow, DDragonItemRow, DDragonRuneTree };
