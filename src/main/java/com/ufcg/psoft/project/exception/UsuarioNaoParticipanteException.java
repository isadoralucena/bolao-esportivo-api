package com.ufcg.psoft.project.exception;

public class UsuarioNaoParticipanteException extends ProjectException {
    public UsuarioNaoParticipanteException() {
        super("Você não é participante desse grupo!");
    }
}
