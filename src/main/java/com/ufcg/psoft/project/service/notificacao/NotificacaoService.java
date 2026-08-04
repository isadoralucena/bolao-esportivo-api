package com.ufcg.psoft.project.service.notificacao;

import com.ufcg.psoft.project.model.Partida;

public interface NotificacaoService {
    void notificarAberturaPalpites(Partida partida);
    void notificarFechamentoPalpites(Partida partida);
    void notificarInicioPartida(Partida partida);
    void notificarPartidaFinalizada(Partida partida);
    void notificarAtualizacaoRanking(Long grupoId);
    void notificarMudancaDePosicao(String nomeUsuario, int posicaoAnterior, int posicaoAtual, Long grupoId);
}