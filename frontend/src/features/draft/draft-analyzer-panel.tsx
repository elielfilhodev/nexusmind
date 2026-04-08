"use client";

import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { toast } from "sonner";
import { ChevronLeft, ChevronRight, Loader2, Sparkles } from "lucide-react";
import { apiPost, ApiError } from "@/shared/lib/api-client";
import { useCurrentPatch, useDDragonChampions } from "@/shared/hooks/use-ddragon-catalog";
import { useOpGgRankedMeta } from "@/shared/hooks/use-opgg-ranked";
import {
  type BanSuggestion,
  type OpGgRankedChampion,
  suggestBansByBanRate,
  suggestTeamByLaneWinRate,
} from "@/shared/lib/opgg-draft-suggestions";
import { championIconUrl } from "@/shared/lib/ddragon";
import { ChampionSlotPicker } from "@/features/champions/champion-slot-picker";
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
import {
  Dialog,
  DialogDescription,
  DialogHeader,
  DialogPopup,
  DialogTitle,
} from "@/components/ui/dialog";
import { StructuredView } from "@/widgets/structured-view";

const LANES = ["TOP", "JUNGLE", "MID", "ADC", "SUPPORT"] as const;

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

  const patchQuery = useCurrentPatch();
  const version = patchQuery.data?.version;
  const championsQuery = useDDragonChampions(version);

  const champions = championsQuery.data?.champions ?? [];
  const cdnVersion = championsQuery.data?.cdnVersion ?? version ?? "";

  const opggQuery = useOpGgRankedMeta();
  const [bansOpen, setBansOpen] = useState(false);
  const [banSuggestions, setBanSuggestions] = useState<BanSuggestion[]>([]);

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

  const metaBusy = opggQuery.isLoading || opggQuery.isFetching;
  const canSuggest = champions.length > 0 && !metaBusy && !opggQuery.isError && opggQuery.data?.data;

  async function refreshMetaAndRun<T>(fn: (rows: OpGgRankedChampion[]) => T): Promise<T | undefined> {
    const res = await opggQuery.refetch();
    const rows = res.data?.data;
    if (!rows?.length) {
      toast.error("Meta ranked indisponível", {
        description: "Não foi possível obter dados OP.GG. Tente de novo em instantes.",
      });
      return undefined;
    }
    return fn(rows);
  }

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
                Composições, matchups sintéticos e plano por fase. Lista de campeões e ícones via Data Dragon (patch{" "}
                {version ? <span className="font-mono text-foreground">{version}</span> : "…"}). IA com JSON estruturado.
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
                    <Select value={side} onValueChange={(v: string | null) => v != null && setSide(v)}>
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
                    <Select value={contextType} onValueChange={(v: string | null) => v != null && setContextType(v)}>
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
                    <Select value={strategicFocus} onValueChange={(v: string | null) => v != null && setStrategicFocus(v)}>
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

              <div className="space-y-3">
                {!version || championsQuery.isLoading ? (
                  <p className="text-sm text-muted-foreground">Carregando campeões…</p>
                ) : championsQuery.isError ? (
                  <p className="text-sm text-destructive">Falha ao carregar Data Dragon.</p>
                ) : (
                  <>
                    <Card className="border-primary/20 bg-muted/10">
                      <CardHeader className="pb-2">
                        <CardTitle className="text-base">Sugestões meta (OP.GG global)</CardTitle>
                        <CardDescription>
                          Win rate por rota e bans por taxa de ban — ranked, atualizado ao clicar (fonte OP.GG).
                        </CardDescription>
                      </CardHeader>
                      <CardContent className="flex flex-wrap gap-2">
                        <Button
                          type="button"
                          variant="secondary"
                          size="sm"
                          disabled={!canSuggest}
                          onClick={async () => {
                            await refreshMetaAndRun((rows) => {
                              setAlly((prev) => suggestTeamByLaneWinRate(prev, prev, enemy, rows, champions));
                              toast.success("Draft aliado sugerido", {
                                description: "Slots vazios preenchidos pela maior WR na rota (dados globais).",
                              });
                            });
                          }}
                        >
                          {metaBusy ? (
                            <Loader2 className="mr-1.5 size-3.5 animate-spin" />
                          ) : (
                            <Sparkles className="mr-1.5 size-3.5" />
                          )}
                          Sugerir aliados (WR)
                        </Button>
                        <Button
                          type="button"
                          variant="secondary"
                          size="sm"
                          disabled={!canSuggest}
                          onClick={async () => {
                            await refreshMetaAndRun((rows) => {
                              setEnemy((prev) => suggestTeamByLaneWinRate(prev, ally, prev, rows, champions));
                              toast.success("Draft inimigo sugerido", {
                                description: "Slots vazios preenchidos pela maior WR na rota (dados globais).",
                              });
                            });
                          }}
                        >
                          {metaBusy ? (
                            <Loader2 className="mr-1.5 size-3.5 animate-spin" />
                          ) : (
                            <Sparkles className="mr-1.5 size-3.5" />
                          )}
                          Sugerir inimigos (WR)
                        </Button>
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          disabled={!canSuggest}
                          onClick={async () => {
                            await refreshMetaAndRun((rows) => {
                              const bans = suggestBansByBanRate(rows, champions, ally, enemy, 5);
                              if (bans.length === 0) {
                                toast.message("Sem sugestões de ban", {
                                  description: "Todos os campeões com maior taxa de ban já estão no draft.",
                                });
                                return;
                              }
                              setBanSuggestions(bans);
                              setBansOpen(true);
                            });
                          }}
                        >
                          {metaBusy ? (
                            <Loader2 className="mr-1.5 size-3.5 animate-spin" />
                          ) : (
                            <Sparkles className="mr-1.5 size-3.5" />
                          )}
                          Sugerir bans
                        </Button>
                        {opggQuery.isError ? (
                          <span className="w-full text-xs text-destructive">
                            Não foi possível carregar o meta OP.GG.
                          </span>
                        ) : null}
                      </CardContent>
                    </Card>

                    <div className="grid gap-4 lg:grid-cols-2">
                    <Card>
                      <CardHeader>
                        <CardTitle>Time aliado</CardTitle>
                      </CardHeader>
                      <CardContent className="space-y-3">
                        {LANES.map((lane) => (
                          <div key={lane} className="grid grid-cols-[100px_1fr] items-start gap-2">
                            <Label className="text-xs uppercase text-muted-foreground">{lane}</Label>
                            <ChampionSlotPicker
                              version={cdnVersion}
                              champions={champions}
                              value={ally[lane]}
                              onChange={(v) => setAlly((a) => ({ ...a, [lane]: v }))}
                              slotLane={lane}
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
                          <div key={lane} className="grid grid-cols-[100px_1fr] items-start gap-2">
                            <Label className="text-xs uppercase text-muted-foreground">{lane}</Label>
                            <ChampionSlotPicker
                              version={cdnVersion}
                              champions={champions}
                              value={enemy[lane]}
                              onChange={(v) => setEnemy((a) => ({ ...a, [lane]: v }))}
                              slotLane={lane}
                            />
                          </div>
                        ))}
                      </CardContent>
                    </Card>
                    </div>
                  </>
                )}
              </div>

              <Dialog open={bansOpen} onOpenChange={setBansOpen}>
                <DialogPopup className="max-w-md p-0">
                  <DialogHeader>
                    <DialogTitle>Sugestões de ban</DialogTitle>
                    <DialogDescription>
                      Ordenadas por taxa de ban global (OP.GG), excluindo campeões já escolhidos no draft.
                    </DialogDescription>
                  </DialogHeader>
                  <ul className="max-h-72 space-y-2 overflow-y-auto px-4 pb-4">
                    {banSuggestions.map((b) => (
                      <li
                        key={b.riotKey}
                        className="flex items-center gap-3 rounded-lg border border-border/60 bg-muted/20 px-2 py-2"
                      >
                        {/* eslint-disable-next-line @next/next/no-img-element */}
                        <img
                          src={championIconUrl(cdnVersion, b.riotKey)}
                          alt=""
                          width={40}
                          height={40}
                          className="size-10 shrink-0 rounded-md bg-muted object-cover"
                        />
                        <div className="min-w-0 flex-1">
                          <p className="truncate font-medium">{b.name}</p>
                          <p className="text-xs text-muted-foreground">
                            Ban {(b.banRate * 100).toFixed(1)}% · WR {(b.winRate * 100).toFixed(1)}%
                          </p>
                        </div>
                      </li>
                    ))}
                  </ul>
                </DialogPopup>
              </Dialog>

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
