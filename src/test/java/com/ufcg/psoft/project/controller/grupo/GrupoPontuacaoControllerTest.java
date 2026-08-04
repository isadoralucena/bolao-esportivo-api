package com.ufcg.psoft.project.controller.grupo;

import static com.ufcg.psoft.project.config.TestClockConfig.FIXED_CLOCK;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.grupo.RegraPontuacaoPostPutRequestDTO;
import com.ufcg.psoft.project.dto.pontuacao.PontuacaoPalpiteResponseDTO;
import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;
import com.ufcg.psoft.project.exception.CustomErrorType;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.partida.PartidaNaoExisteException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoExisteException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoParticipanteException;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.*;
import com.ufcg.psoft.project.service.pontuacao.PontuacaoService;

import org.junit.jupiter.api.*;
import lombok.RequiredArgsConstructor;
import org.springframework.test.context.TestConstructor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes de pontuação automática")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class GrupoPontuacaoControllerTest {

    final String URI_GRUPOS = "/grupos";

    final MockMvc driver;

    final ObjectMapper objectMapper;

    final UsuarioRepository usuarioRepository;

    final CampeonatoRepository campeonatoRepository;

    final GrupoRepository grupoRepository;

    final PartidaRepository partidaRepository;

    final PalpiteRepository palpiteRepository;

    final PontuacaoPalpiteRepository pontuacaoPalpiteRepository;

    final PontuacaoService pontuacaoService;

    Usuario organizador;
    Usuario participante;
    Campeonato campeonato;
    Grupo grupo;
    Partida partida;
    Palpite palpite;

    @BeforeEach
    void setup() {
        organizador = usuarioRepository.save(Usuario.builder()
                .nome("Organizador Pontuacao")
                .email("organizador.pont@teste.com")
                .username("organizador_pont")
                .endereco("Rua A")
                .codigo("ORG123")
                .perfil(PerfilUsuario.PADRAO)
                .build());

        participante = usuarioRepository.save(Usuario.builder()
                .nome("Participante Pontuacao")
                .email("participante.pont@teste.com")
                .username("participante_pont")
                .endereco("Rua B")
                .codigo("PAR456")
                .perfil(PerfilUsuario.PADRAO)
                .build());

        campeonato = campeonatoRepository.save(Campeonato.builder()
                .nome("Campeonato Pontuacao Teste")
                .url("http://campeonato-pont.teste")
                .codigo("CPT01")
                .ativo(true)
                .build());

        grupo = grupoRepository.save(Grupo.builder()
                .nome("Grupo Pontuacao Teste")
                .descricao("Grupo para testes de pontuacao automatica")
                .privacidade(PrivacidadeGrupo.PUBLICA)
                .limiteParticipantes(10)
                .campeonato(campeonato)
                .organizador(organizador)
                .build());
        grupo.getParticipantes().add(organizador);
        grupo.getParticipantes().add(participante);
        grupoRepository.save(grupo);

        partida = partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(100L)
                .mandante("Time A")
                .visitante("Time B")
                .golsMandante(2)
                .golsVisitante(1)
                .data(LocalDateTime.now(FIXED_CLOCK).minusHours(2))
                .status(PartidaStatus.FINALIZADO)
                .mataMata(false)
                .build());

        palpite = palpiteRepository.save(Palpite.builder()
                .partida(partida)
                .usuario(participante)
                .grupo(grupo)
                .data(LocalDateTime.now(FIXED_CLOCK))
                .golsMandante(1)
                .golsVisitante(0)
                .build());
    }

    @AfterEach
    void tearDown() {
        pontuacaoPalpiteRepository.deleteAll();
        palpiteRepository.deleteAll();
        grupoRepository.deleteAll();
        partidaRepository.deleteAll();
        campeonatoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private Long inserirRegra(TipoRegraPontuacao tipo, int pontos) throws Exception {
        RegraPontuacaoPostPutRequestDTO dto = RegraPontuacaoPostPutRequestDTO.builder()
                .tipoRegraPontuacao(tipo)
                .pontos(pontos)
                .build();

        String response = driver.perform(post(URI_GRUPOS + "/" + grupo.getId() + "/regras-pontuacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("usuarioId", organizador.getId().toString())
                        .param("codigoUsuario", organizador.getCodigo())
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response).path("id").asLong();
    }

    @Nested
    @DisplayName("Conjunto de casos para listarPontuacoesParticipantesDoGrupo")
    class ListarPontuacoesParticipantesDoGrupo {

        @Test
        @DisplayName("Quando listar pontuações com sucesso retorna 200 e lista com todos os participantes")
        void quandoListarPontuacoesComSucesso() throws Exception {
            String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/pontuacoes")
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            List<PontuacaoParticipanteResponseDTO> resultado = objectMapper.readValue(
                    responseJsonString,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PontuacaoParticipanteResponseDTO.class));

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(2, resultado.size()) 
            );
        }

        @Test
        @DisplayName("Quando listar pontuações sem regras todos os participantes têm pontuação zero")
        void quandoListarPontuacoesSemRegrasPontuacaoZero() throws Exception {
            String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/pontuacoes")
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            List<PontuacaoParticipanteResponseDTO> resultado = objectMapper.readValue(
                    responseJsonString,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PontuacaoParticipanteResponseDTO.class));

            assertAll(
                    () -> assertFalse(resultado.isEmpty()),
                    () -> resultado.forEach(r -> assertEquals(0, r.getPontuacao())),
                    () -> resultado.forEach(r -> assertEquals(0, r.getErros())),
                    () -> resultado.forEach(r -> assertEquals(0, r.getTotalPalpitesAvaliados()))
            );
        }

        @Test
        @DisplayName("Quando listar pontuações com score calculado retorna pontuação correta do participante")
        void quandoListarPontuacoesComScoreCalculado() throws Exception {
            inserirRegra(TipoRegraPontuacao.ACERTO_VENCEDOR, 10);

            String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/pontuacoes")
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            List<PontuacaoParticipanteResponseDTO> resultado = objectMapper.readValue(
                    responseJsonString,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PontuacaoParticipanteResponseDTO.class));

            PontuacaoParticipanteResponseDTO pontuacaoParticipante = resultado.stream()
                    .filter(r -> r.getUsuarioId().equals(participante.getId()))
                    .findFirst()
                    .orElseThrow();

            assertAll(
                    () -> assertEquals(participante.getId(), pontuacaoParticipante.getUsuarioId()),
                    () -> assertEquals(participante.getNome(), pontuacaoParticipante.getUsuarioNome()),
                    () -> assertEquals(10, pontuacaoParticipante.getPontuacao()),
                    () -> assertEquals(0, pontuacaoParticipante.getErros()),
                    () -> assertEquals(1, pontuacaoParticipante.getAcertosVencedor()),
                    () -> assertEquals(0, pontuacaoParticipante.getAcertosEmpate()),
                    () -> assertEquals(0, pontuacaoParticipante.getPlacaresExatos()),
                    () -> assertEquals(1, pontuacaoParticipante.getTotalPalpitesAvaliados())
            );
        }

        @Test
        @DisplayName("Quando listar pontuações com usuário inexistente retorna 400")
        void quandoListarPontuacoesUsuarioInexistente() throws Exception {
            String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/pontuacoes")
                            .param("usuarioId", "999999")
                            .param("codigoUsuario", "qualquer"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertEquals("O usuário consultado não existe!", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando listar pontuações com código de acesso inválido retorna 400")
        void quandoListarPontuacoesCodigoInvalido() throws Exception {
            String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/pontuacoes")
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", "CODIGO_ERRADO"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertEquals("Código de acesso inválido!", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando listar pontuações de grupo inexistente retorna 400")
        void quandoListarPontuacoesGrupoNaoExiste() throws Exception {
            String responseJsonString = driver.perform(get(URI_GRUPOS + "/999999/pontuacoes")
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertEquals("Esse grupo não existe!", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando usuário não é participante do grupo tenta listar pontuações retorna 400")
        void quandoListarPontuacoesUsuarioNaoParticipante() throws Exception {
            Usuario naoParticipante = usuarioRepository.save(Usuario.builder()
                    .nome("Nao Participante")
                    .email("naop@pont.teste.com")
                    .username("naop_pont")
                    .endereco("Rua C")
                    .codigo("NAO789")
                    .perfil(PerfilUsuario.PADRAO)
                    .build());

            String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/pontuacoes")
                            .param("usuarioId", naoParticipante.getId().toString())
                            .param("codigoUsuario", naoParticipante.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertEquals("Você não é participante desse grupo!", resultado.getMessage());
        }
    }

    @Nested
    @DisplayName("Conjunto de casos para calcularPontuacaoParticipanteNoGrupo")
    class CalcularPontuacaoParticipanteNoGrupo {

        @Test
        @DisplayName("Quando participante não existe lança UsuarioNaoExisteException")
        void quandoParticipanteNaoExiste() {
            assertThrows(UsuarioNaoExisteException.class, () ->
                    pontuacaoService.calcularPontuacaoParticipanteNoGrupo(grupo.getId(), 999999L));
        }

        @Test
        @DisplayName("Quando grupo não existe lança GrupoNaoExisteException")
        void quandoGrupoNaoExiste() {
            assertThrows(GrupoNaoExisteException.class, () ->
                    pontuacaoService.calcularPontuacaoParticipanteNoGrupo(999999L, participante.getId()));
        }

        @Test
        @DisplayName("Quando usuário não é participante do grupo lança UsuarioNaoParticipanteException")
        void quandoUsuarioNaoEhParticipante() {
            Usuario naoParticipante = usuarioRepository.save(Usuario.builder()
                    .nome("Externo")
                    .email("externo@pont.teste.com")
                    .username("externo_pont")
                    .endereco("Rua Ext")
                    .codigo("EXT001")
                    .perfil(PerfilUsuario.PADRAO)
                    .build());

            assertThrows(UsuarioNaoParticipanteException.class, () ->
                    pontuacaoService.calcularPontuacaoParticipanteNoGrupo(grupo.getId(), naoParticipante.getId()));
        }

        @Test
        @DisplayName("Quando participante não tem pontuações calculadas retorna pontuação zero")
        void quandoParticipanteSemPontuacoes() {
            PontuacaoParticipanteResponseDTO resultado = pontuacaoService.calcularPontuacaoParticipanteNoGrupo(grupo.getId(), participante.getId());

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(participante.getId(), resultado.getUsuarioId()),
                    () -> assertEquals(0, resultado.getPontuacao()),
                    () -> assertEquals(0, resultado.getErros()),
                    () -> assertEquals(0, resultado.getAcertosVencedor()),
                    () -> assertEquals(0, resultado.getAcertosEmpate()),
                    () -> assertEquals(0, resultado.getPlacaresExatos()),
                    () -> assertEquals(0, resultado.getTotalPalpitesAvaliados())
            );
        }

        @Test
        @DisplayName("Quando participante erra palpite contabiliza erro")
        void quandoParticipanteErraPalpiteContabilizaErro() throws Exception {
            palpiteRepository.delete(palpite);
            palpiteRepository.save(Palpite.builder()
                    .partida(partida)
                    .usuario(participante)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(0)
                    .golsVisitante(2)
                    .build());

            inserirRegra(TipoRegraPontuacao.ACERTO_VENCEDOR, 10);

            PontuacaoParticipanteResponseDTO resultado = pontuacaoService.calcularPontuacaoParticipanteNoGrupo(grupo.getId(), participante.getId());

            assertAll(
                    () -> assertEquals(0, resultado.getPontuacao()),
                    () -> assertEquals(1, resultado.getErros()),
                    () -> assertEquals(0, resultado.getAcertosVencedor()),
                    () -> assertEquals(0, resultado.getAcertosEmpate()),
                    () -> assertEquals(0, resultado.getPlacaresExatos()),
                    () -> assertEquals(1, resultado.getTotalPalpitesAvaliados())
            );
        }

        @Test
        @DisplayName("Quando participante tem acertos de vencedor, empate e placar exato contabiliza corretamente")
        void quandoParticipanteTemTodosOsTiposDeAcerto() throws Exception {
            Partida partidaEmpateExato = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(900L)
                    .mandante("Time EX1")
                    .visitante("Time EX2")
                    .golsMandante(0)
                    .golsVisitante(0)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusHours(1))
                    .status(PartidaStatus.FINALIZADO)
                    .mataMata(false)
                    .build());

            palpiteRepository.save(Palpite.builder()
                    .partida(partidaEmpateExato)
                    .usuario(participante)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(0)
                    .golsVisitante(0) 
                    .build());

            inserirRegra(TipoRegraPontuacao.ACERTO_VENCEDOR, 5);  
            inserirRegra(TipoRegraPontuacao.ACERTO_EMPATE, 3);    
            inserirRegra(TipoRegraPontuacao.PLACAR_EXATO, 10);    

            PontuacaoParticipanteResponseDTO resultado = pontuacaoService.calcularPontuacaoParticipanteNoGrupo(grupo.getId(), participante.getId());

            assertAll(
                    () -> assertEquals(1, resultado.getAcertosVencedor()), 
                    () -> assertEquals(1, resultado.getAcertosEmpate()),   
                    () -> assertEquals(1, resultado.getPlacaresExatos()),
                    () -> assertEquals(0, resultado.getErros()),
                    () -> assertEquals(18, resultado.getPontuacao()),
                    () -> assertEquals(2, resultado.getTotalPalpitesAvaliados())
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos para calcularPontuacoesDoGrupo")
    class CalcularPontuacoesDoGrupo {

        @Test
        @DisplayName("Quando palpite acertou vencedor regra ACERTO_VENCEDOR concede pontos")
        void quandoAcertouVencedorRecebePotos() throws Exception {
            inserirRegra(TipoRegraPontuacao.ACERTO_VENCEDOR, 10);

            PontuacaoPalpite pp = pontuacaoPalpiteRepository.findByPalpiteId(palpite.getId()).orElseThrow();
            assertAll(
                    () -> assertTrue(pp.isAcertouVencedor()),
                    () -> assertFalse(pp.isAcertouEmpate()),
                    () -> assertFalse(pp.isAcertouPlacarExato()),
                    () -> assertEquals(10, pp.getPontuacao())
            );
        }

        @Test
        @DisplayName("Quando palpite errou vencedor regra ACERTO_VENCEDOR não concede pontos")
        void quandoNaoAcertouVencedorNaoRecebePontos() throws Exception {
            palpiteRepository.delete(palpite);
            Palpite palpiteErrado = palpiteRepository.save(Palpite.builder()
                    .partida(partida)
                    .usuario(participante)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(0)
                    .golsVisitante(2)
                    .build());

            inserirRegra(TipoRegraPontuacao.ACERTO_VENCEDOR, 10);

            PontuacaoPalpite pp = pontuacaoPalpiteRepository.findByPalpiteId(palpiteErrado.getId()).orElseThrow();
            assertAll(
                    () -> assertFalse(pp.isAcertouVencedor()),
                    () -> assertEquals(0, pp.getPontuacao())
            );
        }

        @Test
        @DisplayName("Quando palpite acertou empate regra ACERTO_EMPATE concede pontos")
        void quandoAcertouEmpateRecebePontos() throws Exception {
            palpiteRepository.delete(palpite);

            Partida partidaEmpate = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(201L)
                    .mandante("Time C")
                    .visitante("Time D")
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusHours(1))
                    .status(PartidaStatus.FINALIZADO)
                    .mataMata(false)
                    .build());

            Palpite palpiteEmpate = palpiteRepository.save(Palpite.builder()
                    .partida(partidaEmpate)
                    .usuario(participante)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(0)
                    .golsVisitante(0) 
                    .build());

            inserirRegra(TipoRegraPontuacao.ACERTO_EMPATE, 8);

            PontuacaoPalpite pp = pontuacaoPalpiteRepository.findByPalpiteId(palpiteEmpate.getId()).orElseThrow();
            assertAll(
                    () -> assertTrue(pp.isAcertouEmpate()),
                    () -> assertFalse(pp.isAcertouVencedor()),
                    () -> assertFalse(pp.isAcertouPlacarExato()),
                    () -> assertEquals(8, pp.getPontuacao())
            );
        }

        @Test
        @DisplayName("Quando palpite não acertou empate regra ACERTO_EMPATE não concede pontos")
        void quandoNaoAcertouEmpateNaoRecebePontos() throws Exception {
            inserirRegra(TipoRegraPontuacao.ACERTO_EMPATE, 8);

            PontuacaoPalpite pp = pontuacaoPalpiteRepository.findByPalpiteId(palpite.getId()).orElseThrow();
            assertAll(
                    () -> assertFalse(pp.isAcertouEmpate()),
                    () -> assertEquals(0, pp.getPontuacao())
            );
        }

        @Test
        @DisplayName("Quando palpite acertou placar exato regra PLACAR_EXATO concede pontos")
        void quandoAcertouPlacarExatoRecebePontos() throws Exception {
            palpiteRepository.delete(palpite);
            Palpite palpiteExato = palpiteRepository.save(Palpite.builder()
                    .partida(partida)
                    .usuario(participante)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(2) 
                    .golsVisitante(1)
                    .build());

            inserirRegra(TipoRegraPontuacao.PLACAR_EXATO, 15);

            PontuacaoPalpite pp = pontuacaoPalpiteRepository.findByPalpiteId(palpiteExato.getId()).orElseThrow();
            assertAll(
                    () -> assertTrue(pp.isAcertouPlacarExato()),
                    () -> assertTrue(pp.isAcertouVencedor()),
                    () -> assertEquals(15, pp.getPontuacao())
            );
        }

        @Test
        @DisplayName("Quando palpite não acertou placar exato regra PLACAR_EXATO não concede pontos")
        void quandoNaoAcertouPlacarExatoNaoRecebePontos() throws Exception {
            inserirRegra(TipoRegraPontuacao.PLACAR_EXATO, 15);

            PontuacaoPalpite pp = pontuacaoPalpiteRepository.findByPalpiteId(palpite.getId()).orElseThrow();
            assertAll(
                    () -> assertFalse(pp.isAcertouPlacarExato()),
                    () -> assertEquals(0, pp.getPontuacao())
            );
        }

        @Test
        @DisplayName("Quando partida não é mata-mata e houve acerto regra BONUS_RODADA concede pontos")
        void quandoBonusRodadaPartidaNormalComAcerto() throws Exception {
            inserirRegra(TipoRegraPontuacao.BONUS_RODADA, 5);

            PontuacaoPalpite pp = pontuacaoPalpiteRepository.findByPalpiteId(palpite.getId()).orElseThrow();
            assertAll(
                    () -> assertTrue(pp.isAcertouVencedor()),
                    () -> assertEquals(5, pp.getPontuacao())
            );
        }

        @Test
        @DisplayName("Quando partida é mata-mata regra BONUS_RODADA não concede pontos")
        void quandoBonusRodadaPartidaMataMata() throws Exception {
            Partida partidaMM = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(202L)
                    .mandante("Time MM-A")
                    .visitante("Time MM-B")
                    .golsMandante(2)
                    .golsVisitante(0)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusHours(1))
                    .status(PartidaStatus.FINALIZADO)
                    .mataMata(true) 
                    .build());

            palpiteRepository.delete(palpite);
            Palpite palpiteMM = palpiteRepository.save(Palpite.builder()
                    .partida(partidaMM)
                    .usuario(participante)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(1)
                    .golsVisitante(0)
                    .build());

            inserirRegra(TipoRegraPontuacao.BONUS_RODADA, 5);

            PontuacaoPalpite pp = pontuacaoPalpiteRepository.findByPalpiteId(palpiteMM.getId()).orElseThrow();
            assertEquals(0, pp.getPontuacao());
        }

        @Test
        @DisplayName("Quando partida não é mata-mata mas sem acerto regra BONUS_RODADA não concede pontos")
        void quandoBonusRodadaPartidaNormalSemAcerto() throws Exception {
            palpiteRepository.delete(palpite);
            Palpite palpiteMiss = palpiteRepository.save(Palpite.builder()
                    .partida(partida)
                    .usuario(participante)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(0)
                    .golsVisitante(2)
                    .build());

            inserirRegra(TipoRegraPontuacao.BONUS_RODADA, 5);

            PontuacaoPalpite pp = pontuacaoPalpiteRepository.findByPalpiteId(palpiteMiss.getId()).orElseThrow();
            assertAll(
                    () -> assertFalse(pp.isAcertouVencedor()),
                    () -> assertFalse(pp.isAcertouEmpate()),
                    () -> assertFalse(pp.isAcertouPlacarExato()),
                    () -> assertEquals(0, pp.getPontuacao())
            );
        }

        @Test
        @DisplayName("Quando partida é mata-mata e houve acerto regra BONUS_MATA_MATA concede pontos")
        void quandoBonusMataMataPartidaMataMataComAcerto() throws Exception {
            Partida partidaMM = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(203L)
                    .mandante("Time QF-A")
                    .visitante("Time QF-B")
                    .golsMandante(3)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusHours(1))
                    .status(PartidaStatus.FINALIZADO)
                    .mataMata(true)
                    .build());

            palpiteRepository.delete(palpite);
            Palpite palpiteMM = palpiteRepository.save(Palpite.builder()
                    .partida(partidaMM)
                    .usuario(participante)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(2)
                    .golsVisitante(0) 
                    .build());

            inserirRegra(TipoRegraPontuacao.BONUS_MATA_MATA, 7);

            PontuacaoPalpite pp = pontuacaoPalpiteRepository.findByPalpiteId(palpiteMM.getId()).orElseThrow();
            assertAll(
                    () -> assertTrue(pp.isAcertouVencedor()),
                    () -> assertEquals(7, pp.getPontuacao())
            );
        }

        @Test
        @DisplayName("Quando partida não é mata-mata regra BONUS_MATA_MATA não concede pontos")
        void quandoBonusMataMataPartidaNormal() throws Exception {
            inserirRegra(TipoRegraPontuacao.BONUS_MATA_MATA, 7);

            PontuacaoPalpite pp = pontuacaoPalpiteRepository.findByPalpiteId(palpite.getId()).orElseThrow();
            assertEquals(0, pp.getPontuacao());
        }

        @Test
        @DisplayName("Quando partida é mata-mata mas sem acerto regra BONUS_MATA_MATA não concede pontos")
        void quandoBonusMataMataPartidaMataMataSemAcerto() throws Exception {
            Partida partidaMM = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(204L)
                    .mandante("Time SF-A")
                    .visitante("Time SF-B")
                    .golsMandante(2)
                    .golsVisitante(0)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusHours(1))
                    .status(PartidaStatus.FINALIZADO)
                    .mataMata(true)
                    .build());

            palpiteRepository.delete(palpite);
            Palpite palpiteMiss = palpiteRepository.save(Palpite.builder()
                    .partida(partidaMM)
                    .usuario(participante)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(0)
                    .golsVisitante(1) 
                    .build());

            inserirRegra(TipoRegraPontuacao.BONUS_MATA_MATA, 7);

            PontuacaoPalpite pp = pontuacaoPalpiteRepository.findByPalpiteId(palpiteMiss.getId()).orElseThrow();
            assertAll(
                    () -> assertFalse(pp.isAcertouVencedor()),
                    () -> assertFalse(pp.isAcertouEmpate()),
                    () -> assertFalse(pp.isAcertouPlacarExato()),
                    () -> assertEquals(0, pp.getPontuacao())
            );
        }

        @Test
        @DisplayName("Quando múltiplas regras são aplicadas as pontuações somam corretamente")
        void quandoMultiplasRegrasAsSomamCorretamente() throws Exception {
            inserirRegra(TipoRegraPontuacao.ACERTO_VENCEDOR, 10);
            inserirRegra(TipoRegraPontuacao.BONUS_RODADA, 5);

            PontuacaoPalpite pp = pontuacaoPalpiteRepository.findByPalpiteId(palpite.getId()).orElseThrow();
            assertEquals(15, pp.getPontuacao());
        }

        @Test
        @DisplayName("Quando PontuacaoPalpite já existe é atualizado e não duplicado")
        void quandoPontuacaoPalpiteJaExisteEhAtualizado() throws Exception {
            inserirRegra(TipoRegraPontuacao.ACERTO_VENCEDOR, 10);

            PontuacaoPalpite ppPrimeiro = pontuacaoPalpiteRepository.findByPalpiteId(palpite.getId()).orElseThrow();
            Long idOriginal = ppPrimeiro.getId();
            assertEquals(10, ppPrimeiro.getPontuacao());

            inserirRegra(TipoRegraPontuacao.ACERTO_EMPATE, 3); 

            PontuacaoPalpite ppSegundo = pontuacaoPalpiteRepository.findByPalpiteId(palpite.getId()).orElseThrow();
            assertAll(
                    () -> assertEquals(idOriginal, ppSegundo.getId()), 
                    () -> assertEquals(10, ppSegundo.getPontuacao())  
            );
        }

        @Test
        @DisplayName("Quando regra é removida as pontuações são recalculadas sem ela")
        void quandoRegraRemovidaPontuacoesRecalculadas() throws Exception {
            Long regraId = inserirRegra(TipoRegraPontuacao.ACERTO_VENCEDOR, 10);

            PontuacaoPalpite ppAntes = pontuacaoPalpiteRepository.findByPalpiteId(palpite.getId()).orElseThrow();
            assertEquals(10, ppAntes.getPontuacao());

            driver.perform(delete(URI_GRUPOS + "/" + grupo.getId() + "/regras-pontuacao/" + regraId)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoUsuario", organizador.getCodigo()))
                    .andExpect(status().isNoContent())
                    .andDo(print());

            PontuacaoPalpite ppDepois = pontuacaoPalpiteRepository.findByPalpiteId(palpite.getId()).orElseThrow();
            assertEquals(0, ppDepois.getPontuacao());
        }

        @Test
        @DisplayName("Quando grupo não existe ao calcular pontuações do grupo lança GrupoNaoExisteException")
        void quandoGrupoNaoExisteAoCalcularPontuacoes() {
            assertThrows(GrupoNaoExisteException.class, () ->
                    pontuacaoService.calcularPontuacoesDoGrupo(999999L));
        }

        @Test
        @DisplayName("Quando partida finalizada tem gols nulos no cálculo do grupo lança IllegalArgumentException")
        void quandoPartidaFinalizadaGolsNulosNoGrupo() {
            Partida partidaGolsNulos = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(206L)
                    .mandante("Time Null-A")
                    .visitante("Time Null-B")
                    .golsMandante(null)
                    .golsVisitante(null)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusHours(1))
                    .status(PartidaStatus.FINALIZADO)
                    .build());

            palpiteRepository.delete(palpite);
            palpiteRepository.save(Palpite.builder()
                    .partida(partidaGolsNulos)
                    .usuario(participante)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(1)
                    .golsVisitante(0)
                    .build());

            assertThrows(IllegalArgumentException.class, () ->
                    pontuacaoService.calcularPontuacoesDoGrupo(grupo.getId()));
        }
    }

    @Nested
    @DisplayName("Conjunto de casos para calcularPontuacoesAssociadasAPartida")
    class CalcularPontuacoesAssociadasAPartida {

        @Test
        @DisplayName("Quando partida não existe lança PartidaNaoExisteException")
        void quandoPartidaNaoExiste() {
            assertThrows(PartidaNaoExisteException.class, () ->
                    pontuacaoService.calcularPontuacoesAssociadasAPartida(999999L));
        }

        @Test
        @DisplayName("Quando partida não está finalizada retorna lista vazia")
        void quandoPartidaNaoFinalizada() {
            Partida partidaAberta = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(301L)
                    .mandante("Time Z1")
                    .visitante("Time Z2")
                    .data(LocalDateTime.now(FIXED_CLOCK).plusHours(2))
                    .status(PartidaStatus.ABERTO)
                    .build());

            List<PontuacaoPalpiteResponseDTO> resultado = pontuacaoService.calcularPontuacoesAssociadasAPartida(partidaAberta.getId());

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertTrue(resultado.isEmpty())
            );
        }

        @Test
        @DisplayName("Quando partida finalizada tem gols nulos lança IllegalArgumentException")
        void quandoPartidaFinalizadaGolsNulos() {
            Partida partidaGolsNulos = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(302L)
                    .mandante("Time Nulo-A")
                    .visitante("Time Nulo-B")
                    .golsMandante(null)
                    .golsVisitante(null)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusHours(1))
                    .status(PartidaStatus.FINALIZADO)
                    .build());

            assertThrows(IllegalArgumentException.class, () ->
                    pontuacaoService.calcularPontuacoesAssociadasAPartida(partidaGolsNulos.getId()));
        }

        @Test
        @DisplayName("Quando partida finalizada não tem palpites retorna lista vazia")
        void quandoPartidaFinalizadaSemPalpites() {
            Partida partidaSemPalpites = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(303L)
                    .mandante("Time W1")
                    .visitante("Time W2")
                    .golsMandante(1)
                    .golsVisitante(0)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusHours(1))
                    .status(PartidaStatus.FINALIZADO)
                    .build());

            List<PontuacaoPalpiteResponseDTO> resultado = pontuacaoService.calcularPontuacoesAssociadasAPartida(partidaSemPalpites.getId());

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertTrue(resultado.isEmpty())
            );
        }

        @Test
        @DisplayName("Quando partida finalizada tem palpite sem regras retorna pontuação zero")
        void quandoPartidaFinalizadaComPalpiteSemRegras() {
            List<PontuacaoPalpiteResponseDTO> resultado = pontuacaoService.calcularPontuacoesAssociadasAPartida(partida.getId());

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(1, resultado.size()),
                    () -> assertEquals(0, resultado.get(0).getPontuacao())
            );
        }

        @Test
        @DisplayName("Quando partida finalizada tem palpite e regra retorna pontuação calculada corretamente")
        void quandoPartidaFinalizadaComPalpiteERegra() throws Exception {
            inserirRegra(TipoRegraPontuacao.ACERTO_VENCEDOR, 10);

            List<PontuacaoPalpiteResponseDTO> resultado = pontuacaoService.calcularPontuacoesAssociadasAPartida(partida.getId());

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(1, resultado.size()),
                    () -> assertEquals(palpite.getId(), resultado.get(0).getPalpiteId()),
                    () -> assertEquals(participante.getId(), resultado.get(0).getUsuarioId()),
                    () -> assertEquals(grupo.getId(), resultado.get(0).getGrupoId()),
                    () -> assertEquals(partida.getId(), resultado.get(0).getPartidaId()),
                    () -> assertEquals(10, resultado.get(0).getPontuacao()),
                    () -> assertTrue(resultado.get(0).isAcertouVencedor()),
                    () -> assertFalse(resultado.get(0).isAcertouEmpate()),
                    () -> assertFalse(resultado.get(0).isAcertouPlacarExato())
            );
        }

        @Test
        @DisplayName("Quando calcular pontuações de partida com PontuacaoPalpite existente o atualiza")
        void quandoCalcularNovamentePontuacaoPalpiteExistenteEhAtualizado() throws Exception {
            inserirRegra(TipoRegraPontuacao.ACERTO_VENCEDOR, 10);
            PontuacaoPalpite ppPrimeiro = pontuacaoPalpiteRepository.findByPalpiteId(palpite.getId()).orElseThrow();
            Long idOriginal = ppPrimeiro.getId();

            List<PontuacaoPalpiteResponseDTO> resultado = pontuacaoService.calcularPontuacoesAssociadasAPartida(partida.getId());

            PontuacaoPalpite ppAtualizado = pontuacaoPalpiteRepository.findByPalpiteId(palpite.getId()).orElseThrow();
            assertAll(
                    () -> assertEquals(idOriginal, ppAtualizado.getId()), // mesma entidade
                    () -> assertEquals(1, resultado.size()),
                    () -> assertEquals(10, resultado.get(0).getPontuacao())
            );
        }

        @Test
        @DisplayName("Quando palpite marcou empate real e acertou placar exato de empate os campos são corretos")
        void quandoAcertouEmpateEPlacarExatoSimultaneamente() throws Exception {
            Partida partidaEmpateExato = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(304L)
                    .mandante("Time EE-A")
                    .visitante("Time EE-B")
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusHours(1))
                    .status(PartidaStatus.FINALIZADO)
                    .mataMata(false)
                    .build());

            Palpite palpiteEmpateExato = palpiteRepository.save(Palpite.builder()
                    .partida(partidaEmpateExato)
                    .usuario(organizador)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(1)
                    .golsVisitante(1)
                    .build());

            inserirRegra(TipoRegraPontuacao.ACERTO_EMPATE, 5);
            inserirRegra(TipoRegraPontuacao.PLACAR_EXATO, 10);

            List<PontuacaoPalpiteResponseDTO> resultado = pontuacaoService.calcularPontuacoesAssociadasAPartida(partidaEmpateExato.getId());

            PontuacaoPalpiteResponseDTO dto = resultado.stream()
                    .filter(r -> r.getPalpiteId().equals(palpiteEmpateExato.getId()))
                    .findFirst()
                    .orElseThrow();

            assertAll(
                    () -> assertTrue(dto.isAcertouEmpate()),
                    () -> assertTrue(dto.isAcertouPlacarExato()),
                    () -> assertFalse(dto.isAcertouVencedor()),
                    () -> assertEquals(15, dto.getPontuacao())
            );
        }

        @Test
        @DisplayName("atualizarAcertos: vitoria mandante com palpite empate nao acerta")
        void quandoVitoriaMandanteEPalpiteEmpate() throws Exception {
            Partida partidaVE = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(305L)
                    .mandante("Time V-M")
                    .visitante("Time V-V")
                    .golsMandante(2)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusHours(1))
                    .status(PartidaStatus.FINALIZADO)
                    .build());

            palpiteRepository.delete(palpite);
            palpiteRepository.save(Palpite.builder()
                    .partida(partidaVE)
                    .usuario(participante)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(1)
                    .golsVisitante(1)
                    .build());

            inserirRegra(TipoRegraPontuacao.ACERTO_VENCEDOR, 10);

            List<PontuacaoPalpiteResponseDTO> resultado = pontuacaoService.calcularPontuacoesAssociadasAPartida(partidaVE.getId());

            assertAll(
                    () -> assertFalse(resultado.get(0).isAcertouVencedor()),
                    () -> assertFalse(resultado.get(0).isAcertouEmpate()),
                    () -> assertFalse(resultado.get(0).isAcertouPlacarExato()),
                    () -> assertEquals(0, resultado.get(0).getPontuacao())
            );
        }

        @Test
        @DisplayName("atualizarAcertos: empate real com palpite vencedor nao acerta")
        void quandoEmpateRealEPalpiteVencedor() {
            Partida partidaEE = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(306L)
                    .mandante("Time E-M")
                    .visitante("Time E-V")
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusHours(1))
                    .status(PartidaStatus.FINALIZADO)
                    .build());

            palpiteRepository.delete(palpite);
            palpiteRepository.save(Palpite.builder()
                    .partida(partidaEE)
                    .usuario(participante)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(2)
                    .golsVisitante(0)
                    .build());

            List<PontuacaoPalpiteResponseDTO> resultado = pontuacaoService.calcularPontuacoesAssociadasAPartida(partidaEE.getId());

            assertAll(
                    () -> assertFalse(resultado.get(0).isAcertouVencedor()),
                    () -> assertFalse(resultado.get(0).isAcertouEmpate()),
                    () -> assertFalse(resultado.get(0).isAcertouPlacarExato()),
                    () -> assertEquals(0, resultado.get(0).getPontuacao())
            );
        }

        @Test
        @DisplayName("atualizarAcertos: vitoria visitante com palpite exato acerta vencedor e placar")
        void quandoVitoriaVisitanteEPalpiteExato() throws Exception {
            Partida partidaVV = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(307L)
                    .mandante("Time VV-M")
                    .visitante("Time VV-V")
                    .golsMandante(0)
                    .golsVisitante(2)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusHours(1))
                    .status(PartidaStatus.FINALIZADO)
                    .build());

            palpiteRepository.delete(palpite);
            palpiteRepository.save(Palpite.builder()
                    .partida(partidaVV)
                    .usuario(participante)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(0)
                    .golsVisitante(2)
                    .build());

            inserirRegra(TipoRegraPontuacao.PLACAR_EXATO, 15);

            List<PontuacaoPalpiteResponseDTO> resultado = pontuacaoService.calcularPontuacoesAssociadasAPartida(partidaVV.getId());

            assertAll(
                    () -> assertTrue(resultado.get(0).isAcertouVencedor()),
                    () -> assertFalse(resultado.get(0).isAcertouEmpate()),
                    () -> assertTrue(resultado.get(0).isAcertouPlacarExato()),
                    () -> assertEquals(15, resultado.get(0).getPontuacao())
            );
        }

        @Test
        @DisplayName("atualizarAcertos: vitoria visitante com palpite errado nao acerta")
        void quandoVitoriaVisitanteEPalpiteErrado() {
            Partida partidaVE2 = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(308L)
                    .mandante("Time WE-M")
                    .visitante("Time WE-V")
                    .golsMandante(0)
                    .golsVisitante(2)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusHours(1))
                    .status(PartidaStatus.FINALIZADO)
                    .build());

            palpiteRepository.delete(palpite);
            palpiteRepository.save(Palpite.builder()
                    .partida(partidaVE2)
                    .usuario(participante)
                    .grupo(grupo)
                    .data(LocalDateTime.now(FIXED_CLOCK))
                    .golsMandante(2)
                    .golsVisitante(0)
                    .build());

            List<PontuacaoPalpiteResponseDTO> resultado = pontuacaoService.calcularPontuacoesAssociadasAPartida(partidaVE2.getId());

            assertAll(
                    () -> assertFalse(resultado.get(0).isAcertouVencedor()),
                    () -> assertFalse(resultado.get(0).isAcertouEmpate()),
                    () -> assertFalse(resultado.get(0).isAcertouPlacarExato()),
                    () -> assertEquals(0, resultado.get(0).getPontuacao())
            );
        }
    }
}
