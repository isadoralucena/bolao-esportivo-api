package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.exception.CustomErrorType;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Recomendacoes - US20")
public class RecomendacaoControllerTest {

    final String URI_RECOMENDACAO = "/grupos/{grupoId}/partidas/{partidaId}/recomendacao";

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
    ObjectMapper objectMapper;

    Usuario usuarioPremium;
    Usuario usuarioPadrao;
    Campeonato campeonato;
    Campeonato outroCampeonato;
    Grupo grupo;
    Partida partida;

    @BeforeEach
    void setup() {
        driver = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();

        usuarioPremium = usuarioRepository.save(Usuario.builder()
                .nome("Usuario Premium")
                .username("premium")
                .email("premium@email.com")
                .endereco("Rua A")
                .codigo("111111")
                .perfil(PerfilUsuario.PREMIUM)
                .build());

        usuarioPadrao = usuarioRepository.save(Usuario.builder()
                .nome("Usuario Padrao")
                .username("padrao")
                .email("padrao@email.com")
                .endereco("Rua B")
                .codigo("222222")
                .perfil(PerfilUsuario.PADRAO)
                .build());

        campeonato = campeonatoRepository.save(Campeonato.builder()
                .nome("Campeonato Teste")
                .url("http://campeonato-teste.com")
                .codigo("CAT001")
                .ativo(true)
                .build());

        outroCampeonato = campeonatoRepository.save(Campeonato.builder()
                .nome("Outro Campeonato")
                .url("http://outro-campeonato.com")
                .codigo("CAT002")
                .ativo(true)
                .build());

        grupo = grupoRepository.save(Grupo.builder()
                .nome("Grupo Teste")
                .descricao("Grupo para testes")
                .privacidade(PrivacidadeGrupo.PUBLICA)
                .campeonato(campeonato)
                .organizador(usuarioPremium)
                .build());

        partida = partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(1L)
                .mandante("Time A")
                .visitante("Time B")
                .data(LocalDateTime.now().plusDays(1))
                .status(PartidaStatus.ABERTO)
                .build());
    }

    @AfterEach
    void tearDown() {
        partidaRepository.deleteAll();
        grupoRepository.deleteAll();
        campeonatoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("Cenarios de sucesso")
    class CenariosDeSuccesso {

        @Test
        @DisplayName("Quando usuario Premium solicita recomendacao com estrategia PLACAR_FREQUENTE")
        void quandoPremiumSolicitaRecomendacaoPlacarFrequente() throws Exception {
            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigo", usuarioPremium.getCodigo())
                            .param("estrategia", "PLACAR_FREQUENTE"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            RecomendacaoResponseDTO resultado = objectMapper.readValue(responseJsonString, RecomendacaoResponseDTO.class);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals("PLACAR_FREQUENTE", resultado.getEstrategia()),
                    () -> assertNotNull(resultado.getGolsMandanteRecomendado()),
                    () -> assertNotNull(resultado.getGolsVisitanteRecomendado())
            );
        }

        @Test
        @DisplayName("Quando usuario Premium solicita recomendacao com estrategia MEDIA_GOLS")
        void quandoPremiumSolicitaRecomendacaoMediaGols() throws Exception {
            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigo", usuarioPremium.getCodigo())
                            .param("estrategia", "MEDIA_GOLS"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            RecomendacaoResponseDTO resultado = objectMapper.readValue(responseJsonString, RecomendacaoResponseDTO.class);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals("MEDIA_GOLS", resultado.getEstrategia()),
                    () -> assertNotNull(resultado.getGolsMandanteRecomendado()),
                    () -> assertNotNull(resultado.getGolsVisitanteRecomendado())
            );
        }

        @Test
        @DisplayName("Quando nao ha historico retorna 0x0 com temHistorico false")
        void quandoSemHistoricoRetorna0x0() throws Exception {
            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigo", usuarioPremium.getCodigo())
                            .param("estrategia", "PLACAR_FREQUENTE"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            RecomendacaoResponseDTO resultado = objectMapper.readValue(responseJsonString, RecomendacaoResponseDTO.class);

            assertAll(
                    () -> assertEquals(0, resultado.getGolsMandanteRecomendado()),
                    () -> assertEquals(0, resultado.getGolsVisitanteRecomendado()),
                    () -> assertFalse(resultado.isTemHistorico())
            );
        }

        @Test
        @DisplayName("Quando estrategia nao informada usa PLACAR_FREQUENTE como default")
        void quandoEstrategiaNaoInformadaUsaDefault() throws Exception {
            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigo", usuarioPremium.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            RecomendacaoResponseDTO resultado = objectMapper.readValue(responseJsonString, RecomendacaoResponseDTO.class);

            assertEquals("PLACAR_FREQUENTE", resultado.getEstrategia());
        }

        @Test
        @DisplayName("Quando ha historico retorna recomendacao com temHistorico true")
        void quandoComHistoricoRetornaTemHistoricoTrue() throws Exception {
            partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(99L)
                    .mandante("Time X")
                    .visitante("Time Y")
                    .golsMandante(2)
                    .golsVisitante(1)
                    .data(LocalDateTime.now().minusDays(1))
                    .status(PartidaStatus.FINALIZADO)
                    .consolidada(true)
                    .build());

            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigo", usuarioPremium.getCodigo())
                            .param("estrategia", "PLACAR_FREQUENTE"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            RecomendacaoResponseDTO resultado = objectMapper.readValue(responseJsonString, RecomendacaoResponseDTO.class);

            assertAll(
                    () -> assertTrue(resultado.isTemHistorico()),
                    () -> assertEquals(2, resultado.getGolsMandanteRecomendado()),
                    () -> assertEquals(1, resultado.getGolsVisitanteRecomendado())
            );
        }
    }

    @Nested
    @DisplayName("Validacao de usuario")
    class ValidacaoDeUsuario {

        @Test
        @DisplayName("Quando usuario nao existe retorna 400")
        void quandoUsuarioNaoExiste() throws Exception {
            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", "999999")
                            .param("codigo", "111111"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertNotNull(resultado.getMessage());
        }

        @Test
        @DisplayName("Quando codigo de acesso e invalido retorna 400")
        void quandoCodigoInvalido() throws Exception {
            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigo", "ERRADO"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertNotNull(resultado.getMessage());
        }

        @Test
        @DisplayName("Quando usuario nao e Premium retorna 400")
        void quandoUsuarioNaoPremium() throws Exception {
            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPadrao.getId().toString())
                            .param("codigo", usuarioPadrao.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertEquals("Acesso restrito a usuários Premium!", resultado.getMessage());
        }
    }

    @Nested
    @DisplayName("Validacao de partida e grupo")
    class ValidacaoDePartidaEGrupo {

        @Test
        @DisplayName("Quando partida nao existe retorna 400")
        void quandoPartidaNaoExiste() throws Exception {
            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), 999999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigo", usuarioPremium.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertNotNull(resultado.getMessage());
        }

        @Test
        @DisplayName("Quando grupo nao existe retorna 400")
        void quandoGrupoNaoExiste() throws Exception {
            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, 999999L, partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigo", usuarioPremium.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertNotNull(resultado.getMessage());
        }

        @Test
        @DisplayName("Quando partida nao pertence ao campeonato do grupo retorna 400")
        void quandoPartidaNaoPertenceAoCampeonatoDoGrupo() throws Exception {
            Partida partidaOutroCampeonato = partidaRepository.save(Partida.builder()
                    .campeonato(outroCampeonato)
                    .codigoExterno(2L)
                    .mandante("Time C")
                    .visitante("Time D")
                    .data(LocalDateTime.now().plusDays(1))
                    .status(PartidaStatus.ABERTO)
                    .build());

            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partidaOutroCampeonato.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigo", usuarioPremium.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertNotNull(resultado.getMessage());
        }
    }

    @Nested
    @DisplayName("Validacao de estrategia")
    class ValidacaoDeEstrategia {

        @Test
        @DisplayName("Quando estrategia invalida retorna 400")
        void quandoEstrategiaInvalida() throws Exception {
            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigo", usuarioPremium.getCodigo())
                            .param("estrategia", "ESTRATEGIA_INEXISTENTE"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertEquals("Estratégia de recomendação inválida ou não encontrada!", resultado.getMessage());
        }
    }
}