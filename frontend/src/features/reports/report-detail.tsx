"use client";

import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { Download } from "lucide-react";
import { apiGet, ApiError, downloadReportPdf } from "@/shared/lib/api-client";
import { getClientSessionId } from "@/shared/lib/client-session";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { StructuredView } from "@/widgets/structured-view";

type Detail = {
  id: string;
  kind: string;
  meta: Record<string, unknown>;
  structured: unknown;
};

export function ReportDetail({ id, kind }: { id: string; kind: "CASUAL" | "DRAFT" }) {
  const [sessionId, setSessionId] = useState<string | null>(null);
  useEffect(() => {
    setSessionId(getClientSessionId());
  }, []);

  const q = useQuery({
    queryKey: ["report", id, kind, sessionId],
    queryFn: () => apiGet<Detail>(`/api/reports/${id}?kind=${kind}`),
    enabled: sessionId != null,
  });

  if (sessionId == null || q.isLoading) {
    return <Skeleton className="min-h-[400px] w-full" />;
  }
  if (q.isError) {
    const msg = q.error instanceof ApiError ? q.error.body ?? q.error.message : "Erro";
    return (
      <Card>
        <CardContent className="py-8">
          <p className="text-destructive">{msg}</p>
          <Link href="/reports" className={cn(buttonVariants({ variant: "link" }), "mt-4 px-0")}>
            Voltar
          </Link>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center gap-3">
        <Link href="/reports" className={cn(buttonVariants({ variant: "outline", size: "sm" }))}>
          ← Relatórios
        </Link>
        <button
          type="button"
          className={cn(buttonVariants({ size: "sm" }), "inline-flex items-center")}
          onClick={() => void downloadReportPdf(id, kind)}
        >
          <Download className="mr-2 size-4" />
          Exportar PDF
        </button>
      </div>
      <StructuredView data={q.data?.structured} title={`Relatório ${kind}`} />
    </div>
  );
}
