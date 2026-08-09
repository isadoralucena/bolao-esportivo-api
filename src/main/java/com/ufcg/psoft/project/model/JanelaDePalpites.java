package com.ufcg.psoft.project.model;
import com.ufcg.psoft.project.exception.grupo.RegraDeTempoInvalidaException;

public record JanelaDePalpites(Integer minutosAbertura, Integer minutosFechamento) {
    public JanelaDePalpites {
        if (minutosAbertura <= minutosFechamento) {
            throw new RegraDeTempoInvalidaException();
        }
    }
}