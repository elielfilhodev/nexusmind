package com.nexusmind.infrastructure.persistence;

import com.nexusmind.domain.model.GameItem;
import com.nexusmind.domain.model.PatchVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameItemRepository extends JpaRepository<GameItem, Long> {

    List<GameItem> findByPatchOrderByNameAsc(PatchVersion patch);
}
