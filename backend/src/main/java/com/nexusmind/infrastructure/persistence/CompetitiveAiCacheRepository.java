package com.nexusmind.infrastructure.persistence;

import com.nexusmind.domain.model.CompetitiveAiCacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompetitiveAiCacheRepository extends JpaRepository<CompetitiveAiCacheEntity, UUID> {

    Optional<CompetitiveAiCacheEntity> findByCacheKeyAndKind(String cacheKey, String kind);
}
