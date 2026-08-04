package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.dto.grupo.CriteriosDesempatePutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.CriterioDesempateResponseDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.exception.CustomErrorType;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.CriterioDesempate;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.PerfilUsuario;
import com.ufcg.psoft.project.model.PrivacidadeGrupo;
import com.ufcg.psoft.project.model.TipoCriterioDesempate;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import lombok.RequiredArgsConstructor;
import org.springframework.test.context.TestConstructor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class GrupoCriteriosDesempateControllerTest {

    private static final String URI_GRUPOS = "/grupos";

    final MockMvc driver;

    final ObjectMapper objectMapper;

    final GrupoRepository grupoRepository;

    final UsuarioRepository usuarioRepository;

    final CampeonatoRepository campeonatoRepository;

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

    /**
     * Monta e persiste os critérios de desempate do grupo diretamente via
     * repositório, simulando o mesmo formato que o serviço real produz
     * (prioridade derivada da posição, começando em 1).
     */
    private void configurarCriteriosDiretamente(Grupo grupo, TipoCriterioDesempate... tipos) {
        List<CriterioDesempate> criterios = new ArrayList<>();
        for (int i = 0; i < tipos.length; i++) {
            criterios.add(CriterioDesempate.builder()
                .grupo(grupo)
                .criterio(tipos[i])
                .prioridade(i + 1)
                .build());
        }
        grupo.getCriteriosDesempate().clear();
        grupo.getCriteriosDesempate().addAll(criterios);
        grupoRepository.save(grupo);
    }

    private GrupoResponseDTO reconfigurarCriterios(CriteriosDesempatePutRequestDTO dto) throws Exception {
        String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                .contentType(MediaType.APPLICATION_JSON)
                .param("usuarioId", organizador.getId().toString())
                .param("codigoUsuario", organizador.getCodigo())
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readValue(responseJsonString, GrupoResponseDTO.class);
    }

    private void assertOrdemEPrioridades(List<TipoCriterioDesempate> esperado,
                                         List<CriterioDesempateResponseDTO> resultado) {
        assertEquals(esperado.size(), resultado.size());
        for (int i = 0; i < esperado.size(); i++) {
            assertEquals(esperado.get(i), resultado.get(i).getCriterio());
            assertEquals(i + 1, resultado.get(i).getPrioridade());
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de configuração dos critérios de desempate")
    class configuracaoDeCriteriosDesempate {

        @Test
        @DisplayName("Configura os 4 critérios de desempate, persistindo a prioridade conforme a ordem enviada")
        void quandoOrganizadorConfiguraOrdemValida() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                .criteriosDesempate(List.of(
                    TipoCriterioDesempate.PLACAR_EXATO,
                    TipoCriterioDesempate.ERRO,
                    TipoCriterioDesempate.ACERTO_VENCEDOR,
                    TipoCriterioDesempate.ACERTO_EMPATE))
                .build();

            GrupoResponseDTO resultado = reconfigurarCriterios(dto);

            assertNotNull(resultado.getCriteriosDesempate());
            assertOrdemEPrioridades(
                dto.getCriteriosDesempate().stream().toList(),
                resultado.getCriteriosDesempate());
        }

        @Transactional
        @Test
        @DisplayName("Substitui todos os critérios por um conjunto totalmente novo, removendo os antigos")
        void quandoOrganizadorReconfiguraCriterios() throws Exception {
            configurarCriteriosDiretamente(grupo,
                TipoCriterioDesempate.ERRO,
                TipoCriterioDesempate.PLACAR_EXATO);

            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                .criteriosDesempate(List.of(
                    TipoCriterioDesempate.ACERTO_EMPATE,
                    TipoCriterioDesempate.ACERTO_VENCEDOR))
                .build();

            reconfigurarCriterios(dto);

            Grupo grupoAtualizado = grupoRepository.findById(grupo.getId()).orElseThrow();

            assertEquals(2, grupoAtualizado.getCriteriosDesempate().size());
            assertEquals(TipoCriterioDesempate.ACERTO_EMPATE, grupoAtualizado.getCriteriosDesempate().get(0).getCriterio());
            assertEquals(TipoCriterioDesempate.ACERTO_VENCEDOR, grupoAtualizado.getCriteriosDesempate().get(1).getCriterio());
            assertTrue(grupoAtualizado.getCriteriosDesempate().stream()
                .noneMatch(c -> c.getCriterio() == TipoCriterioDesempate.ERRO
                    || c.getCriterio() == TipoCriterioDesempate.PLACAR_EXATO));
        }

        @Transactional
        @Test
        @DisplayName("Troca a ordem de dois critérios já configurados, sem alterar o conjunto")
        void quandoOrganizadorReordenaCriteriosComMesmoConjunto() throws Exception {
            configurarCriteriosDiretamente(grupo,
                TipoCriterioDesempate.PLACAR_EXATO,
                TipoCriterioDesempate.ERRO);

            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                .criteriosDesempate(List.of(
                    TipoCriterioDesempate.ERRO,
                    TipoCriterioDesempate.PLACAR_EXATO))
                .build();

            GrupoResponseDTO resultado = reconfigurarCriterios(dto);

            assertOrdemEPrioridades(dto.getCriteriosDesempate(), resultado.getCriteriosDesempate());

            Grupo grupoAtualizado = grupoRepository.findById(grupo.getId()).orElseThrow();
            assertEquals(2, grupoAtualizado.getCriteriosDesempate().size());
            assertEquals(TipoCriterioDesempate.ERRO, grupoAtualizado.getCriteriosDesempate().get(0).getCriterio());
            assertEquals(1, grupoAtualizado.getCriteriosDesempate().get(0).getPrioridade());
            assertEquals(TipoCriterioDesempate.PLACAR_EXATO, grupoAtualizado.getCriteriosDesempate().get(1).getCriterio());
            assertEquals(2, grupoAtualizado.getCriteriosDesempate().get(1).getPrioridade());
        }

        @Transactional
        @Test
        @DisplayName("Reconfigura mantendo um critério existente e substituindo outro")
        void quandoOrganizadorReconfiguraComInterseccaoParcial() throws Exception {
            configurarCriteriosDiretamente(grupo,
                TipoCriterioDesempate.PLACAR_EXATO,
                TipoCriterioDesempate.ERRO);

            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                .criteriosDesempate(List.of(
                    TipoCriterioDesempate.ERRO,
                    TipoCriterioDesempate.ACERTO_VENCEDOR))
                .build();

            GrupoResponseDTO resultado = reconfigurarCriterios(dto);

            assertOrdemEPrioridades(dto.getCriteriosDesempate(), resultado.getCriteriosDesempate());

            Grupo grupoAtualizado = grupoRepository.findById(grupo.getId()).orElseThrow();
            assertTrue(grupoAtualizado.getCriteriosDesempate().stream()
                .noneMatch(c -> c.getCriterio() == TipoCriterioDesempate.PLACAR_EXATO));
        }

        @Test
        @DisplayName("Rejeita a requisição quando o mesmo critério aparece mais de uma vez na lista")
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
                    .param("codigoUsuario", organizador.getCodigo())
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("Os critérios de desempate devem conter ao menos 1 critério válido, sem repetição.", resultado.getMessage());
        }

        @Test
        @DisplayName("Configura um único critério de desempate, que recebe prioridade 1")
        void quandoOrganizadorConfiguraUmCriterio() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                .criteriosDesempate(List.of(TipoCriterioDesempate.PLACAR_EXATO))
                .build();

            GrupoResponseDTO resultado = reconfigurarCriterios(dto);

            assertOrdemEPrioridades(dto.getCriteriosDesempate(), resultado.getCriteriosDesempate());
        }

        @Test
        @DisplayName("Configura apenas parte dos critérios disponíveis (2 de 4); somente os enviados são persistidos")
        void quandoOrganizadorConfiguraOrdemParcial() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                .criteriosDesempate(List.of(
                    TipoCriterioDesempate.ACERTO_EMPATE,
                    TipoCriterioDesempate.ERRO))
                .build();

            GrupoResponseDTO resultado = reconfigurarCriterios(dto);

            assertOrdemEPrioridades(dto.getCriteriosDesempate(), resultado.getCriteriosDesempate());
        }

        @Test
        @DisplayName("Rejeita a requisição por validação quando a lista de critérios é vazia")
        void quandoOrganizadorEnviaListaVazia() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                .criteriosDesempate(Collections.emptyList())
                .build();

            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo())
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("Erros de validacao encontrados", resultado.getMessage());
        }

        @Test
        @DisplayName("Rejeita a requisição por validação quando o campo de critérios está ausente no corpo")
        void quandoOrganizadorEnviaListaNula() throws Exception {
            String corpoSemCriterios = "{}";

            String responseJsonString = driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo())
                    .content(corpoSemCriterios))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("Erros de validacao encontrados", resultado.getMessage());
        }

        @Transactional
        @Test
        @DisplayName("Nega acesso e não persiste nada quando um usuário não organizador tenta configurar os critérios")
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
                    .param("codigoUsuario", outroUsuario.getCodigo())
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("Permissão negada para acessar este recurso.", resultado.getMessage());
            Grupo grupoInalterado = grupoRepository.findById(grupo.getId()).orElseThrow();
            assertTrue(grupoInalterado.getCriteriosDesempate().isEmpty());
        }

        @Test
        @DisplayName("Rejeita a configuração quando o grupo informado não existe")
        void quandoGrupoNaoExistePut() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                .criteriosDesempate(List.of(TipoCriterioDesempate.PLACAR_EXATO))
                .build();

            Long idInexistente = 999999L;

            driver.perform(put(URI_GRUPOS + "/" + idInexistente + "/criterios-desempate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo())
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
        }

        @Test
        @DisplayName("Rejeita a configuração quando o código de acesso informado é inválido")
        void quandoCodigoAcessoInvalidoPut() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                .criteriosDesempate(List.of(TipoCriterioDesempate.PLACAR_EXATO))
                .build();

            driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", "codigo-errado")
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
        }

        @Test
        @DisplayName("Rejeita a requisição por validação quando a lista de critérios contém um valor nulo")
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
                    .param("codigoUsuario", organizador.getCodigo())
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
        @DisplayName("Rejeita a configuração quando o usuário informado não existe")
        void quandoUsuarioNaoExistePut() throws Exception {
            CriteriosDesempatePutRequestDTO dto = CriteriosDesempatePutRequestDTO.builder()
                .criteriosDesempate(List.of(TipoCriterioDesempate.PLACAR_EXATO))
                .build();

            Long idInexistente = 999999L;

            driver.perform(put(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("usuarioId", idInexistente.toString())
                    .param("codigoUsuario", "111111")
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de consulta dos critérios de desempate")
    class consultaDeCriteriosDesempate {

        @Test
        @DisplayName("Retorna a ordem e as prioridades persistidas quando o organizador consulta os critérios já configurados")
        void quandoOrganizadorConsultaCriteriosConfigurados() throws Exception {
            configurarCriteriosDiretamente(grupo,
                TipoCriterioDesempate.ERRO,
                TipoCriterioDesempate.PLACAR_EXATO);

            String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            List<CriterioDesempateResponseDTO> resultado = objectMapper.readValue(
                responseJsonString, new TypeReference<>() {});

            assertOrdemEPrioridades(
                List.of(TipoCriterioDesempate.ERRO, TipoCriterioDesempate.PLACAR_EXATO),
                resultado);
        }

        @Test
        @DisplayName("A prioridade retornada reflete o valor persistido, não apenas a posição na lista")
        void prioridadeRetornadaReflitoValorPersistido() throws Exception {
            configurarCriteriosDiretamente(grupo,
                TipoCriterioDesempate.ACERTO_VENCEDOR,
                TipoCriterioDesempate.ACERTO_EMPATE,
                TipoCriterioDesempate.ERRO);

            String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            List<CriterioDesempateResponseDTO> resultado = objectMapper.readValue(
                responseJsonString, new TypeReference<>() {});

            assertOrdemEPrioridades(
                List.of(TipoCriterioDesempate.ACERTO_VENCEDOR,
                    TipoCriterioDesempate.ACERTO_EMPATE,
                    TipoCriterioDesempate.ERRO),
                resultado);
        }

        @Test
        @DisplayName("Quando nenhum critério foi configurado ainda, a consulta retorna lista vazia")
        void quandoConsultaSemCriteriosConfigurados() throws Exception {
            String responseJsonString = driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            List<CriterioDesempateResponseDTO> resultado = objectMapper.readValue(responseJsonString, new TypeReference<>() {});

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Permite a consulta quando um usuário não organizador acessa os critérios de um grupo público")
        void quandoNaoOrganizadorConsultaGrupoPublico() throws Exception {
            configurarCriteriosDiretamente(grupo, TipoCriterioDesempate.PLACAR_EXATO);

            driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                    .param("usuarioId", outroUsuario.getId().toString())
                    .param("codigoUsuario", outroUsuario.getCodigo()))
                .andExpect(status().isOk())
                .andDo(print());
        }

        @Test
        @DisplayName("Rejeita a consulta quando o grupo informado não existe")
        void quandoGrupoNaoExisteGet() throws Exception {
            Long idInexistente = 999999L;

            driver.perform(get(URI_GRUPOS + "/" + idInexistente + "/criterios-desempate")
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", organizador.getCodigo()))
                .andExpect(status().isBadRequest())
                .andDo(print());
        }

        @Test
        @DisplayName("Rejeita a consulta quando o código de acesso informado é inválido")
        void quandoCodigoAcessoInvalidoGet() throws Exception {
            driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/criterios-desempate")
                    .param("usuarioId", organizador.getId().toString())
                    .param("codigoUsuario", "codigo-errado"))
                .andExpect(status().isBadRequest())
                .andDo(print());
        }
    }
}
