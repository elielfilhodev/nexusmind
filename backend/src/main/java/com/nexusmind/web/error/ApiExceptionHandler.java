package com.nexusmind.web.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.nexusmind.infrastructure.riot.RiotApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(ApiExceptionHandler::formatFieldError)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of(
                "error", "VALIDATION",
                "message", msg
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> notReadable(HttpMessageNotReadableException ex) {
        log.debug("Payload inválido: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
                "error", "BAD_JSON",
                "message", "Corpo da requisição inválido"
        ));
    }

    @ExceptionHandler(RiotApiException.class)
    public ResponseEntity<Map<String, Object>> riot(RiotApiException ex) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        HttpStatus st = status != null ? status : HttpStatus.BAD_GATEWAY;
        Map<String, Object> body = new HashMap<>();
        body.put("error", "RIOT");
        body.put("message", ex.getMessage());
        if (ex.riotErrorCode() != null) {
            body.put("riotErrorCode", ex.riotErrorCode());
        }
        return ResponseEntity.status(st).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> status(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        Map<String, Object> body = new HashMap<>();
        body.put("error", status.name());
        body.put("message", ex.getReason() != null ? ex.getReason() : status.getReasonPhrase());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> fallback(Exception ex) {
        log.error("Erro não tratado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL",
                "message", "Erro interno"
        ));
    }

    private static String formatFieldError(FieldError e) {
        return e.getField() + ": " + e.getDefaultMessage();
    }
}
