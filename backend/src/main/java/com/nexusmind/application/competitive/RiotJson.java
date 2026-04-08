package com.nexusmind.application.competitive;

import com.fasterxml.jackson.databind.JsonNode;

public final class RiotJson {

    private RiotJson() {
    }

    public static String text(JsonNode n, String field) {
        if (n == null || !n.has(field) || n.get(field).isNull()) {
            return "";
        }
        return n.get(field).asText("");
    }

    public static int intVal(JsonNode n, String field) {
        if (n == null || !n.has(field) || n.get(field).isNull()) {
            return 0;
        }
        return n.get(field).asInt(0);
    }

    public static long longVal(JsonNode n, String field) {
        if (n == null || !n.has(field) || n.get(field).isNull()) {
            return 0L;
        }
        return n.get(field).asLong(0L);
    }

    public static boolean bool(JsonNode n, String field) {
        if (n == null || !n.has(field) || n.get(field).isNull()) {
            return false;
        }
        return n.get(field).asBoolean(false);
    }
}
