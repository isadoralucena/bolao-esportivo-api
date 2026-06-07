package com.ufcg.psoft.project.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import com.ufcg.psoft.project.dto.CampeonatoPostPutRequestDTO;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.repository.CampeonatoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Campeonatos")
public class CampeonatoControllerTests {
    final String URI_CAMPEONATOS = "/campeonatos";

    @Autowired
    MockMvc driver;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    CampeonatoRepository campeonatoRepository;

    Campeonato campeonato;
    CampeonatoPostPutRequestDTO campeonatoPostPutRequestDTO;

    @BeforeEach
    void setup() {
        campeonato = Campeonato.builder()
                .nome("Campeonato Brasileiro")
                .url("https://api.football-data.org/v4/competitions/2013")
                .codigo("BSA")
                .ativo(false)
                .build();
        campeonatoPostPutRequestDTO = CampeonatoPostPutRequestDTO.builder()
                .nome("Campeonato Brasileiro")
                .url("https://api.football-data.org/v4/competitions/2013")
                .codigo("BSA")
                .build();
    }

    @AfterEach
    void tearDown() {
        campeonatoRepository.deleteAll();
    }

    @Test
    @DisplayName("Quando listar todos os campeonatos salvos")
    void quandoListarCampeonatos() throws Exception {
        campeonatoRepository.save(campeonato);

        driver.perform(get(URI_CAMPEONATOS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Campeonato Brasileiro"));
    }

    @Test
    @DisplayName("Quando buscar um campeonato específico pelo nome")
    void quandoBuscarCampeonatoPorNome() throws Exception {
        campeonatoRepository.save(campeonato);

        driver.perform(get(URI_CAMPEONATOS + "/buscar")
                .param("nome", "Brasileiro")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Campeonato Brasileiro"));
    }

    @Test
    @DisplayName("Quando tentar criar campeonato com código de admin inválido")
    void quandoCriarComAdminInvalido() throws Exception {
        CampeonatoPostPutRequestDTO dto = CampeonatoPostPutRequestDTO.builder()
                .nome("Copa do Nordeste")
                .url("http://api.com")
                .codigo("BSA")
                .build();

        driver.perform(post(URI_CAMPEONATOS)
                .param("codigoAdmin", "CODIGO_ERRADO_999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
