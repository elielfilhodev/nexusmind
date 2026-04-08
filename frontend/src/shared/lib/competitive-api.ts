import { apiGet, apiPost } from "@/shared/lib/api-client";

export type RegionOption = { platformId: string; label: string };

export type ChampionMasterySnippet = {
  championId: number;
  championPoints: number;
  championLevel: number;
};

export type LeaderboardRow = {
  position: number;
  platformId: string;
  puuid: string;
  riotId: string;
  summonerName: string;
  tier: string;
  rankDivision: string;
  leaguePoints: number;
  wins: number;
  losses: number;
  winrate: number;
  profileIconUrl: string;
  rankedEmblemUrl: string;
  professional: boolean;
  teamName: string;
  topChampions: ChampionMasterySnippet[];
  profilePath: string;
};

export type LeaderboardPage = {
  region: string;
  queue: string;
  page: number;
  size: number;
  total: number;
  sort: string;
  entries: LeaderboardRow[];
};

export type PlayerSearchResult = {
  platformId: string;
  puuid: string;
  riotId: string;
  summonerLevel: string;
  profileIconUrl: string;
  professional: boolean;
  profilePath: string;
};

export type RankInfo = {
  queueType: string;
  tier: string;
  rank: string;
  leaguePoints: number;
  wins: number;
  losses: number;
  active: boolean;
};

export type AggregatedStats = {
  totalGamesSample: number;
  winrate: number;
  avgKda: number;
  avgCsPerMin: number;
  avgVisionScore: number;
  killParticipation: number;
};

export type SeasonSummary = {
  seasonId: string;
  games: number;
  wins: number;
  winrate: number;
  avgKda: number;
};

export type PeakRank = {
  seasonId: string;
  queueType: string;
  tier: string;
  rank: string;
  leaguePoints: number;
  note: string;
};

export type ChampionPlay = {
  championId: number;
  games: number;
  wins: number;
  winrate: number;
  avgKda: number;
  avgCsPerMin: number;
};

export type PlayerProfile = {
  platformId: string;
  puuid: string;
  riotId: string;
  summonerName: string;
  summonerLevel: number;
  profileIconUrl: string;
  soloRank: RankInfo | null;
  flexRank: RankInfo | null;
  rankedEmblemUrl: string;
  professional: boolean;
  teamName: string;
  primaryRole: string;
  competitiveRegion: string;
  scoutingTags: string;
  bannerSplashUrl: string;
  bannerChampionId: number;
  topMasteries: ChampionMasterySnippet[];
  aggregated: AggregatedStats;
  seasonSummaries: SeasonSummary[];
  peakRanks: PeakRank[];
  championPool: ChampionPlay[];
  provenance: { ddragonVersion: string; disclaimer: string };
};

export type MatchSummary = {
  matchId: string;
  gameCreation: number;
  durationSec: number;
  queueId: number;
  queueLabel: string;
  championId: number;
  lane: string;
  role: string;
  win: boolean;
  kills: number;
  deaths: number;
  assists: number;
  kda: number;
  visionScore: number;
  cs: number;
  csPerMin: number;
  killParticipation: number;
  items: number[];
  summonerSpellIds: number[];
  perkPrimaryStyle: number;
  perkSubStyle: number;
  perkSelections: number[];
  mvpHeuristic: boolean;
  seasonId: string;
};

export type MatchList = {
  platformId: string;
  puuid: string;
  page: number;
  size: number;
  total: number;
  matches: MatchSummary[];
};

export type MatchParticipantDetail = {
  puuid: string;
  riotId: string;
  championId: number;
  lane: string;
  role: string;
  win: boolean;
  teamId: number;
  kills: number;
  deaths: number;
  assists: number;
  visionScore: number;
  cs: number;
  csPerMin: number;
  killParticipation: number;
  items: number[];
  summonerSpellIds: number[];
  perkPrimaryStyle: number;
  perkSubStyle: number;
  perkSelections: number[];
};

