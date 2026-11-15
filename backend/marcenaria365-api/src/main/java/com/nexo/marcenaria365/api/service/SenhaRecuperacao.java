package com.nexo.marcenaria365.api.service;

import java.nio.charset.StandardCharsets;

public final class SenhaRecuperacao {
    private SenhaRecuperacao() {}

    public static boolean valida(String senha) {
        // Os mesmos cinco requisitos do cadastro. BCrypt aceita no máximo 72 bytes.
        return senha != null && senha.length() >= 8
                && senha.getBytes(StandardCharsets.UTF_8).length <= 72
                && senha.chars().anyMatch(Character::isUpperCase)
                && senha.chars().anyMatch(Character::isLowerCase)
                && senha.chars().anyMatch(Character::isDigit)
                && senha.chars().anyMatch(c -> !Character.isLetterOrDigit(c)
                    && !Character.isWhitespace(c) && !Character.isSpaceChar(c));
    }
}
