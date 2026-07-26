package com.ufcg.psoft.project.controller.grupo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.grupo.RegraPontuacaoPostPutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.RegraPontuacaoResponseDTO;
import com.ufcg.psoft.project.exception.CustomErrorType;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes sobre as regras de pontuação do controlador de grupos de bolão")
public class GrupoRegraPontuacaoControllerTest {

    final String URI_GRUPOS = "/grupos";

    @Autowired
    MockMvc driver;
    @Autowired
    GrupoRepository grupoRepository;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    CampeonatoRepository campeonatoRepository;
    @Autowired
    ObjectMapper objectMapper;

    Usuario organizador;
    Usuario outroUsuario;
    Campeonato campeonatoAtivo;
    Grupo grupoPublico;
    Grupo grupoPrivado;
    RegraPontuacaoPostPutRequestDTO regraPontuacaoDto;

    @BeforeEach
    void setup() {
        organizador = usuarioRepository.save(Usuario.builder()
                .nome("João organizador")
                .codigo("XYZ123")
                .email("joao@teste.com")
                .username("joao")
                .endereco("Rua A")
                .perfil(PerfilUsuario.PADRAO)
                .build());

        outroUsuario = usuarioRepository.save(Usuario.builder()
                .nome("Outro Usuario")
                .username("outrousuario")
                .email("outro@email.com")
                .endereco("Rua do Outro, 3")
                .codigo("333333")
                .build());

        campeonatoAtivo = campeonatoRepository.save(Campeonato.builder()
                .nome("Campeonato Ativo")
                .url("http://campeonato-ativo.com")
                .codigo("CAT001")
                .ativo(true)
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

        regraPontuacaoDto = RegraPontuacaoPostPutRequestDTO.builder()
                .tipoRegraPontuacao(TipoRegraPontuacao.ACERTO_VENCEDOR)
                .pontos(10)
                .build();
    }

    @AfterEach
    void tearDown() {
        grupoRepository.deleteAll();
        campeonatoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("Conjunto de casos de inserção de regras de pontuação")
    class inserirRegraPontuacao {

        @Test
        @DisplayName("Quando o organizador insere uma regra de pontuação com sucesso")
        void quandoOrganizadorInsereRegraPontuacaoComSucesso() throws Exception {
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoUsuario", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(regraPontuacaoDto)))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            assertNotNull(responseJsonString);
        }

        @Test
        @DisplayName("Quando se tenta inserir regra com tipo já existente no grupo")
        void quandoTentaInserirRegraDuplicada() throws Exception {
            driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("usuarioId", organizador.getId().toString())
                        .param("codigoUsuario", organizador.getCodigo())
                        .content(objectMapper.writeValueAsString(regraPontuacaoDto)))
                    .andExpect(status().isCreated());

            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("usuarioId", organizador.getId().toString())
                        .param("codigoUsuario", organizador.getCodigo())
                        .content(objectMapper.writeValueAsString(regraPontuacaoDto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Já existe uma regra de pontuação com esse tipo para o grupo!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando o organizador insere todos os tipos de regra de pontuação")
        void quandoOrganizadorInsereVariostiposDeRegraPontuacao() throws Exception {
            for (TipoRegraPontuacao tipo : TipoRegraPontuacao.values()) {
                RegraPontuacaoPostPutRequestDTO dto = RegraPontuacaoPostPutRequestDTO.builder()
                        .tipoRegraPontuacao(tipo)
                        .pontos(5)
                        .build();

                driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("usuarioId", organizador.getId().toString())
                                .param("codigoUsuario", organizador.getCodigo())
                                .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isCreated())
                        .andDo(print());
            }
        }

        @Test
        @DisplayName("Quando um não-organizador tenta inserir uma regra de pontuação")
        void quandoNaoOrganizadorTentaInserirRegraPontuacao() throws Exception {
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", outroUsuario.getId().toString())
                            .param("codigoUsuario", outroUsuario.getCodigo())
                            .content(objectMapper.writeValueAsString(regraPontuacaoDto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Permissão negada para acessar este recurso.", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando se tenta inserir regra com tipo nulo")
        void quandoTentaInserirRegraComTipoNulo() throws Exception {
            RegraPontuacaoPostPutRequestDTO dtoInvalido = RegraPontuacaoPostPutRequestDTO.builder()
                    .tipoRegraPontuacao(null)
                    .pontos(10)
                    .build();

            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoUsuario", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dtoInvalido)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertTrue(resultado.getErrors().contains("Tipo de regra de pontuação obrigatório"))
            );
        }

        @Test
        @DisplayName("Quando se tenta inserir regra com pontos nulos")
        void quandoTentaInserirRegraComPontosNulos() throws Exception {
            RegraPontuacaoPostPutRequestDTO dtoInvalido = RegraPontuacaoPostPutRequestDTO.builder()
                    .tipoRegraPontuacao(TipoRegraPontuacao.ACERTO_VENCEDOR)
                    .pontos(null)
                    .build();

            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoUsuario", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dtoInvalido)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertTrue(resultado.getErrors().contains("Pontos da regra de pontuação obrigatórios"))
            );
        }

        @Test
        @DisplayName("Quando se tenta inserir regra com pontos não positivos")
        void quandoTentaInserirRegraComPontosNaoPositivos() throws Exception {
            RegraPontuacaoPostPutRequestDTO dtoInvalido = RegraPontuacaoPostPutRequestDTO.builder()
                    .tipoRegraPontuacao(TipoRegraPontuacao.ACERTO_VENCEDOR)
                    .pontos(-5)
                    .build();

            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoUsuario", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dtoInvalido)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertTrue(resultado.getErrors().contains("Os pontos da regra de pontuação devem ser um número positivo"))
            );
        }

        @Test
        @DisplayName("Quando se tenta inserir regra em grupo inexistente")
        void quandoTentaInserirRegraEmGrupoInexistente() throws Exception {
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + 999999L + "/regras-pontuacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoUsuario", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(regraPontuacaoDto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Esse grupo não existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando se tenta inserir regra com código de acesso inválido")
        void quandoTentaInserirRegraComCodigoInvalido() throws Exception {
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoUsuario", "999999")
                            .content(objectMapper.writeValueAsString(regraPontuacaoDto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Código de acesso inválido!", resultado.getMessage())
            );
        }
    }

    @Nested
	@DisplayName("Conjunto de casos de listagem de regras de pontuação")
	class listarRegrasPontuacao {

		@BeforeEach
		void setupRegras() throws Exception {
			driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
					.contentType(MediaType.APPLICATION_JSON)
					.param("usuarioId", organizador.getId().toString())
					.param("codigoUsuario", organizador.getCodigo())
					.content(objectMapper.writeValueAsString(regraPontuacaoDto)))
				.andExpect(status().isCreated());
		}

		@Test
		@DisplayName("Quando o organizador lista regras de pontuação com sucesso")
		void quandoOrganizadorListaRegrasPontuacaoComSucesso() throws Exception {
			String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
							.contentType(MediaType.APPLICATION_JSON)
							.param("usuarioId", organizador.getId().toString())
							.param("codigoUsuario", organizador.getCodigo()))
					.andExpect(status().isOk())
					.andDo(print())
					.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

			assertAll(
					() -> assertNotNull(responseJsonString),
					() -> assertFalse(responseJsonString.isEmpty())
			);
		}

		@Test
		@DisplayName("Quando um participante lista regras de pontuação com sucesso")
		void quandoParticipanteListaRegrasPontuacaoComSucesso() throws Exception {
			Grupo grupoAtualizado = grupoRepository.findById(grupoPublico.getId()).orElseThrow();
			grupoAtualizado.getParticipantes().add(outroUsuario);
			grupoRepository.save(grupoAtualizado);

			String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
							.contentType(MediaType.APPLICATION_JSON)
							.param("usuarioId", outroUsuario.getId().toString())
							.param("codigoUsuario", outroUsuario.getCodigo()))
					.andExpect(status().isOk())
					.andDo(print())
					.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

			assertAll(
					() -> assertNotNull(responseJsonString),
					() -> assertFalse(responseJsonString.isEmpty())
			);
		}

		@Test
		@DisplayName("Quando um usuário não membro tenta listar regras de pontuação de um grupo privado")
		void quandoUsuarioNaoMembroTentaListarRegrasPontuacaoDeGrupoPrivado() throws Exception {
			String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupoPrivado.getId() + "/regras-pontuacao")
							.contentType(MediaType.APPLICATION_JSON)
							.param("usuarioId", outroUsuario.getId().toString())
							.param("codigoUsuario", outroUsuario.getCodigo()))
					.andExpect(status().isBadRequest())
					.andDo(print())
					.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

			CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

			assertAll(
					() -> assertEquals("Permissão negada para acessar este recurso.", resultado.getMessage())
			);
		}

		@Test
		@DisplayName("Quando um usuário não membro lista regras de pontuação de um grupo público com sucesso")
		void quandoUsuarioNaoMembroListaRegrasPontuacaoDeGrupoPublicoComSucesso() throws Exception {
			String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
							.contentType(MediaType.APPLICATION_JSON)
							.param("usuarioId", outroUsuario.getId().toString())
							.param("codigoUsuario", outroUsuario.getCodigo()))
					.andExpect(status().isOk())
					.andDo(print())
					.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

			assertAll(
					() -> assertNotNull(responseJsonString),
					() -> assertFalse(responseJsonString.isEmpty())
			);
		}

