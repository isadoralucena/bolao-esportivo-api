package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.palpite.PalpitePostPutRequestDTO;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes de edição e exclusão de palpites - US8")
public class PalpiteEdicaoControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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

    private Usuario usuario;
    private Usuario outroUsuario;
    private Campeonato campeonato;
    private Grupo grupo;
    private Partida partida;
    private Partida partidaFechada;
    private Palpite palpite;
    private PalpitePostPutRequestDTO dto;

    @BeforeEach
    void setUp() throws Exception {
        usuario = usuarioRepository.save(Usuario.builder()
                .nome("Jogador")
                .email("jogador@teste.com")
                .username("jogador")
                .endereco("Rua A")
                .codigo("123456")
                .build());

        outroUsuario = usuarioRepository.save(Usuario.builder()
                .nome("Outro")
                .email("outro@teste.com")
                .username("outro")
                .endereco("Rua B")
                .codigo("654321")
                .build());

        campeonato = campeonatoRepository.save(Campeonato.builder()
                .nome("Brasileirao")
                .url("https://api.football-data.org/v4/competitions/2013")
                .codigo("BSA")
                .ativo(true)
                .build());

        grupo = grupoRepository.save(Grupo.builder()
                .nome("Grupo Teste")
                .descricao("Descricao")
                .campeonato(campeonato)
                .privacidade(PrivacidadeGrupo.PUBLICA)
                .organizador(usuario)
                .limiteParticipantes(10)
                .build());

        grupo.getParticipantes().add(usuario);
        grupo.getParticipantes().add(outroUsuario);
        grupoRepository.save(grupo);

        partida = partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(1L)
                .mandante("Time A")
                .visitante("Time B")
                .data(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(60))
                .status(PartidaStatus.ABERTO)
                .build());

        partidaFechada = partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(2L)
                .mandante("Time C")
                .visitante("Time D")
                .data(LocalDateTime.now(ZoneOffset.UTC).minusDays(1))
                .status(PartidaStatus.FINALIZADO)
                .build());

        palpite = palpiteRepository.save(Palpite.builder()
                .partida(partida)
                .usuario(usuario)
                .grupo(grupo)
                .golsMandante(2)
                .golsVisitante(1)
                .data(LocalDateTime.now(ZoneOffset.UTC))
                .build());

        dto = PalpitePostPutRequestDTO.builder()
                .golsMandante(3)
                .golsVisitante(0)
                .build();
    }

    @AfterEach
    void clean() {
        palpiteRepository.deleteAll();
        partidaRepository.deleteAll();
        grupoRepository.deleteAll();
        campeonatoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("Conjunto de casos de edição de palpites")
    class edicaoDePalpites {

        @Test
        @DisplayName("Quando usuário edita seu próprio palpite com dados válidos")
        void quandoUsuarioEditaPalpiteValido() throws Exception {
            mockMvc.perform(put("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                            grupo.getId(), partida.getId(), palpite.getId())
                            .param("usuarioId", usuario.getId().toString())
                            .param("codigoUsuario", usuario.getCodigo())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.golsMandante").value(3))
                    .andExpect(jsonPath("$.golsVisitante").value(0));
        }

        @Test
        @DisplayName("Quando usuário tenta editar palpite inexistente")
        void quandoEditaPalpiteInexistente() throws Exception {
            mockMvc.perform(put("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                            grupo.getId(), partida.getId(), 999L)
                            .param("usuarioId", usuario.getId().toString())
                            .param("codigoUsuario", usuario.getCodigo())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("O palpite não existe!"));
        }

        @Test
        @DisplayName("Quando usuário tenta editar palpite de outro usuário")
        void quandoEditaPalpiteDeOutroUsuario() throws Exception {
            mockMvc.perform(put("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                            grupo.getId(), partida.getId(), palpite.getId())
                            .param("usuarioId", outroUsuario.getId().toString())
                            .param("codigoUsuario", outroUsuario.getCodigo())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("O usuário consultado é inválido para essa operação!"));
        }

        @Test
        @DisplayName("Quando usuário tenta editar palpite com código inválido")
        void quandoEditaPalpiteComCodigoInvalido() throws Exception {
            mockMvc.perform(put("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                            grupo.getId(), partida.getId(), palpite.getId())
                            .param("usuarioId", usuario.getId().toString())
                            .param("codigoUsuario", "999999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Quando usuário tenta editar palpite de partida fechada")
        void quandoEditaPalpitePartidaFechada() throws Exception {
            Palpite palpiteFechado = palpiteRepository.save(Palpite.builder()
                    .partida(partidaFechada)
                    .usuario(usuario)
                    .grupo(grupo)
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(ZoneOffset.UTC))
                    .build());

            mockMvc.perform(put("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                            grupo.getId(), partidaFechada.getId(), palpiteFechado.getId())
                            .param("usuarioId", usuario.getId().toString())
                            .param("codigoUsuario", usuario.getCodigo())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("O palpite não pode ser editado ou removido pois o tempo de criação do palpite expirou!"));
        }

        @Test
        @DisplayName("Quando usuário tenta editar palpite sem informar gols")
        void quandoEditaPalpiteSemGols() throws Exception {
            mockMvc.perform(put("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                            grupo.getId(), partida.getId(), palpite.getId())
                            .param("usuarioId", usuario.getId().toString())
                            .param("codigoUsuario", usuario.getCodigo())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de exclusão de palpites")
    class exclusaoDePalpites {

        @Test
        @DisplayName("Quando usuário exclui seu próprio palpite")
        void quandoUsuarioExcluiPalpiteValido() throws Exception {
            mockMvc.perform(delete("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                            grupo.getId(), partida.getId(), palpite.getId())
                            .param("usuarioId", usuario.getId().toString())
                            .param("codigoUsuario", usuario.getCodigo()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Quando usuário tenta excluir palpite inexistente")
        void quandoExcluiPalpiteInexistente() throws Exception {
            mockMvc.perform(delete("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                            grupo.getId(), partida.getId(), 999L)
                            .param("usuarioId", usuario.getId().toString())
                            .param("codigoUsuario", usuario.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("O palpite não existe!"));
        }

        @Test
        @DisplayName("Quando usuário tenta excluir palpite de outro usuário")
        void quandoExcluiPalpiteDeOutroUsuario() throws Exception {
            mockMvc.perform(delete("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                            grupo.getId(), partida.getId(), palpite.getId())
                            .param("usuarioId", outroUsuario.getId().toString())
                            .param("codigoUsuario", outroUsuario.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("O usuário consultado é inválido para essa operação!"));
        }

        @Test
        @DisplayName("Quando usuário tenta excluir palpite com código inválido")
        void quandoExcluiPalpiteComCodigoInvalido() throws Exception {
            mockMvc.perform(delete("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                            grupo.getId(), partida.getId(), palpite.getId())
                            .param("usuarioId", usuario.getId().toString())
                            .param("codigoUsuario", "999999"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Quando usuário tenta excluir palpite de partida fechada")
        void quandoExcluiPalpitePartidaFechada() throws Exception {
            Palpite palpiteFechado = palpiteRepository.save(Palpite.builder()
                    .partida(partidaFechada)
                    .usuario(usuario)
                    .grupo(grupo)
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(ZoneOffset.UTC))
                    .build());

            mockMvc.perform(delete("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                            grupo.getId(), partidaFechada.getId(), palpiteFechado.getId())
                            .param("usuarioId", usuario.getId().toString())
                            .param("codigoUsuario", usuario.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("O palpite não pode ser editado ou removido pois o tempo de criação do palpite expirou!"));
        }
    }
}