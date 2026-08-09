package com.ufcg.psoft.project.model.estadopartida;

import com.ufcg.psoft.project.exception.palpite.PalpiteForaDoTempoException;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.JanelaDePalpites;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;

import java.time.LocalDateTime;

public final class EstadoPartidaFinalizada implements EstadoPartida {

    @Override
    public boolean estaAbertaParaPalpite(Partida partida, JanelaDePalpites janela, LocalDateTime horaAtual) {
        return false;
    }

    @Override
    public void validarCriacaoPalpite(Partida partida, JanelaDePalpites janela, LocalDateTime horaAtual) {
        throw new PalpiteForaDoTempoException();
    }

    @Override
    public void validarEdicaoPalpite(Partida partida, JanelaDePalpites janela, LocalDateTime horaAtual) {
        throw new PalpiteForaDoTempoException();
    }

    @Override
    public void validarExclusaoPalpite(Partida partida, JanelaDePalpites janela, LocalDateTime horaAtual) {
        throw new PalpiteForaDoTempoException();
    }

    @Override
    public void validarConsolidacao() {
        // Apenas o estado finalizado permite que o fluxo de consolidação continue
    }

    @Override
    public PartidaStatus statusEfetivoParaGrupo(Partida partida, Grupo grupo, LocalDateTime agora) {
        return PartidaStatus.FINALIZADO;
    }
}
