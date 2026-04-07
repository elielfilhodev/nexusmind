"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { toast } from "sonner";
import { ChevronLeft, ChevronRight, Loader2 } from "lucide-react";
import { apiGet, apiPost, ApiError } from "@/shared/lib/api-client";
import { Button, buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Separator } from "@/components/ui/separator";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { StructuredView } from "@/widgets/structured-view";

type Champion = { riotKey: string; name: string };

const LANES = ["TOP", "JUNGLE", "MID", "ADC", "SUPPORT"] as const;

function ChampionPickSelect({
  champions,
  value,
  onChange,
}: {
  champions: Champion[];
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <Select
      value={value || "__empty__"}
      onValueChange={(v) => {
        if (v == null) return;
        onChange(v === "__empty__" ? "" : v);
      }}
    >
      <SelectTrigger className="w-full">
        <SelectValue placeholder="—" />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="__empty__">—</SelectItem>
        {champions.map((c) => (
          <SelectItem key={c.riotKey} value={c.riotKey}>
            {c.name}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}

const NAV = [
  { id: "summary", label: "Resumo" },
  { id: "form", label: "Draft" },
  { id: "structured", label: "Relatório" },
];

export function DraftAnalyzerPanel() {
  const [open, setOpen] = useState(true);
  const [side, setSide] = useState<string>("BLUE");
  const [contextType, setContextType] = useState("SCRIM");
  const [strategicFocus, setStrategicFocus] = useState("EARLY_GAME");
  const [ally, setAlly] = useState<Record<(typeof LANES)[number], string>>({
    TOP: "",
    JUNGLE: "",
    MID: "",
    ADC: "",
    SUPPORT: "",
  });
  const [enemy, setEnemy] = useState<Record<(typeof LANES)[number], string>>({
    TOP: "",
    JUNGLE: "",
    MID: "",
    ADC: "",
    SUPPORT: "",
  });

  const championsQuery = useQuery({
    queryKey: ["champions"],
    queryFn: () => apiGet<Champion[]>("/api/champions"),
  });

  const champions = championsQuery.data ?? [];

  const mutation = useMutation({
    mutationFn: () =>
      apiPost<{ id: string; structured: unknown }>("/api/analysis/draft", {
        side: side || undefined,
        contextType: contextType || undefined,
        strategicFocus: strategicFocus || undefined,
        ally: {
          top: ally.TOP || undefined,
          jungle: ally.JUNGLE || undefined,
          mid: ally.MID || undefined,
          adc: ally.ADC || undefined,
          support: ally.SUPPORT || undefined,
        },
        enemy: {
          top: enemy.TOP || undefined,
          jungle: enemy.JUNGLE || undefined,
          mid: enemy.MID || undefined,
          adc: enemy.ADC || undefined,
          support: enemy.SUPPORT || undefined,
        },
      }),
    onError: (e: Error) => {
      const msg = e instanceof ApiError ? e.body ?? e.message : e.message;
      toast.error("Falha no draft analyzer", { description: msg?.slice(0, 200) });
    },
    onSuccess: () => toast.success("Relatório gerado"),
  });


  return (
    <div className="flex min-h-[70vh] gap-0 rounded-xl border border-border/80 bg-card/30 overflow-hidden">
      <aside
        className={`border-r border-border/80 bg-muted/20 transition-[width] duration-200 ${
          open ? "w-52 shrink-0" : "w-0 overflow-hidden border-0"
        }`}
      >
        <ScrollArea className="h-full py-4">
          <nav className="flex flex-col gap-1 px-2">
            {NAV.map((n) => (
              <a
                key={n.id}
                href={`#${n.id}`}
                className={cn(buttonVariants({ variant: "ghost", size: "sm" }), "justify-start")}
              >
                {n.label}
              </a>
            ))}
          </nav>
        </ScrollArea>
      </aside>
      <div className="flex flex-1 flex-col">
        <div className="flex items-center gap-2 border-b border-border/60 px-3 py-2">
          <Button variant="ghost" size="icon" type="button" onClick={() => setOpen((o) => !o)} aria-label="Alternar sidebar">
            {open ? <ChevronLeft className="size-4" /> : <ChevronRight className="size-4" />}
          </Button>
          <span className="text-sm text-muted-foreground">Navegação por seções</span>
        </div>
        <ScrollArea className="h-[calc(70vh-48px)]">
          <div className="space-y-10 p-4 md:p-6">
            <section id="summary">
              <h2 className="text-2xl font-semibold tracking-tight">Draft Analyzer</h2>
              <p className="mt-2 max-w-2xl text-muted-foreground">
                Composições, matchups sintéticos e plano por fase. Integração IA com JSON estruturado; fallback heurístico sem API key.
              </p>
            </section>

            <section id="form" className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle>Contexto</CardTitle>
                  <CardDescription>Lado, formato competitivo e foco estratégico.</CardDescription>
                </CardHeader>
                <CardContent className="grid gap-4 sm:grid-cols-3">
                  <div className="space-y-2">
                    <Label>Lado</Label>
                    <Select value={side} onValueChange={(v) => v != null && setSide(v)}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="BLUE">Blue</SelectItem>
                        <SelectItem value="RED">Red</SelectItem>
                        <SelectItem value="UNKNOWN">Indefinido</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label>Contexto</Label>
                    <Select value={contextType} onValueChange={(v) => v != null && setContextType(v)}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {["SCRIM", "CAMPEONATO", "SOLO_QUEUE", "MD3", "MD5"].map((c) => (
                          <SelectItem key={c} value={c}>
                            {c}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label>Foco</Label>
                    <Select value={strategicFocus} onValueChange={(v) => v != null && setStrategicFocus(v)}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {[
                          "EARLY_GAME",
                          "SCALING",
                          "ENGAGE",
                          "DISENGAGE",
                          "SPLIT",
                          "POKE",
                          "DIVE",
                          "OBJECTIVE_CONTROL",
                        ].map((c) => (
                          <SelectItem key={c} value={c}>
                            {c}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </CardContent>
              </Card>

              <div className="grid gap-4 lg:grid-cols-2">
                <Card>
                  <CardHeader>
                    <CardTitle>Time aliado</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    {LANES.map((lane) => (
                      <div key={lane} className="grid grid-cols-[100px_1fr] items-center gap-2">
                        <Label className="text-xs uppercase text-muted-foreground">{lane}</Label>
                        <ChampionPickSelect
                          champions={champions}
                          value={ally[lane]}
                          onChange={(v) => setAlly((a) => ({ ...a, [lane]: v }))}
                        />
                      </div>
                    ))}
                  </CardContent>
                </Card>
                <Card>
                  <CardHeader>
                    <CardTitle>Time inimigo</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    {LANES.map((lane) => (
                      <div key={lane} className="grid grid-cols-[100px_1fr] items-center gap-2">
                        <Label className="text-xs uppercase text-muted-foreground">{lane}</Label>
                        <ChampionPickSelect
                          champions={champions}
                          value={enemy[lane]}
                          onChange={(v) => setEnemy((a) => ({ ...a, [lane]: v }))}
                        />
                      </div>
                    ))}
                  </CardContent>
                </Card>
              </div>

              <Button onClick={() => mutation.mutate()} disabled={mutation.isPending} size="lg">
                {mutation.isPending ? (
                  <>
                    <Loader2 className="mr-2 size-4 animate-spin" />
                    Gerando relatório…
                  </>
                ) : (
                  "Gerar relatório profissional"
                )}
              </Button>
            </section>

            <Separator />

            <section id="structured">
              <h3 className="mb-4 text-lg font-medium">Saída</h3>
              {mutation.isPending ? (
                <p className="text-muted-foreground">Processando…</p>
              ) : mutation.data ? (
                <StructuredView data={mutation.data.structured} />
              ) : (
                <p className="text-muted-foreground">Nenhum relatório ainda.</p>
              )}
            </section>
          </div>
        </ScrollArea>
      </div>
    </div>
  );
}
