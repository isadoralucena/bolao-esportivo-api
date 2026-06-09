package com.ufcg.psoft.project.exception;

public class UsuarioJaParticipanteException extends ProjectException {
    public UsuarioJaParticipanteException() {
        super("O usuario já é participante deste grupo!");
    }
}