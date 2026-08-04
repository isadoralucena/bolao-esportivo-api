package com.ufcg.psoft.project.exception;

public class CriteriosDesempateInvalidosException extends ProjectException {
    public CriteriosDesempateInvalidosException() {
        super("Os critérios de desempate devem conter ao menos 1 critério válido, sem repetição.");
    }
}