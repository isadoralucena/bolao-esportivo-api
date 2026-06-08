package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.CampeonatoPostPutRequestDTO;
import com.ufcg.psoft.project.model.PerfilUsuario;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CampeonatoController autenticação")
public class CampeonatoControllerAuthTest {

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
	private CampeonatoPostPutRequestDTO campeonatoDto;

	@BeforeEach
	void setUp() {
		adminUser = Usuario.builder()
			.nome("Admin")
			.endereco("Admin St")
			.email("admin@example.com")
			.codigo("123456")
			.perfil(PerfilUsuario.ADMIN)
			.build();
		usuarioRepository.save(adminUser);

		campeonatoDto = CampeonatoPostPutRequestDTO.builder()
			.nome("Teste")
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
	@DisplayName("Criar campeonato com credenciais corretas")
	void createCampeonatoSuccess() throws Exception {
		mockMvc.perform(post(URI_CAMPEONATOS)
				.param("email", adminUser.getEmail())
				.param("senha", adminUser.getCodigo())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(campeonatoDto)))
			.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("Criar campeonato com credenciais erradas")
	void createCampeonatoBadCredentials() throws Exception {
		mockMvc.perform(post(URI_CAMPEONATOS)
				.param("email", adminUser.getEmail())
				.param("senha", "wrongcode")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(campeonatoDto)))
			.andExpect(status().isBadRequest());
	}
}
