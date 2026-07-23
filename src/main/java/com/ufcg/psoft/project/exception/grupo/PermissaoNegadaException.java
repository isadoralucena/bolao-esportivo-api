package com.ufcg.psoft.project.exception.grupo;

import com.ufcg.psoft.project.exception.ProjectException;

public class PermissaoNegadaException extends ProjectException {
    public PermissaoNegadaException() {
        super("Permissão negada para acessar este recurso.");
    }
}
