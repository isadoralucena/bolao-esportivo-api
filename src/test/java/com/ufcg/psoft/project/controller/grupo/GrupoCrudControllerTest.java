package com.ufcg.psoft.project.controller.grupo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import lombok.RequiredArgsConstructor;
import org.springframework.test.context.TestConstructor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ufcg.psoft.project.dto.grupo.GrupoPostRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoPutRequestDTO;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.PerfilUsuario;
import com.ufcg.psoft.project.model.PrivacidadeGrupo;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;

import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes das operações CRUD de grupos de bolão")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class GrupoCrudControllerTest {
    final String URI_GRUPOS = "/grupos";

    final MockMvc driver;
    final ObjectMapper objectMapper;
    final GrupoRepository grupoRepository;
    final UsuarioRepository usuarioRepository;
    final CampeonatoRepository campeonatoRepository;

    Usuario organizador;
    Usuario participante;
    Campeonato campeonato;
    Grupo grupo;

    GrupoPostRequestDTO grupoPostRequestDTO;
    GrupoPutRequestDTO grupoPutRequestDTO;

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

        participante = usuarioRepository.save(Usuario.builder()
                .nome("Maria participante")
                .codigo("ABC456")
                .email("maria@teste.com")
                .username("maria")
                .endereco("Rua B")
                .perfil(PerfilUsuario.PADRAO)
                .build());

        campeonato = campeonatoRepository.save(Campeonato.builder()
                .nome("Campeonato Brasileiro")
                .url("https://api.football-data.org/v4/competitions/2013")
                .codigo("BSA")
                .ativo(true)
                .build());

        HashSet<Usuario> participantesGrupo = new HashSet<>();
        participantesGrupo.add(organizador);

        grupo = grupoRepository.save(Grupo.builder()
                .nome("Grupo base")
                .descricao("Descrição base")
                .privacidade(PrivacidadeGrupo.PUBLICA)
                .limiteParticipantes(5)
                .campeonato(campeonato)
                .organizador(organizador)
                .participantes(participantesGrupo)
                .build());

        grupoPostRequestDTO = GrupoPostRequestDTO.builder()
                .nome("Novo grupo")
                .descricao("Nova descrição")
                .privacidade(PrivacidadeGrupo.PUBLICA)
                .limiteParticipantes(10)
                .campeonatoId(campeonato.getId())
                .build();

        grupoPutRequestDTO = GrupoPutRequestDTO.builder()
                .nome("Grupo modificado")
                .descricao("Descrição modificada")
                .privacidade(PrivacidadeGrupo.PUBLICA)
                .limiteParticipantes(12)
                .build();
    }

    @AfterEach
    void tearDown() {
        grupoRepository.deleteAll();
        campeonatoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("Criação de grupos")
    class CriacaoDeGrupos {
        @Test
        @DisplayName("Quando criar um grupo válido com código de acesso correto")
        void quandoCriarGrupoValido() throws Exception {
                driver.perform(post(URI_GRUPOS)
                        .param("usuarioId", organizador.getId().toString())
                        .param("codigoUsuario", organizador.getCodigo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grupoPostRequestDTO)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").exists())
                        .andExpect(jsonPath("$.nome").value("Novo grupo"))
                        .andExpect(jsonPath("$.descricao").value("Nova descrição"))
                        .andExpect(jsonPath("$.privacidade").value("PUBLICA"))
                        .andExpect(jsonPath("$.limiteParticipantes").value(10))
                        .andExpect(jsonPath("$.organizador.id").value(organizador.getId()))
                        .andExpect(jsonPath("$.participantes.length()").value(1))
                        .andExpect(jsonPath("$.campeonato.id").value(campeonato.getId()));
        }

        @Test
        @DisplayName("Quando tentar criar um grupo com campeonato inativo")
        void quandoCriarGrupoComCampeonatoInativo() throws Exception {
                campeonato.setAtivo(false);
                campeonatoRepository.save(campeonato);

                driver.perform(post(URI_GRUPOS)
                        .param("usuarioId", organizador.getId().toString())
                        .param("codigoUsuario", organizador.getCodigo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grupoPostRequestDTO)))
                        .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Quando tentar criar um grupo com código de acesso inválido")
        void quandoCriarGrupoCodigoInvalido() throws Exception {
                driver.perform(post(URI_GRUPOS)
                        .param("usuarioId", organizador.getId().toString())
                        .param("codigoUsuario", "CODIGO_ERRADO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grupoPostRequestDTO)))
                        .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Quando criar grupo sem nome")
        void quandoCriarGrupoSemNome() throws Exception {
                grupoPostRequestDTO.setNome(null);

                driver.perform(post(URI_GRUPOS)
                        .param("usuarioId", organizador.getId().toString())
                        .param("codigoUsuario", organizador.getCodigo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grupoPostRequestDTO)))
                        .andExpect(status().isBadRequest());
        }

	}

	@Nested
    @DisplayName("Consulta de um grupo específico")
    class ConsultaDeGrupo {
		@Test
		@DisplayName("Quando recuperar um grupo público existente")
		void quandoRecuperarGrupoExistente() throws Exception {
			driver.perform(get(URI_GRUPOS + "/" + grupo.getId())
					.param("usuarioId", participante.getId().toString())
					.param("codigoUsuario", participante.getCodigo()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(grupo.getId()))
					.andExpect(jsonPath("$.nome").value(grupo.getNome()))
					.andExpect(jsonPath("$.descricao").value(grupo.getDescricao()))
					.andExpect(jsonPath("$.organizador.id").value(organizador.getId()));
		}

		@Test
		@DisplayName("Quando recuperar um grupo inexistente")
		void quandoRecuperarGrupoInexistente() throws Exception {
			driver.perform(get(URI_GRUPOS + "/99999")
					.param("usuarioId", participante.getId().toString())
					.param("codigoUsuario", participante.getCodigo()))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Quando usuário não membro tenta recuperar grupo privado")
		void quandoUsuarioNaoMembroTentaRecuperarGrupoPrivado() throws Exception {
			HashSet<Usuario> participantesPrivado = new HashSet<>();
			participantesPrivado.add(organizador);

			Grupo grupoPrivado = grupoRepository.save(Grupo.builder()
					.nome("Grupo privado")
					.descricao("Descrição privada")
					.privacidade(PrivacidadeGrupo.PRIVADA)
					.limiteParticipantes(5)
					.campeonato(campeonato)
					.organizador(organizador)
					.participantes(participantesPrivado)
					.build());

			driver.perform(get(URI_GRUPOS + "/" + grupoPrivado.getId())
					.param("usuarioId", participante.getId().toString())
					.param("codigoUsuario", participante.getCodigo()))
					.andExpect(status().isBadRequest());
		}
	}

	@Nested
    @DisplayName("Listagem de grupos")
    class ListagemDeGrupos {
		@Test
		@DisplayName("Quando listar grupos disponíveis")
		void quandoListarGrupos() throws Exception {
			driver.perform(get(URI_GRUPOS)
					.param("usuarioId", participante.getId().toString())
					.param("codigoUsuario", participante.getCodigo()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$[0].id").value(grupo.getId()))
					.andExpect(jsonPath("$[0].nome").value(grupo.getNome()));
		}

		@Test
        @DisplayName("Quando tentar listar grupos com usuário inexistente")
        void quandoListarGruposComUsuarioInexistente() throws Exception {
            driver.perform(get(URI_GRUPOS)
                    .param("usuarioId", "99999")
                    .param("codigoUsuario", "qualquer"))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        @DisplayName("Quando tentar listar grupos com código de acesso inválido")
        void quandoListarGruposComCodigoInvalido() throws Exception {
            driver.perform(get(URI_GRUPOS)
                    .param("usuarioId", participante.getId().toString())
                    .param("codigoUsuario", "CODIGO_ERRADO"))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        @DisplayName("Quando listar grupos, grupo privado do qual usuário não participa não deve aparecer")
        void quandoListarGruposNaoDeveExibirGrupoPrivadoDeOutroUsuario() throws Exception {
            HashSet<Usuario> participantesPrivado = new HashSet<>();
            participantesPrivado.add(organizador);
 
            Grupo grupoPrivado = grupoRepository.save(Grupo.builder()
                    .nome("Grupo privado")
                    .descricao("Descrição privada")
                    .privacidade(PrivacidadeGrupo.PRIVADA)
                    .limiteParticipantes(5)
                    .campeonato(campeonato)
                    .organizador(organizador)
                    .participantes(participantesPrivado)
                    .build());
 
            driver.perform(get(URI_GRUPOS)
                    .param("usuarioId", participante.getId().toString())
                    .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[?(@.id == " + grupoPrivado.getId() + ")]").doesNotExist());
        }
	}

	@Nested
    @DisplayName("Atualização de grupos")
    class AtualizacaoDeGrupos {
 
        @Test
        @DisplayName("Quando o organizador atualiza os dados do grupo")
        void quandoAtualizarGrupoComSucesso() throws Exception {
            driver.perform(put(URI_GRUPOS + "/" + grupo.getId())
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(grupoPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(grupo.getId()))
                    .andExpect(jsonPath("$.nome").value("Grupo modificado"))
                    .andExpect(jsonPath("$.descricao").value("Descrição modificada"))
                    .andExpect(jsonPath("$.privacidade").value("PUBLICA"))
                    .andExpect(jsonPath("$.limiteParticipantes").value(12));
        }
 
        @Test
        @DisplayName("Quando tentar reduzir limite abaixo da quantidade atual de participantes")
        void quandoTentarReduzirLimiteAbaixoDaQuantidadeAtualDeParticipantes() throws Exception {
            grupo.getParticipantes().add(participante);
            grupoRepository.save(grupo);
 
            grupoPutRequestDTO.setLimiteParticipantes(1);
 
            driver.perform(put(URI_GRUPOS + "/" + grupo.getId())
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(grupoPutRequestDTO)))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        @DisplayName("Quando tentar atualizar grupo inexistente")
        void quandoAtualizarGrupoInexistente() throws Exception {
            driver.perform(put(URI_GRUPOS + "/99999")
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(grupoPutRequestDTO)))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        @DisplayName("Quando tentar atualizar grupo com nome vazio")
        void quandoAtualizarGrupoComNomeInvalido() throws Exception {
            grupoPutRequestDTO.setNome("");
 
            driver.perform(put(URI_GRUPOS + "/" + grupo.getId())
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(grupoPutRequestDTO)))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        @DisplayName("Quando um usuário comum (não organizador) tenta atualizar os dados do grupo")
        void quandoUsuarioTentaAtualizarGrupo() throws Exception {
            driver.perform(put(URI_GRUPOS + "/" + grupo.getId())
                    .param("usuarioId", participante.getId().toString())
                    .param("codigoUsuario", participante.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(grupoPutRequestDTO)))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        @DisplayName("Quando o novo limite é igual à quantidade atual de participantes")
        void quandoAtualizarLimiteIgualAQuantidadeAtualDeParticipantes() throws Exception {
            grupo.getParticipantes().add(participante);
            grupoRepository.save(grupo);
 
            grupoPutRequestDTO.setLimiteParticipantes(2);
 
            driver.perform(put(URI_GRUPOS + "/" + grupo.getId())
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(grupoPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.limiteParticipantes").value(2));
        }
 
        @Test
        @DisplayName("Quando o novo limite é maior que a quantidade atual de participantes")
        void quandoAtualizarLimiteMaiorQueQuantidadeAtualDeParticipantes() throws Exception {
            grupo.getParticipantes().add(participante);
            grupoRepository.save(grupo);
 
            grupoPutRequestDTO.setLimiteParticipantes(10);
 
            driver.perform(put(URI_GRUPOS + "/" + grupo.getId())
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(grupoPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.limiteParticipantes").value(10));
        }
 
        @Test
        @DisplayName("Quando o novo limite é nulo, não deve validar contra a quantidade de participantes")
        void quandoAtualizarGrupoComLimiteNulo() throws Exception {
            grupo.getParticipantes().add(participante);
            grupoRepository.save(grupo);
 
            grupoPutRequestDTO.setLimiteParticipantes(null);
 
            driver.perform(put(URI_GRUPOS + "/" + grupo.getId())
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(grupoPutRequestDTO)))
                    .andExpect(status().isOk());
        }
    }
 
	@Nested
    @DisplayName("Remoção de grupos)")
    class RemocaoDeGrupos {
 
        @Test
        @DisplayName("Quando o organizador remove o grupo")
        void quandoRemoverGrupoComSucesso() throws Exception {
            driver.perform(delete(URI_GRUPOS + "/" + grupo.getId())
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo()))
                    .andExpect(status().isNoContent());
        }
 
        @Test
        @DisplayName("Quando usuário comum tenta remover o grupo")
        void quandoUsuarioComumTentaRemoverGrupo() throws Exception {
            driver.perform(delete(URI_GRUPOS + "/" + grupo.getId())
                    .param("usuarioId", participante.getId().toString())
                    .param("codigoUsuario", participante.getCodigo()))
                    .andExpect(status().isBadRequest());
        }
 
        @Test
        @DisplayName("Quando tentar remover um grupo inexistente")
        void quandoRemoverGrupoInexistente() throws Exception {
            driver.perform(delete(URI_GRUPOS + "/99999")
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo()))
                    .andExpect(status().isBadRequest());
        }
    }
}
