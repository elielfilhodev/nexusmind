package com.nexusmind.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class AnalysisFallbackFactory {

    private final ObjectMapper objectMapper;

    public AnalysisFallbackFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectNode casualFallback(String reason) {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode meta = root.putObject("meta");
        meta.put("status", "FALLBACK");
        meta.put("reason", reason);
        meta.putArray("assumptions").add("Resposta heurística — configure IA para análise completa.");
        root.putArray("recommendedPicks");
        root.putArray("recommendedBans");
        ObjectNode runes = root.putObject("runes");
        runes.put("primaryTree", "precision");
        runes.put("keystone", "Press the Attack / Conqueror (ajuste por campeão)");
        runes.put("secondaryTree", "resolve ou inspiration");
        runes.put("notes", "Use runas do patch atual no cliente; dados detalhados virão da ingestão.");
        ObjectNode spells = root.putObject("summonerSpells");
        spells.put("d", "Flash");
        spells.put("f", "Ignite/Heal/Teleport conforme role");
        spells.put("why", "Padrão solo queue — valide no carregamento.");
        ArrayNode builds = root.putArray("builds");
        ObjectNode b1 = builds.addObject();
        b1.put("title", "Build core genérica");
        b1.putArray("coreItems").add("Itens do patch — ver catálogo no app");
        b1.putArray("situational");
        b1.put("why", "MVP sem stats ao vivo");
        root.putObject("lanePhase")
                .put("levels1to3", "Respeite minion aggro, priorize CS, negocie só com vantagem de habilidade.")
                .put("waveControl", "Freeze perto do seu lado quando vulnerável; slowpush antes de objetivos.")
                .put("trades", "Curto e com saída; evite extender sem visão do jungle inimigo.")
                .put("jungleSynergy", "Ping early path; jogue para prioridade de scuttle quando mid tem push.");
        root.putArray("powerSpikes").add("Itemização completa").add("níveis de ultimate");
        root.put("whenAhead", "Converter lead em visão e pressão de mapa, não em kills arriscadas.");
        root.put("whenBehind", "Farm seguro, componentes defensivos, esperar erros e power spikes.");
        ArrayNode mistakes = root.putArray("commonMistakesAtElo");
        mistakes.add("Overstay sem flash").add("Roaming sem push").add("Ignorar wave antes de drag");
        ObjectNode plan = root.putObject("rankClimbPlan");
        plan.putArray("weeklyFocus").add("Revisão de 3 replays").add("Pool de 2–3 campeões");
        plan.putArray("championPool").add("Confort + meta tolerável");
        plan.putArray("reviewHabits").add("Timer de objetivos").add("checklist de visão");
        return root;
    }

    public ObjectNode draftFallback(String reason) {
        ObjectNode root = objectMapper.createObjectNode();
        root.putObject("meta").put("status", "FALLBACK").put("reason", reason);
        root.put("executiveSummary", "Análise heurística: configure provedor de IA para relatório completo.");
        allyEnemyStub(root.putObject("allyComposition"));
        allyEnemyStub(root.putObject("enemyComposition"));
        root.putArray("laneByLane");
        ObjectNode junglePathing = root.putObject("junglePathing");
        junglePathing.putArray("suggested").add("path flexível conforme matchup de lanes");
        junglePathing.put("crabPriority", "avalie mid prio e matchups de 2v2");
        junglePathing.putArray("playableLanes").add("lanes com setup de CC ou push");
        junglePathing.putArray("riskLanes").add("lanes sem visão / sem prio");
        phaseStub(root.putObject("earlyGame"));
        root.putObject("midGame").put("sideLane", "duo side com TP").put("objectives", "herald → torres").put("visionSetup", "pink em pixel brush");
        root.putObject("lateGame").put("teamfightPattern", "front-to-back padrão").put("winConExecution", "isolar carry inimigo com pick");
        root.put("objectiveControl", "Negocie drag com estado de sums e prio.");
        root.put("vision", "Controle river antes de 2º drag.");
        root.putArray("winConditions").add("Execução de engage").add("melhor scaling");
        root.putArray("loseConditions").add("perder mapa cedo").add("erros de posicionamento");
        root.put("contingency", "Turtle, waveclear, esperar item spikes.");
        return root;
    }

    private static void allyEnemyStub(ObjectNode n) {
        n.putArray("strengths");
        n.putArray("weaknesses");
        n.put("damageProfile", "não calculado no fallback");
    }

    private static void phaseStub(ObjectNode n) {
        n.putArray("priorities").add("visão river").add("estado de flash inimigo");
        n.putArray("rotations").add("mid primeiro em scuttle");
    }
}
