package com.ufcg.psoft.project.exception;

public class PermissaoNegadaException extends ProjectException {
    public PermissaoNegadaException() {
        super("Permissão negada para acessar este recurso.");
    }
}
