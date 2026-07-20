package com.ufcg.psoft.project.exception.grupo;

import com.ufcg.psoft.project.exception.ProjectException;

public class RegraPontuacaoDuplicadaException extends ProjectException {
    public RegraPontuacaoDuplicadaException() {
        super("Já existe uma regra de pontuação com esse tipo para o grupo!");
    }
}
