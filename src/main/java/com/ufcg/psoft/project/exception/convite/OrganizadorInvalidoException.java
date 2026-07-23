package com.ufcg.psoft.project.exception.convite;

import com.ufcg.psoft.project.exception.ProjectException;

public class OrganizadorInvalidoException extends ProjectException {
    public OrganizadorInvalidoException() {
        super("O organizador do grupo é inválido!");
    }
    
}
