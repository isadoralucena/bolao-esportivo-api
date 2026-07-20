package com.ufcg.psoft.project.exception;

public class CampeonatoNaoExisteException extends ProjectException {
    public CampeonatoNaoExisteException() {
        super("Esse campeonato não existe!");
    }
}
