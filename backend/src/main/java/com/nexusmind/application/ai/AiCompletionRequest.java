package com.nexusmind.application.ai;

public record AiCompletionRequest(String systemPrompt, String userMessage, boolean jsonOnly) {
}
