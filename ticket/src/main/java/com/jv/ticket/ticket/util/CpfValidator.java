package com.jv.ticket.ticket.util;

import com.jv.ticket.ticket.exception.InvalidCpfException;

public class CpfValidator {
    
    /**
     * Valida se o CPF tem formato válido
     */
    public static boolean isValidFormat(String cpf) {
        if (cpf == null) return false;
        
        // Remove pontos e hífen
        cpf = cpf.replaceAll("[^0-9]", "");
        
        // Verifica se tem 11 dígitos
        if (cpf.length() != 11) return false;
        
        // Verifica se não são todos os dígitos iguais
        if (cpf.matches("(\\d)\\1{10}")) return false;
        
        return true;
    }
    
    /**
     * Formata CPF removendo caracteres especiais
     */
    public static String formatCpf(String cpf) {
        if (cpf == null) return null;
        return cpf.replaceAll("[^0-9]", "");
    }
    
    /**
     * Valida CPF e lança exceção se inválido
     */
    public static void validateCpf(String cpf) {
        if (!isValidFormat(cpf)) {
            throw new InvalidCpfException("CPF format is invalid: " + cpf);
        }
    }
}
