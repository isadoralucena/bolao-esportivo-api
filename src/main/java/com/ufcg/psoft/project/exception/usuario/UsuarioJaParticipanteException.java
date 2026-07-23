package com.ufcg.psoft.project.exception.usuario;

import com.ufcg.psoft.project.exception.ProjectException;

public class UsuarioJaParticipanteException extends ProjectException {
    public UsuarioJaParticipanteException() {
        super("O usuário já é participante deste grupo!");
    }
}