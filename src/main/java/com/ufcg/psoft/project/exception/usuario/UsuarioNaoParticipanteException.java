package com.ufcg.psoft.project.exception.usuario;

import com.ufcg.psoft.project.exception.ProjectException;

public class UsuarioNaoParticipanteException extends ProjectException {
    public UsuarioNaoParticipanteException() {
        super("Você não é participante desse grupo!");
    }
}
