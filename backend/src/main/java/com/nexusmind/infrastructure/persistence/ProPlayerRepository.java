package com.nexusmind.infrastructure.persistence;

import com.nexusmind.domain.model.ProPlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProPlayerRepository extends JpaRepository<ProPlayerEntity, UUID> {

    Optional<ProPlayerEntity> findByPuuidAndPlatformIdAndActiveIsTrue(String puuid, String platformId);
}
