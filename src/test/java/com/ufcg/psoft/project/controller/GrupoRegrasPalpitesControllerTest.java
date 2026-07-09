package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.dto.palpite.PalpitePostPutRequestDTO;
import com.ufcg.psoft.project.dto.palpite.RegrasPalpitesRequestDTO;
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
@DisplayName("Testes de configuração de regras de palpites - US10")
public class GrupoRegrasPalpitesControllerTest {

    final String URI_GRUPOS = "/grupos";

    @Autowired
    MockMvc driver;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    GrupoRepository grupoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    CampeonatoRepository campeonatoRepository;

    @Autowired
    PartidaRepository partidaRepository;

    @Autowired
    PalpiteRepository palpiteRepository;

    @Autowired
    ObjectMapper objectMapper;

    Usuario organizador;
    Usuario participante;
    Usuario outroUsuario;
    Campeonato campeonato;
    Grupo grupo;
    RegrasPalpitesRequestDTO dto;

    @BeforeEach
    void setup() {
        driver = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();

        organizador = usuarioRepository.save(Usuario.builder()
                .nome("Organizador Teste")
                .username("organizador")
                .email("organizador@email.com")
                .endereco("Rua A")
                .codigo("111111")
                .build());

        participante = usuarioRepository.save(Usuario.builder()
                .nome("Participante Teste")
                .username("participante")
                .email("participante@email.com")
                .endereco("Rua B")
                .codigo("222222")
                .build());

        outroUsuario = usuarioRepository.save(Usuario.builder()
                .nome("Outro Usuario")
                .username("outro")
                .email("outro@email.com")
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
        grupo.getParticipantes().add(participante);
        grupoRepository.save(grupo);

        dto = new RegrasPalpitesRequestDTO(120, 0);
    }

    @AfterEach
    void tearDown() {
        palpiteRepository.deleteAll();
        partidaRepository.deleteAll();
        grupoRepository.deleteAll();
        campeonatoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("Conjunto de casos de configuração de regras de palpites")
    class configuracaoDeRegrasPalpites {

        @Test
        @DisplayName("Quando organizador configura regras de palpites com dados válidos")
        void quandoOrganizadorConfiguraRegrasValidas() throws Exception {
            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/regras-palpites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigo", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            GrupoResponseDTO resultado = objectMapper.readValue(responseJsonString, GrupoResponseDTO.class);

            assertAll(
                    () -> assertEquals(120, resultado.getMinutosAberturaPalpites()),
                    () -> assertEquals(0, resultado.getMinutosFechamentoPalpites())
            );
        }

        @Test
        @DisplayName("Quando organizador configura abertura e fechamento personalizados")
        void quandoOrganizadorConfiguraAberturaFechamentoPersonalizados() throws Exception {
            RegrasPalpitesRequestDTO dtoPersonalizado = new RegrasPalpitesRequestDTO(60, 30);

            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/regras-palpites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigo", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dtoPersonalizado)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            GrupoResponseDTO resultado = objectMapper.readValue(responseJsonString, GrupoResponseDTO.class);

            assertAll(
                    () -> assertEquals(60, resultado.getMinutosAberturaPalpites()),
                    () -> assertEquals(30, resultado.getMinutosFechamentoPalpites())
            );
        }

        @Test
        @DisplayName("Quando não-organizador tenta configurar regras de palpites")
        void quandoNaoOrganizadorTentaConfigurarRegras() throws Exception {
            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/regras-palpites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", outroUsuario.getId().toString())
                            .param("codigo", outroUsuario.getCodigo())
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertEquals("Permissão negada para acessar este recurso.", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando tenta configurar com código de acesso inválido")
        void quandoConfiguraComCodigoInvalido() throws Exception {
            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/regras-palpites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigo", "999999")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertEquals("Código de acesso inválido!", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando tenta configurar regras em grupo inexistente")
        void quandoConfiguraEmGrupoInexistente() throws Exception {
            String responseJsonString = driver.perform(put(URI_GRUPOS + "/999999/regras-palpites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigo", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertEquals("Esse grupo não existe!", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando tenta configurar com minutosAbertura menor que minutosFechamento")
        void quandoAberturaEMenorQueFechamento() throws Exception {
            RegrasPalpitesRequestDTO dtoInvalido = new RegrasPalpitesRequestDTO(30, 60);

            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/regras-palpites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigo", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dtoInvalido)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertEquals("O tempo de abertura deve ser maior que o tempo de fechamento dos palpites.", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando tenta configurar com minutosAbertura igual ao minutosFechamento")
        void quandoAberturaIgualFechamento() throws Exception {
            RegrasPalpitesRequestDTO dtoInvalido = new RegrasPalpitesRequestDTO(60, 60);

            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/regras-palpites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigo", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dtoInvalido)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertEquals("O tempo de abertura deve ser maior que o tempo de fechamento dos palpites.", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando tenta configurar com minutosAbertura nulo")
        void quandoAberturaEhNula() throws Exception {
            RegrasPalpitesRequestDTO dtoInvalido = new RegrasPalpitesRequestDTO(null, 0);

            driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/regras-palpites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigo", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dtoInvalido)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Quando tenta configurar com minutosFechamento nulo")
        void quandoFechamentoEhNulo() throws Exception {
            RegrasPalpitesRequestDTO dtoInvalido = new RegrasPalpitesRequestDTO(120, null);

            driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/regras-palpites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigo", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dtoInvalido)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Quando tenta configurar com minutos negativos")
        void quandoMinutosNegativos() throws Exception {
            RegrasPalpitesRequestDTO dtoInvalido = new RegrasPalpitesRequestDTO(-30, -60);

            driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/regras-palpites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigo", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dtoInvalido)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Efeito das regras de palpites na criação de palpites")
    class efeitoDasRegrasPalpites {

        Partida partida;
        PalpitePostPutRequestDTO palpiteDto;

        @BeforeEach
        void setupPartida() {
            palpiteDto = PalpitePostPutRequestDTO.builder()
                    .golsMandante(1)
                    .golsVisitante(0)
                    .build();
        }

        @Test
        @DisplayName("Quando partida está dentro da janela de palpites configurada")
        void quandoPartidaDentroJanela() throws Exception {
            // janela: abertura 120min antes, fechamento 0min antes
            // partida daqui 60min → dentro da janela
            partida = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(1L)
                    .mandante("Time A")
                    .visitante("Time B")
                    .data(LocalDateTime.now().plusMinutes(60))
                    .status(PartidaStatus.ABERTO)
                    .build());

            driver.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites",
                            grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString())
                            .param("codigo", participante.getCodigo())
                            .content(objectMapper.writeValueAsString(palpiteDto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Quando partida está fora da janela de abertura configurada")
        void quandoPartidaForaJanelaAbertura() throws Exception {
            // janela padrão: abertura 120min antes
            // partida daqui 180min → ainda não abriu para palpites
            partida = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(2L)
                    .mandante("Time C")
                    .visitante("Time D")
                    .data(LocalDateTime.now().plusMinutes(180))
                    .status(PartidaStatus.ABERTO)
                    .build());

            String responseJsonString = driver.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites",
                            grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString())
                            .param("codigo", participante.getCodigo())
                            .content(objectMapper.writeValueAsString(palpiteDto)))
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertEquals("O palpite não pode ser editado ou removido pois o tempo de criação do palpite expirou!", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando janela é reconfigurada e palpite passa a ser aceito")
        void quandoJanelaReconfiguradaPalpiteAceito() throws Exception {
            // partida daqui 180min — fora da janela padrão de 120min
            partida = partidaRepository.save(Partida.builder()
                    .campeonato(campeonato)
                    .codigoExterno(3L)
                    .mandante("Time E")
                    .visitante("Time F")
                    .data(LocalDateTime.now().plusMinutes(180))
                    .status(PartidaStatus.ABERTO)
                    .build());

            // reconfigura janela para 240min de abertura
            driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/regras-palpites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigo", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(new RegrasPalpitesRequestDTO(240, 0))))
                    .andExpect(status().isOk());

            // agora deve aceitar
            driver.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites",
                            grupo.getId(), partida.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString())
                            .param("codigo", participante.getCodigo())
                            .content(objectMapper.writeValueAsString(palpiteDto)))
                    .andExpect(status().isCreated());
        }
    }
}