package com.ufcg.psoft.project.exception;

public class EmailJaCadastradoException extends ProjectException {
    public EmailJaCadastradoException() {
        super("Já existe outro usuário com esse email cadastrado!");
    }
}