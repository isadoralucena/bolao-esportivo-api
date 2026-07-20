package com.ufcg.psoft.project.exception.palpite;

import com.ufcg.psoft.project.exception.ProjectException;

public class PalpiteNaoExisteException extends ProjectException {
    public PalpiteNaoExisteException() {
        super("O palpite não existe!");
    }
}