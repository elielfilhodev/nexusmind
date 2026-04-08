"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Activity, Brain, ChevronRight, Crosshair, History, LineChart, Sparkles, Swords, Target } from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogHeader, DialogPopup, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useCurrentPatch, useDDragonChampions } from "@/shared/hooks/use-ddragon-catalog";
import { championIconUrl, itemIconUrl } from "@/shared/lib/ddragon";
import {
  getMatchDetail,
  getMatches,
  getPlayerProfile,
  postMatchAiAnalysis,
  postProfileAiAnalysis,
  type MatchAiAnalysis,
  type MatchDetail,
  type MatchSummary,
  type PlayerProfile,
  type ProfileAiAnalysis,
} from "@/shared/lib/competitive-api";
import { cn } from "@/lib/utils";

function fmtDate(ms: number) {
  return new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" }).format(new Date(ms));
}

function fmtDuration(sec: number) {
  const m = Math.floor(sec / 60);
  const s = sec % 60;
  return `${m}:${s.toString().padStart(2, "0")}`;
}

export function PlayerProfileView({ region, puuid }: { region: string; puuid: string }) {
  const profileQ = useQuery({
    queryKey: ["competitive", "profile", region, puuid],
    queryFn: () => getPlayerProfile(region, puuid),
    staleTime: 120_000,
  });

  const patch = useCurrentPatch();
  const { data: champs } = useDDragonChampions(patch.data?.version);

  const idToKey = useMemo(() => {
    const m = new Map<number, string>();
    if (!champs?.champions) return m;
    for (const c of champs.champions) {
      const id = Number.parseInt(c.numericKey, 10);
      if (!Number.isNaN(id)) m.set(id, c.riotKey);
    }
    return m;
  }, [champs]);

  const version = champs?.cdnVersion ?? patch.data?.version;

  if (profileQ.isLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-48 w-full rounded-2xl" />
        <Skeleton className="h-40 w-full rounded-xl" />
      </div>
    );
  }

  if (profileQ.isError || !profileQ.data) {
    return (
      <Card className="border-destructive/40">
        <CardContent className="py-12 text-center text-sm text-muted-foreground">
          Perfil indisponível. Confirme RIOT_API_KEY, região e PUUID.
        </CardContent>
      </Card>
    );
  }

  const p = profileQ.data;

  return (
    <div className="space-y-8">
      <ProfileHero p={p} version={version} idToKey={idToKey} />

      <Tabs defaultValue="overview" className="w-full">
        <TabsList className="flex w-full flex-wrap justify-start gap-1 bg-muted/40 p-1">
          <TabsTrigger value="overview" className="gap-1.5">
            <Activity className="size-4" />
            Visão geral
          </TabsTrigger>
          <TabsTrigger value="stats" className="gap-1.5">
            <LineChart className="size-4" />
            Estatísticas
          </TabsTrigger>
          <TabsTrigger value="matches" className="gap-1.5">
            <History className="size-4" />
            Partidas
          </TabsTrigger>
          <TabsTrigger value="ai" className="gap-1.5">
            <Brain className="size-4" />
            IA
          </TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="mt-6 space-y-6">
          <div className="grid gap-4 md:grid-cols-3">
            <StatCard title="Winrate (amostra)" value={`${p.aggregated.winrate.toFixed(1)}%`} hint={`${p.aggregated.totalGamesSample} jogos`} />
            <StatCard title="KDA médio" value={p.aggregated.avgKda.toFixed(2)} hint="Histórico recente" />
            <StatCard title="CS/min" value={p.aggregated.avgCsPerMin.toFixed(1)} hint="Média amostral" />
          </div>
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Temporadas (agregado)</CardTitle>
              <CardDescription>Baseado em timestamps de partida — aproximação de season.</CardDescription>
            </CardHeader>
            <CardContent className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {p.seasonSummaries.map((s) => (
                <div key={s.seasonId} className="rounded-lg border border-border/50 bg-muted/20 p-3">
                  <div className="text-xs font-semibold uppercase tracking-wide text-primary">{s.seasonId}</div>
                  <div className="mt-2 text-sm text-muted-foreground">
                    {s.games} partidas · {s.winrate.toFixed(1)}% WR · KDA {s.avgKda.toFixed(2)}
                  </div>
                </div>
              ))}
              {p.seasonSummaries.length === 0 ? <p className="text-sm text-muted-foreground">Sem amostra suficiente.</p> : null}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="stats" className="mt-6 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <Crosshair className="size-4" />
                Campeões mais jogados (amostra)
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {p.championPool.map((c) => {
                const key = idToKey.get(c.championId);
                const url = version && key ? championIconUrl(version, key) : "";
                return (
                  <div
                    key={c.championId}
                    className="flex items-center justify-between gap-3 rounded-lg border border-border/50 bg-card/40 px-3 py-2"
                  >
                    <div className="flex items-center gap-3">
                      {url ? (
                        // eslint-disable-next-line @next/next/no-img-element
                        <img src={url} alt="" className="h-10 w-10 rounded-md border border-border/60" />
                      ) : (
                        <div className="h-10 w-10 rounded-md bg-muted" />
                      )}
                      <div>
                        <div className="text-sm font-medium">Campeão #{c.championId}</div>
                        <div className="text-xs text-muted-foreground">
                          {c.games} jogos · {c.winrate.toFixed(1)}% WR
                        </div>
                      </div>
                    </div>
                    <div className="text-right text-xs tabular-nums text-muted-foreground">
                      KDA {c.avgKda.toFixed(2)}
                      <br />
                      CS/m {c.avgCsPerMin.toFixed(1)}
                    </div>
                  </div>
                );
              })}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <Target className="size-4" />
                Picos / ligas
              </CardTitle>
              <CardDescription>{p.provenance.disclaimer}</CardDescription>
            </CardHeader>
            <CardContent className="space-y-2">
              {p.peakRanks.map((pk) => (
                <div key={pk.queueType + pk.seasonId} className="rounded-lg border border-border/40 bg-muted/15 p-3 text-sm">
                  <div className="font-medium">
                    {pk.queueType} · {pk.tier} {pk.rank} ({pk.leaguePoints} LP)
                  </div>
                  <div className="text-xs text-muted-foreground">{pk.note}</div>
                </div>
              ))}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="matches" className="mt-6">
          <MatchHistorySection region={region} puuid={puuid} idToKey={idToKey} version={version} />
        </TabsContent>

        <TabsContent value="ai" className="mt-6 space-y-4">
          <Card className="border-primary/20 bg-gradient-to-br from-primary/5 to-transparent">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Brain className="size-5 text-primary" />
                Análise de perfil (IA)
              </CardTitle>
              <CardDescription>Interpretação agregada do estilo de jogo e padrões — útil para coach e scout.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <ProfileAiActions region={region} puuid={puuid} />
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}

