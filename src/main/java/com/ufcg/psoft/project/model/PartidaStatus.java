package com.ufcg.psoft.project.model;

import com.ufcg.psoft.project.model.estadopartida.EstadoPartida;
import com.ufcg.psoft.project.model.estadopartida.EstadoPartidaAberta;
import com.ufcg.psoft.project.model.estadopartida.EstadoPartidaCancelada;
import com.ufcg.psoft.project.model.estadopartida.EstadoPartidaEmAndamento;
import com.ufcg.psoft.project.model.estadopartida.EstadoPartidaFinalizada;

public enum PartidaStatus {
    ABERTO(new EstadoPartidaAberta()),
    EM_ANDAMENTO(new EstadoPartidaEmAndamento()),
    FINALIZADO(new EstadoPartidaFinalizada()),
    CANCELADO(new EstadoPartidaCancelada());

    private final EstadoPartida estado;

    PartidaStatus(EstadoPartida estado) {
        this.estado = estado;
    }

    EstadoPartida getEstado() {
        return estado;
    }
}
