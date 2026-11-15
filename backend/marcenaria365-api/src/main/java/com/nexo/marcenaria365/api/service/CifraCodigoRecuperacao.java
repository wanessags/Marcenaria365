package com.nexo.marcenaria365.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CifraCodigoRecuperacao {
    private final String chaveConfigurada;
    private final SecureRandom random = new SecureRandom();

    public CifraCodigoRecuperacao(@Value("${app.recovery.encryption-key:}") String chave) {
        this.chaveConfigurada = chave;
    }

    public boolean configurada() {
        try { return Base64.getDecoder().decode(chaveConfigurada).length == 32; }
        catch (IllegalArgumentException erro) { return false; }
    }

    public String cifrar(String codigo, String pedidoId) {
        byte[] nonce = new byte[12];
        random.nextBytes(nonce);
        try {
            var cipher = cipher(Cipher.ENCRYPT_MODE, nonce, pedidoId);
            byte[] cifrado = cipher.doFinal(codigo.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(nonce.length + cifrado.length)
                    .put(nonce).put(cifrado).array());
        } catch (Exception erro) { throw falha(); }
    }

    public String decifrar(String valor, String pedidoId) {
        try {
            var buffer = ByteBuffer.wrap(Base64.getDecoder().decode(valor));
            byte[] nonce = new byte[12];
            buffer.get(nonce);
            byte[] cifrado = new byte[buffer.remaining()];
            buffer.get(cifrado);
            return new String(cipher(Cipher.DECRYPT_MODE, nonce, pedidoId).doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (Exception erro) { throw falha(); }
    }

    private Cipher cipher(int modo, byte[] nonce, String pedidoId) throws Exception {
        byte[] chave = Base64.getDecoder().decode(chaveConfigurada);
        if (chave.length != 32) throw falha();
        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(modo, new SecretKeySpec(chave, "AES"), new GCMParameterSpec(128, nonce));
        cipher.updateAAD(pedidoId.getBytes(StandardCharsets.UTF_8));
        return cipher;
    }

    private static IllegalStateException falha() {
        // Não propagar conteúdo, chave, código ou exceção do provedor criptográfico aos logs.
        return new IllegalStateException("Não foi possível acessar o código protegido da recuperação.");
    }
}