function ProfileAiActions({ region, puuid }: { region: string; puuid: string }) {
  const qc = useQueryClient();
  const [pending, setPending] = useState(false);
  return (
    <>
      <Button
        className="gap-2"
        onClick={async () => {
          setPending(true);
          try {
            const res = await postProfileAiAnalysis(region, puuid);
            qc.setQueryData(["competitive", "profileAi", region, puuid], res);
            toast.success("Análise pronta");
          } catch {
            toast.error("Não foi possível gerar a análise");
          } finally {
            setPending(false);
          }
        }}
        disabled={pending}
      >
        <Sparkles className="size-4" />
        Gerar / atualizar análise
      </Button>
      <ProfileAiPanel data={qc.getQueryData(["competitive", "profileAi", region, puuid]) as ProfileAiAnalysis | undefined} />
    </>
  );
}

function ProfileAiPanel({ data }: { data: ProfileAiAnalysis | undefined }) {
  if (!data) {
    return <p className="text-sm text-muted-foreground">Gere uma análise para ver insights estruturados.</p>;
  }
  return (
    <div className="space-y-4 rounded-xl border border-border/50 bg-card/50 p-4">
      <p className="text-sm leading-relaxed">{data.overview}</p>
      <Separator />
      <div className="grid gap-4 md:grid-cols-3">
        <div>
          <p className="text-xs font-semibold uppercase text-muted-foreground">Traços</p>
          <ul className="mt-2 list-inside list-disc text-sm">
            {data.traits.map((t) => (
              <li key={t}>{t}</li>
            ))}
          </ul>
        </div>
        <div>
          <p className="text-xs font-semibold uppercase text-muted-foreground">Riscos</p>
          <ul className="mt-2 list-inside list-disc text-sm">
            {data.risks.map((t) => (
              <li key={t}>{t}</li>
            ))}
          </ul>
        </div>
        <div>
          <p className="text-xs font-semibold uppercase text-muted-foreground">Recomendações</p>
          <ul className="mt-2 list-inside list-disc text-sm">
            {data.recommendations.map((t) => (
              <li key={t}>{t}</li>
            ))}
          </ul>
        </div>
      </div>
      <p className="text-sm text-muted-foreground">
        <strong className="text-foreground">Leitura competitiva:</strong> {data.competitiveRead}
      </p>
      <p className="text-[10px] text-muted-foreground">Modelo: {data.model}</p>
    </div>
  );
}

