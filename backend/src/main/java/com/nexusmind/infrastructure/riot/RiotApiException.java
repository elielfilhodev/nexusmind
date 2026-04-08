package com.nexusmind.infrastructure.riot;

import org.springframework.http.HttpStatusCode;

/**
 * Erro da integração Riot (sem expor corpo bruto ao cliente final).
 */
public class RiotApiException extends RuntimeException {

    private final int status;
    private final String riotErrorCode;

    public RiotApiException(int status, String message) {
        super(message);
        this.status = status;
        this.riotErrorCode = null;
    }

    public RiotApiException(HttpStatusCode status, String message, String riotErrorCode) {
        super(message);
        this.status = status.value();
        this.riotErrorCode = riotErrorCode;
    }

    public int status() {
        return status;
    }

    public String riotErrorCode() {
        return riotErrorCode;
    }
}
