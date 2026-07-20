package com.ufcg.psoft.project.exception;

public class GrupoNaoExisteException extends ProjectException {
    public GrupoNaoExisteException() {
        super("Esse grupo não existe!");
    }
}