function ProfileHero({ p, version, idToKey }: { p: PlayerProfile; version: string | undefined; idToKey: Map<number, string> }) {
  const banner = p.bannerSplashUrl;
  const top = p.topMasteries[0];
  const topKey = top ? idToKey.get(top.championId) : undefined;
  const topIcon = version && topKey ? championIconUrl(version, topKey) : "";

  return (
    <section className="overflow-hidden rounded-2xl border border-border/60">
      <div className="relative h-44 w-full md:h-56">
        {banner ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={banner} alt="" className="h-full w-full object-cover object-top" />
        ) : (
          <div className="h-full w-full bg-gradient-to-br from-zinc-900 via-background to-zinc-800" />
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/80 to-transparent" />
        <div className="absolute bottom-0 left-0 right-0 p-6 md:p-8">
          <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
            <div className="flex items-end gap-4">
              <div className="relative -mt-14 h-24 w-24 shrink-0 overflow-hidden rounded-2xl border-2 border-primary/40 bg-background shadow-xl md:h-28 md:w-28">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img src={p.profileIconUrl} alt="" className="h-full w-full object-cover" />
              </div>
              <div>
                <div className="flex flex-wrap items-center gap-2">
                  <h1 className="text-2xl font-semibold tracking-tight md:text-3xl">{p.riotId}</h1>
                  {p.professional ? (
                    <Badge className="bg-amber-500/20 text-amber-100 hover:bg-amber-500/25">
                      <Swords className="mr-1 size-3" />
                      Pro
                    </Badge>
                  ) : null}
                </div>
                <p className="text-sm text-muted-foreground">
                  {p.summonerName} · Nv. {p.summonerLevel} · {p.platformId}
                  {p.teamName ? ` · ${p.teamName}` : ""}
                </p>
                <div className="mt-2 flex flex-wrap gap-2">
                  {p.primaryRole ? (
                    <Badge variant="outline" className="text-xs">
                      {p.primaryRole}
                    </Badge>
                  ) : null}
                  {p.competitiveRegion ? (
                    <Badge variant="outline" className="text-xs">
                      {p.competitiveRegion}
                    </Badge>
                  ) : null}
                </div>
              </div>
            </div>
            <div className="flex items-center gap-3">
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img src={p.rankedEmblemUrl} alt="" className="h-14 w-14 object-contain" />
              <div className="text-right text-sm">
                <div className="font-semibold">
                  {p.soloRank ? `${p.soloRank.tier} ${p.soloRank.rank} · ${p.soloRank.leaguePoints} LP` : "Unranked (solo)"}
                </div>
                <div className="text-xs text-muted-foreground">
                  {p.flexRank ? `Flex: ${p.flexRank.tier} ${p.flexRank.rank}` : "Flex — /"}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      {p.scoutingTags ? (
        <div className="border-t border-border/50 bg-muted/10 px-6 py-3 text-xs text-muted-foreground md:px-8">
          <span className="font-medium text-foreground">Tags: </span>
          {p.scoutingTags}
        </div>
      ) : null}
      <div className="flex gap-2 overflow-x-auto border-t border-border/50 bg-card/30 px-6 py-3 md:px-8">
        {p.topMasteries.slice(0, 3).map((m) => {
          const k = idToKey.get(m.championId);
          const url = version && k ? championIconUrl(version, k) : topIcon;
          return (
            <div key={m.championId} className="flex items-center gap-2 rounded-lg bg-muted/20 px-2 py-1">
              {url ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img src={url} alt="" className="h-8 w-8 rounded-md" />
              ) : (
                <div className="h-8 w-8 rounded-md bg-muted" />
              )}
              <span className="text-xs text-muted-foreground">{m.championPoints.toLocaleString("pt-BR")} pts</span>
            </div>
          );
        })}
      </div>
    </section>
  );
}

function StatCard({ title, value, hint }: { title: string; value: string; hint: string }) {
  return (
    <Card className="border-border/60 bg-card/50">
      <CardHeader className="pb-2">
        <CardDescription>{title}</CardDescription>
        <CardTitle className="text-2xl tabular-nums">{value}</CardTitle>
      </CardHeader>
      <CardContent className="text-xs text-muted-foreground">{hint}</CardContent>
    </Card>
  );
}

