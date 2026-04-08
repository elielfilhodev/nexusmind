"use client";

import { useQuery } from "@tanstack/react-query";
import {
  fetchChampionPositionsMap,
  type DraftLaneKey,
} from "@/shared/lib/champion-positions";

export function useChampionPositions() {
  return useQuery({
    queryKey: ["opgg", "champion-positions"],
    queryFn: fetchChampionPositionsMap,
    staleTime: 60 * 60 * 1000,
    gcTime: 2 * 60 * 60 * 1000,
    retry: 1,
  });
}

export type { DraftLaneKey };