		@Test
		@DisplayName("Quando o organizador lista regras e o retorno contém os dados corretos")
		void quandoOrganizadorListaRegrasEVerificaDados() throws Exception {
			String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
							.contentType(MediaType.APPLICATION_JSON)
							.param("usuarioId", organizador.getId().toString())
							.param("codigoUsuario", organizador.getCodigo()))
					.andExpect(status().isOk())
					.andDo(print())
					.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

			Set<RegraPontuacaoResponseDTO> resultado = objectMapper.readValue(
					responseJsonString,
					objectMapper.getTypeFactory().constructCollectionType(Set.class, RegraPontuacaoResponseDTO.class)
			);

			assertAll(
					() -> assertFalse(resultado.isEmpty()),
					() -> assertEquals(1, resultado.size()),
					() -> assertEquals(regraPontuacaoDto.getTipoRegraPontuacao(), resultado.iterator().next().getTipoRegraPontuacao()),
					() -> assertEquals(regraPontuacaoDto.getPontos(), resultado.iterator().next().getPontos())
			);
		}

		@Test
		@DisplayName("Quando o organizador lista regras de um grupo sem regras cadastradas")
		void quandoOrganizadorListaRegrasDeGrupoSemRegras() throws Exception {
			String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupoPrivado.getId() + "/regras-pontuacao")
							.contentType(MediaType.APPLICATION_JSON)
							.param("usuarioId", organizador.getId().toString())
							.param("codigoUsuario", organizador.getCodigo()))
					.andExpect(status().isOk())
					.andDo(print())
					.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

			Set<RegraPontuacaoResponseDTO> resultado = objectMapper.readValue(
					responseJsonString,
					objectMapper.getTypeFactory().constructCollectionType(Set.class, RegraPontuacaoResponseDTO.class)
			);

			assertAll(
					() -> assertNotNull(resultado),
					() -> assertTrue(resultado.isEmpty())
			);
		}

		@Test
		@DisplayName("Quando se tenta listar regras com usuário inexistente")
		void quandoTentaListarRegrasComUsuarioInexistente() throws Exception {
			String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
							.contentType(MediaType.APPLICATION_JSON)
							.param("usuarioId", "999999")
							.param("codigoUsuario", organizador.getCodigo()))
					.andExpect(status().isBadRequest())
					.andDo(print())
					.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

			CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

			assertAll(
					() -> assertEquals("O usuário consultado não existe!", resultado.getMessage())
			);
		}

		@Test
		@DisplayName("Quando se tenta listar regras de grupo inexistente")
		void quandoTentaListarRegrasDeGrupoInexistente() throws Exception {
			String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + 999999L + "/regras-pontuacao")
							.contentType(MediaType.APPLICATION_JSON)
							.param("usuarioId", organizador.getId().toString())
							.param("codigoUsuario", organizador.getCodigo()))
					.andExpect(status().isBadRequest())
					.andDo(print())
					.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

			CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

			assertAll(
					() -> assertEquals("Esse grupo não existe!", resultado.getMessage())
			);
		}

		@Test
		@DisplayName("Quando se tenta listar regras com código de acesso inválido")
		void quandoTentaListarRegrasComCodigoInvalido() throws Exception {
			String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
							.contentType(MediaType.APPLICATION_JSON)
							.param("usuarioId", organizador.getId().toString())
							.param("codigoUsuario", "999999"))
					.andExpect(status().isBadRequest())
					.andDo(print())
					.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

			CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

			assertAll(
					() -> assertEquals("Código de acesso inválido!", resultado.getMessage())
			);
		}
    }

    @Nested
    @DisplayName("Conjunto de casos de remoção de regras de pontuação")
    class removerRegraPontuacao {

        Long regraId;

        @BeforeEach
        void setupRegra() throws Exception {
            String responseJsonString = driver.perform(post(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoUsuario", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(regraPontuacaoDto)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            regraId = objectMapper.readTree(responseJsonString)
                    .path("id")
                    .asLong();
        }

        @Test
        @DisplayName("Quando o organizador remove uma regra de pontuação com sucesso")
        void quandoOrganizadorRemoveRegraPontuacaoComSucesso() throws Exception {
            driver.perform(delete(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao/" + regraId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoUsuario", organizador.getCodigo()))
                    .andExpect(status().isNoContent())
                    .andDo(print());
        }

        @Test
        @DisplayName("Quando um não-organizador tenta remover uma regra de pontuação")
        void quandoNaoOrganizadorTentaRemoverRegraPontuacao() throws Exception {
            String responseJsonString = driver.perform(delete(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao/" + regraId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", outroUsuario.getId().toString())
                            .param("codigoUsuario", outroUsuario.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Permissão negada para acessar este recurso.", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando se tenta remover uma regra inexistente")
        void quandoTentaRemoverRegraInexistente() throws Exception {
            String responseJsonString = driver.perform(delete(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao/" + 999999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoUsuario", organizador.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Essa regra de pontuação não existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando se tenta remover regra de grupo inexistente")
        void quandoTentaRemoverRegraDeGrupoInexistente() throws Exception {
            String responseJsonString = driver.perform(delete(URI_GRUPOS + "/" + 999999L + "/regras-pontuacao/" + regraId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoUsuario", organizador.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Esse grupo não existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando se tenta remover regra com código de acesso inválido")
        void quandoTentaRemoverRegraComCodigoInvalido() throws Exception {
            String responseJsonString = driver.perform(delete(URI_GRUPOS + "/" + grupoPublico.getId() + "/regras-pontuacao/" + regraId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoUsuario", "999999"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Código de acesso inválido!", resultado.getMessage())
            );
        }
    }
}