function MatchHistorySection({
  region,
  puuid,
  idToKey,
  version,
}: {
  region: string;
  puuid: string;
  idToKey: Map<number, string>;
  version: string | undefined;
}) {
  const [page, setPage] = useState(0);
  const [season, setSeason] = useState<string | undefined>(undefined);
  const [championFilter, setChampionFilter] = useState("");
  const [queueId, setQueueId] = useState<string>("all");
  const [outcome, setOutcome] = useState<string>("all");
  const [open, setOpen] = useState<string | null>(null);
  const [lastAi, setLastAi] = useState<MatchAiAnalysis | null>(null);

  const listQ = useQuery({
    queryKey: ["competitive", "matches", region, puuid, page, season, championFilter, queueId, outcome],
    queryFn: () =>
      getMatches(region, puuid, {
        page,
        size: 12,
        season: season || undefined,
        championId: championFilter ? Number.parseInt(championFilter, 10) : undefined,
        queueId: queueId === "all" ? undefined : Number.parseInt(queueId, 10),
        outcome: outcome === "all" ? undefined : outcome,
      }),
    staleTime: 60_000,
  });

  const detailQ = useQuery({
    queryKey: ["competitive", "match", region, open, puuid],
    queryFn: () => getMatchDetail(region, open!, puuid),
    enabled: Boolean(open),
  });

  const matchAi = useMutation({
    mutationFn: (mid: string) => postMatchAiAnalysis(region, mid, puuid),
    onSuccess: (data) => {
      setLastAi(data);
      toast.success("Análise da partida pronta");
    },
    onError: () => toast.error("Falha na análise"),
  });

  const rows = listQ.data?.matches ?? [];

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader className="pb-4">
          <CardTitle className="text-base">Filtros do histórico</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-wrap gap-3">
          <div className="space-y-1.5">
            <Label className="text-xs">Season</Label>
            <Input placeholder="S15" value={season ?? ""} onChange={(e) => setSeason(e.target.value || undefined)} />
          </div>
          <div className="space-y-1.5">
            <Label className="text-xs">Champion id</Label>
            <Input placeholder="157" value={championFilter} onChange={(e) => setChampionFilter(e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label className="text-xs">Fila</Label>
            <Select value={queueId} onValueChange={(v) => v && setQueueId(v)}>
              <SelectTrigger className="w-[180px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Todas</SelectItem>
                <SelectItem value="420">Ranked Solo</SelectItem>
                <SelectItem value="440">Flex</SelectItem>
                <SelectItem value="450">ARAM</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label className="text-xs">Resultado</Label>
            <Select value={outcome} onValueChange={(v) => v && setOutcome(v)}>
              <SelectTrigger className="w-[160px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Todos</SelectItem>
                <SelectItem value="win">Vitória</SelectItem>
                <SelectItem value="loss">Derrota</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {listQ.isLoading ? (
        <Skeleton className="h-40 w-full" />
      ) : (
        <div className="space-y-2">
          {rows.map((m) => (
            <MatchRow
              key={m.matchId}
              m={m}
              version={version}
              idToKey={idToKey}
              onOpen={() => setOpen(m.matchId)}
              onAi={() => matchAi.mutate(m.matchId)}
              aiLoading={matchAi.isPending}
            />
          ))}
          {rows.length === 0 ? <p className="py-8 text-center text-sm text-muted-foreground">Nenhuma partida na amostra.</p> : null}
        </div>
      )}

      <div className="flex justify-between gap-2">
        <Button variant="outline" size="sm" disabled={page <= 0} onClick={() => setPage((p) => p - 1)}>
          Anterior
        </Button>
        <Button
          variant="outline"
          size="sm"
          disabled={!listQ.data || (page + 1) * 12 >= listQ.data.total}
          onClick={() => setPage((p) => p + 1)}
        >
          Próxima
        </Button>
      </div>

      <Dialog open={Boolean(open)} onOpenChange={(v) => !v && setOpen(null)}>
        <DialogPopup className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>Detalhe da partida</DialogTitle>
          </DialogHeader>
          <div className="px-4 pb-4">
            {detailQ.isLoading ? <Skeleton className="h-40 w-full" /> : null}
            {detailQ.data ? <MatchDetailInner d={detailQ.data} version={version} idToKey={idToKey} /> : null}
          </div>
        </DialogPopup>
      </Dialog>

      {lastAi ? (
        <Card className="border-primary/25">
          <CardHeader>
            <CardTitle className="text-base">Última análise de partida</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-sm">
            <p>{lastAi.summary}</p>
            <Separator />
            <ul className="list-inside list-disc text-xs text-muted-foreground">
              {lastAi.strengths.map((s) => (
                <li key={s}>{s}</li>
              ))}
            </ul>
            <p className="text-xs text-muted-foreground">{lastAi.confidenceNote}</p>
          </CardContent>
        </Card>
      ) : null}
    </div>
  );
}

function MatchRow({
  m,
  version,
  idToKey,
  onOpen,
  onAi,
  aiLoading,
}: {
  m: MatchSummary;
  version: string | undefined;
  idToKey: Map<number, string>;
  onOpen: () => void;
  onAi: () => void;
  aiLoading: boolean;
}) {
  const key = idToKey.get(m.championId);
  const url = version && key ? championIconUrl(version, key) : "";
  return (
    <div
      className={cn(
        "flex flex-col gap-3 rounded-xl border border-border/50 bg-card/40 p-4 transition-colors hover:border-primary/30 md:flex-row md:items-center md:justify-between"
      )}
    >
      <div className="flex items-center gap-3">
        {url ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={url} alt="" className="h-12 w-12 rounded-lg border border-border/60" />
        ) : (
          <div className="h-12 w-12 rounded-lg bg-muted" />
        )}
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <span className={cn("text-sm font-semibold", m.win ? "text-emerald-400" : "text-rose-400")}>
              {m.win ? "Vitória" : "Derrota"}
            </span>
            <Badge variant="outline" className="text-[10px]">
              {m.queueLabel}
            </Badge>
            {m.mvpHeuristic ? (
              <Badge className="text-[10px]">Destaque</Badge>
            ) : null}
          </div>
          <p className="text-xs text-muted-foreground">
            {fmtDate(m.gameCreation)} · {fmtDuration(m.durationSec)} · {m.seasonId}
          </p>
          <p className="text-sm text-muted-foreground">
            KDA {m.kills}/{m.deaths}/{m.assists} · KP {m.killParticipation.toFixed(0)}% · CS/m {m.csPerMin.toFixed(1)}
          </p>
        </div>
      </div>
      <div className="flex flex-wrap gap-2">
        <div className="flex gap-0.5">
          {m.items
            .filter((i) => i > 0)
            .map((iid) => (
              // eslint-disable-next-line @next/next/no-img-element
              <img
                key={iid}
                src={version ? itemIconUrl(version, iid) : ""}
                alt=""
                className="h-7 w-7 rounded border border-border/40 bg-muted/40"
              />
            ))}
        </div>
        <Button size="sm" variant="secondary" className="gap-1" onClick={onOpen}>
          Detalhes
          <ChevronRight className="size-4" />
        </Button>
        <Button size="sm" variant="outline" className="gap-1" onClick={onAi} disabled={aiLoading}>
          <Brain className="size-4" />
          IA
        </Button>
      </div>
    </div>
  );
}

