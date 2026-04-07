package com.nexusmind.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexusmind.infrastructure.pdf.OpenPdfReportRenderer;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReportPdfService {

    private final ReportDetailService reportDetailService;
    private final OpenPdfReportRenderer pdfRenderer;

    public ReportPdfService(ReportDetailService reportDetailService, OpenPdfReportRenderer pdfRenderer) {
        this.reportDetailService = reportDetailService;
        this.pdfRenderer = pdfRenderer;
    }

    public byte[] buildPdf(String kind, UUID id) {
        JsonNode payload = reportDetailService.structuredPayload(kind, id);
        String title = "NexusMind — " + kind + " — " + id;
        return pdfRenderer.render(title, payload);
    }
}
