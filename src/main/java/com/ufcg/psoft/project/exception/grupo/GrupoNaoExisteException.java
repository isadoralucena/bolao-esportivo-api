package com.ufcg.psoft.project.exception.grupo;

import com.ufcg.psoft.project.exception.ProjectException;

public class GrupoNaoExisteException extends ProjectException {
    public GrupoNaoExisteException() {
        super("Esse grupo não existe!");
    }
}
