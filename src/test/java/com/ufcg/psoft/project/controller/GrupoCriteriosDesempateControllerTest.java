package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.grupo.CriteriosDesempatePutRequestDTO;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
                            TipoCriterioDesempate.PLACARES_EXATOS,
                            TipoCriterioDesempate.ERROS,
                            TipoCriterioDesempate.ACERTOS_VENCEDOR,
                            TipoCriterioDesempate.ACERTOS_EMPATE))
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
                            TipoCriterioDesempate.PLACARES_EXATOS,
                            TipoCriterioDesempate.PLACARES_EXATOS,
                            TipoCriterioDesempate.ACERTOS_VENCEDOR,
                            TipoCriterioDesempate.ACERTOS_EMPATE))
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
                    .criteriosDesempate(List.of(TipoCriterioDesempate.PLACARES_EXATOS))
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
        @DisplayName("Quando um usuário que não é organizador tenta configurar os critérios")
        void quandoNaoOrganizadorTentaConfigurar() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                    .criteriosDesempate(List.of(
                            TipoCriterioDesempate.PLACARES_EXATOS,
                            TipoCriterioDesempate.ERROS,
                            TipoCriterioDesempate.ACERTOS_VENCEDOR,
                            TipoCriterioDesempate.ACERTOS_EMPATE))
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
    }
}