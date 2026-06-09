package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.CampeonatoPostPutRequestDTO;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CampeonatoController combined tests")
public class CampeonatoControllerTest {

	private static final String URI_CAMPEONATOS = "/campeonatos";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	UsuarioRepository usuarioRepository;

	@Autowired
	CampeonatoRepository campeonatoRepository;

	private Usuario adminUser;
	private Usuario normalUser;
	private CampeonatoPostPutRequestDTO campeonatoDto;

	@BeforeEach
	void setUp() {
		adminUser = Usuario.builder()
			.nome("Admin")
			.endereco("Floriano peixoto")
			.email(UUID.randomUUID().toString() + "@example.com")
			.username("adminUser")
			.codigo("123456")
			.administrador(true)
			.build();
		usuarioRepository.save(adminUser);

		normalUser = Usuario.builder()
			.nome("Normal")
			.endereco("Rua do sol")
			.email(UUID.randomUUID().toString() + "@example.com")
			.username("normalUser")
			.codigo("654321")
			.administrador(false)
			.build();
		usuarioRepository.save(normalUser);

		campeonatoDto = CampeonatoPostPutRequestDTO.builder()
			.nome("Teste campeonato")
			.url("http://example.com")
			.codigo("C001")
			.build();
	}

	@AfterEach
	void clean() {
		campeonatoRepository.deleteAll();
		usuarioRepository.deleteAll();
	}

