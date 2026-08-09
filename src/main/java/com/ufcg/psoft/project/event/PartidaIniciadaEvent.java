package com.ufcg.psoft.project.event;

import org.springframework.context.ApplicationEvent;

public class PartidaIniciadaEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private final Long partidaId;

    public PartidaIniciadaEvent(Object source, Long partidaId) {
        super(source);
        this.partidaId = partidaId;
    }

    public Long getPartidaId() {
        return partidaId;
    }
}
