package com.nexusmind.application.service;

import com.nexusmind.application.dto.ChampionDto;
import com.nexusmind.application.dto.GameItemDto;
import com.nexusmind.application.dto.PatchVersionDto;
import com.nexusmind.application.dto.RuneTreeDto;
import com.nexusmind.application.dto.SummonerSpellDto;
import com.nexusmind.domain.model.PatchVersion;
import com.nexusmind.infrastructure.persistence.ChampionRepository;
import com.nexusmind.infrastructure.persistence.GameItemRepository;
import com.nexusmind.infrastructure.persistence.PatchVersionRepository;
import com.nexusmind.infrastructure.persistence.RuneTreeRepository;
import com.nexusmind.infrastructure.persistence.SummonerSpellRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GameCatalogService {

    private final PatchVersionRepository patchVersionRepository;
    private final ChampionRepository championRepository;
    private final GameItemRepository gameItemRepository;
    private final RuneTreeRepository runeTreeRepository;
    private final SummonerSpellRepository summonerSpellRepository;

    public GameCatalogService(
            PatchVersionRepository patchVersionRepository,
            ChampionRepository championRepository,
            GameItemRepository gameItemRepository,
            RuneTreeRepository runeTreeRepository,
            SummonerSpellRepository summonerSpellRepository
    ) {
        this.patchVersionRepository = patchVersionRepository;
        this.championRepository = championRepository;
        this.gameItemRepository = gameItemRepository;
        this.runeTreeRepository = runeTreeRepository;
        this.summonerSpellRepository = summonerSpellRepository;
    }

    public PatchVersionDto getCurrentPatch() {
        PatchVersion p = patchVersionRepository.findByCurrentTrue()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Patch atual não configurado"));
        return new PatchVersionDto(p.getVersion(), p.isCurrent(), p.getReleasedAt());
    }

    public List<ChampionDto> listChampionsForCurrentPatch() {
        PatchVersion p = requireCurrentPatch();
        return championRepository.findByPatchOrderByNameAsc(p).stream()
                .map(c -> new ChampionDto(c.getRiotKey(), c.getName(), c.getTitle(), c.getLanesJson(), c.getTagsJson()))
                .toList();
    }

    public List<GameItemDto> listItemsForCurrentPatch() {
        PatchVersion p = requireCurrentPatch();
        return gameItemRepository.findByPatchOrderByNameAsc(p).stream()
                .map(i -> new GameItemDto(i.getRiotKey(), i.getName(), i.getDescription(), i.getGoldCost(), i.getStatsJson()))
                .toList();
    }

    public List<RuneTreeDto> listRuneTreesForCurrentPatch() {
        PatchVersion p = requireCurrentPatch();
        return runeTreeRepository.findByPatchOrderByNameAsc(p).stream()
                .map(r -> new RuneTreeDto(r.getTreeKey(), r.getName(), r.getSlotsJson()))
                .toList();
    }

    public List<SummonerSpellDto> listSummonerSpellsForCurrentPatch() {
        PatchVersion p = requireCurrentPatch();
        return summonerSpellRepository.findByPatchOrderByNameAsc(p).stream()
                .map(s -> new SummonerSpellDto(s.getRiotKey(), s.getName(), s.getCooldownSec(), s.getModesJson()))
                .toList();
    }

    public String buildChampionContextSnippet() {
        return listChampionsForCurrentPatch().stream()
                .map(c -> c.riotKey() + ":" + c.name())
                .reduce((a, b) -> a + ", " + b)
                .orElse("(sem campeões seed — ingestão Data Dragon pendente)");
    }

    private PatchVersion requireCurrentPatch() {
        return patchVersionRepository.findByCurrentTrue()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Patch atual não configurado"));
    }
}
