package com.nexusmind.infrastructure.persistence;

import com.nexusmind.domain.model.PatchVersion;
import com.nexusmind.domain.model.RuneTree;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuneTreeRepository extends JpaRepository<RuneTree, Long> {

    List<RuneTree> findByPatchOrderByNameAsc(PatchVersion patch);
}
