package com.ufcg.psoft.project.service.notificacao;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ufcg.psoft.project.event.*;
import com.ufcg.psoft.project.exception.partida.PartidaNaoExisteException;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.repository.PartidaRepository;

@Component
public class NotificacaoEventListener {
    private final NotificacaoService notificacaoService;
    private final PartidaRepository partidaRepository;

    public NotificacaoEventListener(
            NotificacaoService notificacaoService,
            PartidaRepository partidaRepository
    ) {
        this.notificacaoService = notificacaoService;
        this.partidaRepository = partidaRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAbrirPalpites(PalpitesAbertosEvent event) {
        notificacaoService.notificarAberturaPalpites(obterPartida(event.getPartidaId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoFecharPalpites(PalpitesFechadosEvent event) {
        notificacaoService.notificarFechamentoPalpites(obterPartida(event.getPartidaId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoIniciarPartida(PartidaIniciadaEvent event) {
        notificacaoService.notificarInicioPartida(obterPartida(event.getPartidaId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoFinalizarPartida(PartidaFinalizadaEvent event) {
        notificacaoService.notificarPartidaFinalizada(obterPartida(event.getPartidaId()));
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

    private Partida obterPartida(Long partidaId) {
        return partidaRepository.findById(partidaId)
                .orElseThrow(PartidaNaoExisteException::new);
    }
}
