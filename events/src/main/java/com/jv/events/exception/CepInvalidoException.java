package com.jv.events.exception;

public class CepInvalidoException extends RuntimeException {
    public CepInvalidoException(String cep) {
        super("CEP inválido: " + cep);
    }
}
