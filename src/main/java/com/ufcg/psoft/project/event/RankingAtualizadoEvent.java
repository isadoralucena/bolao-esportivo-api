package com.ufcg.psoft.project.event;

import org.springframework.context.ApplicationEvent;

public class RankingAtualizadoEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private final Long grupoId;
    private final Long partidaId;

    public RankingAtualizadoEvent(Object source, Long grupoId) {
        this(source, grupoId, null);
    }

    public RankingAtualizadoEvent(Object source, Long grupoId, Long partidaId) {
        super(source);
        this.grupoId = grupoId;
        this.partidaId = partidaId;
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public Long getPartidaId() {
        return partidaId;
    }
}