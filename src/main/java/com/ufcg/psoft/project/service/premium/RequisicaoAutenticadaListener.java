package com.ufcg.psoft.project.service.premium;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener que observa eventos de requisição autenticada e incrementa
 * o contador de requisições do usuário correspondente.
 */
@Component
public class RequisicaoAutenticadaListener {

    private final ContadorRequisicoes contadorRequisicoes;

    public RequisicaoAutenticadaListener(ContadorRequisicoes contadorRequisicoes) {
        this.contadorRequisicoes = contadorRequisicoes;
    }

    @EventListener
    public void onRequisicaoAutenticada(RequisicaoAutenticadaEvent event) {
        contadorRequisicoes.incrementar(event.usuarioId());
    }
}
