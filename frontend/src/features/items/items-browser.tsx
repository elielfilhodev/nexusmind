"use client";

import { useMemo, useState } from "react";
import { useCurrentPatch, useDDragonItems } from "@/shared/hooks/use-ddragon-catalog";
import { itemIconUrl } from "@/shared/lib/ddragon";
import { ApiError } from "@/shared/lib/api-client";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { sanitizeRiotPlainText } from "@/shared/lib/lol-text";

export function ItemsBrowser() {
  const patchQ = useCurrentPatch();
  const patchVersion = patchQ.data?.version;
  const itemsQ = useDDragonItems(patchVersion);
  const [q, setQ] = useState("");

  const filtered = useMemo(() => {
    const list = itemsQ.data?.items ?? [];
    const s = q.trim().toLowerCase();
    if (!s) return list;
    return list.filter((i) => {
      const name = sanitizeRiotPlainText(i.name).toLowerCase();
      const plain = sanitizeRiotPlainText(i.plaintext).toLowerCase();
      const desc = sanitizeRiotPlainText(i.description).toLowerCase();
      return name.includes(s) || plain.includes(s) || desc.includes(s) || i.id.includes(s);
    });
  }, [itemsQ.data, q]);

  if (patchQ.isLoading || itemsQ.isLoading) {
    return (
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {Array.from({ length: 12 }).map((_, i) => (
          <Skeleton key={i} className="h-36" />
        ))}
      </div>
    );
  }

  if (patchQ.isError || itemsQ.isError) {
    const err = patchQ.error ?? itemsQ.error;
    return (
      <p className="text-destructive">
        {err instanceof ApiError ? err.message : "Não foi possível carregar itens do Data Dragon."}
      </p>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm text-muted-foreground">
          CDN{" "}
          <span className="font-mono text-foreground">{itemsQ.data?.cdnVersion ?? patchVersion}</span> — {filtered.length}{" "}
          itens
        </p>
        <Input
          className="max-w-md"
          placeholder="Buscar por nome, ID ou descrição curta…"
          value={q}
          onChange={(e) => setQ(e.target.value)}
        />
      </div>
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {filtered.map((item) => {
          const title = sanitizeRiotPlainText(item.name);
          const plain = sanitizeRiotPlainText(item.plaintext);
          const desc = sanitizeRiotPlainText(item.description);
          return (
            <Card key={item.id} className="border-border/80 overflow-hidden">
              <CardHeader className="flex flex-row items-start gap-3 space-y-0 pb-2">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  src={itemIconUrl(itemsQ.data?.cdnVersion ?? patchVersion!, item.id)}
                  alt=""
                  width={48}
                  height={48}
                  className="size-12 shrink-0 rounded-md border border-border/60 bg-muted/40"
                  loading="lazy"
                />
                <div className="min-w-0 flex-1">
                  <CardTitle className="text-base leading-tight">{title}</CardTitle>
                  <div className="mt-1 flex flex-wrap gap-1">
                    <Badge variant="secondary" className="font-mono text-xs">
                      {item.id}
                    </Badge>
                    {item.gold?.total != null ? (
                      <Badge variant="outline" className="text-xs">
                        {item.gold.total} ouro
                      </Badge>
                    ) : null}
                  </div>
                </div>
              </CardHeader>
              <CardContent className="space-y-2 pt-0">
                {plain ? <p className="text-xs text-muted-foreground">{plain}</p> : null}
                {desc ? (
                  <p className="line-clamp-4 text-xs leading-relaxed text-muted-foreground">{desc}</p>
                ) : null}
              </CardContent>
            </Card>
          );
        })}
      </div>
    </div>
  );
}
