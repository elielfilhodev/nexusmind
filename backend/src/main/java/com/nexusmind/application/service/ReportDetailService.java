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

    public CasualAnalysisReport getCasual(UUID id, String clientSessionId) {
        CasualAnalysisReport r = casualAnalysisReportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertSameSession(r.getClientSessionId(), clientSessionId);
        return r;
    }

    public DraftAnalysisReport getDraft(UUID id, String clientSessionId) {
        DraftAnalysisReport r = draftAnalysisReportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertSameSession(r.getClientSessionId(), clientSessionId);
        return r;
    }

    public List<ReportSection> sections(ReportKind kind, UUID id, String clientSessionId) {
        if (kind == ReportKind.CASUAL) {
            getCasual(id, clientSessionId);
        } else {
            getDraft(id, clientSessionId);
        }
        return reportSectionRepository.findByReportKindAndReportIdOrderBySortOrderAsc(kind, id);
    }

    public JsonNode structuredPayload(String kind, UUID id, String clientSessionId) {
        if ("CASUAL".equalsIgnoreCase(kind)) {
            return getCasual(id, clientSessionId).getStructuredPayload();
        }
        if ("DRAFT".equalsIgnoreCase(kind)) {
            return getDraft(id, clientSessionId).getStructuredPayload();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kind inválido");
    }

    private static void assertSameSession(String stored, String clientSessionId) {
        if (clientSessionId == null || !clientSessionId.equals(stored)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
