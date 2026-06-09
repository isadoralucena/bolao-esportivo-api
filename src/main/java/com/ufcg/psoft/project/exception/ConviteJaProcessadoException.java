package com.ufcg.psoft.project.exception;

public class ConviteJaProcessadoException extends ProjectException {
    public ConviteJaProcessadoException() {
        super("O convite já foi processado e não pode ser modificado");
    }
}
