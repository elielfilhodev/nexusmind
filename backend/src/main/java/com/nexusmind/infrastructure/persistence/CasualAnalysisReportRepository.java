package com.nexusmind.infrastructure.persistence;

import com.nexusmind.domain.model.CasualAnalysisReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CasualAnalysisReportRepository extends JpaRepository<CasualAnalysisReport, UUID> {

    Page<CasualAnalysisReport> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
