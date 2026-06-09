package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.exception.CustomErrorType;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Privacidade;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Grupos - US5")
public class GrupoControllerTests {

    final String URI_GRUPOS = "/grupos";

    @Autowired
    MockMvc driver;

    @Autowired
    GrupoRepository grupoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    CampeonatoRepository campeonatoRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    Usuario organizador;
    Usuario participante;
    Campeonato campeonatoAtivo;
    Campeonato campeonatoInativo;
    Grupo grupoPublico;
    Grupo grupoPrivado;
    Grupo grupoPublicoSemVagas;

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());

        organizador = usuarioRepository.save(Usuario.builder()
                .nome("Organizador Teste")
                .username("organizador")
                .email("organizador@email.com")
                .endereco("Rua do Organizador, 1")
                .codigo("111111")
                .build());

        participante = usuarioRepository.save(Usuario.builder()
                .nome("Participante Teste")
                .username("participante")
                .email("participante@email.com")
                .endereco("Rua do Participante, 2")
                .codigo("222222")
                .build());

        campeonatoAtivo = campeonatoRepository.save(Campeonato.builder()
                .nome("Campeonato Ativo")
                .url("http://campeonato-ativo.com")
                .codigo("CAT001")
                .ativo(true)
                .build());

        campeonatoInativo = campeonatoRepository.save(Campeonato.builder()
                .nome("Campeonato Inativo")
                .url("http://campeonato-inativo.com")
                .codigo("CIN001")
                .ativo(false)
                .build());

        grupoPublico = grupoRepository.save(Grupo.builder()
                .nome("Grupo Publico")
                .descricao("Grupo publico para testes")
                .privacidade(Privacidade.PUBLICA)
                .limiteParticipantes(10)
                .campeonato(campeonatoAtivo)
                .organizador(organizador)
                .build());

        grupoPrivado = grupoRepository.save(Grupo.builder()
                .nome("Grupo Privado")
                .descricao("Grupo privado para testes")
                .privacidade(Privacidade.PRIVADA)
                .limiteParticipantes(10)
                .campeonato(campeonatoAtivo)
                .organizador(organizador)
                .build());

        grupoPublicoSemVagas = grupoRepository.save(Grupo.builder()
                .nome("Grupo Publico Sem Vagas")
                .descricao("Grupo publico sem vagas para testes")
                .privacidade(Privacidade.PUBLICA)
                .limiteParticipantes(1)
                .campeonato(campeonatoAtivo)
                .organizador(organizador)
                .build());

        // ocupa a unica vaga do grupoPublicoSemVagas
        grupoPublicoSemVagas.getParticipantes().add(organizador);
        grupoRepository.save(grupoPublicoSemVagas);
    }

    @AfterEach
    void tearDown() {
        grupoRepository.deleteAll();
        campeonatoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("Conjunto de casos de entrada em grupos publicos")
    class entradaEmGrupoPublico {

        @Test
        @DisplayName("Quando um usuario entra em um grupo publico com sucesso")
        void quandoUsuarioEntraEmGrupoPublicoComSucesso() throws Exception {
            // Act
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcesso", participante.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            GrupoResponseDTO resultado = objectMapper.readValue(responseJsonString, GrupoResponseDTO.class);

            // Assert
            assertAll(
                    () -> assertNotNull(resultado.getId()),
                    () -> assertEquals(grupoPublico.getNome(), resultado.getNome()),
                    () -> assertTrue(resultado.getParticipantes().stream()
                            .anyMatch(p -> p.getId().equals(participante.getId())))
            );
        }

        @Test
        @DisplayName("Quando um usuario tenta entrar em um grupo privado")
        void quandoUsuarioTentaEntrarEmGrupoPrivado() throws Exception {
            // Act
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPrivado.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcesso", participante.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Permissão negada para acessar este recurso.", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando um usuario tenta entrar em grupo com campeonato inativo")
        void quandoUsuarioTentaEntrarEmGrupoComCampeonatoInativo() throws Exception {
            // Arrange
            Grupo grupoComCampeonatoInativo = grupoRepository.save(Grupo.builder()
                    .nome("Grupo Campeonato Inativo")
                    .descricao("Grupo com campeonato inativo")
                    .privacidade(Privacidade.PUBLICA)
                    .limiteParticipantes(10)
                    .campeonato(campeonatoInativo)
                    .organizador(organizador)
                    .build());

            // Act
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoComCampeonatoInativo.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcesso", participante.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("O campeonato associado a este grupo nao esta ativo!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando um usuario tenta entrar em um grupo que ja participa")
        void quandoUsuarioTentaEntrarEmGrupoQueJaParticipa() throws Exception {
            // Arrange
            grupoPublico.getParticipantes().add(participante);
            grupoRepository.save(grupoPublico);

            // Act
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcesso", participante.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Usuario ja e participante deste grupo!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando um usuario tenta entrar em um grupo sem vagas")
        void quandoUsuarioTentaEntrarEmGrupoSemVagas() throws Exception {
            // Act
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublicoSemVagas.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcesso", participante.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("O limite de participantes para este grupo já foi atingido!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando um usuario tenta entrar em um grupo inexistente")
        void quandoUsuarioTentaEntrarEmGrupoInexistente() throws Exception {
            // Act
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + 999999L + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcesso", participante.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Esse grupo não existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando um usuario com codigo invalido tenta entrar em um grupo")
        void quandoUsuarioComCodigoInvalidoTentaEntrarEmGrupo() throws Exception {
            // Act
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcesso", "999999"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Codigo de acesso invalido!", resultado.getMessage())
            );
        }
    }
}