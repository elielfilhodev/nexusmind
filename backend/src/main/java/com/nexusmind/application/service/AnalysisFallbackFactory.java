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

        ObjectNode team = root.putObject("teamCompAnalysis");
        team.putArray("alliedStrengths").add("Configure IA para síntese por composição.");
        team.putArray("alliedWeaknesses");
        team.putArray("enemyStrengths");
        team.putArray("enemyWeaknesses");
        team.putArray("winConditions").add("Execução de win conditions do draft — detalhar com IA.");
        team.putArray("loseConditions").add("Erros macro e perda de objetivos — detalhar com IA.");

        ObjectNode lanes = root.putObject("laneReports");
        putStandardLane(lanes.putObject("top"));
        putJungleLane(lanes.putObject("jungle"));
        putStandardLane(lanes.putObject("mid"));
        putStandardLane(lanes.putObject("adc"));
        putSupportLane(lanes.putObject("support"));

        ObjectNode macro = root.putObject("macroPlan");
        macro.putArray("earlyGame").add("Visão river e prio de scuttle conforme matchups.");
        macro.putArray("midGame").add("Side lane com TP e contest de Herald.");
        macro.putArray("lateGame").add("Teamfight pelo padrão da composição.");
        macro.putArray("objectiveControl").add("Negociar drag com estado de sums e prio de lanes.");
        macro.putArray("visionSetup").add("Pixel e entradas de jungle antes de objetivos.");
        macro.putArray("teamfightApproach").add("Front-to-back ou pick conforme draft.");
        macro.putArray("sideLanePlan").add("Duo com pressão em uma lateral.");

        ObjectNode adapt = root.putObject("adaptations");
        adapt.putArray("ifLosingEarly").add("Farm seguro, visão defensiva, esperar spikes.");
        adapt.putArray("ifWinningEarly").add("Converter lead em mapa e objetivos, não overstay.");
        adapt.putArray("highRiskMistakesToAvoid").add("Overstay sem flash").add("fight sem prio de wave.");
        return root;
    }

    private static void putStandardLane(ObjectNode n) {
        n.put("matchupOverview", "Configure IA para matchup específico.");
        n.put("lanePlan", "Priorize CS e trades curtos com saída.");
        n.put("tradingPattern", "Negocie só com vantagem clara de habilidade ou minion advantage.");
        n.put("waveManagement", "Freeze perto do seu lado se vulnerável; slowpush antes de objetivos.");
        n.put("wardingTips", "Trinket river e brush lateral conforme push.");
        n.put("playFromAhead", "Congelar ou dive com jungler — valide visão.");
        n.put("playFromBehind", "Sobreviver, perder menos CS que o oponente, escalar.");
    }

    private static void putJungleLane(ObjectNode n) {
        n.put("pathingPlan", "Path flexível: avalie leash, matchup mid e prio de scuttle.");
        n.put("gankWindows", "Após clear ou quando lane tiver setup de CC ou overextension.");
        n.putArray("priorityLanes").add("Lanes com push ou CC de setup.");
        n.putArray("riskLanes").add("Lanes sem visão ou sem prio.");
        n.put("objectivePlan", "Crabs → drag/herald conforme prio e estado de HP/mana.");
        n.put("playFromAhead", "Invade com prio de lanes e visão.");
        n.put("playFromBehind", "Farm seguro, countergank e visão defensiva.");
    }

    private static void putSupportLane(ObjectNode n) {
        n.put("matchupOverview", "Configure IA para matchup de bot.");
        n.put("lanePlan", "Trade de nível 2/3 conforme engage do duo.");
        n.put("tradingPattern", "Curto com foco no alvo correto (carry ou suporte).");
        n.put("visionPlan", "Control wards em river brush e tri-brush conforme estado da wave.");
        n.put("roamingPlan", "Roam após push ou recall sincronizado com jungle.");
        n.put("playFromAhead", "Deep wards e dive com prio.");
        n.put("playFromBehind", "Wards defensivos e peel no carry.");
    }
}
