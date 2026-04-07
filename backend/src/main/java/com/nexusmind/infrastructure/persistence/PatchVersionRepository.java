package com.nexusmind.infrastructure.persistence;

import com.nexusmind.domain.model.PatchVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatchVersionRepository extends JpaRepository<PatchVersion, Long> {

    Optional<PatchVersion> findByCurrentTrue();
}
