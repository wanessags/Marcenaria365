package com.nexo.marcenaria365.api;

import com.nexo.marcenaria365.api.controller.OrcamentoController;
import com.nexo.marcenaria365.api.entity.*;
import com.nexo.marcenaria365.api.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:orcamentos;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrcamentosPersistenciaTest {
    @DynamicPropertySource
    static void chaveDeTeste(DynamicPropertyRegistry properties) {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        String chave = java.util.Base64.getEncoder().encodeToString(bytes);
        properties.add("app.jwt.secret", () -> chave);
    }

    @Autowired OrcamentoController controller;
    @Autowired ClienteRepository clientes;
    @Autowired ServicoRepository servicos;

    @Test void gravaReleEditaItensETotaisPreservandoStatus() {
        var cliente = new Cliente();
        cliente.setNome("Cliente de teste");
        cliente.setUsuarioId(1L);
        cliente = clientes.saveAndFlush(cliente);
        var servico = new Servico();
        servico.setClienteId(cliente.getId());
        servico.setTitulo("Armário");
        servico = servicos.saveAndFlush(servico);
        var jwt = Jwt.withTokenValue("teste").header("alg", "HS256").subject("1").build();
        var itens = List.of(
                new OrcamentoController.ItemRequest("Materiais", "MDF", new BigDecimal("2"), new BigDecimal("100.25")),
                new OrcamentoController.ItemRequest("Mão de obra", "Montagem", BigDecimal.ONE, new BigDecimal("300.00")));
        var dados = new OrcamentoController.OrcamentoRequest(cliente.getId(), servico.getId(),
                "EM_APROVACAO", null, "Observações", BigDecimal.ZERO, 40, itens);
        var resposta = controller.cadastrar(dados, jwt);
        assertEquals(201, resposta.getStatusCode().value());
        var salvo = (OrcamentoController.OrcamentoResponse) resposta.getBody();
        assertNotNull(salvo);
        var relido = controller.buscar(salvo.id(), jwt).getBody();
        assertEquals(0, new BigDecimal("500.50").compareTo(relido.total()));
        assertEquals(40, relido.percentualEntrada());
        assertEquals("MDF", relido.itens().getFirst().getDetalhe());
        assertEquals(2, relido.itens().size());
        for (String status : List.of("ENVIADO", "APROVADO", "RECUSADO")) {
            var editado = new OrcamentoController.OrcamentoRequest(cliente.getId(), servico.getId(),
                    status, null, "Observações editadas", new BigDecimal("10.00"), 50, itens);
            assertEquals(200, controller.editar(salvo.id(), editado, jwt).getStatusCode().value());
            relido = controller.buscar(salvo.id(), jwt).getBody();
            assertEquals(status, relido.status());
            assertEquals(0, new BigDecimal("490.50").compareTo(relido.total()));
            assertEquals(2, relido.itens().size());
        }
        var outroJwt = Jwt.withTokenValue("teste").header("alg", "HS256").subject("2").build();
        assertEquals(404, controller.buscar(salvo.id(), outroJwt).getStatusCode().value());
    }
}
