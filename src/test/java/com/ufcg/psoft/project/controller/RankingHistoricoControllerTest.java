package com.ufcg.psoft.project.controller;

import static com.ufcg.psoft.project.config.TestClockConfig.FIXED_CLOCK;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.ranking.HistoricoRankingResponseDTO;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.*;
import org.junit.jupiter.api.*;
import lombok.RequiredArgsConstructor;
import org.springframework.test.context.TestConstructor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Historico de Rankings - US18")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RankingHistoricoControllerTest {

    final String URI_HISTORICO = "/grupos/{grupoId}/ranking/historico";

    MockMvc driver;
    final WebApplicationContext webApplicationContext;
    final UsuarioRepository usuarioRepository;
    final CampeonatoRepository campeonatoRepository;
    final GrupoRepository grupoRepository;
    final PartidaRepository partidaRepository;
    final RankingSnapshotRepository rankingSnapshotRepository;
    final ObjectMapper objectMapper;

    Usuario usuario;
    Campeonato campeonato;
    Grupo grupo;
    Partida partida;

    @BeforeEach
    void setup() {
        driver = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();

        usuario = usuarioRepository.save(Usuario.builder()
                .nome("Usuario Teste")
                .username("teste")
                .email("teste@email.com")
                .endereco("Rua A")
                .codigo("111111")
                .perfil(PerfilUsuario.PADRAO)
                .build());

        campeonato = campeonatoRepository.save(Campeonato.builder()
                .nome("Campeonato Teste")
                .url("http://campeonato.com")
                .codigo("CAT001")
                .ativo(true)
                .build());

        grupo = grupoRepository.save(Grupo.builder()
                .nome("Grupo Teste")
                .descricao("Grupo para testes")
                .privacidade(PrivacidadeGrupo.PUBLICA)
                .campeonato(campeonato)
                .organizador(usuario)
                .build());

        partida = partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(1L)
                .mandante("Time A")
                .visitante("Time B")
                .golsMandante(2)
                .golsVisitante(1)
                .data(LocalDateTime.now(FIXED_CLOCK).minusDays(1))
                .status(PartidaStatus.FINALIZADO)
                .consolidada(true)
                .build());
    }

    @AfterEach
    void tearDown() {
        rankingSnapshotRepository.deleteAll();
        partidaRepository.deleteAll();
        grupoRepository.deleteAll();
        campeonatoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /historico")
    class ObterHistorico {

        @Test
        @DisplayName("Deve retornar historico vazio quando nao ha snapshots")
        void deveRetornarHistoricoVazio() throws Exception {
            String responseJsonString = driver.perform(get(URI_HISTORICO, grupo.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            HistoricoRankingResponseDTO resultado = objectMapper.readValue(responseJsonString, HistoricoRankingResponseDTO.class);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(grupo.getId(), resultado.getGrupoId()),
                    () -> assertTrue(resultado.getSnapshots().isEmpty())
            );
        }

        @Test
        @DisplayName("Deve retornar historico com snapshots quando existem")
        void deveRetornarHistoricoComSnapshots() throws Exception {
            rankingSnapshotRepository.save(RankingSnapshot.builder()
                    .grupo(grupo)
                    .usuario(usuario)
                    .partida(partida)
                    .posicao(1)
                    .pontuacao(10)
                    .dataSnapshot(LocalDateTime.now(FIXED_CLOCK))
                    .build());

            String responseJsonString = driver.perform(get(URI_HISTORICO, grupo.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            HistoricoRankingResponseDTO resultado = objectMapper.readValue(responseJsonString, HistoricoRankingResponseDTO.class);

            assertAll(
                    () -> assertEquals(1, resultado.getSnapshots().size()),
                    () -> assertEquals(1, resultado.getSnapshots().get(0).getPosicao()),
                    () -> assertEquals(10, resultado.getSnapshots().get(0).getPontuacao())
            );
        }

        @Test
        @DisplayName("Deve retornar 400 quando grupo nao existe")
        void deveRetornar400QuandoGrupoNaoExiste() throws Exception {
            driver.perform(get(URI_HISTORICO, 999999L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("GET /historico/{usuarioId}")
    class ObterHistoricoPorParticipante {

        @Test
        @DisplayName("Deve retornar historico do participante")
        void deveRetornarHistoricoDoParticipante() throws Exception {
            rankingSnapshotRepository.save(RankingSnapshot.builder()
                    .grupo(grupo)
                    .usuario(usuario)
                    .partida(partida)
                    .posicao(1)
                    .pontuacao(10)
                    .dataSnapshot(LocalDateTime.now(FIXED_CLOCK))
                    .build());

            String responseJsonString = driver.perform(get(URI_HISTORICO + "/{usuarioId}", grupo.getId(), usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            HistoricoRankingResponseDTO resultado = objectMapper.readValue(responseJsonString, HistoricoRankingResponseDTO.class);

            assertAll(
                    () -> assertEquals(1, resultado.getSnapshots().size()),
                    () -> assertEquals(usuario.getId(), resultado.getSnapshots().get(0).getUsuarioId())
            );
        }

        @Test
        @DisplayName("Deve retornar historico vazio para participante sem snapshots")
        void deveRetornarVazioParaParticipanteSemSnapshots() throws Exception {
            String responseJsonString = driver.perform(get(URI_HISTORICO + "/{usuarioId}", grupo.getId(), usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            HistoricoRankingResponseDTO resultado = objectMapper.readValue(responseJsonString, HistoricoRankingResponseDTO.class);
            assertTrue(resultado.getSnapshots().isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /historico/lideres")
    class ObterLideresHistoricos {

        @Test
        @DisplayName("Deve retornar lideres historicos")
        void deveRetornarLideresHistoricos() throws Exception {
            rankingSnapshotRepository.save(RankingSnapshot.builder()
                    .grupo(grupo)
                    .usuario(usuario)
                    .partida(partida)
                    .posicao(1)
                    .pontuacao(10)
                    .dataSnapshot(LocalDateTime.now(FIXED_CLOCK))
                    .build());

            String responseJsonString = driver.perform(get(URI_HISTORICO + "/lideres", grupo.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            assertNotNull(responseJsonString);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando nao ha lideres")
        void deveRetornarListaVaziaQuandoSemLideres() throws Exception {
            String responseJsonString = driver.perform(get(URI_HISTORICO + "/lideres", grupo.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            assertTrue(objectMapper.readTree(responseJsonString).isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /historico/recente")
    class ObterDesempenhoRecente {

        @Test
        @DisplayName("Deve retornar desempenho recente")
        void deveRetornarDesempenhoRecente() throws Exception {
            rankingSnapshotRepository.save(RankingSnapshot.builder()
                    .grupo(grupo)
                    .usuario(usuario)
                    .partida(partida)
                    .posicao(1)
                    .pontuacao(10)
                    .dataSnapshot(LocalDateTime.now(FIXED_CLOCK))
                    .build());

            String responseJsonString = driver.perform(get(URI_HISTORICO + "/recente", grupo.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            assertNotNull(responseJsonString);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando nao ha snapshots recentes")
        void deveRetornarListaVaziaQuandoSemSnapshots() throws Exception {
            String responseJsonString = driver.perform(get(URI_HISTORICO + "/recente", grupo.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            assertTrue(objectMapper.readTree(responseJsonString).isEmpty());
        }
    }
}
