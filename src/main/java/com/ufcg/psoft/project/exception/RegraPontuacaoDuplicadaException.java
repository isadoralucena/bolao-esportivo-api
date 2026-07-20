package com.ufcg.psoft.project.exception;

public class RegraPontuacaoDuplicadaException extends ProjectException {
    public RegraPontuacaoDuplicadaException() {
        super("Já existe uma regra de pontuação com esse tipo para o grupo!");
    }
}
