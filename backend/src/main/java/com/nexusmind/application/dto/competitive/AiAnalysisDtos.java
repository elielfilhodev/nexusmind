package com.nexusmind.application.dto.competitive;

import java.util.List;

public final class AiAnalysisDtos {

    private AiAnalysisDtos() {
    }

    public record MatchAiAnalysisDto(
            String matchId,
            String puuid,
            String summary,
            List<String> strengths,
            List<String> mistakes,
            List<String> tips,
            String playstyle,
            String tempo,
            String confidenceNote,
            String model
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
