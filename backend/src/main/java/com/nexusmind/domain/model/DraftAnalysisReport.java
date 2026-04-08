package com.nexusmind.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "draft_analysis_reports")
public class DraftAnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(length = 8)
    private String side;

    @Column(name = "context_type", length = 32)
    private String contextType;

    @Column(name = "strategic_focus", length = 32)
    private String strategicFocus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "draft_payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode draftPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structured_payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode structuredPayload;

    @Column(name = "summary_text", columnDefinition = "TEXT")
    private String summaryText;

    @Column(name = "client_session_id", nullable = false, length = 64)
    private String clientSessionId = "";

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getContextType() {
        return contextType;
    }

    public void setContextType(String contextType) {
        this.contextType = contextType;
    }

    public String getStrategicFocus() {
        return strategicFocus;
    }

    public void setStrategicFocus(String strategicFocus) {
        this.strategicFocus = strategicFocus;
    }

    public JsonNode getDraftPayload() {
        return draftPayload;
    }

    public void setDraftPayload(JsonNode draftPayload) {
        this.draftPayload = draftPayload;
    }

    public JsonNode getStructuredPayload() {
        return structuredPayload;
    }

    public void setStructuredPayload(JsonNode structuredPayload) {
        this.structuredPayload = structuredPayload;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public String getClientSessionId() {
        return clientSessionId;
    }

    public void setClientSessionId(String clientSessionId) {
        this.clientSessionId = clientSessionId != null ? clientSessionId : "";
    }
}
