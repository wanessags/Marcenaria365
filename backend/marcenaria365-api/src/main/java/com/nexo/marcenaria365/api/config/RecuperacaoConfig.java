package com.nexo.marcenaria365.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import com.nexo.marcenaria365.api.service.RecuperacaoSenhaService;
import org.slf4j.LoggerFactory;

@Configuration
@EnableScheduling
public class RecuperacaoConfig {
    private final RecuperacaoSenhaService service;
    private final boolean habilitado;

    public RecuperacaoConfig(RecuperacaoSenhaService service,
            @Value("${app.recovery.worker-enabled:true}") boolean habilitado) {
        this.service = service;
        this.habilitado = habilitado;
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void processar() {
        if (!habilitado || !service.configuracaoDisponivel()) return;
        try {
            service.verificarConfiguracao();
            String id = service.prepararProximo();
            if (id != null) service.enviarPreparado(id);
        } catch (Exception erro) {
            // Não registrar argumentos, exceções SQL/SMTP, e-mail, código ou chave.
            LoggerFactory.getLogger(RecuperacaoConfig.class)
                    .error("Falha no processamento da recuperação. Verifique banco, chave de cifra e SMTP.");
        }
    }
}
