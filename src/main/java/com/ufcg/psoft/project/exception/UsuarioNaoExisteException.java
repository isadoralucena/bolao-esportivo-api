package com.ufcg.psoft.project.exception;

public class UsuarioNaoExisteException extends ProjectException {
    public UsuarioNaoExisteException() {
        super("O usuário consultado não existe!");
    }
}
