package com.nexusmind.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "pro_player",
        uniqueConstraints = @UniqueConstraint(columnNames = {"puuid", "platform_id"})
)
public class ProPlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "puuid", nullable = false, length = 78)
    private String puuid;

    @Column(name = "platform_id", nullable = false, length = 16)
    private String platformId;

    @Column(name = "game_name", length = 64)
    private String gameName;

    @Column(name = "tag_line", length = 8)
    private String tagLine;

    @Column(name = "team_name", length = 128)
    private String teamName;

    @Column(name = "competitive_region", length = 32)
    private String competitiveRegion;

    @Column(name = "primary_role", length = 16)
    private String primaryRole;

    @Column(name = "scouting_tags", columnDefinition = "text")
    private String scoutingTags;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPuuid() {
        return puuid;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getTagLine() {
        return tagLine;
    }

    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getCompetitiveRegion() {
        return competitiveRegion;
    }

    public void setCompetitiveRegion(String competitiveRegion) {
        this.competitiveRegion = competitiveRegion;
    }

    public String getPrimaryRole() {
        return primaryRole;
    }

    public void setPrimaryRole(String primaryRole) {
        this.primaryRole = primaryRole;
    }

    public String getScoutingTags() {
        return scoutingTags;
    }

    public void setScoutingTags(String scoutingTags) {
        this.scoutingTags = scoutingTags;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
