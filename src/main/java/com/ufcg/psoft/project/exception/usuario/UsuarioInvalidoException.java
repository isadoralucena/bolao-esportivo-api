package com.ufcg.psoft.project.exception.usuario;

import com.ufcg.psoft.project.exception.ProjectException;

public class UsuarioInvalidoException extends ProjectException {
    public UsuarioInvalidoException() {
        super("O usuário consultado é inválido para essa operação!");
    }
}
