package com.ufcg.psoft.project.service.notificacao;

import com.ufcg.psoft.project.model.Partida;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoServiceImpl implements NotificacaoService {

    @Override
    public void notificarAberturaPalpites(Partida partida) {
        System.out.println(String.format(
                "[NOTIFICAÇÃO] Palpites abertos para a partida: %s x %s (ID: %d)",
                partida.getMandante(), partida.getVisitante(), partida.getId()
        ));
    }

    @Override
    public void notificarFechamentoPalpites(Partida partida) {
        System.out.println(String.format(
                "[NOTIFICAÇÃO] Palpites encerrados para a partida: %s x %s (ID: %d)",
                partida.getMandante(), partida.getVisitante(), partida.getId()
        ));
    }

    @Override
    public void notificarInicioPartida(Partida partida) {
        System.out.println(String.format(
                "[NOTIFICAÇÃO] Partida iniciada: %s x %s (ID: %d)",
                partida.getMandante(), partida.getVisitante(), partida.getId()
        ));
    }

    @Override
    public void notificarPartidaFinalizada(Partida partida) {
        System.out.println(String.format(
                "[NOTIFICAÇÃO] Partida finalizada: %s %d x %d %s (ID: %d)",
                partida.getMandante(), partida.getGolsMandante(),
                partida.getGolsVisitante(), partida.getVisitante(),
                partida.getId()
        ));
    }

    @Override
    public void notificarAtualizacaoRanking(Long grupoId) {
        System.out.println(String.format(
                "[NOTIFICAÇÃO] Ranking atualizado para o grupo ID: %d",
                grupoId
        ));
    }

    @Override
    public void notificarMudancaDePosicao(String nomeUsuario, int posicaoAnterior, int posicaoAtual, Long grupoId) {
        System.out.println(String.format(
                "[NOTIFICAÇÃO] %s mudou de posição no grupo ID %d: %dº → %dº",
                nomeUsuario, grupoId, posicaoAnterior, posicaoAtual
        ));
    }
}