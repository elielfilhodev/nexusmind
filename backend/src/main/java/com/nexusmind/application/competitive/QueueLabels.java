package com.nexusmind.application.competitive;

public final class QueueLabels {

    private QueueLabels() {
    }

    public static String labelForQueueId(int queueId) {
        return switch (queueId) {
            case 420 -> "Ranked Solo/Duo";
            case 440 -> "Ranked Flex";
            case 450 -> "ARAM";
            case 400 -> "Normal Draft";
            case 430 -> "Normal Blind";
            case 490 -> "Quickplay";
            default -> "Queue " + queueId;
        };
    }

    public static Integer rankedSoloQueueId() {
        return 420;
    }

    public static Integer rankedFlexQueueId() {
        return 440;
    }
}
