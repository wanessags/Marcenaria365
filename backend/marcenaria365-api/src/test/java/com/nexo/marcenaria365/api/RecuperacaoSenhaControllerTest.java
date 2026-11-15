package com.nexo.marcenaria365.api;

import com.nexo.marcenaria365.api.controller.RecuperacaoSenhaController;
import com.nexo.marcenaria365.api.service.RecuperacaoSenhaService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RecuperacaoSenhaControllerTest {
    @Test void dadosInvalidosNaoExibemCodigoOuSenhaNaResposta() throws Exception {
        var service = mock(RecuperacaoSenhaService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new RecuperacaoSenhaController(service)).build();
        mvc.perform(post("/api/auth/redefinicao").contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"codigo-invalido\",\"senha\":\"curta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"mensagem\":\"Confira os dados da recuperação.\"}"));
        verifyNoInteractions(service);
    }

    @Test void respostaNaoRevelaContaELimitaPedidos() {
        var service = mock(RecuperacaoSenhaService.class);
        var controller = new RecuperacaoSenhaController(service);
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        var existente = controller.solicitar(new RecuperacaoSenhaController.Solicitacao("cadastrado@example.test"), request);
        var ausente = controller.solicitar(new RecuperacaoSenhaController.Solicitacao("ausente@example.test"), request);
        assertEquals(202, existente.getStatusCode().value());
        assertEquals(existente, ausente);
        for (int i = 0; i < 3; i++) controller.solicitar(new RecuperacaoSenhaController.Solicitacao("outro@example.test"), request);
        assertEquals(429, assertThrows(ResponseStatusException.class, () -> controller.solicitar(
                new RecuperacaoSenhaController.Solicitacao("outro@example.test"), request)).getStatusCode().value());
    }

    @Test void naoConfirmaRedefinicaoQuandoServicoFalha() {
        var service = mock(RecuperacaoSenhaService.class);
        doThrow(new IllegalStateException("Falha de persistência")).when(service).redefinir(anyString(), anyString());
        var controller = new RecuperacaoSenhaController(service);
        assertThrows(IllegalStateException.class, () -> controller.redefinir(
                new RecuperacaoSenhaController.Redefinicao("a".repeat(64), "Segura123!"), new MockHttpServletRequest()));
    }
}
