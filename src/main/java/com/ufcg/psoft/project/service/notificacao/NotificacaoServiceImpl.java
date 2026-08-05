package com.ufcg.psoft.project.service.notificacao;

import com.ufcg.psoft.project.model.Partida;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificacaoServiceImpl implements NotificacaoService {

    @Override
    public void notificarAberturaPalpites(Partida partida) {
        log.info(
            "[NOTIFICAÇÃO] Palpites abertos para a partida: {} x {} (ID: {})",
            partida.getMandante(), partida.getVisitante(), partida.getId()
        );
    }

    @Override
    public void notificarFechamentoPalpites(Partida partida) {
        log.info(
            "[NOTIFICAÇÃO] Palpites encerrados para a partida: {} x {} (ID: {})",
            partida.getMandante(), partida.getVisitante(), partida.getId()
        );
    }

    @Override
    public void notificarInicioPartida(Partida partida) {
        log.info(
            "[NOTIFICAÇÃO] Partida iniciada: {} x {} (ID: {})",
            partida.getMandante(), partida.getVisitante(), partida.getId()
        );
    }

    @Override
    public void notificarPartidaFinalizada(Partida partida) {
        log.info(
            "[NOTIFICAÇÃO] Partida finalizada: {} {} x {} {} (ID: {})",
            partida.getMandante(), partida.getGolsMandante(),
            partida.getGolsVisitante(), partida.getVisitante(),
            partida.getId()
        );
    }

    @Override
    public void notificarAtualizacaoRanking(Long grupoId) {
        log.info(
            "[NOTIFICAÇÃO] Ranking atualizado para o grupo ID: {}",
            grupoId
        );
    }

    @Override
    public void notificarMudancaDePosicao(String nomeUsuario, int posicaoAnterior, int posicaoAtual, Long grupoId) {
        log.info(
            "[NOTIFICAÇÃO] {} mudou de posição no grupo ID {}: {}º → {}º",
            nomeUsuario, grupoId, posicaoAnterior, posicaoAtual
        );
    }
}