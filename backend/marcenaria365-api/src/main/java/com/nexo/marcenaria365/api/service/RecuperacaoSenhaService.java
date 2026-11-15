package com.nexo.marcenaria365.api.service;

import com.nexo.marcenaria365.api.entity.RecuperacaoSenha;
import com.nexo.marcenaria365.api.entity.PedidoRecuperacaoSenha;
import com.nexo.marcenaria365.api.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class RecuperacaoSenhaService {
    private static final Logger LOG = LoggerFactory.getLogger(RecuperacaoSenhaService.class);
    private final UsuarioRepository usuarios;
    private final RecuperacaoSenhaRepository recuperacoes;
    private final PasswordEncoder encoder;
    private final JavaMailSender mail;
    private final PedidoRecuperacaoSenhaRepository pedidos;
    private final CifraCodigoRecuperacao cifra;
    private final TransactionTemplate transacao;
    private final JdbcTemplate jdbc;
    private final String host;
    private final String remetente;
    private final SecureRandom random = new SecureRandom();

    public RecuperacaoSenhaService(UsuarioRepository usuarios, RecuperacaoSenhaRepository recuperacoes,
            PasswordEncoder encoder, JavaMailSender mail,
            PedidoRecuperacaoSenhaRepository pedidos, CifraCodigoRecuperacao cifra,
            PlatformTransactionManager transactions, JdbcTemplate jdbc,
            @Value("${spring.mail.host:}") String host,
            @Value("${app.recovery.from:}") String remetente) {
        this.usuarios = usuarios;
        this.recuperacoes = recuperacoes;
        this.encoder = encoder;
        this.mail = mail;
        this.pedidos = pedidos;
        this.cifra = cifra;
        this.transacao = new TransactionTemplate(transactions);
        this.transacao.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.jdbc = jdbc;
        this.host = host;
        this.remetente = remetente;
    }

    public void verificarConfiguracao() {
        if (!configuracaoDisponivel()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Recuperação temporariamente indisponível.");
        }
    }

    public boolean configuracaoDisponivel() {
        return !host.isBlank() && !remetente.isBlank() && cifra.configurada();
    }

    // O retorno só ocorre após commit. Persistimos pedidos de contas conhecidas ou não.
    public void solicitar(String email) {
        verificarConfiguracao();
        String normalizado = email.trim().toLowerCase(Locale.ROOT);
        String id = hash(normalizado);
        transacao.executeWithoutResult(status -> {
            // Mutex apenas durante a admissão; não é mantido durante SMTP.
            jdbc.queryForObject("SELECT id FROM recuperacao_fila_controle WHERE id = 1 FOR UPDATE", Integer.class);
            var pedido = pedidos.bloquear(id).orElse(null);
            var agora = Instant.now();
            if (pedido != null && (pedido.isPendente()
                    || pedido.getSolicitadoEm().plusSeconds(60).isAfter(agora))) return;
            if (pedidos.countByPendenteTrue() >= 50) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Recuperação temporariamente indisponível.");
            }
            if (pedido == null) pedido = new PedidoRecuperacaoSenha(id, normalizado);
            pedido.solicitar(agora);
            pedidos.saveAndFlush(pedido);
        });
    }

    /** Primeira transação: hash e mensagem cifrada ficam persistidos ANTES do SMTP. */
    public String prepararProximo() {
        return transacao.execute(status -> {
            var lista = pedidos.proximos(Instant.now(), PageRequest.of(0, 1));
            if (lista.isEmpty()) return null;
            var pedido = lista.getFirst();
            var usuario = usuarios.findByEmailIgnoreCase(pedido.getEmail());
            if (usuario.isEmpty()) {
                pedido.concluir();
                pedido.descartarCodigo();
                return null;
            }
            var anterior = recuperacoes.findById(usuario.get().getId()).orElse(null);
            String codigo = pedido.getCodigoCifrado() == null ? null
                    : cifra.decifrar(pedido.getCodigoCifrado(), pedido.getId());
            boolean mesmoCodigo = codigo != null && anterior != null && anterior.getTokenHash().equals(hash(codigo));
            if (pedido.isPreparado() && (!mesmoCodigo || anterior.isUsado())) {
                // Nunca trocar um código consumido/sobreposto por efeito de um retry.
                pedido.concluir();
                pedido.descartarCodigo();
                return null;
            }
            if (!mesmoCodigo || anterior.isUsado() || !anterior.getExpiraEm().isAfter(Instant.now())) {
                byte[] bytes = new byte[32];
                random.nextBytes(bytes);
                codigo = HexFormat.of().formatHex(bytes);
                recuperacoes.saveAndFlush(new RecuperacaoSenha(usuario.get().getId(), hash(codigo), Instant.now()));
                pedido.preparar(cifra.cifrar(codigo, pedido.getId()));
            } else {
                // Reenvios e retomadas reutilizam o mesmo código e o mesmo vencimento.
                pedido.preparar(pedido.getCodigoCifrado());
            }
            pedidos.saveAndFlush(pedido);
            return pedido.getId();
        });
    }

    /** Segunda transação: bloqueio do pedido impede envios simultâneos entre instâncias. */
    public void enviarPreparado(String id) {
        transacao.executeWithoutResult(status -> {
            var pedido = pedidos.bloquear(id).orElse(null);
            if (pedido == null || !pedido.isPendente() || !pedido.isPreparado()
                    || pedido.getProximaTentativa().isAfter(Instant.now())) return;
            String codigo = cifra.decifrar(pedido.getCodigoCifrado(), pedido.getId());
            var token = recuperacoes.buscarParaConsumo(hash(codigo)).orElse(null);
            if (token == null || token.isUsado()) {
                pedido.concluir();
                pedido.descartarCodigo();
                return;
            }
            if (!token.getExpiraEm().isAfter(Instant.now())) {
                // Não enviar código vencido. O próximo ciclo prepara um novo, já que o antigo expirou.
                pedido.descartarCodigo();
                return;
            }
            var mensagem = new SimpleMailMessage();
            mensagem.setFrom(remetente);
            mensagem.setTo(pedido.getEmail());
            mensagem.setSubject("Marcenaria 365 — recuperação de senha");
            mensagem.setText("Use este código no aplicativo Marcenaria 365:\n\n" + codigo
                    + "\n\nUso único. Expira em " + token.getExpiraEm() + " (UTC)."
                    + "\nPrazo de 15 minutos a partir da emissão, sem renovação em reenvios."
                    + "\nSe não solicitou a recuperação, ignore este e-mail. Sua senha não foi alterada.");
            try {
                mail.send(mensagem);
                pedido.concluir();
            } catch (org.springframework.mail.MailException erro) {
                // Manter código e pedido persistidos para retentativa; nunca registrar detalhes do provedor.
                pedido.reagendar(Instant.now());
                LOG.error("Falha SMTP na recuperação. Pedido mantido para nova tentativa.");
            }
        });
    }

    @Transactional
    public void redefinir(String codigo, String senha) {
        if (!SenhaRecuperacao.valida(senha)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confira os requisitos da nova senha.");
        }
        var recuperacao = recuperacoes.buscarParaConsumo(hash(codigo.trim().toLowerCase(Locale.ROOT)))
                .orElseThrow(RecuperacaoSenhaService::codigoInvalido);
        if (recuperacao.isUsado() || !recuperacao.getExpiraEm().isAfter(Instant.now())) {
            throw codigoInvalido();
        }
        var usuario = usuarios.findById(recuperacao.getUsuarioId())
                .orElseThrow(RecuperacaoSenhaService::codigoInvalido);
        usuario.setSenhaHash(encoder.encode(senha));
        usuarios.save(usuario);
        recuperacao.consumir();
        recuperacoes.save(recuperacao);
    }

    private static ResponseStatusException codigoInvalido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido ou expirado. Solicite outro.");
    }

    public static String hash(String codigo) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(codigo.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException erro) {
            throw new IllegalStateException("SHA-256 indisponível.");
        }
    }
}
