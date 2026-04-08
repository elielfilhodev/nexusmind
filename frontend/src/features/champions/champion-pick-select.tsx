"use client";

import { useMemo, useState } from "react";
import { championIconUrl } from "@/shared/lib/ddragon";
import type { DDragonChampionRow } from "@/shared/hooks/use-ddragon-catalog";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

type Props = {
  version: string;
  champions: DDragonChampionRow[];
  value: string;
  onChange: (v: string) => void;
  showSearch?: boolean;
  placeholder?: string;
  emptyValue?: string;
};

export function ChampionPickSelect({
  version,
  champions,
  value,
  onChange,
  showSearch = true,
  placeholder = "—",
  emptyValue = "__empty__",
}: Props) {
  const [q, setQ] = useState("");
  const filtered = useMemo(() => {
    const s = q.trim().toLowerCase();
    if (!s) return champions;
    return champions.filter(
      (c) => c.name.toLowerCase().includes(s) || c.riotKey.toLowerCase().includes(s)
    );
  }, [champions, q]);

  return (
    <div className="space-y-1.5">
      {showSearch ? (
        <Input
          className="h-8 text-xs"
          placeholder="Buscar…"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          onKeyDown={(e) => e.stopPropagation()}
        />
      ) : null}
      <Select
        value={value || emptyValue}
        onValueChange={(v) => {
          if (v == null) return;
          onChange(v === emptyValue ? "" : v);
        }}
      >
        <SelectTrigger className="w-full">
          <SelectValue placeholder={placeholder} />
        </SelectTrigger>
        <SelectContent className="max-h-72">
          <SelectItem value={emptyValue}>{placeholder}</SelectItem>
          {filtered.map((c) => (
            <SelectItem key={c.riotKey} value={c.riotKey}>
              <span className="flex items-center gap-2">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  src={championIconUrl(version, c.riotKey)}
                  alt=""
                  width={28}
                  height={28}
                  className="size-7 shrink-0 rounded-md bg-muted object-cover"
                  loading="lazy"
                />
                <span className="truncate">{c.name}</span>
              </span>
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}
