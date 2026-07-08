package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.grupo.CriteriosDesempatePutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.CriteriosDesempateResponseDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.exception.CustomErrorType;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.PerfilUsuario;
import com.ufcg.psoft.project.model.PrivacidadeGrupo;
import com.ufcg.psoft.project.model.TipoCriterioDesempate;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes sobre os critérios de desempate do controlador de grupos de bolão")
public class GrupoCriteriosDesempateControllerTest {

    private static final String URI_GRUPOS = "/grupos";

    @Autowired
    MockMvc driver;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    GrupoRepository grupoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    CampeonatoRepository campeonatoRepository;

    Usuario organizador;
    Usuario outroUsuario;
    Grupo grupo;

    @BeforeEach
    void setup() {
        organizador = usuarioRepository.save(Usuario.builder()
                .nome("Organizador Teste")
                .username("organizador")
                .email("organizador@email.com")
                .endereco("Rua A")
                .codigo("111111")
                .perfil(PerfilUsuario.PADRAO)
                .build());

        outroUsuario = usuarioRepository.save(Usuario.builder()
                .nome("Outro Usuario")
                .username("outro")
                .email("outro@email.com")
                .endereco("Rua B")
                .codigo("222222")
                .perfil(PerfilUsuario.PADRAO)
                .build());

        Campeonato campeonato = campeonatoRepository.save(Campeonato.builder()
                .nome("Campeonato Teste")
                .url("http://campeonato-teste.com")
                .codigo("CAT001")
                .ativo(true)
                .build());

        grupo = grupoRepository.save(Grupo.builder()
                .nome("Grupo Teste")
                .descricao("Grupo para testes")
                .privacidade(PrivacidadeGrupo.PUBLICA)
                .limiteParticipantes(10)
                .campeonato(campeonato)
                .organizador(organizador)
                .build());
    }

    @AfterEach
    void tearDown() {
        grupoRepository.deleteAll();
        campeonatoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("Conjunto de casos de configuração dos critérios de desempate")
    class configuracaoDeCriteriosDesempate {

        @Test
        @DisplayName("Quando o organizador configura uma ordem válida de critérios")
        void quandoOrganizadorConfiguraOrdemValida() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                    .criteriosDesempate(List.of(
                            TipoCriterioDesempate.PLACAR_EXATO,
                            TipoCriterioDesempate.ERRO,
                            TipoCriterioDesempate.ACERTO_VENCEDOR,
                            TipoCriterioDesempate.ACERTO_EMPATE))
                    .build();

            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoAcesso", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            GrupoResponseDTO resultado = objectMapper.readValue(responseJsonString, GrupoResponseDTO.class);

            assertNotNull(resultado.getCriteriosDesempate());
            assertEquals(dto.getCriteriosDesempate(), resultado.getCriteriosDesempate());
        }

        @Test
        @DisplayName("Quando o organizador envia critérios repetidos")
        void quandoOrganizadorEnviaCriteriosRepetidos() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                    .criteriosDesempate(List.of(
                            TipoCriterioDesempate.PLACAR_EXATO,
                            TipoCriterioDesempate.PLACAR_EXATO,
                            TipoCriterioDesempate.ACERTO_VENCEDOR,
                            TipoCriterioDesempate.ACERTO_EMPATE))
                    .build();

            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoAcesso", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("Os critérios de desempate devem conter ao menos 1 critério válido, sem repetição.", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando o organizador configura apenas um critério de desempate")
        void quandoOrganizadorConfiguraUmCriterio() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                    .criteriosDesempate(List.of(TipoCriterioDesempate.PLACAR_EXATO))
                    .build();

            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoAcesso", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            GrupoResponseDTO resultado = objectMapper.readValue(responseJsonString, GrupoResponseDTO.class);

