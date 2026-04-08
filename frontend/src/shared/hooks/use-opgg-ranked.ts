"use client";

import { useQuery } from "@tanstack/react-query";
import type { OpGgRankedResponse } from "@/shared/lib/opgg-draft-suggestions";

async function fetchOpGgRanked(): Promise<OpGgRankedResponse> {
  const res = await fetch("/api/opgg/champions/ranked", {
    headers: { Accept: "application/json" },
  });
  if (!res.ok) {
    const t = await res.text();
    throw new Error(t || `OP.GG ${res.status}`);
  }
  return res.json() as Promise<OpGgRankedResponse>;
}

export function useOpGgRankedMeta() {
  return useQuery({
    queryKey: ["opgg", "champions", "ranked", "global"],
    queryFn: fetchOpGgRanked,
    staleTime: 60 * 1000,
    gcTime: 10 * 60 * 1000,
    retry: 1,
  });
}
