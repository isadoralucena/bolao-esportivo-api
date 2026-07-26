package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.ranking.RankingEntryResponseDTO;
import com.ufcg.psoft.project.dto.ranking.RankingResponseDTO;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Rankings - US13")
public class RankingControllerTest {

    final String URI_RANKING = "/ranking";

    @Autowired
    MockMvc driver;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    CampeonatoRepository campeonatoRepository;

    @Autowired
    GrupoRepository grupoRepository;

    @Autowired
    PartidaRepository partidaRepository;

    @Autowired
    PalpiteRepository palpiteRepository;

    @Autowired
    PontuacaoPalpiteRepository pontuacaoPalpiteRepository;

    @Autowired
    RegraPontuacaoRepository regraPontuacaoRepository;

    @Autowired
    ObjectMapper objectMapper;

    Usuario organizador;
    Usuario participante1;
    Usuario participante2;
    Campeonato campeonato;
    Grupo grupo;

    @BeforeEach
    void setup() {
        driver = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();

        organizador = usuarioRepository.save(Usuario.builder()
                .nome("Organizador")
                .username("organizador")
                .email("organizador@email.com")
                .endereco("Rua A")
                .codigo("111111")
                .build());

        participante1 = usuarioRepository.save(Usuario.builder()
                .nome("Participante 1")
                .username("participante1")
                .email("participante1@email.com")
                .endereco("Rua B")
                .codigo("222222")
                .build());

        participante2 = usuarioRepository.save(Usuario.builder()
                .nome("Participante 2")
                .username("participante2")
                .email("participante2@email.com")
                .endereco("Rua C")
                .codigo("333333")
                .build());

        campeonato = campeonatoRepository.save(Campeonato.builder()
                .nome("Campeonato Teste")
                .url("http://campeonato-teste.com")
                .codigo("CAT001")
                .ativo(true)
                .build());

        grupo = grupoRepository.save(Grupo.builder()
                .nome("Grupo Teste")
                .descricao("Grupo para testes")
                .privacidade(PrivacidadeGrupo.PUBLICA)
                .limiteParticipantes(10)
                .campeonato(campeonato)
                .organizador(organizador)
                .build());

        grupo.getParticipantes().add(organizador);
        grupo.getParticipantes().add(participante1);
        grupo.getParticipantes().add(participante2);
        grupoRepository.save(grupo);
    }

    @AfterEach
    void tearDown() {
        pontuacaoPalpiteRepository.deleteAll();
        palpiteRepository.deleteAll();
        regraPontuacaoRepository.deleteAll();
        partidaRepository.deleteAll();
        grupoRepository.deleteAll();
        campeonatoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private Partida criarPartida(Long codigoExterno) {
        return partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(codigoExterno)
                .mandante("Time A")
                .visitante("Time B")
                .golsMandante(2)
                .golsVisitante(1)
                .data(LocalDateTime.now().minusDays(1))
                .status(PartidaStatus.FINALIZADO)
                .build());
    }

    private Palpite criarPalpite(Partida partida, Usuario usuario, int golsMandante, int golsVisitante) {
        return palpiteRepository.save(Palpite.builder()
                .partida(partida)
                .usuario(usuario)
                .grupo(grupo)
                .golsMandante(golsMandante)
                .golsVisitante(golsVisitante)
                .data(LocalDateTime.now())
                .build());
    }

    private PontuacaoPalpite criarPontuacao(Palpite palpite, int pontos,
            boolean acertouVencedor, boolean acertouEmpate, boolean acertouPlacarExato) {
        return pontuacaoPalpiteRepository.save(PontuacaoPalpite.builder()
                .palpite(palpite)
                .pontuacao(pontos)
                .acertouVencedor(acertouVencedor)
                .acertouEmpate(acertouEmpate)
                .acertouPlacarExato(acertouPlacarExato)
                .build());
    }

    @Nested
    @DisplayName("Ranking do grupo")
    class RankingDoGrupo {

