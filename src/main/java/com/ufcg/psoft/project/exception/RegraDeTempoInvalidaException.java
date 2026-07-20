package com.ufcg.psoft.project.exception;

public class RegraDeTempoInvalidaException extends ProjectException {
    public RegraDeTempoInvalidaException() {
        super("O tempo de abertura deve ser maior que o tempo de fechamento dos palpites.");
    }
}