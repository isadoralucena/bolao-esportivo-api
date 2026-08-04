package com.ufcg.psoft.project.exception;

public class ConviteDuplicadoException extends ProjectException {
    public ConviteDuplicadoException() {
        super("Já existe um convite pendente para esse usuário nesse grupo!");
    }
}
