package com.ufcg.psoft.project.exception.usuario;

import com.ufcg.psoft.project.exception.ProjectException;

public class EmailJaCadastradoException extends ProjectException {
    public EmailJaCadastradoException() {
        super("Já existe outro usuário com esse email cadastrado!");
    }
}