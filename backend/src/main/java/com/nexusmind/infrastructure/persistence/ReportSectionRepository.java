package com.nexusmind.infrastructure.persistence;

import com.nexusmind.domain.model.ReportKind;
import com.nexusmind.domain.model.ReportSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface ReportSectionRepository extends JpaRepository<ReportSection, Long> {

    List<ReportSection> findByReportKindAndReportIdOrderBySortOrderAsc(ReportKind kind, UUID reportId);

    @Modifying
    @Transactional
    void deleteByReportKindAndReportId(ReportKind kind, UUID reportId);
}
