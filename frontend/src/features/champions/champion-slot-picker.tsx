"use client";

import { useState } from "react";
import { ChevronDown } from "lucide-react";
import { championIconUrl } from "@/shared/lib/ddragon";
import type { DDragonChampionRow } from "@/shared/hooks/use-ddragon-catalog";
import type { DraftLaneKey } from "@/shared/lib/champion-positions";
import { Button } from "@/components/ui/button";
import { ChampionPickModal } from "@/features/champions/champion-pick-modal";
import { cn } from "@/lib/utils";

type Props = {
  version: string;
  champions: DDragonChampionRow[];
  value: string;
  onChange: (riotKey: string) => void;
  slotLane: DraftLaneKey;
};

export function ChampionSlotPicker({ version, champions, value, onChange, slotLane }: Props) {
  const [open, setOpen] = useState(false);
  const selected = value ? champions.find((c) => c.riotKey === value) : undefined;

  return (
    <>
      <Button
        type="button"
        variant="outline"
        className={cn(
          "h-auto min-h-11 w-full justify-between gap-2 px-2.5 py-1.5 text-left font-normal",
          !selected && "text-muted-foreground"
        )}
        onClick={() => setOpen(true)}
      >
        <span className="flex min-w-0 flex-1 items-center gap-2.5">
          {selected ? (
            <>
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={championIconUrl(version, selected.riotKey)}
                alt=""
                width={36}
                height={36}
                className="size-9 shrink-0 rounded-md bg-muted object-cover"
              />
              <span className="truncate text-sm font-medium text-foreground">{selected.name}</span>
            </>
          ) : (
            <span className="text-sm">Escolher campeão</span>
          )}
        </span>
        <ChevronDown className="size-4 shrink-0 opacity-50" />
      </Button>
      <ChampionPickModal
        open={open}
        onOpenChange={setOpen}
        version={version}
        champions={champions}
        value={value}
        onChange={onChange}
        slotLane={slotLane}
      />
    </>
  );
}
