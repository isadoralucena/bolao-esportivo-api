package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.campeonato.CampeonatoResponseDTO;
import com.ufcg.psoft.project.dto.palpite.PalpitePostPutRequestDTO;
import com.ufcg.psoft.project.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.*;
import com.ufcg.psoft.project.service.campeonato.CampeonatoService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
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

    @MockBean
    CampeonatoService campeonatoService;

    private Usuario usuario;
    private Usuario adminUser;
    private Campeonato campeonato;
    private Grupo grupo;
    private Partida partida;
    private Partida partidaAbertaForaJanela;
    private Partida partidaAbertaJanelaFechada;
    private Partida partidaEmAndamento;
    private Partida partidaFinalizada;
    private Partida partidaCancelada;
    private PalpitePostPutRequestDTO dto;
    private PalpitePostPutRequestDTO dtoEdicao;

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
                .data(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(60))
                .status(PartidaStatus.ABERTO)
                .build());

        partidaAbertaForaJanela = partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(2L)
                .mandante("Time C")
                .visitante("Time D")
                .data(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(180))
                .status(PartidaStatus.ABERTO)
                .build());

        partidaAbertaJanelaFechada = partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(6L)
                .mandante("Time K")
                .visitante("Time L")
                .data(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5))
                .status(PartidaStatus.ABERTO)
                .build());

        partidaEmAndamento = partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(3L)
                .mandante("Time E")
                .visitante("Time F")
                .data(LocalDateTime.now(ZoneOffset.UTC))
                .status(PartidaStatus.EM_ANDAMENTO)
                .build());

        partidaFinalizada = partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(4L)
                .mandante("Time G")
                .visitante("Time H")
                .data(LocalDateTime.now(ZoneOffset.UTC).minusHours(2))
                .status(PartidaStatus.FINALIZADO)
                .golsMandante(2)
                .golsVisitante(0)
                .build());

        partidaCancelada = partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(5L)
                .mandante("Time I")
                .visitante("Time J")
                .data(LocalDateTime.now(ZoneOffset.UTC).plusDays(1))
                .status(PartidaStatus.CANCELADO)
                .build());

        dto = PalpitePostPutRequestDTO.builder()
                .golsMandante(2)
                .golsVisitante(1)
                .build();

        dtoEdicao = PalpitePostPutRequestDTO.builder()
                .golsMandante(3)
                .golsVisitante(0)
                .build();

    when(campeonatoService.sincronizarCampeonato(campeonato.getId(), adminUser.getId(), adminUser.getCodigo()))
            .thenReturn(new CampeonatoResponseDTO(campeonato));
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
                .param("codigoUsuario", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Criar palpite com codigo errado retorna 400")
    void criarPalpiteCodigoErrado() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigoUsuario", "999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Palpite duplicado retorna 400")
    void criarPalpiteDuplicado() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigoUsuario", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigoUsuario", usuario.getCodigo())
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
                .data(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(60))
                .status(PartidaStatus.ABERTO)
                .build());

        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), outraPartida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigoUsuario", usuario.getCodigo())
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
                .param("codigoUsuario", naoParticipante.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Criar palpite com partida inexistente retorna 400")
    void criarPalpitePartidaInexistente() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), 999L)
                .param("usuarioId", usuario.getId().toString())
                .param("codigoUsuario", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Criar palpite com grupo inexistente retorna 400")
    void criarPalpiteGrupoInexistente() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", 999L, partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigoUsuario", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Criar palpite com usuario inexistente retorna 400")
    void criarPalpiteUsuarioInexistente() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", "999")
                .param("codigoUsuario", "123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Criar palpite com gols nulos retorna 400")
    void criarPalpiteGolsNulos() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigoUsuario", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Listar palpites de uma partida apos criar um palpite")
    void listarPalpitesDaPartida() throws Exception {
        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigoUsuario", usuario.getCodigo())
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
                .data(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(60))
                .status(PartidaStatus.ABERTO)
                .build());

        PalpitePostPutRequestDTO dto2 = PalpitePostPutRequestDTO.builder()
                .golsMandante(3)
                .golsVisitante(0)
                .build();

        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigoUsuario", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida2.getId())
                .param("usuarioId", usuario.getId().toString())
                .param("codigoUsuario", usuario.getCodigo())
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
                .param("codigoUsuario", usuario.getCodigo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/usuarios/{usuarioId}/palpites", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].golsMandante").value(2));
    }

    @Test
    @DisplayName("Sincronizar partidas com admin retorna campeonato na resposta")
    void sincronizarPartidasComAdmin() throws Exception {
        mockMvc.perform(post("/campeonatos/{campeonatoId}/sincronizar", campeonato.getId())
                .param("userId", adminUser.getId().toString())
                .param("senha", adminUser.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").isNotEmpty());
    }

    @Test
    @DisplayName("Sincronizar partidas com usuario nao admin retorna 400")
    void sincronizarPartidasNaoAdmin() throws Exception {
        when(campeonatoService.sincronizarCampeonato(campeonato.getId(), usuario.getId(), usuario.getCodigo()))
                .thenThrow(new CodigoDeAcessoInvalidoException());

        mockMvc.perform(post("/campeonatos/{campeonatoId}/sincronizar", campeonato.getId())
                .param("userId", usuario.getId().toString())
                .param("senha", usuario.getCodigo()))
                .andExpect(status().isBadRequest());
    }

    @Nested
    @DisplayName("Controle de estado das partidas na criacao de palpites")
    class ControleEstadoCriacaoPalpites {

        @Test
        @DisplayName("Criar palpite em partida aberta e dentro da janela aceita")
        void criarPalpitePartidaAbertaEDentroJanela() throws Exception {
            mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partida.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Criar palpite em partida aberta fora da janela retorna 400")
        void criarPalpitePartidaAbertaForaJanela() throws Exception {
            mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partidaAbertaForaJanela.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Criar palpite em partida em andamento retorna 400")
        void criarPalpitePartidaEmAndamento() throws Exception {
            mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partidaEmAndamento.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Criar palpite em partida finalizada retorna 400")
        void criarPalpitePartidaFinalizada() throws Exception {
            mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partidaFinalizada.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Criar palpite em partida cancelada retorna 400")
        void criarPalpitePartidaCancelada() throws Exception {
            mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partidaCancelada.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Criar palpite em partida aberta mas janela ja fechada retorna 400")
        void criarPalpitePartidaAbertaJanelaFechada() throws Exception {
            mockMvc.perform(post("/grupos/{grupoId}/partidas/{partidaId}/palpites", grupo.getId(), partidaAbertaJanelaFechada.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Controle de estado das partidas na edicao de palpites")
    class ControleEstadoEdicaoPalpites {

        @Test
        @DisplayName("Editar palpite em partida aberta e dentro da janela aceita")
        void editarPalpitePartidaAberta() throws Exception {
            Palpite palpiteExistente = palpiteRepository.save(Palpite.builder()
                    .partida(partida)
                    .usuario(usuario)
                    .grupo(grupo)
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(ZoneOffset.UTC))
                    .build());

            mockMvc.perform(put("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                    grupo.getId(), partida.getId(), palpiteExistente.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dtoEdicao)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.golsMandante").value(3))
                    .andExpect(jsonPath("$.golsVisitante").value(0));
        }

        @Test
        @DisplayName("Editar palpite em partida aberta fora da janela retorna 400")
        void editarPalpitePartidaAbertaForaJanela() throws Exception {
            Palpite palpiteExistente = palpiteRepository.save(Palpite.builder()
                    .partida(partidaAbertaForaJanela)
                    .usuario(usuario)
                    .grupo(grupo)
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(ZoneOffset.UTC))
                    .build());

            mockMvc.perform(put("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                    grupo.getId(), partidaAbertaForaJanela.getId(), palpiteExistente.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dtoEdicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Editar palpite em partida em andamento retorna 400")
        void editarPalpitePartidaEmAndamento() throws Exception {
            Palpite palpiteExistente = palpiteRepository.save(Palpite.builder()
                    .partida(partidaEmAndamento)
                    .usuario(usuario)
                    .grupo(grupo)
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(ZoneOffset.UTC))
                    .build());

            mockMvc.perform(put("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                    grupo.getId(), partidaEmAndamento.getId(), palpiteExistente.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dtoEdicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Editar palpite em partida finalizada retorna 400")
        void editarPalpitePartidaFinalizada() throws Exception {
            Palpite palpiteExistente = palpiteRepository.save(Palpite.builder()
                    .partida(partidaFinalizada)
                    .usuario(usuario)
                    .grupo(grupo)
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(ZoneOffset.UTC))
                    .build());

            mockMvc.perform(put("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                    grupo.getId(), partidaFinalizada.getId(), palpiteExistente.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dtoEdicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Editar palpite em partida cancelada retorna 400")
        void editarPalpitePartidaCancelada() throws Exception {
            Palpite palpiteExistente = palpiteRepository.save(Palpite.builder()
                    .partida(partidaCancelada)
                    .usuario(usuario)
                    .grupo(grupo)
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(ZoneOffset.UTC))
                    .build());

            mockMvc.perform(put("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                    grupo.getId(), partidaCancelada.getId(), palpiteExistente.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dtoEdicao)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Controle de estado das partidas na exclusao de palpites")
    class ControleEstadoExclusaoPalpites {

        @Test
        @DisplayName("Excluir palpite em partida aberta e dentro da janela aceita")
        void excluirPalpitePartidaAberta() throws Exception {
            Palpite palpiteExistente = palpiteRepository.save(Palpite.builder()
                    .partida(partida)
                    .usuario(usuario)
                    .grupo(grupo)
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(ZoneOffset.UTC))
                    .build());

            mockMvc.perform(delete("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                    grupo.getId(), partida.getId(), palpiteExistente.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Excluir palpite em partida aberta fora da janela retorna 400")
        void excluirPalpitePartidaAbertaForaJanela() throws Exception {
            Palpite palpiteExistente = palpiteRepository.save(Palpite.builder()
                    .partida(partidaAbertaForaJanela)
                    .usuario(usuario)
                    .grupo(grupo)
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(ZoneOffset.UTC))
                    .build());

            mockMvc.perform(delete("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                    grupo.getId(), partidaAbertaForaJanela.getId(), palpiteExistente.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Excluir palpite em partida em andamento retorna 400")
        void excluirPalpitePartidaEmAndamento() throws Exception {
            Palpite palpiteExistente = palpiteRepository.save(Palpite.builder()
                    .partida(partidaEmAndamento)
                    .usuario(usuario)
                    .grupo(grupo)
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(ZoneOffset.UTC))
                    .build());

            mockMvc.perform(delete("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                    grupo.getId(), partidaEmAndamento.getId(), palpiteExistente.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Excluir palpite em partida finalizada retorna 400")
        void excluirPalpitePartidaFinalizada() throws Exception {
            Palpite palpiteExistente = palpiteRepository.save(Palpite.builder()
                    .partida(partidaFinalizada)
                    .usuario(usuario)
                    .grupo(grupo)
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(ZoneOffset.UTC))
                    .build());

            mockMvc.perform(delete("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                    grupo.getId(), partidaFinalizada.getId(), palpiteExistente.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Excluir palpite em partida cancelada retorna 400")
        void excluirPalpitePartidaCancelada() throws Exception {
            Palpite palpiteExistente = palpiteRepository.save(Palpite.builder()
                    .partida(partidaCancelada)
                    .usuario(usuario)
                    .grupo(grupo)
                    .golsMandante(1)
                    .golsVisitante(1)
                    .data(LocalDateTime.now(ZoneOffset.UTC))
                    .build());

            mockMvc.perform(delete("/grupos/{grupoId}/partidas/{partidaId}/palpites/{palpiteId}",
                    grupo.getId(), partidaCancelada.getId(), palpiteExistente.getId())
                    .param("usuarioId", usuario.getId().toString())
                    .param("codigoUsuario", usuario.getCodigo()))
                    .andExpect(status().isBadRequest());
        }
    }
}
