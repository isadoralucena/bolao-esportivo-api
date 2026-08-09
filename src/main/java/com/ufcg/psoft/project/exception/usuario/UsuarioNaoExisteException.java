package com.ufcg.psoft.project.exception.usuario;

import com.ufcg.psoft.project.exception.ProjectException;

public class UsuarioNaoExisteException extends ProjectException {
    public UsuarioNaoExisteException() {
        super("O usuário consultado não existe!");
    }
}
