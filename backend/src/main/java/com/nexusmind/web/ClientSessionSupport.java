package com.nexusmind.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Identificador por navegador (localStorage), enviado em {@value #HEADER_NAME}.
 * Não é autenticação forte — apenas isola listagens e relatórios entre visitantes.
 */
public final class ClientSessionSupport {

    public static final String HEADER_NAME = "X-Client-Session-Id";

    private ClientSessionSupport() {
    }

    public static String requireValid(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cabeçalho " + HEADER_NAME + " é obrigatório (identifique o navegador — ex.: UUID em localStorage)."
            );
        }
        String t = raw.trim();
        if (t.length() < 8 || t.length() > 64) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sessão inválida (tamanho).");
        }
        if (!t.matches("^[a-zA-Z0-9_-]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sessão inválida (caracteres).");
        }
        return t;
    }
}
