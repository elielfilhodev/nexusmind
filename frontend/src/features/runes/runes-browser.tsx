"use client";

import { useCurrentPatch, useDDragonRunes } from "@/shared/hooks/use-ddragon-catalog";
import { perkIconUrl } from "@/shared/lib/ddragon";
import { ApiError } from "@/shared/lib/api-client";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";

export function RunesBrowser() {
  const patchQ = useCurrentPatch();
  const version = patchQ.data?.version;
  const runesQ = useDDragonRunes(version);

  if (patchQ.isLoading || runesQ.isLoading) {
    return (
      <div className="grid gap-4 md:grid-cols-2">
        <Skeleton className="h-64" />
        <Skeleton className="h-64" />
      </div>
    );
  }

  if (patchQ.isError || runesQ.isError) {
    const err = patchQ.error ?? runesQ.error;
    return (
      <p className="text-destructive">
        {err instanceof ApiError ? err.message : "Não foi possível carregar runas do Data Dragon."}
      </p>
    );
  }

  const trees = runesQ.data?.trees ?? [];

  return (
    <div className="space-y-6">
      <p className="text-sm text-muted-foreground">
        CDN <span className="font-mono text-foreground">{runesQ.data?.cdnVersion ?? version}</span> — {trees.length} árvores.
      </p>
      <div className="grid gap-6 lg:grid-cols-2">
        {trees.map((tree) => (
          <Card key={tree.id} className="border-border/80 overflow-hidden">
            <CardHeader className="flex flex-row items-center gap-4 space-y-0 pb-4">
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={perkIconUrl(tree.icon)}
                alt=""
                width={56}
                height={56}
                className="size-14 shrink-0 rounded-lg border border-border/60 bg-muted/30"
              />
              <div className="min-w-0">
                <CardTitle className="text-xl">{tree.name}</CardTitle>
                <Badge variant="outline" className="mt-1 font-mono text-xs">
                  {tree.key}
                </Badge>
              </div>
            </CardHeader>
            <CardContent className="space-y-6">
              {(tree.slots ?? []).map((slot, si) => (
                <div key={si}>
                  <p className="mb-2 text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    Linha {si + 1}
                  </p>
                  <ul className="grid gap-3 sm:grid-cols-2">
                    {(slot.runes ?? []).map((rune) => (
                      <li
                        key={rune.id}
                        className="flex gap-3 rounded-lg border border-border/50 bg-muted/20 p-2 text-sm"
                      >
                        {/* eslint-disable-next-line @next/next/no-img-element */}
                        <img
                          src={perkIconUrl(rune.icon)}
                          alt=""
                          width={40}
                          height={40}
                          className="size-10 shrink-0 rounded-md bg-background"
                          loading="lazy"
                        />
                        <div className="min-w-0">
                          <p className="font-medium leading-tight">{rune.name}</p>
                          <p className="mt-1 line-clamp-3 text-xs text-muted-foreground">{rune.shortDesc}</p>
                        </div>
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