export type MatchDetail = {
  matchId: string;
  gameCreation: number;
  durationSec: number;
  queueId: number;
  queueLabel: string;
  gameVersion: string;
  subject: MatchParticipantDetail;
  sameTeam: MatchParticipantDetail[];
  enemyTeam: MatchParticipantDetail[];
};

export type PerformanceRating = {
  score: number;
  label: string;
};

/** Resposta alinhada ao prompt de analista/coach (análise de partida). */
export type MatchAiAnalysis = {
  matchId: string;
  puuid: string;
  summary: string;
  performanceRating: PerformanceRating;
  strengths: string[];
  mistakes: string[];
  playstyleRead: string[];
  lanePhaseAssessment: string;
  midGameAssessment: string;
  lateGameAssessment: string;
  buildAssessment: string;
  runeAssessment: string;
  spellAssessment: string;
  macroAssessment: string;
  consistencyNotes: string[];
  improvementActions: string[];
  coachNotes: string[];
  model: string;
};

export type ProfileAiAnalysis = {
  puuid: string;
  overview: string;
  traits: string[];
  risks: string[];
  recommendations: string[];
  competitiveRead: string;
  model: string;
};

export function getRegions() {
  return apiGet<RegionOption[]>("/api/leaderboard/regions");
}

export function getLeaderboard(params: {
  region: string;
  queue: string;
  page: number;
  size: number;
  sort: string;
  prosOnly: boolean;
}) {
  const q = new URLSearchParams({
    region: params.region,
    queue: params.queue,
    page: String(params.page),
    size: String(params.size),
    sort: params.sort,
    prosOnly: String(params.prosOnly),
  });
  return apiGet<LeaderboardPage>(`/api/leaderboard?${q.toString()}`);
}

export function searchPlayer(region: string, gameName: string, tagLine: string) {
  const q = new URLSearchParams({ region, gameName, tagLine });
  return apiGet<PlayerSearchResult>(`/api/players/search?${q.toString()}`);
}

export function getPlayerProfile(region: string, puuid: string) {
  return apiGet<PlayerProfile>(`/api/players/${encodeURIComponent(region)}/${encodeURIComponent(puuid)}`);
}

export function getPlayerSeasons(region: string, puuid: string) {
  return apiGet<string[]>(`/api/players/${encodeURIComponent(region)}/${encodeURIComponent(puuid)}/seasons`);
}

export function getMatches(
  region: string,
  puuid: string,
  params: {
    page: number;
    size: number;
    season?: string;
    championId?: number;
    queueId?: number;
    outcome?: string;
  }
) {
  const q = new URLSearchParams({
    page: String(params.page),
    size: String(params.size),
  });
  if (params.season) q.set("season", params.season);
  if (params.championId != null) q.set("championId", String(params.championId));
  if (params.queueId != null) q.set("queueId", String(params.queueId));
  if (params.outcome) q.set("outcome", params.outcome);
  return apiGet<MatchList>(
    `/api/players/${encodeURIComponent(region)}/${encodeURIComponent(puuid)}/matches?${q.toString()}`
  );
}

export function getMatchDetail(region: string, matchId: string, puuid: string) {
  const q = new URLSearchParams({ region, puuid });
  return apiGet<MatchDetail>(`/api/matches/${encodeURIComponent(matchId)}?${q.toString()}`);
}

export function postMatchAiAnalysis(region: string, matchId: string, puuid: string) {
  const q = new URLSearchParams({ region, puuid });
  return apiPost<MatchAiAnalysis>(`/api/matches/${encodeURIComponent(matchId)}/ai-analysis?${q.toString()}`, {});
}

export function postProfileAiAnalysis(region: string, puuid: string) {
  return apiPost<ProfileAiAnalysis>(
    `/api/players/${encodeURIComponent(region)}/${encodeURIComponent(puuid)}/ai-analysis`,
    {}
  );
}
