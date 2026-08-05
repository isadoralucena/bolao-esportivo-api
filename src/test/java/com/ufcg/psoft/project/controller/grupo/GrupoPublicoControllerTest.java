package com.ufcg.psoft.project.controller.grupo;

import static com.ufcg.psoft.project.config.TestClockConfig.FIXED_CLOCK;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.exception.CustomErrorType;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.model.PrivacidadeGrupo;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;

import org.junit.jupiter.api.*;
import lombok.RequiredArgsConstructor;
import org.springframework.test.context.TestConstructor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes das regras de entrada em grupos públicos de bolão")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class GrupoPublicoControllerTest {

    final String URI_GRUPOS = "/grupos";

    MockMvc driver;

    final GrupoRepository grupoRepository;

    final UsuarioRepository usuarioRepository;

    final CampeonatoRepository campeonatoRepository;

    final PartidaRepository partidaRepository;

    final WebApplicationContext webApplicationContext;

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
		driver = MockMvcBuilders
				.webAppContextSetup(webApplicationContext)
				.defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
				.build();

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
				.privacidade(PrivacidadeGrupo.PUBLICA)
				.limiteParticipantes(10)
				.campeonato(campeonatoAtivo)
				.organizador(organizador)
				.build());
		grupoPublico.getParticipantes().add(organizador);
		grupoRepository.save(grupoPublico);

		grupoPrivado = grupoRepository.save(Grupo.builder()
				.nome("Grupo Privado")
				.descricao("Grupo privado para testes")
				.privacidade(PrivacidadeGrupo.PRIVADA)
				.limiteParticipantes(10)
				.campeonato(campeonatoAtivo)
				.organizador(organizador)
				.build());
		grupoPrivado.getParticipantes().add(organizador);
		grupoRepository.save(grupoPrivado);

		grupoPublicoSemVagas = grupoRepository.save(Grupo.builder()
				.nome("Grupo Publico Sem Vagas")
				.descricao("Grupo publico sem vagas para testes")
				.privacidade(PrivacidadeGrupo.PUBLICA)
				.limiteParticipantes(1)
				.campeonato(campeonatoAtivo)
				.organizador(organizador)
				.build());

		grupoPublicoSemVagas.getParticipantes().add(organizador);
		grupoRepository.save(grupoPublicoSemVagas);
	}

    @AfterEach
    void tearDown() {
        partidaRepository.deleteAll();
        grupoRepository.deleteAll();
        campeonatoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("Casos de sucesso ao entrar em grupo público")
    class CasosDeSucesso {
 
        @Test
        @DisplayName("Quando um usuário entra em um grupo público com sucesso")
        void quandoUsuarioEntraEmGrupoPublicoComSucesso() throws Exception {
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
 
            GrupoResponseDTO resultado = objectMapper.readValue(responseJsonString, GrupoResponseDTO.class);
 
            assertAll(
                    () -> assertNotNull(resultado.getId()),
                    () -> assertEquals(grupoPublico.getNome(), resultado.getNome()),
                    () -> assertTrue(resultado.getParticipantes().stream()
                            .anyMatch(p -> p.getId().equals(participante.getId())))
            );
        }
 
        @Test
        @DisplayName("Quando um usuário entra em grupo público sem limite de participantes")
        void quandoUsuarioEntraEmGrupoSemLimite() throws Exception {
            Grupo grupoSemLimite = grupoRepository.save(Grupo.builder()
                    .nome("Grupo Sem Limite")
                    .descricao("Grupo sem limite de participantes")
                    .privacidade(PrivacidadeGrupo.PUBLICA)
                    .limiteParticipantes(null)
                    .campeonato(campeonatoAtivo)
                    .organizador(organizador)
                    .build());
            grupoSemLimite.getParticipantes().add(organizador);
            grupoRepository.save(grupoSemLimite);
 
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoSemLimite.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
 
            GrupoResponseDTO resultado = objectMapper.readValue(responseJsonString, GrupoResponseDTO.class);
 
            assertAll(
                    () -> assertNotNull(resultado.getId()),
                    () -> assertTrue(resultado.getParticipantes().stream()
                            .anyMatch(p -> p.getId().equals(participante.getId())))
            );
        }
 
        @Test
        @DisplayName("Quando múltiplos usuários entram no mesmo grupo público")
        void quandoMultiplosUsuariosEntramNoMesmoGrupo() throws Exception {
            Usuario outroParticipante = usuarioRepository.save(Usuario.builder()
                    .nome("Outro Participante")
                    .username("outro")
                    .email("outro@email.com")
                    .endereco("Rua do Outro, 3")
                    .codigo("333333")
                    .build());
 
            Grupo grupoAtualizado = grupoRepository.findById(grupoPublico.getId()).orElseThrow();
            grupoAtualizado.getParticipantes().add(participante);
            grupoRepository.save(grupoAtualizado);
 
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", outroParticipante.getId().toString())
                            .param("codigoUsuario", outroParticipante.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
 
            GrupoResponseDTO resultado = objectMapper.readValue(responseJsonString, GrupoResponseDTO.class);
 
            assertAll(
                    () -> assertEquals(3, resultado.getParticipantes().size()),
                    () -> assertTrue(resultado.getParticipantes().stream()
                            .anyMatch(p -> p.getId().equals(organizador.getId()))),
                    () -> assertTrue(resultado.getParticipantes().stream()
                            .anyMatch(p -> p.getId().equals(participante.getId()))),
                    () -> assertTrue(resultado.getParticipantes().stream()
                            .anyMatch(p -> p.getId().equals(outroParticipante.getId())))
            );
        }
 
        @Test
        @DisplayName("Quando o campeonato possui partidas inválidas e ao menos uma válida, deve permitir entrada")
        void quandoUsuarioEntraEmGrupoComPartidasMistas() throws Exception {
            partidaRepository.save(Partida.builder()
                    .campeonato(campeonatoAtivo)
                    .codigoExterno(123L)
                    .mandante("Time A")
                    .visitante("Time B")
                    .data(LocalDateTime.now(FIXED_CLOCK).minusDays(1))
                    .status(PartidaStatus.FINALIZADO)
                    .build());
 
            partidaRepository.save(Partida.builder()
                    .campeonato(campeonatoAtivo)
                    .codigoExterno(125L)
                    .mandante("Time E")
                    .visitante("Time F")
                    .data(LocalDateTime.now(FIXED_CLOCK).plusDays(1))
                    .status(PartidaStatus.ABERTO)
                    .build());
 
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
 
            GrupoResponseDTO resultado = objectMapper.readValue(responseJsonString, GrupoResponseDTO.class);
 
            assertAll(
                    () -> assertNotNull(resultado.getId()),
                    () -> assertTrue(resultado.getParticipantes().stream()
                            .anyMatch(p -> p.getId().equals(participante.getId())))
            );
        }
    }
 
    @Nested
    @DisplayName("Validações relacionadas ao grupo")
    class ValidacaoDeGrupo {
 
        @Test
        @DisplayName("Quando um usuário tenta entrar em um grupo inexistente")
        void quandoUsuarioTentaEntrarEmGrupoInexistente() throws Exception {
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + 999999L + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
 
            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
 
            assertAll(
                    () -> assertEquals("Esse grupo não existe!", resultado.getMessage())
            );
        }
 
        @Test
        @DisplayName("Quando um usuário tenta entrar em um grupo privado")
        void quandoUsuarioTentaEntrarEmGrupoPrivado() throws Exception {
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPrivado.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
 
            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
 
            assertAll(
                    () -> assertEquals("Permissão negada para acessar este recurso.", resultado.getMessage())
            );
        }
 
        @Test
        @DisplayName("Quando um usuário tenta entrar em grupo com campeonato inativo")
        void quandoUsuarioTentaEntrarEmGrupoComCampeonatoInativo() throws Exception {
            Grupo grupoComCampeonatoInativo = grupoRepository.save(Grupo.builder()
                    .nome("Grupo Campeonato Inativo")
                    .descricao("Grupo com campeonato inativo")
                    .privacidade(PrivacidadeGrupo.PUBLICA)
                    .limiteParticipantes(10)
                    .campeonato(campeonatoInativo)
                    .organizador(organizador)
                    .build());
            grupoComCampeonatoInativo.getParticipantes().add(organizador);
            grupoRepository.save(grupoComCampeonatoInativo);
 
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoComCampeonatoInativo.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
 
            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
 
            assertAll(
                    () -> assertEquals("O campeonato associado a este grupo não está ativo!", resultado.getMessage())
            );
        }
 
        @Test
        @DisplayName("Quando um usuário tenta entrar em grupo sem partidas válidas")
        void quandoUsuarioTentaEntrarEmGrupoSemPartidasValidas() throws Exception {
            partidaRepository.save(Partida.builder()
                    .campeonato(campeonatoAtivo)
                    .codigoExterno(123L)
                    .mandante("Time A")
                    .visitante("Time B")
                    .data(LocalDateTime.now(FIXED_CLOCK).plusDays(1))
                    .status(PartidaStatus.FINALIZADO)
                    .build());
 
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
 
            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
 
            assertAll(
                    () -> assertEquals("O campeonato não possui partida válidas!", resultado.getMessage())
            );
        }
 
        @Test
        @DisplayName("Quando o campeonato possui múltiplas partidas, todas inválidas")
        void quandoUsuarioTentaEntrarEmGrupoComMultiplasPartidasInvalidas() throws Exception {
            partidaRepository.save(Partida.builder()
                    .campeonato(campeonatoAtivo)
                    .codigoExterno(123L)
                    .mandante("Time A")
                    .visitante("Time B")
                    .data(LocalDateTime.now(FIXED_CLOCK).minusDays(1))
                    .status(PartidaStatus.FINALIZADO)
                    .build());
 
            partidaRepository.save(Partida.builder()
                    .campeonato(campeonatoAtivo)
                    .codigoExterno(124L)
                    .mandante("Time C")
                    .visitante("Time D")
                    .data(LocalDateTime.now(FIXED_CLOCK).minusDays(2))
                    .status(PartidaStatus.FINALIZADO)
                    .build());
 
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
 
            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
 
            assertAll(
                    () -> assertEquals("O campeonato não possui partida válidas!", resultado.getMessage())
            );
        }
 
        @Test
        @DisplayName("Quando um usuário tenta entrar em um grupo sem vagas")
        void quandoUsuarioTentaEntrarEmGrupoSemVagas() throws Exception {
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublicoSemVagas.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
 
            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
 
            assertAll(
                    () -> assertEquals("O limite de participantes para este grupo já foi atingido!", resultado.getMessage())
            );
        }
    }

    @Nested
    @DisplayName("Validações relacionadas ao usuário")
    class ValidacaoDeUsuario {
 
        @Test
        @DisplayName("Quando um usuário inexistente tenta entrar em um grupo")
        void quandoUsuarioInexistenteTentaEntrarEmGrupo() throws Exception {
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", "999999")
                            .param("codigoUsuario", "111111"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
 
            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
 
            assertAll(
                    () -> assertEquals("O usuário consultado não existe!", resultado.getMessage())
            );
        }
 
        @Test
        @DisplayName("Quando um usuário com código inválido tenta entrar em um grupo")
        void quandoUsuarioComCodigoInvalidoTentaEntrarEmGrupo() throws Exception {
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", "999999"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
 
            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
 
            assertAll(
                    () -> assertEquals("Código de acesso inválido!", resultado.getMessage())
            );
        }
 
        @Test
        @DisplayName("Quando um usuário tenta entrar em um grupo que já participa")
        void quandoUsuarioTentaEntrarEmGrupoQueJaParticipa() throws Exception {
            Grupo grupoAtualizado = grupoRepository.findById(grupoPublico.getId()).orElseThrow();
            grupoAtualizado.getParticipantes().add(participante);
            grupoRepository.save(grupoAtualizado);
 
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString())
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
 
            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
 
            assertAll(
                    () -> assertEquals("O usuário já é participante deste grupo!", resultado.getMessage())
            );
        }
 
        @Test
        @DisplayName("Quando o organizador tenta entrar no próprio grupo")
        void quandoOrganizadorTentaEntrarNoProprioGrupo() throws Exception {
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoUsuario", organizador.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
 
            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
 
            assertAll(
                    () -> assertEquals("O usuário já é participante deste grupo!", resultado.getMessage())
            );
        }
    }

    @Nested
    @DisplayName("Validações de parâmetros obrigatórios")
    class ValidacaoDeParametros {
 
        @Test
        @DisplayName("Quando tenta entrar sem informar o usuarioId")
        void quandoEntrarSemInformarUsuarioId() throws Exception {
            driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        @DisplayName("Quando tenta entrar sem informar o codigoAcesso")
        void quandoEntrarSemInformarCodigoAcesso() throws Exception {
            driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/entrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", participante.getId().toString()))
                    .andExpect(status().isBadRequest());
        }
    }
}
