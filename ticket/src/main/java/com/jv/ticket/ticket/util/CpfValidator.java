package com.jv.ticket.ticket.util;

import com.jv.ticket.ticket.exception.InvalidCpfException;

public class CpfValidator {
    
    public static boolean isValidFormat(String cpf) {
        if (cpf == null) return false;
        
        cpf = cpf.replaceAll("[^0-9]", "");
        
        if (cpf.length() != 11) return false;
        
        if (cpf.matches("(\\d)\\1{10}")) return false;
        
        return true;
    }
    
    public static String formatCpf(String cpf) {
        if (cpf == null) return null;
        return cpf.replaceAll("[^0-9]", "");
    }

    public static void validateCpf(String cpf) {
        if (!isValidFormat(cpf)) {
            throw new InvalidCpfException("CPF format is invalid: " + cpf);
        }
    }
}
