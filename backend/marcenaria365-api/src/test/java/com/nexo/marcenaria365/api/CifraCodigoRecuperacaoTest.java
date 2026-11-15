package com.nexo.marcenaria365.api;

import com.nexo.marcenaria365.api.service.CifraCodigoRecuperacao;
import org.junit.jupiter.api.Test;
import java.security.SecureRandom;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

class CifraCodigoRecuperacaoTest {
    static String chave() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Test void cifraAutenticadaUsaNonceAleatorioEResisteTrocaDePedidoOuChave() {
        String chave = chave();
        var cifra = new CifraCodigoRecuperacao(chave);
        String codigo = "a".repeat(64);
        String cifrado = cifra.cifrar(codigo, "pedido1");
        assertNotEquals(cifrado, cifra.cifrar(codigo, "pedido1"));
        assertFalse(cifrado.contains(codigo));
        assertEquals(codigo, new CifraCodigoRecuperacao(chave).decifrar(cifrado, "pedido1"));
        assertThrows(IllegalStateException.class, () -> cifra.decifrar(cifrado, "pedido2"));
        assertThrows(IllegalStateException.class, () -> new CifraCodigoRecuperacao(chave()).decifrar(cifrado, "pedido1"));
        byte[] alterado = Base64.getDecoder().decode(cifrado);
        alterado[alterado.length - 1] ^= 1;
        assertThrows(IllegalStateException.class, () -> cifra.decifrar(Base64.getEncoder().encodeToString(alterado), "pedido1"));
    }

    @Test void chaveAusenteOuInvalidaNaoHabilitaRecuperacao() {
        assertFalse(new CifraCodigoRecuperacao("").configurada());
        assertFalse(new CifraCodigoRecuperacao("invalida").configurada());
        assertTrue(new CifraCodigoRecuperacao(chave()).configurada());
    }
}
