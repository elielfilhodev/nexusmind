package com.nexusmind.web.api;

import com.nexusmind.application.dto.ChampionDto;
import com.nexusmind.application.dto.GameItemDto;
import com.nexusmind.application.dto.RuneTreeDto;
import com.nexusmind.application.dto.SummonerSpellDto;
import com.nexusmind.application.service.GameCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GameDataController {

    private final GameCatalogService gameCatalogService;

    public GameDataController(GameCatalogService gameCatalogService) {
        this.gameCatalogService = gameCatalogService;
    }

    @GetMapping("/champions")
    public List<ChampionDto> champions() {
        return gameCatalogService.listChampionsForCurrentPatch();
    }

    @GetMapping("/items")
    public List<GameItemDto> items() {
        return gameCatalogService.listItemsForCurrentPatch();
    }

    @GetMapping("/runes")
    public List<RuneTreeDto> runes() {
        return gameCatalogService.listRuneTreesForCurrentPatch();
    }

    @GetMapping("/summoner-spells")
    public List<SummonerSpellDto> summonerSpells() {
        return gameCatalogService.listSummonerSpellsForCurrentPatch();
    }
}
