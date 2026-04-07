package com.nexusmind.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(
        name = "items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"patch_id", "riot_key"})
)
public class GameItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patch_id", nullable = false)
    private PatchVersion patch;

    @Column(name = "riot_key", nullable = false, length = 32)
    private String riotKey;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "gold_cost")
    private Integer goldCost;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stats_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode statsJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public PatchVersion getPatch() {
        return patch;
    }

    public void setPatch(PatchVersion patch) {
        this.patch = patch;
    }

    public String getRiotKey() {
        return riotKey;
    }

    public void setRiotKey(String riotKey) {
        this.riotKey = riotKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getGoldCost() {
        return goldCost;
    }

    public void setGoldCost(Integer goldCost) {
        this.goldCost = goldCost;
    }

    public JsonNode getStatsJson() {
        return statsJson;
    }

    public void setStatsJson(JsonNode statsJson) {
        this.statsJson = statsJson;
    }
}
