package com.nexusmind.application.service;

import com.nexusmind.application.dto.ReportSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReportListService {

    private static final String UNION =
            """
                    SELECT * FROM (
                        SELECT 'CASUAL'::varchar AS kind, id, created_at,
                               LEFT(COALESCE(summary_text, ''), 500) AS summary
                        FROM casual_analysis_reports
                        UNION ALL
                        SELECT 'DRAFT'::varchar, id, created_at,
                               LEFT(COALESCE(summary_text, ''), 500)
                        FROM draft_analysis_reports
                    ) u
                    """;

    private static final String COUNT_TOTAL =
            """
                    SELECT (SELECT COUNT(*) FROM casual_analysis_reports)
                         + (SELECT COUNT(*) FROM draft_analysis_reports)
                    """;

    private final JdbcTemplate jdbcTemplate;

    public ReportListService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<ReportSummaryDto> list(Pageable pageable) {
        Long total = jdbcTemplate.queryForObject(COUNT_TOTAL, Long.class);
        if (total == null) {
            total = 0L;
        }
        String sql = UNION + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<ReportSummaryDto> content = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ReportSummaryDto(
                        rs.getString("kind"),
                        rs.getObject("id", UUID.class),
                        toInstant(rs.getTimestamp("created_at")),
                        rs.getString("summary")
                ),
                pageable.getPageSize(),
                pageable.getOffset()
        );
        return new PageImpl<>(content, pageable, total);
    }

    private static Instant toInstant(Timestamp ts) {
        return ts != null ? ts.toInstant() : Instant.EPOCH;
    }
}
