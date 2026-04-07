"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";
import { apiGet, apiPost, ApiError } from "@/shared/lib/api-client";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { StructuredView } from "@/widgets/structured-view";

const schema = z.object({
  elo: z.string().min(1, "Informe o elo"),
  lane: z.enum(["TOP", "JUNGLE", "MID", "ADC", "SUPPORT"]),
  playstyle: z.string().min(1, "Escolha um estilo"),
  region: z.string().optional(),
  favoriteChampionKey: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

const elos = [
  "IRON",
  "BRONZE",
  "SILVER",
  "GOLD",
  "PLATINUM",
  "EMERALD",
  "DIAMOND",
  "MASTER",
  "GRANDMASTER",
  "CHALLENGER",
];

const playstyles = [
  { v: "AGRESSIVO", l: "Agressivo" },
  { v: "ESCALAVEL", l: "Escalável" },
  { v: "SEGURO", l: "Seguro" },
  { v: "ROAMING", l: "Roaming" },
  { v: "TEAMFIGHT", l: "Teamfight" },
  { v: "SPLITPUSH", l: "Split push" },
];

type Champion = { riotKey: string; name: string };

export function CasualAnalysisPanel() {
  const championsQuery = useQuery({
    queryKey: ["champions"],
    queryFn: () => apiGet<Champion[]>("/api/champions"),
  });

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      elo: "GOLD",
      lane: "MID",
      playstyle: "ESCALAVEL",
      region: "",
      favoriteChampionKey: "",
    },
  });

  const mutation = useMutation({
    mutationFn: (body: FormValues) =>
      apiPost<{ id: string; structured: unknown; summary: string }>("/api/analysis/casual", {
        elo: body.elo,
        lane: body.lane,
        playstyle: body.playstyle,
        region: body.region || undefined,
        favoriteChampionKey: body.favoriteChampionKey || undefined,
      }),
    onError: (e: Error) => {
      const msg = e instanceof ApiError ? e.body ?? e.message : e.message;
      toast.error("Falha ao gerar análise", { description: msg?.slice(0, 200) });
    },
    onSuccess: () => toast.success("Análise gerada e salva"),
  });

  return (
    <div className="grid gap-8 lg:grid-cols-2">
      <Card className="border-border/80">
        <CardHeader>
          <CardTitle>Parâmetros</CardTitle>
          <CardDescription>Informe seu contexto de solo queue — a IA combina com o catálogo do patch.</CardDescription>
        </CardHeader>
        <CardContent>
          <form
            className="space-y-4"
            onSubmit={form.handleSubmit((v) => mutation.mutate(v))}
          >
            <div className="space-y-2">
              <Label>Elo</Label>
              <Select
                value={form.watch("elo")}
                onValueChange={(v) => v && form.setValue("elo", v)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Elo" />
                </SelectTrigger>
                <SelectContent>
                  {elos.map((e) => (
                    <SelectItem key={e} value={e}>
                      {e}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Lane</Label>
              <Select
                value={form.watch("lane")}
                onValueChange={(v) => v && form.setValue("lane", v as FormValues["lane"])}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {(["TOP", "JUNGLE", "MID", "ADC", "SUPPORT"] as const).map((l) => (
                    <SelectItem key={l} value={l}>
                      {l}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Estilo</Label>
              <Select
                value={form.watch("playstyle")}
                onValueChange={(v) => v && form.setValue("playstyle", v)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {playstyles.map((p) => (
                    <SelectItem key={p.v} value={p.v}>
                      {p.l}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="region">Região (opcional)</Label>
              <Input id="region" {...form.register("region")} placeholder="BR, EUW…" />
            </div>
            <div className="space-y-2">
              <Label>Campeão favorito (opcional)</Label>
              {championsQuery.isLoading ? (
                <Skeleton className="h-10 w-full" />
              ) : (
                <Select
                  value={form.watch("favoriteChampionKey") || "__none__"}
                  onValueChange={(v) => {
                    if (!v) return;
                    form.setValue("favoriteChampionKey", v === "__none__" ? "" : v);
                  }}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Nenhum" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="__none__">Nenhum</SelectItem>
                    {(championsQuery.data ?? []).map((c) => (
                      <SelectItem key={c.riotKey} value={c.riotKey}>
                        {c.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            </div>
            <Button type="submit" className="w-full" disabled={mutation.isPending}>
              {mutation.isPending ? (
                <>
                  <Loader2 className="mr-2 size-4 animate-spin" />
                  Gerando…
                </>
              ) : (
                "Gerar análise"
              )}
            </Button>
          </form>
        </CardContent>
      </Card>

      <div className="min-h-[320px]">
        {mutation.isPending ? (
          <div className="space-y-3">
            <Skeleton className="h-40 w-full" />
            <Skeleton className="h-40 w-full" />
          </div>
        ) : mutation.data ? (
          <div className="space-y-4">
            <p className="text-sm text-muted-foreground">
              ID: <span className="font-mono text-foreground">{mutation.data.id}</span>
            </p>
            <StructuredView data={mutation.data.structured} title="Resultado estruturado" />
          </div>
        ) : (
          <Card className="border-dashed border-border/80 bg-muted/20">
            <CardContent className="flex min-h-[280px] items-center justify-center p-8 text-center text-muted-foreground">
              Envie o formulário para ver picks, bans, runas, macro e plano de climb.
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}
