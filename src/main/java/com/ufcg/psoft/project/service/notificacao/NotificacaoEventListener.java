package com.ufcg.psoft.project.service.notificacao;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ufcg.psoft.project.event.*;

@Component
public class NotificacaoEventListener {
    private final NotificacaoService notificacaoService;

    public NotificacaoEventListener(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAbrirPalpites(PalpitesAbertosEvent event) {
        notificacaoService.notificarAberturaPalpites(event.getPartida());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoFecharPalpites(PalpitesFechadosEvent event) {
        notificacaoService.notificarFechamentoPalpites(event.getPartida());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoIniciarPartida(PartidaIniciadaEvent event) {
        notificacaoService.notificarInicioPartida(event.getPartida());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoFinalizarPartida(PartidaFinalizadaEvent event) {
        notificacaoService.notificarPartidaFinalizada(event.getPartida());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAtualizarRanking(RankingAtualizadoEvent event) {
        notificacaoService.notificarAtualizacaoRanking(event.getGrupoId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoMudarGrupoPosicao(MudancaGrupoPosicaoEvent event) {
        notificacaoService.notificarMudancaDePosicao(
            event.getNomeUsuario(),
            event.getPosicaoAnterior(), 
            event.getPosicaoAtual(), 
            event.getGrupoId()
        );
    }
}