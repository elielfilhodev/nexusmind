package com.nexusmind.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexusmind.domain.model.ReportKind;
import com.nexusmind.domain.model.ReportSection;
import com.nexusmind.infrastructure.persistence.ReportSectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportSectionSyncService {

    private final ReportSectionRepository reportSectionRepository;

    public ReportSectionSyncService(ReportSectionRepository reportSectionRepository) {
        this.reportSectionRepository = reportSectionRepository;
    }

    @Transactional
    public void replaceSections(ReportKind kind, UUID reportId, JsonNode structured) {
        reportSectionRepository.deleteByReportKindAndReportId(kind, reportId);
        if (structured == null || !structured.isObject()) {
            return;
        }
        List<Map.Entry<String, JsonNode>> ordered = new ArrayList<>();
        Iterator<String> it = structured.fieldNames();
        int i = 0;
        while (it.hasNext()) {
            String key = it.next();
            ordered.add(Map.entry(key, structured.get(key)));
            i++;
        }
        int order = 0;
        for (Map.Entry<String, JsonNode> e : ordered) {
            ReportSection s = new ReportSection();
            s.setReportKind(kind);
            s.setReportId(reportId);
            s.setSectionKey(sanitizeKey(e.getKey()));
            s.setTitle(humanTitle(e.getKey()));
            s.setBody(e.getValue());
            s.setSortOrder(order++);
            reportSectionRepository.save(s);
        }
    }

    private static String sanitizeKey(String key) {
        String s = key.replaceAll("[^a-zA-Z0-9_-]", "_");
        if (s.length() > 64) {
            s = s.substring(0, 64);
        }
        return s.isEmpty() ? "section" : s;
    }

    private static String humanTitle(String key) {
        return key.replaceAll("([A-Z])", " $1").trim();
    }
}
