package com.nexusmind.infrastructure.datadragon;

import org.springframework.stereotype.Component;

@Component
public class NoOpDataDragonIngestion implements DataDragonIngestionPort {

    @Override
    public void syncPatch(String riotPatchVersion) {
        // Intencionalmente vazio no MVP — substituir por cliente HTTP + parsing JSON oficial.
    }
}
