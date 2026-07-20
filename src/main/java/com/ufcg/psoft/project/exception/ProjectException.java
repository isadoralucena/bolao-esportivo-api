package com.ufcg.psoft.project.exception;

public class ProjectException extends RuntimeException {
    public ProjectException() {
        super("Erro inesperado no PSoft League!");
    }

    public ProjectException(String message) {
        super(message);
    }
}
