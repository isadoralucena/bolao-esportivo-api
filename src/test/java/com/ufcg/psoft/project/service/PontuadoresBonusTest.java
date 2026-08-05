package com.ufcg.psoft.project.service;

import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PontuacaoPalpite;
import com.ufcg.psoft.project.model.RegraPontuacao;
import com.ufcg.psoft.project.service.pontuacao.PontuadorBonusMataMata;
import com.ufcg.psoft.project.service.pontuacao.PontuadorBonusRodada;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Testes dos pontuadores de bonus")
class PontuadoresBonusTest {

    private final RegraPontuacao regra = RegraPontuacao.builder().pontos(7).build();

    @ParameterizedTest(name = "empate={0}, placarExato={1}")
    @CsvSource({"true,false", "false,true"})
    @DisplayName("Bonus mata-mata deve reconhecer diferentes tipos de acerto")
    void bonusMataMataDeveReconhecerDiferentesAcertos(boolean acertouEmpate, boolean acertouPlacarExato) {
        PontuacaoPalpite pontuacao = criarPontuacao(true, acertouEmpate, acertouPlacarExato);

        assertEquals(7, new PontuadorBonusMataMata().calcular(pontuacao, regra));
    }

    @ParameterizedTest(name = "empate={0}, placarExato={1}")
    @CsvSource({"true,false", "false,true"})
    @DisplayName("Bonus de rodada deve reconhecer diferentes tipos de acerto")
    void bonusRodadaDeveReconhecerDiferentesAcertos(boolean acertouEmpate, boolean acertouPlacarExato) {
        PontuacaoPalpite pontuacao = criarPontuacao(false, acertouEmpate, acertouPlacarExato);

        assertEquals(7, new PontuadorBonusRodada().calcular(pontuacao, regra));
    }

    private PontuacaoPalpite criarPontuacao(
            boolean mataMata,
            boolean acertouEmpate,
            boolean acertouPlacarExato) {
        Partida partida = Partida.builder().mataMata(mataMata).build();
        Palpite palpite = Palpite.builder().partida(partida).build();
        return PontuacaoPalpite.builder()
                .palpite(palpite)
                .acertouEmpate(acertouEmpate)
                .acertouPlacarExato(acertouPlacarExato)
                .acertouVencedor(false)
                .build();
    }
}