	@Test
	@DisplayName("Criar campeonato com credenciais corretas (admin)")
	void createCampeonatoSuccess() throws Exception {
		mockMvc.perform(post(URI_CAMPEONATOS)
				.param("userId", adminUser.getId().toString())
				.param("senha", adminUser.getCodigo())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(campeonatoDto)))
			.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("Criar campeonato com credenciais erradas")
	void createCampeonatoBadCredentials() throws Exception {
		mockMvc.perform(post(URI_CAMPEONATOS)
				.param("userId", adminUser.getId().toString())
				.param("senha", "errada")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(campeonatoDto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Listar campeonatos")
	void listCampeonatosEmpty() throws Exception {
		mockMvc.perform(get(URI_CAMPEONATOS))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
	}

	@Test
	@DisplayName("Listar campeonatos com dados")
	void listarCampeonatosComDados() throws Exception {
		campeonatoRepository.save(Campeonato.builder()
				.nome("Campeonato Brasileiro")
				.url("https://api.football-data.org/v4/competitions/2013")
				.codigo("BSA")
				.ativo(false)
				.build());

		mockMvc.perform(get(URI_CAMPEONATOS))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].nome").value("Campeonato Brasileiro"));
	}

	@Test
	@DisplayName("Buscar campeonato por nome")
	void buscarCampeonatoPorNomeEmpty() throws Exception {
		mockMvc.perform(get(URI_CAMPEONATOS + "/buscar").param("nome", "Teste"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
	}

	@Test
	@DisplayName("Buscar campeonato por nome com resultado")
	void buscarCampeonatoPorNomeComResultado() throws Exception {
		campeonatoRepository.save(Campeonato.builder()
				.nome("Campeonato Brasileiro")
				.url("https://api.football-data.org/v4/competitions/2013")
				.codigo("BSA")
				.ativo(false)
				.build());

		mockMvc.perform(get(URI_CAMPEONATOS + "/buscar").param("nome", "Brasileiro"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].nome").value("Campeonato Brasileiro"));
	}

	@Test
	@DisplayName("Criar campeonato sem nome")
	void criarCampeonatoSemNome() throws Exception {
		CampeonatoPostPutRequestDTO dto = CampeonatoPostPutRequestDTO.builder()
			.url("http://example.com")
			.codigo("C002")
			.build();
		mockMvc.perform(post(URI_CAMPEONATOS)
				.param("userId", adminUser.getId().toString())
				.param("senha", adminUser.getCodigo())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Criar campeonato com usuário não admin")
	void criarCampeonatoComUsuarioNaoAdmin() throws Exception {
		mockMvc.perform(post(URI_CAMPEONATOS)
				.param("userId", normalUser.getId().toString())
				.param("senha", normalUser.getCodigo())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(campeonatoDto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Ativar campeonato com admin")
	void ativarCampeonatoComAdmin() throws Exception {
		String response = mockMvc.perform(post(URI_CAMPEONATOS)
				.param("userId", adminUser.getId().toString())
				.param("senha", adminUser.getCodigo())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(campeonatoDto)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		Long id = objectMapper.readTree(response).get("id").asLong();
		mockMvc.perform(put(URI_CAMPEONATOS + "/" + id + "/ativar")
				.param("userId", adminUser.getId().toString())
				.param("senha", adminUser.getCodigo()))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Desativar campeonato com admin")
	void desativarCampeonatoComAdmin() throws Exception {
		String response = mockMvc.perform(post(URI_CAMPEONATOS)
				.param("userId", adminUser.getId().toString())
				.param("senha", adminUser.getCodigo())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(campeonatoDto)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		Long id = objectMapper.readTree(response).get("id").asLong();

		mockMvc.perform(put(URI_CAMPEONATOS + "/" + id + "/ativar")
				.param("userId", adminUser.getId().toString())
				.param("senha", adminUser.getCodigo()))
			.andExpect(status().isOk());

		mockMvc.perform(put(URI_CAMPEONATOS + "/" + id + "/desativar")
				.param("userId", adminUser.getId().toString())
				.param("senha", adminUser.getCodigo()))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Excluir campeonato com admin")
	void excluirCampeonatoComAdmin() throws Exception {
		String response = mockMvc.perform(post(URI_CAMPEONATOS)
				.param("userId", adminUser.getId().toString())
				.param("senha", adminUser.getCodigo())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(campeonatoDto)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		Long id = objectMapper.readTree(response).get("id").asLong();
		mockMvc.perform(delete(URI_CAMPEONATOS + "/" + id)
				.param("userId", adminUser.getId().toString())
				.param("senha", adminUser.getCodigo()))
			.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("Sincronizar campeonatos com admin")
	void sincronizarComAdmin() throws Exception {
		mockMvc.perform(post(URI_CAMPEONATOS + "/sincronizar")
				.param("userId", adminUser.getId().toString())
				.param("senha", adminUser.getCodigo()))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Ativar campeonato com usuário não admin")
	void ativarCampeonatoComUsuarioNaoAdmin() throws Exception {
		String response = mockMvc.perform(post(URI_CAMPEONATOS)
				.param("userId", adminUser.getId().toString())
				.param("senha", adminUser.getCodigo())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(campeonatoDto)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		Long id = objectMapper.readTree(response).get("id").asLong();
		mockMvc.perform(put(URI_CAMPEONATOS + "/" + id + "/ativar")
				.param("userId", normalUser.getId().toString())
				.param("senha", normalUser.getCodigo()))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Excluir campeonato com usuário não admin")
	void excluirCampeonatoComUsuarioNaoAdmin() throws Exception {
		String response = mockMvc.perform(post(URI_CAMPEONATOS)
				.param("userId", adminUser.getId().toString())
				.param("senha", adminUser.getCodigo())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(campeonatoDto)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		Long id = objectMapper.readTree(response).get("id").asLong();
		mockMvc.perform(delete(URI_CAMPEONATOS + "/" + id)
				.param("userId", normalUser.getId().toString())
				.param("senha", normalUser.getCodigo()))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Sincronizar campeonatos com usuário não admin")
	void sincronizarComUsuarioNaoAdmin() throws Exception {
		mockMvc.perform(post(URI_CAMPEONATOS + "/sincronizar")
				.param("userId", normalUser.getId().toString())
				.param("senha", normalUser.getCodigo()))
			.andExpect(status().isBadRequest());
	}
}
