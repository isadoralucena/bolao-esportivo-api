package com.ufcg.psoft.project.model.estadopartida;

import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.JanelaDePalpites;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.exception.palpite.PalpiteForaDoTempoException;
import com.ufcg.psoft.project.exception.partida.PartidaNaoFinalizadaException;

import java.time.LocalDateTime;

public final class EstadoPartidaAberta implements EstadoPartida {

    @Override
    public boolean estaAbertaParaPalpite(Partida partida, JanelaDePalpites janela, LocalDateTime horaAtual) {
        LocalDateTime horarioAbertura = partida.getData().minusMinutes(janela.minutosAbertura());
        LocalDateTime horarioFechamento = partida.getData().minusMinutes(janela.minutosFechamento());

        return !horaAtual.isBefore(horarioAbertura) && horaAtual.isBefore(horarioFechamento);
    }

    @Override
    public void validarCriacaoPalpite(Partida partida, JanelaDePalpites janela, LocalDateTime horaAtual) {
        validarJanelaDePalpites(partida, janela, horaAtual);
    }

    @Override
    public void validarEdicaoPalpite(Partida partida, JanelaDePalpites janela, LocalDateTime horaAtual) {
        validarJanelaDePalpites(partida, janela, horaAtual);
    }

    @Override
    public void validarExclusaoPalpite(Partida partida, JanelaDePalpites janela, LocalDateTime horaAtual) {
        validarJanelaDePalpites(partida, janela, horaAtual);
    }

    @Override
    public void validarConsolidacao() {
        throw new PartidaNaoFinalizadaException();
    }

    @Override
    public PartidaStatus statusEfetivoParaGrupo(Partida partida, Grupo grupo, LocalDateTime agora) {
        if (!estaAbertaParaPalpite(partida, grupo.getJanelaDePalpites(), agora)) {
            return PartidaStatus.EM_ANDAMENTO;
        }
        return PartidaStatus.ABERTO;
    }

    private void validarJanelaDePalpites(
            Partida partida,
            JanelaDePalpites janela,
            LocalDateTime horaAtual) {
        if (!estaAbertaParaPalpite(partida, janela, horaAtual)) {
            throw new PalpiteForaDoTempoException();
        }
    }
}
