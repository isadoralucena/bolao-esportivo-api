package com.ufcg.psoft.project.exception.usuario;

import com.ufcg.psoft.project.exception.ProjectException;

public class UsuarioNaoPromovidoException extends ProjectException {
    public UsuarioNaoPromovidoException() {
        super("O usuário não foi promovido ao plano Premium!");
    }
}
