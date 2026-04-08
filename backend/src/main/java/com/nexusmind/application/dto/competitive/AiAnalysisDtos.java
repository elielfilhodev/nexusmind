package com.nexusmind.application.dto.competitive;

import java.util.List;

public final class AiAnalysisDtos {

    private AiAnalysisDtos() {
    }

    /**
     * Análise de partida (IA) — alinhado ao prompt de analista/coach/scout.
     * {@code model} é preenchido pelo backend após a resposta da IA.
     */
    public record MatchAiAnalysisDto(
            String matchId,
            String puuid,
            String summary,
            PerformanceRatingDto performanceRating,
            List<String> strengths,
            List<String> mistakes,
            List<String> playstyleRead,
            String lanePhaseAssessment,
            String midGameAssessment,
            String lateGameAssessment,
            String buildAssessment,
            String runeAssessment,
            String spellAssessment,
            String macroAssessment,
            List<String> consistencyNotes,
            List<String> improvementActions,
            List<String> coachNotes,
            String model
    ) {
    }

    public record PerformanceRatingDto(
            int score,
            String label
    ) {
    }

    public record ProfileAiAnalysisDto(
            String puuid,
            String overview,
            List<String> traits,
            List<String> risks,
            List<String> recommendations,
            String competitiveRead,
            String model
    ) {
    }
}
