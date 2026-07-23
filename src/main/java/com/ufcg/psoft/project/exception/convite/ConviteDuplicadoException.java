package com.ufcg.psoft.project.exception.convite;

import com.ufcg.psoft.project.exception.ProjectException;

public class ConviteDuplicadoException extends ProjectException {
    public ConviteDuplicadoException() {
        super("Já existe um convite pendente para esse usuário nesse grupo!");
    }
}
