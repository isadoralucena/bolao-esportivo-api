package com.ufcg.psoft.project.exception.usuario;

import com.ufcg.psoft.project.exception.ProjectException;

public class UsuarioNaoPremiumException extends ProjectException {
    public UsuarioNaoPremiumException() {
        super("Acesso restrito a usuários Premium!");
    }
}