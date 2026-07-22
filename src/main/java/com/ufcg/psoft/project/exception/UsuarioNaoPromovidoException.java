package com.ufcg.psoft.project.exception;

public class UsuarioNaoPromovidoException extends ProjectException {
    public UsuarioNaoPromovidoException() {
        super("O usuário não foi promovido ao plano Premium!");
    }
}
