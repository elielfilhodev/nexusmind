package com.nexusmind.infrastructure.persistence;

import com.nexusmind.domain.model.Champion;
import com.nexusmind.domain.model.PatchVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChampionRepository extends JpaRepository<Champion, Long> {

    List<Champion> findByPatchOrderByNameAsc(PatchVersion patch);
}
