package com.ufcg.psoft.project.exception;

public class UsuarioInvalidoException extends ProjectException {
    public UsuarioInvalidoException() {
        super("O usuário consultado é inválido para essa operação!");
    }
}
