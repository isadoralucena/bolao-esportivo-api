package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.palpite.PalpitePostPutRequestDTO;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Palpites")
public class PalpiteControllerTests {

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
    private Usuario adminUser;
    private Campeonato campeonato;
    private Grupo grupo;
    private Partida partida;
    private PalpitePostPutRequestDTO dto;

    @BeforeEach
    void setUp() {
        usuario = usuarioRepository.save(Usuario.builder()
                .nome("Jogador")
                .email("jogador@teste.com")
                .username("jogador")
                .endereco("Rua A")
                .codigo("123456")
                .build());

        adminUser = usuarioRepository.save(Usuario.builder()
                .nome("Admin")
                .email("admin@teste.com")
                .username("admin")
                .endereco("Rua B")
                .codigo("654321")
                .administrador(true)
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
        grupoRepository.save(grupo);

        partida = partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(1L)
                .mandante("Time A")
                .visitante("Time B")
                .data(LocalDateTime.now().plusDays(7))
                .status(PartidaStatus.ABERTO)
                .rodada(1)
                .build());

        dto = PalpitePostPutRequestDTO.builder()
                .golsMandante(2)
                .golsVisitante(1)
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

    @Test
    @DisplayName("Criar palpite com dados validos")
    void criarPalpiteValido() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigo", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Criar palpite com codigo errado retorna 400")
    void criarPalpiteCodigoErrado() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigo", "999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Palpite duplicado retorna 400")
    void criarPalpiteDuplicado() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigo", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigo", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Partida de outro campeonato retorna 400")
    void criarPalpitePartidaDeOutroCampeonato() throws Exception {
        Campeonato outroCampeonato = campeonatoRepository.save(Campeonato.builder()
                .nome("Copa do Brasil")
                .url("https://api.football-data.org/v4/competitions/2015")
                .codigo("CDB")
                .ativo(true)
                .build());

        Partida outraPartida = partidaRepository.save(Partida.builder()
                .campeonato(outroCampeonato)
                .codigoExterno(2L)
                .mandante("Time X")
                .visitante("Time Y")
                .data(LocalDateTime.now().plusDays(7))
                .status(PartidaStatus.ABERTO)
                .rodada(1)
                .build());

        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), outraPartida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigo", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Criar palpite com usuario nao participante retorna 400")
    void criarPalpiteUsuarioNaoParticipante() throws Exception {
        Usuario naoParticipante = usuarioRepository.save(Usuario.builder()
                .nome("Forasteiro")
                .email("forasteiro@teste.com")
                .username("forasteiro")
                .endereco("Rua C")
                .codigo("111111")
                .build());

        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", naoParticipante.getId().toString())
                .param("codigo", naoParticipante.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Criar palpite com partida inexistente retorna 400")
    void criarPalpitePartidaInexistente() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), 999L)
                .param("usuarioId", usuario.getId().toString())
                .param("codigo", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Criar palpite com grupo inexistente retorna 400")
    void criarPalpiteGrupoInexistente() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", 999L, partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigo", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Criar palpite com usuario inexistente retorna 400")
    void criarPalpiteUsuarioInexistente() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", "999")
                .param("codigo", "123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Criar palpite com gols nulos retorna 400")
    void criarPalpiteGolsNulos() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigo", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Listar palpites de uma partida apos criar um palpite")
    void listarPalpitesDaPartida() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigo", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].golsMandante").value(2))
                .andExpect(jsonPath("$[0].golsVisitante").value(1));
    }

    @Test
    @DisplayName("Listar palpites de uma partida sem palpites retorna array vazio")
    void listarPalpitesDaPartidaVazia() throws Exception {
        mockMvc.perform(get("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Listar palpites do grupo apos criar palpites em varias partidas")
    void listarPalpitesDoGrupo() throws Exception {
        Partida partida2 = partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(2L)
                .mandante("Time C")
                .visitante("Time D")
                .data(LocalDateTime.now().plusDays(8))
                .status(PartidaStatus.ABERTO)
                .rodada(2)
                .build());

        PalpitePostPutRequestDTO dto2 = PalpitePostPutRequestDTO.builder()
                .golsMandante(3)
                .golsVisitante(0)
                .build();

        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigo", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida2.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigo", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/grupos/{grupoId}/palpites", grupo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Listar palpites do usuario apos criar um palpite")
    void listarPalpitesDoUsuario() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigo", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/usuarios/{usuarioId}/palpites", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].golsMandante").value(2));
    }

    @Test
    @DisplayName("Sincronizar partidas com admin retorna campeonatos atualizados")
    void sincronizarPartidasComAdmin() throws Exception {
        mockMvc.perform(post("/campeonatos/sincronizar")
                .param("userId", adminUser.getId().toString())
                .param("senha", adminUser.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Campeonato Brasileiro Série A"));
    }

    @Test
    @DisplayName("Sincronizar partidas com usuario nao admin retorna 400")
    void sincronizarPartidasNaoAdmin() throws Exception {
        mockMvc.perform(post("/campeonatos/sincronizar")
                .param("userId", usuario.getId().toString())
                .param("senha", usuario.getCodigo()))
                .andExpect(status().isBadRequest());
    }
}
