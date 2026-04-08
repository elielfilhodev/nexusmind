"use client";

import { useQuery } from "@tanstack/react-query";
import {
  fetchChampionPositionsMap,
  type DraftLaneKey,
} from "@/shared/lib/champion-positions";

export function useChampionPositions() {
  return useQuery({
    queryKey: ["meraki", "champion-positions"],
    queryFn: fetchChampionPositionsMap,
    staleTime: 7 * 24 * 60 * 60 * 1000,
    gcTime: 8 * 24 * 60 * 60 * 1000,
    retry: 1,
  });
}

export type { DraftLaneKey };
