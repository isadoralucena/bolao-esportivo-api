package com.ufcg.psoft.project.exception;

public class EmailJaCadastradoException extends ProjectException {
    public EmailJaCadastradoException() {
        super("Ja existe outro usuario com esse email cadastrado!");
    }
}