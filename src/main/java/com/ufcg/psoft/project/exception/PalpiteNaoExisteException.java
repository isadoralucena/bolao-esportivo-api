package com.ufcg.psoft.project.exception;

public class PalpiteNaoExisteException extends ProjectException {
    public PalpiteNaoExisteException() {
        super("O palpite não existe!");
    }
}