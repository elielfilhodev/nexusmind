"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { Search, Sparkles, Trophy } from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button, buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { useCurrentPatch, useDDragonChampions } from "@/shared/hooks/use-ddragon-catalog";
import { championIconUrl } from "@/shared/lib/ddragon";
import { getLeaderboard, getRegions, searchPlayer, type LeaderboardPage } from "@/shared/lib/competitive-api";
import { cn } from "@/lib/utils";

const QUEUES = [
  { value: "RANKED_SOLO_5x5", label: "Solo/Duo" },
  { value: "RANKED_FLEX_SR", label: "Flex" },
];

const SORTS = [
  { value: "LP_DESC", label: "LP (maior)" },
  { value: "WINRATE_DESC", label: "Winrate" },
];

function champIcon(version: string | undefined, championId: number, map: Map<number, string>) {
  const key = map.get(championId);
  if (!version || !key) return "";
  return championIconUrl(version, key);
}

export function LeaderboardView() {
  const router = useRouter();
  const [region, setRegion] = useState("BR1");
  const [queue, setQueue] = useState("RANKED_SOLO_5x5");
  const [sort, setSort] = useState("LP_DESC");
  const [prosOnly, setProsOnly] = useState(false);
  const [page, setPage] = useState(0);
  const pageSize = 15;

  const [searchName, setSearchName] = useState("");
  const [searchTag, setSearchTag] = useState("");

  const regionsQ = useQuery({ queryKey: ["competitive", "regions"], queryFn: getRegions, staleTime: 24 * 60 * 60_000 });
  const lbQ = useQuery({
    queryKey: ["competitive", "leaderboard", region, queue, page, pageSize, sort, prosOnly],
    queryFn: () => getLeaderboard({ region, queue, page, size: pageSize, sort, prosOnly }),
    staleTime: 60_000,
  });

  const patch = useCurrentPatch();
  const { data: champs } = useDDragonChampions(patch.data?.version);
  const versionForIcons = champs?.cdnVersion;

  const idToKey = useMemo(() => {
    const m = new Map<number, string>();
    if (!champs?.champions) return m;
    for (const c of champs.champions) {
      const id = Number.parseInt(c.numericKey, 10);
      if (!Number.isNaN(id)) m.set(id, c.riotKey);
    }
    return m;
  }, [champs]);

  async function onSearch(e: React.FormEvent) {
    e.preventDefault();
    if (!searchName.trim() || !searchTag.trim()) {
      toast.error("Informe nome e tag (ex.: Player#BR1)");
      return;
    }
    try {
      const res = await searchPlayer(region, searchName.trim(), searchTag.trim());
      router.push(res.profilePath);
    } catch {
      toast.error("Jogador não encontrado ou API indisponível.");
    }
  }

  return (
    <div className="space-y-8">
      <section className="relative overflow-hidden rounded-2xl border border-border/60 bg-gradient-to-br from-zinc-950 via-background to-zinc-900 p-6 md:p-8">
        <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-primary/15 via-transparent to-transparent" />
        <div className="relative flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-primary">Intel competitivo</p>
            <h1 className="mt-2 text-3xl font-semibold tracking-tight md:text-4xl">Leaderboard regional</h1>
            <p className="mt-2 max-w-xl text-sm text-muted-foreground leading-relaxed">
              Ranking agregado a partir das ligas Challenger e Grandmaster. Dados em tempo real via Riot API, com
              enriquecimento de maestria e destaque para jogadores cadastrados como profissionais.
            </p>
          </div>
          <form onSubmit={onSearch} className="flex w-full max-w-md flex-col gap-2 sm:flex-row sm:items-end">
            <div className="grid flex-1 gap-2 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label htmlFor="gn" className="text-xs">
                  Nome
                </Label>
                <Input id="gn" placeholder="Hide on bush" value={searchName} onChange={(e) => setSearchName(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="tg" className="text-xs">
                  Tag
                </Label>
                <Input id="tg" placeholder="br1" value={searchTag} onChange={(e) => setSearchTag(e.target.value)} />
              </div>
            </div>
            <Button type="submit" className="shrink-0 gap-2">
              <Search className="size-4" />
              Buscar
            </Button>
          </form>
        </div>
      </section>

      <Card className="border-border/70 bg-card/60">
        <CardHeader className="pb-4">
          <CardTitle className="flex items-center gap-2 text-lg">
            <Trophy className="size-5 text-amber-400" />
            Filtros
          </CardTitle>
          <CardDescription>Ajuste região, fila e ordenação. Paginação abaixo da tabela.</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-wrap gap-4">
          <div className="space-y-1.5">
            <Label className="text-xs">Região</Label>
            <Select
              value={region}
              onValueChange={(v) => {
                if (v) {
                  setRegion(v);
                  setPage(0);
                }
              }}
            >
              <SelectTrigger className="w-[220px]">
                <SelectValue placeholder="Região" />
              </SelectTrigger>
              <SelectContent>
                {(regionsQ.data ?? []).map((r) => (
                  <SelectItem key={r.platformId} value={r.platformId}>
                    {r.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label className="text-xs">Fila</Label>
            <Select
              value={queue}
              onValueChange={(v) => {
                if (v) {
                  setQueue(v);
                  setPage(0);
                }
              }}
            >
              <SelectTrigger className="w-[200px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {QUEUES.map((q) => (
                  <SelectItem key={q.value} value={q.value}>
                    {q.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label className="text-xs">Ordenação</Label>
            <Select
              value={sort}
              onValueChange={(v) => {
                if (v) {
                  setSort(v);
                  setPage(0);
                }
              }}
            >
              <SelectTrigger className="w-[200px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {SORTS.map((s) => (
                  <SelectItem key={s.value} value={s.value}>
                    {s.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label className="text-xs">Jogadores</Label>
            <Select
              value={prosOnly ? "pro" : "all"}
              onValueChange={(v) => {
                if (!v) return;
                setProsOnly(v === "pro");
                setPage(0);
              }}
            >
              <SelectTrigger className="w-[200px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Todos</SelectItem>
                <SelectItem value="pro">Somente Pro</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      <LeaderboardTable
        data={lbQ.data}
        loading={lbQ.isLoading}
        error={lbQ.isError}
        versionForIcons={versionForIcons}
        idToKey={idToKey}
        page={page}
        pageSize={pageSize}
        onPageChange={setPage}
      />
    </div>
  );
}

function LeaderboardTable({
  data,
  loading,
  error,
  versionForIcons,
  idToKey,
  page,
  pageSize,
  onPageChange,
}: {
  data: LeaderboardPage | undefined;
  loading: boolean;
  error: boolean;
  versionForIcons: string | undefined;
  idToKey: Map<number, string>;
  page: number;
  pageSize: number;
  onPageChange: (p: number) => void;
}) {
  if (error) {
    return (
      <Card className="border-destructive/40 bg-destructive/5">
        <CardContent className="py-10 text-center text-sm text-muted-foreground">
          Não foi possível carregar o leaderboard. Verifique RIOT_API_KEY no backend e tente novamente.
        </CardContent>
      </Card>
    );
  }

  if (loading) {
    return (
      <div className="space-y-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <Skeleton key={i} className="h-20 w-full rounded-xl" />
        ))}
      </div>
    );
  }

  const rows = data?.entries ?? [];
  const totalPages = data ? Math.max(1, Math.ceil(data.total / pageSize)) : 1;

  return (
    <div className="space-y-4">
      <ScrollArea className="w-full">
        <div className="min-w-[920px] space-y-2">
          <div className="grid grid-cols-[48px_1.2fr_100px_120px_80px_140px_160px_120px] gap-3 rounded-lg border border-border/50 bg-muted/20 px-4 py-2 text-xs font-medium uppercase tracking-wide text-muted-foreground">
            <span>#</span>
            <span>Jogador</span>
            <span>Elo</span>
            <span>LP</span>
            <span>WR</span>
            <span>Pool</span>
            <span>Região</span>
            <span />
          </div>
          {rows.map((r) => (
            <div
              key={r.puuid}
              className={cn(
                "grid grid-cols-[48px_1.2fr_100px_120px_80px_140px_160px_120px] gap-3 rounded-xl border border-border/40 bg-card/50 px-4 py-3 transition-colors hover:border-primary/30 hover:bg-card/80"
              )}
            >
              <div className="flex items-center text-sm font-medium text-muted-foreground">{r.position}</div>
              <div className="flex items-center gap-3 min-w-0">
                <div className="relative h-11 w-11 shrink-0 overflow-hidden rounded-full border border-border/60 bg-muted">
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img src={r.profileIconUrl} alt="" className="h-full w-full object-cover" />
                </div>
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="truncate font-medium">{r.riotId || r.summonerName}</span>
                    {r.professional ? (
                      <Badge className="bg-amber-500/15 text-amber-200 hover:bg-amber-500/20">
                        <Sparkles className="mr-1 size-3" />
                        Pro
                      </Badge>
                    ) : null}
                  </div>
                  {r.teamName ? <p className="truncate text-xs text-muted-foreground">{r.teamName}</p> : null}
                </div>
              </div>
              <div className="flex items-center gap-2">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img src={r.rankedEmblemUrl} alt="" className="h-9 w-9 object-contain opacity-90" />
                <div className="text-xs leading-tight">
                  <div className="font-medium">{r.tier}</div>
                  <div className="text-muted-foreground">{r.rankDivision}</div>
                </div>
              </div>
              <div className="flex items-center font-semibold tabular-nums">{r.leaguePoints}</div>
              <div className="flex items-center text-sm tabular-nums text-muted-foreground">
                {r.winrate.toFixed(1)}%
              </div>
              <div className="flex items-center gap-1">
                {r.topChampions.slice(0, 3).map((c) => {
                  const url = champIcon(versionForIcons, c.championId, idToKey);
                  return url ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img key={c.championId} src={url} alt="" className="h-8 w-8 rounded-md border border-border/50" />
                  ) : (
                    <div key={c.championId} className="h-8 w-8 rounded-md bg-muted" />
                  );
                })}
              </div>
              <div className="flex items-center text-xs text-muted-foreground">{r.platformId}</div>
              <div className="flex items-center justify-end">
                <Link href={r.profilePath} className={cn(buttonVariants({ size: "sm", variant: "secondary" }))}>
                  Perfil
                </Link>
              </div>
            </div>
          ))}
          {rows.length === 0 ? (
            <p className="py-10 text-center text-sm text-muted-foreground">Nenhum jogador nesta página.</p>
          ) : null}
        </div>
        <ScrollBar orientation="horizontal" />
      </ScrollArea>

      <div className="flex items-center justify-between gap-4">
        <p className="text-xs text-muted-foreground">
          Total aproximado: {data?.total ?? 0} · Página {page + 1} / {totalPages}
        </p>
        <div className="flex gap-2">
          <Button variant="outline" size="sm" disabled={page <= 0} onClick={() => onPageChange(page - 1)}>
            Anterior
          </Button>
          <Button variant="outline" size="sm" disabled={page + 1 >= totalPages} onClick={() => onPageChange(page + 1)}>
            Próxima
          </Button>
        </div>
      </div>
    </div>
  );
}
