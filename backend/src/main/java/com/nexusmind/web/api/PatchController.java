package com.nexusmind.web.api;

import com.nexusmind.application.dto.PatchVersionDto;
import com.nexusmind.application.service.GameCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patch")
public class PatchController {

    private final GameCatalogService gameCatalogService;

    public PatchController(GameCatalogService gameCatalogService) {
        this.gameCatalogService = gameCatalogService;
    }

    @GetMapping("/current")
    public PatchVersionDto current() {
        return gameCatalogService.getCurrentPatch();
    }
}
