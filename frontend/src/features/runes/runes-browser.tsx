"use client";

import { useQuery } from "@tanstack/react-query";
import { apiGet, ApiError } from "@/shared/lib/api-client";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";

type RuneTree = { treeKey: string; name: string; slots: unknown };

export function RunesBrowser() {
  const q = useQuery({
    queryKey: ["runes"],
    queryFn: async () => {
      const raw = await apiGet<Array<{ treeKey: string; name: string; slots: unknown }>>("/api/runes");
      return raw.map((r) => ({ ...r, slots: r.slots })) as RuneTree[];
    },
  });

  if (q.isLoading) {
    return (
      <div className="grid gap-4 md:grid-cols-2">
        <Skeleton className="h-48" />
        <Skeleton className="h-48" />
      </div>
    );
  }
  if (q.isError) {
    return <p className="text-destructive">{q.error instanceof ApiError ? q.error.message : "Erro"}</p>;
  }

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      {(q.data ?? []).map((tree) => (
        <Card key={tree.treeKey} className="border-border/80">
          <CardHeader className="pb-2">
            <div className="flex items-center justify-between gap-2">
              <CardTitle className="text-lg">{tree.name}</CardTitle>
              <Badge variant="outline">{tree.treeKey}</Badge>
            </div>
          </CardHeader>
          <CardContent>
            <pre className="max-h-64 overflow-auto rounded-md bg-muted/40 p-3 text-xs leading-relaxed">
              {JSON.stringify(tree.slots, null, 2)}
            </pre>
            <p className="mt-2 text-xs text-muted-foreground">
              Estrutura preparada para mapear ícones oficiais via Data Dragon (perk styles / runes).
            </p>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
