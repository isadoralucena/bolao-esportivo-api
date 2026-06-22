package com.ufcg.psoft.project.exception;

public class RegraPontuacaoNaoExisteException extends ProjectException {
    public RegraPontuacaoNaoExisteException() {
        super("Essa regra de pontuação não existe!");
    }
}
