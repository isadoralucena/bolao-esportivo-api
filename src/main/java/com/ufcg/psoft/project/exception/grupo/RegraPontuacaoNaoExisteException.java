package com.ufcg.psoft.project.exception.grupo;

import com.ufcg.psoft.project.exception.ProjectException;

public class RegraPontuacaoNaoExisteException extends ProjectException {
    public RegraPontuacaoNaoExisteException() {
        super("Essa regra de pontuação não existe!");
    }
}