        @Test
        @DisplayName("Quando participante consulta ranking do grupo com dados válidos")
        void quandoParticipanteConsultaRankingDoGrupo() throws Exception {
            String responseJsonString = driver.perform(get(URI_RANKING + "/grupo/" + grupo.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante1.getId().toString())
                            .param("codigoUsuario", participante1.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            RankingResponseDTO resultado = objectMapper.readValue(responseJsonString, RankingResponseDTO.class);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(grupo.getId(), resultado.getGrupoId()),
                    () -> assertNotNull(resultado.getRankingEntrys())
            );
        }

        @Test
        @DisplayName("Quando ranking retorna campos obrigatórios da spec")
        void quandoRankingRetornaCamposObrigatorios() throws Exception {
            Partida partida = criarPartida(10L);
            Palpite p1 = criarPalpite(partida, participante1, 2, 1);
            criarPontuacao(p1, 10, true, false, true);

            String responseJsonString = driver.perform(get(URI_RANKING + "/grupo/" + grupo.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante1.getId().toString())
                            .param("codigoUsuario", participante1.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            RankingResponseDTO resultado = objectMapper.readValue(responseJsonString, RankingResponseDTO.class);

            RankingEntryResponseDTO entry = resultado.getRankingEntrys().stream()
                    .filter(e -> e.getPontuacaoParticipante().getUsuarioId().equals(participante1.getId()))
                    .findFirst()
                    .orElseThrow();

            assertAll(
                    () -> assertTrue(entry.getPosicao() > 0),
                    () -> assertNotNull(entry.getPontuacaoParticipante().getPontuacao()),
                    () -> assertNotNull(entry.getPontuacaoParticipante().getAcertosVencedor()),
                    () -> assertNotNull(entry.getPontuacaoParticipante().getAcertosEmpate()),
                    () -> assertNotNull(entry.getPontuacaoParticipante().getTotalPalpitesAvaliados()),
                    () -> assertNotNull(entry.getPontuacaoParticipante().getPlacaresExatos())
            );
        }

        @Test
        @DisplayName("Quando ranking do grupo tem múltiplos participantes retorna todos")
        void quandoRankingDoGrupoRetornaTodosParticipantes() throws Exception {
            String responseJsonString = driver.perform(get(URI_RANKING + "/grupo/" + grupo.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante1.getId().toString())
                            .param("codigoUsuario", participante1.getCodigo()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            RankingResponseDTO resultado = objectMapper.readValue(responseJsonString, RankingResponseDTO.class);

            assertEquals(3, resultado.getRankingEntrys().size());
        }

        @Test
        @DisplayName("Quando participante com mais pontos aparece primeiro no ranking")
        void quandoParticipanteComMaisPontosApareiroPrimeiro() throws Exception {
            Partida partida = criarPartida(20L);
            Palpite p1 = criarPalpite(partida, participante1, 2, 1);
            criarPontuacao(p1, 10, true, false, true);
            Palpite p2 = criarPalpite(partida, participante2, 1, 0);
            criarPontuacao(p2, 3, true, false, false);

            String responseJsonString = driver.perform(get(URI_RANKING + "/grupo/" + grupo.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante1.getId().toString())
                            .param("codigoUsuario", participante1.getCodigo()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            RankingResponseDTO resultado = objectMapper.readValue(responseJsonString, RankingResponseDTO.class);
            List<RankingEntryResponseDTO> entries = resultado.getRankingEntrys();

            assertEquals(1, entries.get(0).getPosicao());
            assertEquals(participante1.getId(),
                    entries.get(0).getPontuacaoParticipante().getUsuarioId());
        }

        @Test
        @DisplayName("Quando dois participantes têm mesma pontuação ficam na mesma posição")
        void quandoDoisParticipantesComMesmaPontuacaoFicamNaMesmaPosicao() throws Exception {
            Partida partida = criarPartida(30L);
            Palpite p1 = criarPalpite(partida, participante1, 2, 1);
            criarPontuacao(p1, 5, true, false, false);
            Palpite p2 = criarPalpite(partida, participante2, 2, 1);
            criarPontuacao(p2, 5, true, false, false);

            String responseJsonString = driver.perform(get(URI_RANKING + "/grupo/" + grupo.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante1.getId().toString())
                            .param("codigoUsuario", participante1.getCodigo()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            RankingResponseDTO resultado = objectMapper.readValue(responseJsonString, RankingResponseDTO.class);

            long empatados = resultado.getRankingEntrys().stream()
                    .filter(e -> e.getPosicao() == 1 &&
                            e.getPontuacaoParticipante().getPontuacao() == 5)
                    .count();

            assertTrue(empatados >= 2);
        }

        @Test
        @DisplayName("Quando usuário não participante tenta consultar ranking do grupo")
        void quandoNaoParticipanteTentaConsultarRanking() throws Exception {
            Usuario forasteiro = usuarioRepository.save(Usuario.builder()
                    .nome("Forasteiro")
                    .username("forasteiro")
                    .email("forasteiro@email.com")
                    .endereco("Rua D")
                    .codigo("444444")
                    .build());

            driver.perform(get(URI_RANKING + "/grupo/" + grupo.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", forasteiro.getId().toString())
                            .param("codigoUsuario", forasteiro.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Quando consulta ranking de grupo inexistente")
        void quandoConsultaRankingDeGrupoInexistente() throws Exception {
            driver.perform(get(URI_RANKING + "/grupo/999999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante1.getId().toString())
                            .param("codigoUsuario", participante1.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Quando consulta ranking com código de acesso inválido")
        void quandoConsultaRankingComCodigoInvalido() throws Exception {
            driver.perform(get(URI_RANKING + "/grupo/" + grupo.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante1.getId().toString())
                            .param("codigoUsuario", "999999"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("Ranking global")
    class RankingGlobal {

        @Test
        @DisplayName("Quando usuário válido consulta ranking global")
        void quandoUsuarioValidoConsultaRankingGlobal() throws Exception {
            String responseJsonString = driver.perform(get(URI_RANKING)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante1.getId().toString())
                            .param("codigoUsuario", participante1.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            RankingResponseDTO resultado = objectMapper.readValue(responseJsonString, RankingResponseDTO.class);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertNull(resultado.getGrupoId()),
                    () -> assertNotNull(resultado.getRankingEntrys())
            );
        }

        @Test
        @DisplayName("Quando ranking global retorna campos obrigatórios da spec")
        void quandoRankingGlobalRetornaCamposObrigatorios() throws Exception {
            Partida partida = criarPartida(40L);
            Palpite p1 = criarPalpite(partida, participante1, 2, 1);
            criarPontuacao(p1, 10, true, false, true);

            String responseJsonString = driver.perform(get(URI_RANKING)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante1.getId().toString())
                            .param("codigoUsuario", participante1.getCodigo()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            RankingResponseDTO resultado = objectMapper.readValue(responseJsonString, RankingResponseDTO.class);

            RankingEntryResponseDTO entry = resultado.getRankingEntrys().stream()
                    .filter(e -> e.getPontuacaoParticipante().getUsuarioId().equals(participante1.getId()))
                    .findFirst()
                    .orElseThrow();

            assertAll(
                    () -> assertTrue(entry.getPosicao() > 0),
                    () -> assertNotNull(entry.getPontuacaoParticipante().getPontuacao()),
                    () -> assertNotNull(entry.getPontuacaoParticipante().getAcertosVencedor()),
                    () -> assertNotNull(entry.getPontuacaoParticipante().getAcertosEmpate()),
                    () -> assertNotNull(entry.getPontuacaoParticipante().getPlacaresExatos())
            );
        }

        @Test
        @DisplayName("Quando consulta ranking global com código de acesso inválido")
        void quandoConsultaRankingGlobalComCodigoInvalido() throws Exception {
            driver.perform(get(URI_RANKING)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante1.getId().toString())
                            .param("codigoUsuario", "999999"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Quando consulta ranking global com usuário inexistente")
        void quandoConsultaRankingGlobalComUsuarioInexistente() throws Exception {
            driver.perform(get(URI_RANKING)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", "999999")
                            .param("codigoUsuario", "999999"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}