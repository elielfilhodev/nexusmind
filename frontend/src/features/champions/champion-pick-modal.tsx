"use client";

import { useEffect, useMemo, useState } from "react";
import { Search } from "lucide-react";
import { championIconUrl } from "@/shared/lib/ddragon";
import type { DDragonChampionRow } from "@/shared/hooks/use-ddragon-catalog";
import { useChampionPositions } from "@/shared/hooks/use-champion-positions";
import {
  championVisibleForLaneFilter,
  type DraftLaneKey,
  type LaneFilterTab,
} from "@/shared/lib/champion-positions";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogDescription,
  DialogHeader,
  DialogPopup,
  DialogTitle,
} from "@/components/ui/dialog";
import { cn } from "@/lib/utils";

const LANE_TABS: { id: LaneFilterTab; label: string }[] = [
  { id: "ALL", label: "Todos" },
  { id: "TOP", label: "Topo" },
  { id: "JUNGLE", label: "Selva" },
  { id: "MID", label: "Meio" },
  { id: "ADC", label: "Bot" },
  { id: "SUPPORT", label: "Suporte" },
];

function laneTitle(lane: DraftLaneKey): string {
  const m: Record<DraftLaneKey, string> = {
    TOP: "Topo",
    JUNGLE: "Selva",
    MID: "Meio",
    ADC: "Bot",
    SUPPORT: "Suporte",
  };
  return m[lane];
}

type Props = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  version: string;
  champions: DDragonChampionRow[];
  value: string;
  onChange: (riotKey: string) => void;
  /** Rota do slot — define o filtro inicial ao abrir. */
  slotLane: DraftLaneKey;
  title?: string;
};

export function ChampionPickModal({
  open,
  onOpenChange,
  version,
  champions,
  value,
  onChange,
  slotLane,
  title,
}: Props) {
  const positionsQuery = useChampionPositions();
  const positionsMap = positionsQuery.data;

  const [q, setQ] = useState("");
  const [laneTab, setLaneTab] = useState<LaneFilterTab>("ALL");

  useEffect(() => {
    if (open) {
      setLaneTab(slotLane);
      setQ("");
    }
  }, [open, slotLane]);

  const filtered = useMemo(() => {
    const s = q.trim().toLowerCase();
    let list = champions;
    if (s) {
      list = list.filter(
        (c) => c.name.toLowerCase().includes(s) || c.riotKey.toLowerCase().includes(s)
      );
    }
    return list.filter((c) => championVisibleForLaneFilter(c.numericKey, laneTab, positionsMap));
  }, [champions, q, laneTab, positionsMap]);

  const selectedName = useMemo(() => {
    if (!value) return null;
    return champions.find((c) => c.riotKey === value)?.name ?? value;
  }, [champions, value]);

  const pick = (riotKey: string) => {
    onChange(riotKey);
    onOpenChange(false);
  };

  const clear = () => {
    onChange("");
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogPopup className="flex max-h-[min(90vh,760px)] flex-col overflow-hidden p-0">
        <DialogHeader className="shrink-0 space-y-3 px-4 pb-3 pt-4">
          <div>
            <DialogTitle>{title ?? `Campeão — ${laneTitle(slotLane)}`}</DialogTitle>
            <DialogDescription>
              Toque no ícone para selecionar. Filtros por rota (meta ranked global OP.GG).
              {positionsQuery.isError ? (
                <span className="text-amber-600 dark:text-amber-500">
                  {" "}
                  Filtros de rota indisponíveis — mostrando todos.
                </span>
              ) : null}
            </DialogDescription>
          </div>
          <div className="flex flex-wrap gap-1.5">
            {LANE_TABS.map((t) => (
              <Button
                key={t.id}
                type="button"
                size="xs"
                variant={laneTab === t.id ? "default" : "outline"}
                className="rounded-md"
                onClick={() => setLaneTab(t.id)}
              >
                {t.label}
              </Button>
            ))}
          </div>
          <div className="relative">
            <Search className="absolute left-2.5 top-1/2 size-3.5 -translate-y-1/2 text-muted-foreground" />
            <Input
              className="h-9 pl-8"
              placeholder="Buscar por nome…"
              value={q}
              onChange={(e) => setQ(e.target.value)}
              autoComplete="off"
            />
          </div>
        </DialogHeader>

        <div className="min-h-0 flex-1 overflow-y-auto overflow-x-hidden overscroll-contain px-3 pb-2">
          <div className="grid grid-cols-[repeat(auto-fill,minmax(76px,1fr))] gap-2 pb-4 pt-1">
            {filtered.map((c) => {
              const active = value === c.riotKey;
              return (
                <button
                  key={c.riotKey}
                  type="button"
                  onClick={() => pick(c.riotKey)}
                  className={cn(
                    "flex flex-col items-center gap-1 rounded-lg border border-transparent p-1.5 text-center transition-colors hover:bg-muted/80 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring",
                    active && "border-primary/60 bg-muted/50"
                  )}
                >
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={championIconUrl(version, c.riotKey)}
                    alt=""
                    width={56}
                    height={56}
                    className="size-14 shrink-0 rounded-md bg-muted object-cover shadow-sm"
                    loading="lazy"
                  />
                  <span className="line-clamp-2 w-full text-[10px] leading-tight font-medium text-foreground">
                    {c.name}
                  </span>
                </button>
              );
            })}
          </div>
          {filtered.length === 0 ? (
            <p className="py-8 text-center text-sm text-muted-foreground">
              Nenhum campeão com esse filtro ou busca.
            </p>
          ) : null}
        </div>

        <div className="flex shrink-0 flex-wrap items-center justify-between gap-2 border-t border-border/60 px-4 py-3">
          <p className="truncate text-xs text-muted-foreground">
            {selectedName ? (
              <>
                Selecionado: <span className="font-medium text-foreground">{selectedName}</span>
              </>
            ) : (
              "Nenhum campeão selecionado"
            )}
          </p>
          <Button type="button" variant="ghost" size="sm" onClick={clear}>
            Limpar
          </Button>
        </div>
      </DialogPopup>
    </Dialog>
  );
}
