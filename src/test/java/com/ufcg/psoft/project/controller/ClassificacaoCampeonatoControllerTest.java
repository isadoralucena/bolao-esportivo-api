package com.ufcg.psoft.project.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import lombok.RequiredArgsConstructor;
import org.springframework.test.context.TestConstructor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.ClassificacaoCampeonato;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.ClassificacaoCampeonatoRepository;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de classificação de campeonato")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ClassificacaoCampeonatoControllerTest {

    final MockMvc mockMvc;

    final CampeonatoRepository campeonatoRepository;

    final ClassificacaoCampeonatoRepository classificacaoCampeonatoRepository;

    private Campeonato campeonato;

    @BeforeEach
    void setUp() {
        campeonato = campeonatoRepository.save(Campeonato.builder()
                .nome("Campeonato Brasileiro")
                .url("https://api.football-data.org/v4/competitions/2013")
                .codigo("BSA")
                .ativo(true)
                .build());
    }

    @AfterEach
    void clean() {
        classificacaoCampeonatoRepository.deleteAll();
        campeonatoRepository.deleteAll();
    }

    @Test
    @DisplayName("Listar classificação de campeonato retorna classificações ordenadas por posição")
    void listarClassificacaoRetornaClassificacoesOrdenadasPorPosicao() throws Exception {
        classificacaoCampeonatoRepository.save(ClassificacaoCampeonato.builder()
                .campeonato(campeonato)
                .posicao(2)
                .nomeTime("Time B")
                .build());

        classificacaoCampeonatoRepository.save(ClassificacaoCampeonato.builder()
                .campeonato(campeonato)
                .posicao(1)
                .nomeTime("Time A")
                .build());

        mockMvc.perform(get("/campeonatos/{campeonatoId}/classificacao", campeonato.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].campeonatoId").value(campeonato.getId()))
                .andExpect(jsonPath("$[0].posicao").value(1))
                .andExpect(jsonPath("$[0].nomeTime").value("Time A"))
                .andExpect(jsonPath("$[1].campeonatoId").value(campeonato.getId()))
                .andExpect(jsonPath("$[1].posicao").value(2))
                .andExpect(jsonPath("$[1].nomeTime").value("Time B"));
    }

    @Test
    @DisplayName("Listar classificação de campeonato sem classificação retorna array vazio")
    void listarClassificacaoVaziaRetornaArrayVazio() throws Exception {
        mockMvc.perform(get("/campeonatos/{campeonatoId}/classificacao", campeonato.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Listar classificação de campeonato inexistente retorna 400")
    void listarClassificacaoDeCampeonatoInexistenteRetorna400() throws Exception {
        mockMvc.perform(get("/campeonatos/{campeonatoId}/classificacao", 999L))
                .andExpect(status().isBadRequest());
    }
}
