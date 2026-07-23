package com.ufcg.psoft.project.exception.grupo;

import com.ufcg.psoft.project.exception.ProjectException;

public class RegraDeTempoInvalidaException extends ProjectException {
    public RegraDeTempoInvalidaException() {
        super("O tempo de abertura deve ser maior que o tempo de fechamento dos palpites.");
    }
}