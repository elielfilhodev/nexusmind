package com.nexusmind.application.ai;

/**
 * Engenharia de prompt centralizada. Ajuste versões aqui sem espalhar strings pela aplicação.
 */
public final class TacticPromptCatalog {

    private TacticPromptCatalog() {
    }

    public static String systemAnalyst() {
        return """
                Você é analista de League of Legends (nível coaching staff / CBLOL / internacional).
                Regras obrigatórias:
                - Use apenas o contexto de dados fornecido pelo sistema; não invente patch, números exatos de winrate ou buffs não listados.
                - Se faltar dado, diga explicitamente "dado não disponível no MVP" e raciocine com princípios gerais do meta.
                - Tom profissional, direto, acionável.
                - Responda ESTRITAMENTE em JSON válido UTF-8, sem markdown, sem texto fora do objeto JSON.
                """;
    }

    public static String casualJsonSchemaHint() {
        return """
                Estrutura JSON esperada (campos podem ser arrays de strings ou objetos simples):
                {
                  "meta": { "assumptions": [] },
                  "recommendedPicks": [{ "championKey": "", "reason": "" }],
                  "recommendedBans": [{ "championKey": "", "reason": "" }],
                  "runes": { "primaryTree": "", "keystone": "", "secondaryTree": "", "notes": "" },
                  "summonerSpells": { "d": "", "f": "", "why": "" },
                  "builds": [{ "title": "", "coreItems": [], "situational": [], "why": "" }],
                  "lanePhase": { "levels1to3": "", "waveControl": "", "trades": "", "jungleSynergy": "" },
                  "powerSpikes": [],
                  "whenAhead": "",
                  "whenBehind": "",
                  "commonMistakesAtElo": [],
                  "rankClimbPlan": { "weeklyFocus": [], "championPool": [], "reviewHabits": [] }
                }
                """;
    }

    public static String draftJsonSchemaHint() {
        return """
                Estrutura JSON esperada:
                {
                  "executiveSummary": "",
                  "allyComposition": { "strengths": [], "weaknesses": [], "damageProfile": "" },
                  "enemyComposition": { "strengths": [], "weaknesses": [], "damageProfile": "" },
                  "laneByLane": [{
                    "lane": "TOP|JUNGLE|MID|ADC|SUPPORT",
                    "matchupSummary": "",
                    "levels1to3": "",
                    "scaling": "",
                    "aggroWindows": "",
                    "respectSpikes": "",
                    "jungleInteraction": "",
                    "warding": "",
                    "waveManagement": "",
                    "recoveryIfLosing": ""
                  }],
                  "junglePathing": { "suggested": [], "crabPriority": "", "playableLanes": [], "riskLanes": [] },
                  "earlyGame": { "priorities": [], "rotations": [] },
                  "midGame": { "sideLane": "", "objectives": "", "visionSetup": "" },
                  "lateGame": { "teamfightPattern": "", "winConExecution": "" },
                  "objectiveControl": "",
                  "vision": "",
                  "winConditions": [],
                  "loseConditions": [],
                  "contingency": ""
                }
                """;
    }
}
