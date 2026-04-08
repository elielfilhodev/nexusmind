package com.nexusmind.web.api;

import com.nexusmind.application.dto.competitive.AiAnalysisDtos;
import com.nexusmind.application.dto.competitive.LeaderboardDtos;
import com.nexusmind.application.dto.competitive.MatchDtos;
import com.nexusmind.application.dto.competitive.PlayerProfileDtos;
import com.nexusmind.application.service.CompetitiveAiAnalysisService;
import com.nexusmind.application.service.RiotLeaderboardService;
import com.nexusmind.application.service.RiotMatchAggregationService;
import com.nexusmind.application.service.RiotPlayerProfileService;
import com.nexusmind.application.service.RiotPlayerSearchService;
import com.nexusmind.infrastructure.riot.RiotPlatformId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "Competitive", description = "Leaderboard e perfil via Riot API")
public class CompetitiveController {

    private final RiotLeaderboardService leaderboardService;
    private final RiotPlayerSearchService playerSearchService;
    private final RiotPlayerProfileService playerProfileService;
    private final RiotMatchAggregationService matchAggregationService;
    private final CompetitiveAiAnalysisService competitiveAiAnalysisService;

    public CompetitiveController(
            RiotLeaderboardService leaderboardService,
            RiotPlayerSearchService playerSearchService,
            RiotPlayerProfileService playerProfileService,
            RiotMatchAggregationService matchAggregationService,
            CompetitiveAiAnalysisService competitiveAiAnalysisService
    ) {
        this.leaderboardService = leaderboardService;
        this.playerSearchService = playerSearchService;
        this.playerProfileService = playerProfileService;
        this.matchAggregationService = matchAggregationService;
        this.competitiveAiAnalysisService = competitiveAiAnalysisService;
    }

    @GetMapping("/leaderboard/regions")
    @Operation(summary = "Regiões/plataformas suportadas")
    public List<LeaderboardDtos.RegionOptionDto> regions() {
        return leaderboardService.regions();
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Leaderboard (Challenger + Grandmaster) por fila")
    public LeaderboardDtos.LeaderboardPageDto leaderboard(
            @RequestParam(name = "region") @NotBlank @Size(max = 8) String region,
            @RequestParam(name = "queue", defaultValue = "RANKED_SOLO_5x5") @Size(max = 32) String queue,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(name = "sort", defaultValue = "LP_DESC") @Size(max = 24) String sort,
            @RequestParam(name = "prosOnly", defaultValue = "false") boolean prosOnly
    ) {
        RiotPlatformId platform = requirePlatform(region);
        return leaderboardService.leaderboard(platform, queue, page, size, sort, prosOnly);
    }

    @GetMapping("/players/search")
    @Operation(summary = "Busca por Riot ID (gameName + tagLine)")
    public LeaderboardDtos.PlayerSearchResultDto searchPlayers(
            @RequestParam @NotBlank @Size(max = 32) String gameName,
            @RequestParam @NotBlank @Size(max = 8) String tagLine,
            @RequestParam(name = "region") @NotBlank @Size(max = 8) String region
    ) {
        RiotPlatformId platform = requirePlatform(region);
        return playerSearchService.search(platform, gameName, tagLine);
    }

    @GetMapping("/players/{region}/{puuid}")
    @Operation(summary = "Perfil agregado do jogador")
    public PlayerProfileDtos.PlayerProfileDto playerProfile(
            @PathVariable @Size(max = 8) String region,
            @PathVariable @Size(max = 128) String puuid
    ) {
        RiotPlatformId platform = requirePlatform(region);
        return playerProfileService.profile(platform, puuid);
    }

    @GetMapping("/players/{region}/{puuid}/seasons")
    @Operation(summary = "Temporadas detectadas no histórico recente")
    public List<String> seasons(
            @PathVariable @Size(max = 8) String region,
            @PathVariable @Size(max = 128) String puuid
    ) {
        RiotPlatformId platform = requirePlatform(region);
        return playerProfileService.seasonsForPlayer(platform, puuid);
    }

    @GetMapping("/players/{region}/{puuid}/matches")
    @Operation(summary = "Histórico de partidas com filtros")
    public MatchDtos.MatchListDto matches(
            @PathVariable @Size(max = 8) String region,
            @PathVariable @Size(max = 128) String puuid,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "15") @Min(1) @Max(50) int size,
            @RequestParam(name = "season", required = false) @Size(max = 16) String season,
            @RequestParam(name = "championId", required = false) Integer championId,
            @RequestParam(name = "queueId", required = false) Integer queueId,
            @RequestParam(name = "outcome", required = false) @Size(max = 8) String outcome
    ) {
        RiotPlatformId platform = requirePlatform(region);
        return matchAggregationService.listMatches(platform, puuid, page, size, season, championId, queueId, outcome);
    }

    @GetMapping("/players/{region}/{puuid}/champions")
    @Operation(summary = "Pool de campeões (amostra recente)")
    public List<PlayerProfileDtos.ChampionPlayDto> champions(
            @PathVariable @Size(max = 8) String region,
            @PathVariable @Size(max = 128) String puuid
    ) {
        RiotPlatformId platform = requirePlatform(region);
        return playerProfileService.profile(platform, puuid).championPool();
    }

    @GetMapping("/players/{region}/{puuid}/peak-ranks")
    @Operation(summary = "Picos / snapshots de liga (limitado ao que a Riot API expõe)")
    public List<PlayerProfileDtos.PeakRankDto> peakRanks(
            @PathVariable @Size(max = 8) String region,
            @PathVariable @Size(max = 128) String puuid
    ) {
        RiotPlatformId platform = requirePlatform(region);
        return playerProfileService.profile(platform, puuid).peakRanks();
    }

    @GetMapping("/matches/{matchId}")
    @Operation(summary = "Detalhe da partida para o jogador informado")
    public MatchDtos.MatchDetailDto matchDetail(
            @PathVariable @Size(max = 32) String matchId,
            @RequestParam(name = "region") @NotBlank @Size(max = 8) String region,
            @RequestParam(name = "puuid") @NotBlank @Size(max = 128) String puuid
    ) {
        RiotPlatformId platform = requirePlatform(region);
        return matchAggregationService.matchDetail(platform, matchId, puuid);
    }

    @PostMapping("/matches/{matchId}/ai-analysis")
    @Operation(summary = "Gera análise IA da partida para o jogador")
    public AiAnalysisDtos.MatchAiAnalysisDto matchAi(
            @PathVariable @Size(max = 32) String matchId,
            @RequestParam(name = "region") @NotBlank @Size(max = 8) String region,
            @RequestParam(name = "puuid") @NotBlank @Size(max = 128) String puuid
    ) {
        RiotPlatformId platform = requirePlatform(region);
        try {
            return competitiveAiAnalysisService.analyzeMatch(platform, matchId, puuid);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PostMapping("/players/{region}/{puuid}/ai-analysis")
    @Operation(summary = "Gera análise IA do perfil")
    public AiAnalysisDtos.ProfileAiAnalysisDto profileAi(
            @PathVariable @Size(max = 8) String region,
            @PathVariable @Size(max = 128) String puuid
    ) {
        RiotPlatformId platform = requirePlatform(region);
        return competitiveAiAnalysisService.analyzeProfile(platform, puuid);
    }

    private static RiotPlatformId requirePlatform(String region) {
        return RiotPlatformId.fromString(region)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Região inválida"));
    }
}
