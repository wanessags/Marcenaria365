package com.nexo.marcenaria365.api;

import com.nexo.marcenaria365.api.controller.ClienteController;
import com.nexo.marcenaria365.api.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:clientes;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ClientesPersistenciaTest {
    @DynamicPropertySource
    static void chaveDeTeste(DynamicPropertyRegistry properties) {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        String chave = java.util.Base64.getEncoder().encodeToString(bytes);
        properties.add("app.jwt.secret", () -> chave);
    }

    @Autowired ClienteController controller;
    @Autowired ClienteRepository repository;

    @Test void inativacaoPersisteEOmissaoDoCampoNaoReativa() {
        var jwt = Jwt.withTokenValue("teste").header("alg", "HS256").subject("1").build();
        var cadastrado = controller.cadastrar(new ClienteController.ClienteRequest(
                "Cliente", "123", "cliente@example.test", "Endereço", "Histórico", null), jwt).getBody();
        assertNotNull(cadastrado);
        assertTrue(cadastrado.ativo());
        var id = cadastrado.id();
        controller.editar(id, new ClienteController.ClienteRequest(
                "Cliente", "123", "cliente@example.test", "Endereço", "Histórico", false), jwt);
        assertFalse(controller.buscar(id, jwt).getBody().ativo());
        assertFalse(repository.findById(id).orElseThrow().isAtivo());
        controller.editar(id, new ClienteController.ClienteRequest(
                "Cliente editado", "123", "cliente@example.test", "Endereço", "Histórico", null), jwt);
        var relido = controller.buscar(id, jwt).getBody();
        assertFalse(relido.ativo());
        assertEquals("Histórico", relido.observacoes());
        assertEquals("Endereço", relido.endereco());
        assertEquals(1, controller.listar(jwt).stream().filter(c -> c.id().equals(id)).count());
        var outroUsuario = Jwt.withTokenValue("teste").header("alg", "HS256").subject("2").build();
        assertEquals(404, controller.buscar(id, outroUsuario).getStatusCode().value());
    }
}
