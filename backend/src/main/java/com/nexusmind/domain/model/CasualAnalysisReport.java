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
@Table(name = "casual_analysis_reports")
public class CasualAnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false, length = 32)
    private String elo;

    @Column(nullable = false, length = 16)
    private String lane;

    @Column(nullable = false, length = 32)
    private String playstyle;

    @Column(length = 32)
    private String region;

    @Column(name = "favorite_champion_key", length = 64)
    private String favoriteChampionKey;

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

    public String getElo() {
        return elo;
    }

    public void setElo(String elo) {
        this.elo = elo;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public String getPlaystyle() {
        return playstyle;
    }

    public void setPlaystyle(String playstyle) {
        this.playstyle = playstyle;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getFavoriteChampionKey() {
        return favoriteChampionKey;
    }

    public void setFavoriteChampionKey(String favoriteChampionKey) {
        this.favoriteChampionKey = favoriteChampionKey;
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
