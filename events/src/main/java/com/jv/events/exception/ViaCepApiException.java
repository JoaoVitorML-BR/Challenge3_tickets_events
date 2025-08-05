package com.jv.events.exception;

public class ViaCepApiException extends RuntimeException {
    public ViaCepApiException(String message, Throwable cause) {
        super("Erro ao buscar CEP via API ViaCEP: " + message, cause);
    }
    
    public ViaCepApiException(String message) {
        super("Erro ao buscar CEP via API ViaCEP: " + message);
    }
}
