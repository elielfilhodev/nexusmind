package com.nexusmind.application.ai;

/**
 * Porta para provedores compatíveis com chat/completions (OpenRouter, Groq, OpenAI, etc.).
 */
public interface AiProvider {

    /**
     * @return conteúdo textual da mensagem do assistente ou vazio se indisponível
     */
    String complete(AiCompletionRequest request);
}
