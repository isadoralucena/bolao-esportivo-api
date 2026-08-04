package com.ufcg.psoft.project.event;

import org.springframework.context.ApplicationEvent;

public class MudancaGrupoPosicaoEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private final String nomeUsuario;
    private final int posicaoAnterior;
    private final int posicaoAtual;
    private final Long grupoId;

    public MudancaGrupoPosicaoEvent(Object source, String nomeUsuario, int posicaoAnterior, int posicaoAtual, Long grupoId) {
        super(source);
        this.nomeUsuario = nomeUsuario;
        this.posicaoAnterior = posicaoAnterior;
        this.posicaoAtual = posicaoAtual;
        this.grupoId = grupoId;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public int getPosicaoAnterior() {
        return posicaoAnterior;
    }

    public int getPosicaoAtual() {
        return posicaoAtual;
    }

    public Long getGrupoId() {
        return grupoId;
    }
}
