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
        name = "rune_trees",
        uniqueConstraints = @UniqueConstraint(columnNames = {"patch_id", "tree_key"})
)
public class RuneTree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patch_id", nullable = false)
    private PatchVersion patch;

    @Column(name = "tree_key", nullable = false, length = 32)
    private String treeKey;

    @Column(nullable = false, length = 128)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "slots_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode slotsJson;

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

    public String getTreeKey() {
        return treeKey;
    }

    public void setTreeKey(String treeKey) {
        this.treeKey = treeKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonNode getSlotsJson() {
        return slotsJson;
    }

    public void setSlotsJson(JsonNode slotsJson) {
        this.slotsJson = slotsJson;
    }
}
