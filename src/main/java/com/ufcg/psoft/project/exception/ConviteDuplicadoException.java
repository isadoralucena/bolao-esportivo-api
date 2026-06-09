package com.ufcg.psoft.project.exception;

public class ConviteDuplicadoException extends ProjectException {
    public ConviteDuplicadoException() {
        super("O convidado já participa desse grupo");
    }
}
