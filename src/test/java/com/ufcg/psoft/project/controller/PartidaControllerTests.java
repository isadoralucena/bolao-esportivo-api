package com.ufcg.psoft.project.controller;

import static com.ufcg.psoft.project.config.TestClockConfig.FIXED_CLOCK;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.campeonato.CampeonatoResponseDTO;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.*;
import com.ufcg.psoft.project.service.campeonato.CampeonatoService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import lombok.RequiredArgsConstructor;
import org.springframework.test.context.TestConstructor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Partidas")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PartidaControllerTests {

    final MockMvc mockMvc;

    final ObjectMapper objectMapper;

    final UsuarioRepository usuarioRepository;

    final CampeonatoRepository campeonatoRepository;

    final GrupoRepository grupoRepository;

    final PartidaRepository partidaRepository;

    final PalpiteRepository palpiteRepository;

    @MockBean
    CampeonatoService campeonatoService;

    private Usuario usuario;
    private Usuario adminUser;
    private Campeonato campeonato;
    private Campeonato campeonatoVazio;
    private Grupo grupo;
    private Grupo grupoVazio;
    private Partida partidaAberta;
    private Partida partidaFinalizada;

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

        campeonatoVazio = campeonatoRepository.save(Campeonato.builder()
                .nome("Campeonato Vazio")
                .url("https://api.football-data.org/v4/competitions/2013")
                .codigo("VAZ")
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

        grupoVazio = grupoRepository.save(Grupo.builder()
                .nome("Grupo Vazio")
                .descricao("Sem partidas")
                .campeonato(campeonatoVazio)
                .privacidade(PrivacidadeGrupo.PUBLICA)
                .organizador(usuario)
                .limiteParticipantes(10)
                .build());

        grupoVazio.getParticipantes().add(usuario);
        grupoRepository.save(grupoVazio);

        partidaAberta = partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(1L)
                .mandante("Time A")
                .visitante("Time B")
                .data(LocalDateTime.now(FIXED_CLOCK).plusDays(7))
                .status(PartidaStatus.ABERTO)
                .build());

        partidaFinalizada = partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(2L)
                .mandante("Time C")
                .visitante("Time D")
                .data(LocalDateTime.now(FIXED_CLOCK).minusDays(1))
                .status(PartidaStatus.FINALIZADO)
                .golsMandante(3)
                .golsVisitante(1)
                .build());

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

    // ========== Listagem por campeonato ==========

    @Test
    @DisplayName("Listar partidas de campeonato com partidas")
    void listarPartidasDoCampeonato_comPartidas() throws Exception {
        mockMvc.perform(get("/campeonatos/{campeonatoId}/partidas", campeonato.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Listar partidas de campeonato sem partidas retorna array vazio")
    void listarPartidasDoCampeonato_semPartidas() throws Exception {
        mockMvc.perform(get("/campeonatos/{campeonatoId}/partidas", campeonatoVazio.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Listar partidas de campeonato inexistente retorna array vazio")
    void listarPartidasDoCampeonato_campeonatoInexistente() throws Exception {
        mockMvc.perform(get("/campeonatos/{campeonatoId}/partidas", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Listar partidas verifica campos retornados")
    void listarPartidasDoCampeonato_verificaCampos() throws Exception {
        mockMvc.perform(get("/campeonatos/{campeonatoId}/partidas", campeonato.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mandante").value("Time A"))
                .andExpect(jsonPath("$[0].visitante").value("Time B"));
    }

    @Test
    @DisplayName("Listar partidas com status ABERTO e gols nulos")
    void listarPartidasDoCampeonato_statusAberto() throws Exception {
        mockMvc.perform(get("/campeonatos/{campeonatoId}/partidas", campeonato.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ABERTO"))
                .andExpect(jsonPath("$[0].golsMandante").isEmpty());
    }

    @Test
    @DisplayName("Listar partidas com status FINALIZADO e gols preenchidos")
    void listarPartidasDoCampeonato_statusFinalizado() throws Exception {
        mockMvc.perform(get("/campeonatos/{campeonatoId}/partidas", campeonato.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].status").value("FINALIZADO"))
                .andExpect(jsonPath("$[1].golsMandante").value(3))
                .andExpect(jsonPath("$[1].golsVisitante").value(1));
    }

    // ========== Listagem por grupo ==========

    @Test
    @DisplayName("Listar partidas do grupo com partidas")
    void listarPartidasDoGrupo_comPartidas() throws Exception {
        mockMvc.perform(get("/grupos/{grupoId}/partidas", grupo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Listar partidas do grupo sem partidas retorna array vazio")
    void listarPartidasDoGrupo_semPartidas() throws Exception {
        mockMvc.perform(get("/grupos/{grupoId}/partidas", grupoVazio.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Listar partidas de grupo inexistente retorna 400")
    void listarPartidasDoGrupo_grupoInexistente() throws Exception {
        mockMvc.perform(get("/grupos/{grupoId}/partidas", 999L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Listar partidas do grupo verifica campos")
    void listarPartidasDoGrupo_verificaCampos() throws Exception {
        mockMvc.perform(get("/grupos/{grupoId}/partidas", grupo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mandante").value("Time A"))
                .andExpect(jsonPath("$[0].visitante").value("Time B"));
    }

    @Test
    @DisplayName("Listar partidas do grupo com multiplas partidas")
    void listarPartidasDoGrupo_multiplasPartidas() throws Exception {
        partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(3L)
                .mandante("Time E")
                .visitante("Time F")
                .data(LocalDateTime.now(FIXED_CLOCK).plusDays(14))
                .status(PartidaStatus.ABERTO)
                .build());

        mockMvc.perform(get("/grupos/{grupoId}/partidas", grupo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("Listar partidas futuras do usuario autenticado")
    void listarPartidasFuturasDoUsuario() throws Exception {
        mockMvc.perform(get("/partidas/futuras")
                        .param("usuarioId", usuario.getId().toString())
                        .param("codigoUsuario", usuario.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(partidaAberta.getId()));
    }

    // ========== Integração com sincronização ==========

    @Test
    @DisplayName("Sincronizar partidas com admin gera partidas no campeonato")
    void sincronizarGeraPartidasNoCampeonato() throws Exception {
        mockMvc.perform(post("/campeonatos/{campeonatoId}/sincronizar", campeonato.getId())
                .param("userId", adminUser.getId().toString())
                .param("senha", adminUser.getCodigo()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/campeonatos/{campeonatoId}/partidas", campeonato.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Sincronizar partidas propaga dados corretos (mandante, visitante)")
    void sincronizarPartidasDadosCorretos() throws Exception {
        mockMvc.perform(post("/campeonatos/{campeonatoId}/sincronizar", campeonato.getId())
                .param("userId", adminUser.getId().toString())
                .param("senha", adminUser.getCodigo()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/campeonatos/{campeonatoId}/partidas", campeonato.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mandante").isNotEmpty())
                .andExpect(jsonPath("$[0].visitante").isNotEmpty());
    }

    @Test
    @DisplayName("Sincronizar partidas retorna campeonato sincronizado na resposta")
    void sincronizarRetornaCampeonatoNaResposta() throws Exception {
        mockMvc.perform(post("/campeonatos/{campeonatoId}/sincronizar", campeonato.getId())
                .param("userId", adminUser.getId().toString())
                .param("senha", adminUser.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").isNotEmpty());
    }

    @Test
    @DisplayName("Sincronizar duas vezes nao duplica partidas")
    void sincronizarDuasVezesNaoDuplica() throws Exception {
        mockMvc.perform(post("/campeonatos/{campeonatoId}/sincronizar", campeonato.getId())
                .param("userId", adminUser.getId().toString())
                .param("senha", adminUser.getCodigo()))
                .andExpect(status().isOk());

        String primeiroResponse = mockMvc.perform(get("/campeonatos/{campeonatoId}/partidas", campeonato.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int primeiroTamanho = JsonPath.parse(primeiroResponse).read("$", List.class).size();

        mockMvc.perform(post("/campeonatos/{campeonatoId}/sincronizar", campeonato.getId())
                .param("userId", adminUser.getId().toString())
                .param("senha", adminUser.getCodigo()))
                .andExpect(status().isOk());

        String segundoResponse = mockMvc.perform(get("/campeonatos/{campeonatoId}/partidas", campeonato.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int segundoTamanho = JsonPath.parse(segundoResponse).read("$", List.class).size();

        assertEquals(primeiroTamanho, segundoTamanho);
    }

    @Test
    @DisplayName("Partida ABERTO com janela fechada retorna EM_ANDAMENTO")
    void partidaAbertoJanelaFechadaRetornaEmAndamento() throws Exception {
        partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(10L)
                .mandante("X")
                .visitante("Y")
                .data(LocalDateTime.now(FIXED_CLOCK).minusMinutes(5))
                .status(PartidaStatus.ABERTO)
                .build());

        mockMvc.perform(get("/grupos/{grupoId}/partidas", grupo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status")
                        .value(hasItem("EM_ANDAMENTO")));
    }

    @Test
    @DisplayName("Partida ABERTO dentro da janela retorna ABERTO")
    void partidaAbertoDentroDaJanelaRetornaAberto() throws Exception {
        partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(11L)
                .mandante("W")
                .visitante("Z")
                .data(LocalDateTime.now(FIXED_CLOCK).plusMinutes(60))
                .status(PartidaStatus.ABERTO)
                .build());

        mockMvc.perform(get("/grupos/{grupoId}/partidas", grupo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status")
                        .value(hasItem("ABERTO")));
    }

    @Test
    @DisplayName("Partida FINALIZADO retorna FINALIZADO independente da janela")
    void partidaFinalizadoIgnoraJanela() throws Exception {
        partidaRepository.save(Partida.builder()
                .campeonato(campeonato)
                .codigoExterno(12L)
                .mandante("M")
                .visitante("N")
                .data(LocalDateTime.now(FIXED_CLOCK).minusDays(1))
                .status(PartidaStatus.FINALIZADO)
                .golsMandante(2)
                .golsVisitante(1)
                .build());

        mockMvc.perform(get("/grupos/{grupoId}/partidas", grupo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status")
                        .value(hasItem("FINALIZADO")));
    }
}
