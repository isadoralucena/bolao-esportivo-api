package com.ufcg.psoft.project.exception;

public class UsuarioInvalidoException extends RuntimeException {
    public UsuarioInvalidoException() {
        super("O Usuario consultado é inválido para essa operação!");
    }
}