function MatchDetailInner({
  d,
  version,
  idToKey,
}: {
  d: MatchDetail;
  version: string | undefined;
  idToKey: Map<number, string>;
}) {
  return (
    <ScrollArea className="max-h-[480px] pr-3">
      <div className="space-y-4 text-sm">
        <p className="text-xs text-muted-foreground">
          {d.queueLabel} · {fmtDate(d.gameCreation)} · {d.gameVersion}
        </p>
        <Separator />
        <p className="font-medium">Seu time</p>
        <div className="space-y-2">
          {[d.subject, ...d.sameTeam].map((p) => (
            <ParticipantLine key={p.puuid + p.championId} p={p} version={version} idToKey={idToKey} />
          ))}
        </div>
        <p className="font-medium">Time inimigo</p>
        <div className="space-y-2">
          {d.enemyTeam.map((p) => (
            <ParticipantLine key={p.puuid + p.championId} p={p} version={version} idToKey={idToKey} />
          ))}
        </div>
      </div>
    </ScrollArea>
  );
}

function ParticipantLine({
  p,
  version,
  idToKey,
}: {
  p: MatchDetail["subject"];
  version: string | undefined;
  idToKey: Map<number, string>;
}) {
  const k = idToKey.get(p.championId);
  const url = version && k ? championIconUrl(version, k) : "";
  return (
    <div className="flex items-center justify-between gap-2 rounded-lg border border-border/40 bg-muted/15 px-2 py-1.5">
      <div className="flex items-center gap-2 min-w-0">
        {url ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={url} alt="" className="h-8 w-8 rounded-md" />
        ) : (
          <div className="h-8 w-8 rounded-md bg-muted" />
        )}
        <span className="truncate text-xs">{p.riotId}</span>
      </div>
      <span className="text-xs tabular-nums text-muted-foreground">
        {p.kills}/{p.deaths}/{p.assists}
      </span>
    </div>
  );
}
