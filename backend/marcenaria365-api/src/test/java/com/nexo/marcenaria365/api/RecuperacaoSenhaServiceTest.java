package com.nexo.marcenaria365.api;

import com.nexo.marcenaria365.api.entity.*;
import com.nexo.marcenaria365.api.repository.*;
import com.nexo.marcenaria365.api.service.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.Optional;
import java.util.List;
import java.util.Base64;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecuperacaoSenhaServiceTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final RecuperacaoSenhaRepository tokens = mock(RecuperacaoSenhaRepository.class);
    private final JavaMailSender mail = mock(JavaMailSender.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final PedidoRecuperacaoSenhaRepository pedidos = mock(PedidoRecuperacaoSenhaRepository.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    private final CifraCodigoRecuperacao cifra = new CifraCodigoRecuperacao(chaveAleatoria());
    private final RecuperacaoSenhaService service = new RecuperacaoSenhaService(
            usuarios, tokens, encoder, mail, pedidos, cifra, transactions, jdbc,
            "smtp.example.test", "usuario", "senha", true, "app@example.test");

    private static String chaveAleatoria() {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private PedidoRecuperacaoSenha pedido() {
        var pedido = new PedidoRecuperacaoSenha("pedido", "pessoa@example.test");
        pedido.solicitar(Instant.now().minusSeconds(1));
        when(pedidos.proximos(any(), any())).thenReturn(List.of(pedido));
        when(pedidos.bloquear("pedido")).thenReturn(Optional.of(pedido));
        return pedido;
    }

    @Test void exigeCincoRequisitos() {
        assertTrue(SenhaRecuperacao.valida("Segura123!"));
        for (String senha : new String[]{"Ab1!", "segura123!", "SEGURA123!", "Seguraaaa!", "Segura123 ", "Áa1!".repeat(20)}) {
            assertFalse(SenhaRecuperacao.valida(senha));
        }
    }

    @Test void emailDesconhecidoNaoGeraTokenNemEnviaEmail() {
        var pedido = pedido();
        when(usuarios.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        assertNull(service.prepararProximo());
        assertFalse(pedido.isPendente());
        verifyNoInteractions(tokens, mail);
    }

    @Test void geraCodigoAleatorioEArmazenaSomenteHash() {
        var pedido = pedido();
        var usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(1L);
        when(usuario.getEmail()).thenReturn("pessoa@example.test");
        when(usuarios.findByEmailIgnoreCase("pessoa@example.test")).thenReturn(Optional.of(usuario));
        when(tokens.findById(1L)).thenReturn(Optional.empty());
        service.prepararProximo();
        var token = ArgumentCaptor.forClass(RecuperacaoSenha.class);
        var mensagem = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(tokens).saveAndFlush(token.capture());
        when(tokens.buscarParaConsumo(anyString())).thenReturn(Optional.of(token.getValue()));
        service.enviarPreparado(pedido.getId());
        verify(mail).send(mensagem.capture());
        String codigo = mensagem.getValue().getText().split("\n\n")[1];
        assertTrue(codigo.matches("[a-f0-9]{64}"));
        assertNotEquals(codigo, token.getValue().getTokenHash());
        assertEquals(RecuperacaoSenhaService.hash(codigo), token.getValue().getTokenHash());
        assertEquals(900, token.getValue().getExpiraEm().getEpochSecond() - token.getValue().getSolicitadoEm().getEpochSecond());
        assertFalse(token.getValue().isUsado());
        assertFalse(pedido.getCodigoCifrado().contains(codigo));
        assertEquals(codigo, cifra.decifrar(pedido.getCodigoCifrado(), pedido.getId()));
    }

    @Test void rejeitaCodigoInexistente() {
        when(tokens.buscarParaConsumo(anyString())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.redefinir("a".repeat(64), "Segura123!"));
        verify(usuarios, never()).save(any());
    }

    @Test void rejeitaCodigoExpiradoEUsado() {
        var expirado = new RecuperacaoSenha(1L, "hash", Instant.now().minusSeconds(901));
        when(tokens.buscarParaConsumo(anyString())).thenReturn(Optional.of(expirado));
        assertThrows(ResponseStatusException.class, () -> service.redefinir("a".repeat(64), "Segura123!"));
        var usado = new RecuperacaoSenha(1L, "hash", Instant.now());
        usado.consumir();
        when(tokens.buscarParaConsumo(anyString())).thenReturn(Optional.of(usado));
        assertThrows(ResponseStatusException.class, () -> service.redefinir("a".repeat(64), "Segura123!"));
        verifyNoInteractions(usuarios);
    }

    @Test void atualizaHashEConsomeCodigo() {
        var token = new RecuperacaoSenha(1L, "hash", Instant.now());
        var usuario = new Usuario();
        when(tokens.buscarParaConsumo(anyString())).thenReturn(Optional.of(token));
        when(usuarios.findById(1L)).thenReturn(Optional.of(usuario));
        service.redefinir("a".repeat(64), "Segura123!");
        assertTrue(encoder.matches("Segura123!", usuario.getSenhaHash()));
        assertTrue(token.isUsado());
        verify(usuarios).save(usuario);
        verify(tokens).save(token);
        assertThrows(ResponseStatusException.class, () -> service.redefinir("a".repeat(64), "Outra123!"));
    }

    @Test void limitaReenvioPorConta() {
        var pedido = new PedidoRecuperacaoSenha("id", "pessoa@example.test");
        pedido.solicitar(Instant.now());
        pedido.concluir();
        when(pedidos.bloquear(anyString())).thenReturn(Optional.of(pedido));
        service.solicitar("pessoa@example.test");
        verifyNoInteractions(mail);
        verify(tokens, never()).saveAndFlush(any());
    }

    @Test void falhaSmtpNaoExpoeDetalhesDoProvedor() {
        var pedido = pedido();
        var usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(1L);
        when(usuario.getEmail()).thenReturn("pessoa@example.test");
        when(usuarios.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(usuario));
        when(tokens.findById(1L)).thenReturn(Optional.empty());
        doThrow(new MailSendException("conteudo sensivel")).when(mail).send(any(SimpleMailMessage.class));
        service.prepararProximo();
        var token = ArgumentCaptor.forClass(RecuperacaoSenha.class);
        verify(tokens).saveAndFlush(token.capture());
        when(tokens.buscarParaConsumo(anyString())).thenReturn(Optional.of(token.getValue()));
        assertDoesNotThrow(() -> service.enviarPreparado(pedido.getId()));
        assertTrue(pedido.isPendente());
        assertEquals(1, pedido.getTentativas());
        assertTrue(pedido.getProximaTentativa().isAfter(Instant.now()));
    }

    @Test void semSmtpRetornaIndisponibilidade() {
        var semSmtp = new RecuperacaoSenhaService(usuarios, tokens, encoder, mail,
                pedidos, cifra, transactions, jdbc, "", "", "", true, "");
        assertThrows(ResponseStatusException.class, semSmtp::verificarConfiguracao);
        verifyNoInteractions(usuarios, tokens, mail);
    }

    @Test void exigeCredenciaisQuandoAutenticacaoSmtpEstaHabilitada() {
        var semUsuario = new RecuperacaoSenhaService(usuarios, tokens, encoder, mail,
                pedidos, cifra, transactions, jdbc,
                "smtp.example.test", "", "senha", true, "app@example.test");
        var semSenha = new RecuperacaoSenhaService(usuarios, tokens, encoder, mail,
                pedidos, cifra, transactions, jdbc,
                "smtp.example.test", "usuario", "", true, "app@example.test");

        assertThrows(ResponseStatusException.class, semUsuario::verificarConfiguracao);
        assertThrows(ResponseStatusException.class, semSenha::verificarConfiguracao);
        verifyNoInteractions(usuarios, tokens, mail);
    }

    @Test void naoExigeCredenciaisQuandoAutenticacaoSmtpEstaDesabilitada() {
        var semAutenticacao = new RecuperacaoSenhaService(usuarios, tokens, encoder, mail,
                pedidos, cifra, transactions, jdbc,
                "smtp.example.test", "", "", false, "app@example.test");

        assertTrue(semAutenticacao.configuracaoDisponivel());
        assertDoesNotThrow(semAutenticacao::verificarConfiguracao);
        verifyNoInteractions(usuarios, tokens, mail);
    }
}
