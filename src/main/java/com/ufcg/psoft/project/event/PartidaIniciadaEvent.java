package com.ufcg.psoft.project.event;

import org.springframework.context.ApplicationEvent;

import com.ufcg.psoft.project.model.Partida;

public class PartidaIniciadaEvent extends ApplicationEvent {

    private final Partida partida;

    public PartidaIniciadaEvent(Object source, Partida partida) {
        super(source);
        this.partida = partida;
    }

    public Partida getPartida() {
        return partida;
    }
}