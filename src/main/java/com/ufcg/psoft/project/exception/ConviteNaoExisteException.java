package com.ufcg.psoft.project.exception;

public class ConviteNaoExisteException extends ProjectException {
    public ConviteNaoExisteException() {
        super("O convite não existe");
    }
}
