package com.ufcg.psoft.project.exception;

public class UsuarioJaParticipanteException extends ProjectException {
    public UsuarioJaParticipanteException() {
        super("O usuário já é participante deste grupo!");
    }
}