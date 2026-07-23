package com.ufcg.psoft.project.exception.palpite;

import com.ufcg.psoft.project.exception.ProjectException;

public class PalpiteJaExisteException extends ProjectException {
    public PalpiteJaExisteException() {
        super("Você já possui um palpite para essa partida nesse grupo!");
    }
}
