package com.nexusmind.infrastructure.persistence;

import com.nexusmind.domain.model.PatchVersion;
import com.nexusmind.domain.model.SummonerSpellEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SummonerSpellRepository extends JpaRepository<SummonerSpellEntity, Long> {

    List<SummonerSpellEntity> findByPatchOrderByNameAsc(PatchVersion patch);
}
