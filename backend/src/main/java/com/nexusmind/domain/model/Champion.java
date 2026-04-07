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
        name = "champions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"patch_id", "riot_key"})
)
public class Champion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patch_id", nullable = false)
    private PatchVersion patch;

    @Column(name = "riot_key", nullable = false, length = 64)
    private String riotKey;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 256)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lanes_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode lanesJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode tagsJson;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public JsonNode getLanesJson() {
        return lanesJson;
    }

    public void setLanesJson(JsonNode lanesJson) {
        this.lanesJson = lanesJson;
    }

    public JsonNode getTagsJson() {
        return tagsJson;
    }

    public void setTagsJson(JsonNode tagsJson) {
        this.tagsJson = tagsJson;
    }
}
