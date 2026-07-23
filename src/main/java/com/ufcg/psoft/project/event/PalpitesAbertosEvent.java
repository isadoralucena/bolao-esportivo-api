package com.ufcg.psoft.project.event;

import org.springframework.context.ApplicationEvent;

import com.ufcg.psoft.project.model.Partida;

public class PalpitesAbertosEvent extends ApplicationEvent {
    private final Partida partida;

    public PalpitesAbertosEvent(Object source, Partida partida) {
        super(source);
        this.partida = partida;
    }

    public Partida getPartida() {
        return partida;
    }
}
