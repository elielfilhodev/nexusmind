package com.nexusmind.infrastructure.persistence;

import com.nexusmind.domain.model.DraftAnalysisReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DraftAnalysisReportRepository extends JpaRepository<DraftAnalysisReport, UUID> {

    Page<DraftAnalysisReport> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
