package com.ufcg.psoft.project.controller;

import static com.ufcg.psoft.project.config.TestClockConfig.FIXED_CLOCK;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.exception.CustomErrorType;
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
@DisplayName("Testes do controlador de Recomendacoes - US20")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RecomendacaoControllerTest {

    final String URI_RECOMENDACAO = "/grupos/{grupoId}/partidas/{partidaId}/recomendacao";

    MockMvc driver;
    final WebApplicationContext webApplicationContext;
    final UsuarioRepository usuarioRepository;
    final CampeonatoRepository campeonatoRepository;
    final GrupoRepository grupoRepository;
    final PartidaRepository partidaRepository;
    final ObjectMapper objectMapper;

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
                .nome("Usuario Premium").username("premium")
                .email("premium@email.com").endereco("Rua A")
                .codigo("111111").perfil(PerfilUsuario.PREMIUM).build());

        usuarioPadrao = usuarioRepository.save(Usuario.builder()
                .nome("Usuario Padrao").username("padrao")
                .email("padrao@email.com").endereco("Rua B")
                .codigo("222222").perfil(PerfilUsuario.PADRAO).build());

        campeonato = campeonatoRepository.save(Campeonato.builder()
                .nome("Campeonato Teste").url("http://campeonato-teste.com")
                .codigo("CAT001").ativo(true).build());

        outroCampeonato = campeonatoRepository.save(Campeonato.builder()
                .nome("Outro Campeonato").url("http://outro-campeonato.com")
                .codigo("CAT002").ativo(true).build());

        grupo = grupoRepository.save(Grupo.builder()
                .nome("Grupo Teste").descricao("Grupo para testes")
                .privacidade(PrivacidadeGrupo.PUBLICA)
                .campeonato(campeonato).organizador(usuarioPremium).build());

        partida = partidaRepository.save(Partida.builder()
                .campeonato(campeonato).codigoExterno(1L)
                .mandante("Time A").visitante("Time B")
                .data(LocalDateTime.now(FIXED_CLOCK).plusDays(1))
                .status(PartidaStatus.ABERTO).build());
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
        @DisplayName("Quando usuario Premium solicita recomendacao")
        void quandoPremiumSolicitaRecomendacao() throws Exception {
            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigoUsuario", usuarioPremium.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            RecomendacaoResponseDTO resultado = objectMapper.readValue(responseJsonString, RecomendacaoResponseDTO.class);

            assertAll(
                () -> assertNotNull(resultado),
                () -> assertNotNull(resultado.getMensagem()),
                () -> assertEquals(partida.getId(), resultado.getPartidaId()),
                () -> assertEquals("Time A", resultado.getMandante()),
                () -> assertEquals("Time B", resultado.getVisitante()),
                () -> assertFalse(resultado.isTemRecomendacao())
        );
        }

        @Test
        @DisplayName("Quando nao ha historico retorna sem recomendacao")
        void quandoSemHistoricoRetornaFallback() throws Exception {
            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigoUsuario", usuarioPremium.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            RecomendacaoResponseDTO resultado = objectMapper.readValue(responseJsonString, RecomendacaoResponseDTO.class);

            assertAll(
                () -> assertFalse(resultado.isTemRecomendacao()),
                () -> assertNull(resultado.getGolsMandanteRecomendado()),
                () -> assertNull(resultado.getGolsVisitanteRecomendado())
            );
        }

        @Test
        @DisplayName("Quando ha historico retorna recomendacao com dados reais")
        void quandoComHistoricoRetornaDadosReais() throws Exception {
            partidaRepository.save(Partida.builder()
                    .campeonato(campeonato).codigoExterno(99L)
                    .mandante("Time X").visitante("Time Y")
                    .golsMandante(2).golsVisitante(1)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusDays(1))
                    .status(PartidaStatus.FINALIZADO).consolidada(true).build());

            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigoUsuario", usuarioPremium.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            RecomendacaoResponseDTO resultado = objectMapper.readValue(responseJsonString, RecomendacaoResponseDTO.class);

            assertAll(
                    () -> assertTrue(resultado.getGolsMandanteRecomendado() >= 0),
                    () -> assertTrue(resultado.getGolsVisitanteRecomendado() >= 0),
                    () -> assertTrue(resultado.isTemRecomendacao())
            );
        }
    }

    @Nested
    @DisplayName("Validacao de usuario")
    class ValidacaoDeUsuario {

        @Test
        @DisplayName("Quando usuario nao existe retorna 400")
        void quandoUsuarioNaoExiste() throws Exception {
            driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", "999999")
                            .param("codigoUsuario", "111111"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Quando codigo invalido retorna 400")
        void quandoCodigoInvalido() throws Exception {
            driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigoUsuario", "ERRADO"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Quando usuario nao e Premium retorna 400")
        void quandoUsuarioNaoPremium() throws Exception {
            String responseJsonString = driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPadrao.getId().toString())
                            .param("codigoUsuario", usuarioPadrao.getCodigo()))
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
            driver.perform(get(URI_RECOMENDACAO, grupo.getId(), 999999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigoUsuario", usuarioPremium.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Quando grupo nao existe retorna 400")
        void quandoGrupoNaoExiste() throws Exception {
            driver.perform(get(URI_RECOMENDACAO, 999999L, partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigoUsuario", usuarioPremium.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Quando partida nao pertence ao campeonato do grupo retorna 400")
        void quandoPartidaNaoPertenceAoCampeonato() throws Exception {
            Partida partidaOutroCampeonato = partidaRepository.save(Partida.builder()
                    .campeonato(outroCampeonato).codigoExterno(2L)
                    .mandante("Time C").visitante("Time D")
                    .data(LocalDateTime.now(FIXED_CLOCK).plusDays(1))
                    .status(PartidaStatus.ABERTO).build());

            driver.perform(get(URI_RECOMENDACAO, grupo.getId(), partidaOutroCampeonato.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", usuarioPremium.getId().toString())
                            .param("codigoUsuario", usuarioPremium.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}