            assertEquals(dto.getCriteriosDesempate(), resultado.getCriteriosDesempate());
        }

        @Test
        @DisplayName("Quando o organizador configura uma ordem parcial (2 de 4 critérios)")
        void quandoOrganizadorConfiguraOrdemParcial() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                    .criteriosDesempate(List.of(
                            TipoCriterioDesempate.ACERTO_EMPATE,
                            TipoCriterioDesempate.ERRO))
                    .build();

            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoAcesso", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            GrupoResponseDTO resultado = objectMapper.readValue(responseJsonString, GrupoResponseDTO.class);

            assertEquals(dto.getCriteriosDesempate(), resultado.getCriteriosDesempate());
        }

        @Test
        @DisplayName("Quando o organizador envia uma lista vazia de critérios")
        void quandoOrganizadorEnviaListaVazia() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                    .criteriosDesempate(Collections.emptyList())
                    .build();

            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoAcesso", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("Erros de validacao encontrados", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando o organizador envia o campo de critérios nulo (ausente no corpo)")
        void quandoOrganizadorEnviaListaNula() throws Exception {
            String corpoSemCriterios = "{}";

            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoAcesso", organizador.getCodigo())
                            .content(corpoSemCriterios))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("Erros de validacao encontrados", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando um usuário que não é organizador tenta configurar os critérios")
        void quandoNaoOrganizadorTentaConfigurar() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                    .criteriosDesempate(List.of(
                            TipoCriterioDesempate.PLACAR_EXATO,
                            TipoCriterioDesempate.ERRO,
                            TipoCriterioDesempate.ACERTO_VENCEDOR,
                            TipoCriterioDesempate.ACERTO_EMPATE))
                    .build();

            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", outroUsuario.getId().toString())
                            .param("codigoAcesso", outroUsuario.getCodigo())
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("Permissão negada para acessar este recurso.", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando o grupo informado não existe")
        void quandoGrupoNaoExistePut() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                    .criteriosDesempate(List.of(TipoCriterioDesempate.PLACAR_EXATO))
                    .build();

            Long idInexistente = 999999L;

            driver.perform(put(URI_GRUPOS + "/" + idInexistente + "/criterios-desempate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoAcesso", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Quando o código de acesso informado é inválido")
        void quandoCodigoAcessoInvalidoPut() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                    .criteriosDesempate(List.of(TipoCriterioDesempate.PLACAR_EXATO))
                    .build();

            driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoAcesso", "codigo-errado")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
        
        @Test
		@DisplayName("Quando o organizador envia um critério de desempate inválido (nulo)")
		void quandoOrganizadorEnviaCriterioDesempateInvalido() throws Exception {
			CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
					.criteriosDesempate(Arrays.asList(
							TipoCriterioDesempate.PLACAR_EXATO,
							null,
							TipoCriterioDesempate.ACERTO_VENCEDOR))
					.build();

			String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
							.contentType(MediaType.APPLICATION_JSON)
							.param("usuarioId", organizador.getId().toString())
							.param("codigoAcesso", organizador.getCodigo())
							.content(objectMapper.writeValueAsString(dto)))
					.andExpect(status().isBadRequest())
					.andDo(print())
					.andReturn()
					.getResponse()
					.getContentAsString(StandardCharsets.UTF_8);

			CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

			assertEquals(
					"Os critérios de desempate devem conter ao menos 1 critério válido, sem repetição.",
					resultado.getMessage()
			);
		}

        @Test
        @DisplayName("Quando o usuário informado não existe")
        void quandoUsuarioNaoExistePut() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                    .criteriosDesempate(List.of(TipoCriterioDesempate.PLACAR_EXATO))
                    .build();

            Long idInexistente = 999999L;

            driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("usuarioId", idInexistente.toString())
                            .param("codigoAcesso", "111111")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de consulta dos critérios de desempate")
    class consultaDeCriteriosDesempate {

        @Test
        @DisplayName("Quando o organizador consulta os critérios já configurados")
        void quandoOrganizadorConsultaCriteriosConfigurados() throws Exception {
            grupo.setCriteriosDesempate(List.of(
                    TipoCriterioDesempate.ERRO,
                    TipoCriterioDesempate.PLACAR_EXATO));
            grupoRepository.save(grupo);

            String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoAcesso", organizador.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            List<CriteriosDesempateResponseDTO> resultado = objectMapper.readValue(
                    responseJsonString, new TypeReference<>() {});

            assertEquals(2, resultado.size());
            assertEquals(TipoCriterioDesempate.ERRO, resultado.get(0).getTipoCriterioDesempate());
            assertEquals(1, resultado.get(0).getPrioridade());
            assertEquals(TipoCriterioDesempate.PLACAR_EXATO, resultado.get(1).getTipoCriterioDesempate());
            assertEquals(2, resultado.get(1).getPrioridade());
        }

        @Test
        @DisplayName("Quando nenhum critério foi configurado ainda, a consulta retorna lista vazia")
        void quandoConsultaSemCriteriosConfigurados() throws Exception {
            String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoAcesso", organizador.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            List<CriteriosDesempateResponseDTO> resultado = objectMapper.readValue(responseJsonString, new TypeReference<>() {});

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Quando um usuário que não é organizador consulta os critérios de um grupo público")
        void quandoNaoOrganizadorConsultaGrupoPublico() throws Exception {
            grupo.setCriteriosDesempate(List.of(TipoCriterioDesempate.PLACAR_EXATO));
            grupoRepository.save(grupo);

            driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                            .param("usuarioId", outroUsuario.getId().toString())
                            .param("codigoAcesso", outroUsuario.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        @DisplayName("Quando o grupo informado não existe")
        void quandoGrupoNaoExisteGet() throws Exception {
            Long idInexistente = 999999L;

            driver.perform(get(URI_GRUPOS + "/" + idInexistente + "/criterios-desempate")
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoAcesso", organizador.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Quando o código de acesso informado é inválido")
        void quandoCodigoAcessoInvalidoGet() throws Exception {
            driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                            .param("usuarioId", organizador.getId().toString())
                            .param("codigoAcesso", "codigo-errado"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}