package com.ufcg.psoft.project.exception.campeonato;

import com.ufcg.psoft.project.exception.ProjectException;

public class CampeonatoNaoExisteException extends ProjectException {
    public CampeonatoNaoExisteException() {
        super("Esse campeonato não existe!");
    }
}
