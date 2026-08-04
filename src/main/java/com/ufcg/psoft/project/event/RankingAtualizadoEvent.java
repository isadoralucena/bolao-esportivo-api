package com.ufcg.psoft.project.event;

import org.springframework.context.ApplicationEvent;

public class RankingAtualizadoEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private final Long grupoId;

    public RankingAtualizadoEvent(Object source, Long grupoId) {
        super(source);
        this.grupoId = grupoId;
    }

    public Long getGrupoId() {
        return grupoId;
    }
}
