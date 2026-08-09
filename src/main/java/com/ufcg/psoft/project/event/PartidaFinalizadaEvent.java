package com.ufcg.psoft.project.event;

import org.springframework.context.ApplicationEvent;

public class PartidaFinalizadaEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private final Long partidaId;

    public PartidaFinalizadaEvent(Object source, Long partidaId) {
        super(source);
        this.partidaId = partidaId;
    }

    public Long getPartidaId() {
        return partidaId;
    }
}
