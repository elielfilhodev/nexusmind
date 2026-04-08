"use client";

import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { apiGet, ApiError } from "@/shared/lib/api-client";
import { getClientSessionId } from "@/shared/lib/client-session";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

type PageReports = {
  content: Array<{
    kind: string;
    id: string;
    createdAt: string;
    summary: string;
  }>;
  totalElements: number;
  number: number;
  size: number;
};

export function ReportsTable() {
  const [sessionId, setSessionId] = useState<string | null>(null);
  useEffect(() => {
    setSessionId(getClientSessionId());
  }, []);

  const q = useQuery({
    queryKey: ["reports", sessionId],
    queryFn: () => apiGet<PageReports>("/api/reports?page=0&size=30"),
    enabled: sessionId != null,
  });

  if (sessionId == null || q.isLoading) {
    return (
      <div className="space-y-2">
        <Skeleton className="h-12 w-full" />
        <Skeleton className="h-12 w-full" />
      </div>
    );
  }

  if (q.isError) {
    const msg = q.error instanceof ApiError ? q.error.message : "Erro";
    return <p className="text-destructive">{msg}</p>;
  }

  const rows = q.data?.content ?? [];

  if (rows.length === 0) {
    return (
      <Card className="border-dashed">
        <CardContent className="py-12 text-center text-muted-foreground">
          Nenhum relatório ainda. Gere uma análise casual ou de draft.
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Recentes</CardTitle>
      </CardHeader>
      <CardContent className="space-y-2">
        {rows.map((r) => (
          <div
            key={`${r.kind}-${r.id}`}
            className="flex flex-col gap-2 rounded-lg border border-border/60 p-3 sm:flex-row sm:items-center sm:justify-between"
          >
            <div>
              <div className="flex items-center gap-2">
                <Badge variant={r.kind === "DRAFT" ? "default" : "secondary"}>{r.kind}</Badge>
                <span className="font-mono text-xs text-muted-foreground">{r.id.slice(0, 8)}…</span>
              </div>
              <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">{r.summary || "—"}</p>
              <p className="text-xs text-muted-foreground">{new Date(r.createdAt).toLocaleString()}</p>
            </div>
            <Link
              href={`/reports/${r.id}?kind=${r.kind}`}
              className={cn(buttonVariants({ size: "sm", variant: "outline" }))}
            >
              Abrir
            </Link>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
