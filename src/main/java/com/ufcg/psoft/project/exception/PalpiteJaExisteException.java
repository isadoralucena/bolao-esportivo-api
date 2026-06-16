package com.ufcg.psoft.project.exception;

public class PalpiteJaExisteException extends ProjectException {
    public PalpiteJaExisteException() {
        super("Você já possui um palpite para essa partida nesse grupo!");
    }
}
