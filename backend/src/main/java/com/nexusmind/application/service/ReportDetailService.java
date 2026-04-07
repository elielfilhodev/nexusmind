package com.nexusmind.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexusmind.domain.model.CasualAnalysisReport;
import com.nexusmind.domain.model.DraftAnalysisReport;
import com.nexusmind.domain.model.ReportKind;
import com.nexusmind.domain.model.ReportSection;
import com.nexusmind.infrastructure.persistence.CasualAnalysisReportRepository;
import com.nexusmind.infrastructure.persistence.DraftAnalysisReportRepository;
import com.nexusmind.infrastructure.persistence.ReportSectionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ReportDetailService {

    private final CasualAnalysisReportRepository casualAnalysisReportRepository;
    private final DraftAnalysisReportRepository draftAnalysisReportRepository;
    private final ReportSectionRepository reportSectionRepository;

    public ReportDetailService(
            CasualAnalysisReportRepository casualAnalysisReportRepository,
            DraftAnalysisReportRepository draftAnalysisReportRepository,
            ReportSectionRepository reportSectionRepository
    ) {
        this.casualAnalysisReportRepository = casualAnalysisReportRepository;
        this.draftAnalysisReportRepository = draftAnalysisReportRepository;
        this.reportSectionRepository = reportSectionRepository;
    }

    public CasualAnalysisReport getCasual(UUID id) {
        return casualAnalysisReportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public DraftAnalysisReport getDraft(UUID id) {
        return draftAnalysisReportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<ReportSection> sections(ReportKind kind, UUID id) {
        return reportSectionRepository.findByReportKindAndReportIdOrderBySortOrderAsc(kind, id);
    }

    public JsonNode structuredPayload(String kind, UUID id) {
        if ("CASUAL".equalsIgnoreCase(kind)) {
            return getCasual(id).getStructuredPayload();
        }
        if ("DRAFT".equalsIgnoreCase(kind)) {
            return getDraft(id).getStructuredPayload();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kind inválido");
    }
}
