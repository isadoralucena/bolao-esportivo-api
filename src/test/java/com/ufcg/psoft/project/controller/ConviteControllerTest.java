package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.project.dto.convite.ConvitePostPutRequestDTO;
import com.ufcg.psoft.project.dto.convite.ConviteResponseDTO;
import com.ufcg.psoft.project.exception.CustomErrorType;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.ConviteRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Convites - US4")
public class ConviteControllerTest {

    final String URI_CONVITES = "/convites";

    @Autowired
    MockMvc driver;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    ConviteRepository conviteRepository;

    @Autowired
    GrupoRepository grupoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    CampeonatoRepository campeonatoRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    Usuario organizador;
    Usuario convidado;
    Campeonato campeonato;
    Grupo grupoPrivado;
    Grupo grupoPublico;
    ConvitePostPutRequestDTO conviteDTO;

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

        convidado = usuarioRepository.save(Usuario.builder()
                .nome("Convidado Teste")
                .username("convidado")
                .email("convidado@email.com")
                .endereco("Rua do Convidado, 2")
                .codigo("222222")
                .build());

        campeonato = campeonatoRepository.save(Campeonato.builder()
                .nome("Campeonato Teste")
                .url("http://campeonato-teste.com")
                .codigo("CAT001")
                .ativo(true)
                .build());

        grupoPrivado = grupoRepository.save(Grupo.builder()
                .nome("Grupo Privado")
                .descricao("Grupo privado para testes")
                .privacidade(PrivacidadeGrupo.PRIVADA)
                .limiteParticipantes(10)
                .campeonato(campeonato)
                .organizador(organizador)
                .build());
        grupoPrivado.getParticipantes().add(organizador);
        grupoRepository.save(grupoPrivado);

        grupoPublico = grupoRepository.save(Grupo.builder()
                .nome("Grupo Publico")
                .descricao("Grupo publico para testes")
                .privacidade(PrivacidadeGrupo.PUBLICA)
                .limiteParticipantes(10)
                .campeonato(campeonato)
                .organizador(organizador)
                .build());
        grupoPublico.getParticipantes().add(organizador);
        grupoRepository.save(grupoPublico);

        conviteDTO = ConvitePostPutRequestDTO.builder()
                .grupo(grupoPrivado.getId())
                .organizador(organizador.getId())
                .convidado(convidado.getId())
                .build();
    }

    @AfterEach
    void tearDown() {
        conviteRepository.deleteAll();
        grupoRepository.deleteAll();
        campeonatoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("Conjunto de casos de criação de convites")
    class criacaoDeConvites {

        @Test
        @DisplayName("Quando um organizador cria um convite válido")
        void quandoOrganizadorCriaConviteValido() throws Exception {
            String responseJsonString = driver.perform(post(URI_CONVITES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcesso", organizador.getCodigo())
                    .content(objectMapper.writeValueAsString(conviteDTO)))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            ConviteResponseDTO resultado = objectMapper.readValue(responseJsonString, ConviteResponseDTO.class);

            assertAll(
                    () -> assertNotNull(resultado.getId()),
                    () -> assertEquals(StatusConvite.PENDENTE, resultado.getStatus()),
                    () -> assertEquals(grupoPrivado.getId(), resultado.getGrupo()),
                    () -> assertEquals(convidado.getId(), resultado.getConvidado())
            );
        }

        @Test
        @DisplayName("Quando cria convite para grupo privado sem vagas")
        void quandoCriaConviteParaGrupoPrivadoSemVagas() throws Exception {
            Grupo grupoPrivadoAtualizado = grupoRepository.findById(grupoPrivado.getId()).orElseThrow();
            grupoPrivadoAtualizado.setLimiteParticipantes(1);
            grupoRepository.save(grupoPrivadoAtualizado);

            String responseJsonString = driver.perform(post(URI_CONVITES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcesso", organizador.getCodigo())
                    .content(objectMapper.writeValueAsString(conviteDTO)))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            ConviteResponseDTO resultado = objectMapper.readValue(responseJsonString, ConviteResponseDTO.class);

            assertAll(
                    () -> assertNotNull(resultado.getId()),
                    () -> assertEquals(StatusConvite.PENDENTE, resultado.getStatus()),
                    () -> assertEquals(grupoPrivado.getId(), resultado.getGrupo()),
                    () -> assertEquals(convidado.getId(), resultado.getConvidado())
            );
        }

        @Test
        @DisplayName("Quando tenta criar convite para grupo com campeonato inativo")
        void quandoCriaConviteParaGrupoComCampeonatoInativo() throws Exception {
            campeonato.setAtivo(false);
            campeonatoRepository.save(campeonato);

            String responseJsonString = driver.perform(post(URI_CONVITES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcesso", organizador.getCodigo())
                    .content(objectMapper.writeValueAsString(conviteDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O campeonato associado a este grupo não está ativo!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando tenta criar convite com código de acesso inválido")
        void quandoCriaConviteComCodigoInvalido() throws Exception {
            String responseJsonString = driver.perform(post(URI_CONVITES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcesso", "999999")
                    .content(objectMapper.writeValueAsString(conviteDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O usuário consultado é inválido para essa operação!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando tenta criar convite para grupo público")
        void quandoCriaConviteParaGrupoPublico() throws Exception {
            conviteDTO.setGrupo(grupoPublico.getId());

            String responseJsonString = driver.perform(post(URI_CONVITES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcesso", organizador.getCodigo())
                    .content(objectMapper.writeValueAsString(conviteDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Privacidade inválida!", resultado.getMessage())
                );
        }

        @Test
        @DisplayName("Quando tenta criar convite para usuário que já é participante")
        void quandoCriaConviteParaParticipanteExistente() throws Exception {
            Grupo grupoAtualizado = grupoRepository.findById(grupoPrivado.getId()).orElseThrow();
            grupoAtualizado.getParticipantes().add(convidado);
            grupoRepository.save(grupoAtualizado);

            String responseJsonString = driver.perform(post(URI_CONVITES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcesso", organizador.getCodigo())
                    .content(objectMapper.writeValueAsString(conviteDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O usuário já é participante deste grupo!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando tenta criar convite duplicado para o mesmo convidado no mesmo grupo")
        void quandoCriaConviteDuplicado() throws Exception {
            driver.perform(post(URI_CONVITES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcesso", organizador.getCodigo())
                    .content(objectMapper.writeValueAsString(conviteDTO)))
                    .andExpect(status().isCreated());

            String responseJsonString = driver.perform(post(URI_CONVITES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcesso", organizador.getCodigo())
                    .content(objectMapper.writeValueAsString(conviteDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Já existe um convite pendente para esse usuário nesse grupo!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando tenta criar convite sendo usuário não organizador")
        void quandoNaoOrganizadorTentaCriarConvite() throws Exception {
            conviteDTO.setOrganizador(convidado.getId());

            String responseJsonString = driver.perform(post(URI_CONVITES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcesso", convidado.getCodigo())
                    .content(objectMapper.writeValueAsString(conviteDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O organizador do grupo é inválido!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando tenta criar convite sem informar o grupo")
        void quandoCriaConviteSemGrupo() throws Exception {
            conviteDTO.setGrupo(null);

            driver.perform(post(URI_CONVITES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcesso", organizador.getCodigo())
                    .content(objectMapper.writeValueAsString(conviteDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Quando tenta criar convite sem informar o convidado")
        void quandoCriaConviteSemConvidado() throws Exception {
            conviteDTO.setConvidado(null);

            driver.perform(post(URI_CONVITES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcesso", organizador.getCodigo())
                    .content(objectMapper.writeValueAsString(conviteDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de resposta a convites")
    class respostaAConvites {

        Convite convite;

        @BeforeEach
        void setupConvite() throws Exception {
            String responseJsonString = driver.perform(post(URI_CONVITES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcesso", organizador.getCodigo())
                    .content(objectMapper.writeValueAsString(conviteDTO)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            ConviteResponseDTO dto = objectMapper.readValue(responseJsonString, ConviteResponseDTO.class);
            convite = conviteRepository.findById(dto.getId()).orElseThrow();
        }

        @Test
        @DisplayName("Quando convidado aceita um convite pendente")
        void quandoConvidadoAceitaConvite() throws Exception {
            String responseJsonString = driver.perform(post(URI_CONVITES + "/" + convite.getId() + "/aceitar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcessoConvidado", convidado.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            ConviteResponseDTO resultado = objectMapper.readValue(responseJsonString, ConviteResponseDTO.class);
            Grupo grupoAtualizado = grupoRepository.findById(grupoPrivado.getId()).orElseThrow();

            assertAll(
                    () -> assertEquals(StatusConvite.ACEITO, resultado.getStatus()),
                    () -> assertTrue(grupoAtualizado.getParticipantes().stream()
                            .anyMatch(p -> p.getId().equals(convidado.getId())))
            );
        }

        @Test
        @DisplayName("Quando tenta aceitar convite para grupo sem vagas")
        void quandoTentaAceitarConviteParaGrupoSemVagas() throws Exception {
            Grupo grupoPrivadoAtualizado = grupoRepository.findById(grupoPrivado.getId()).orElseThrow();
            grupoPrivadoAtualizado.setLimiteParticipantes(1);
            grupoRepository.save(grupoPrivadoAtualizado);

            String responseJsonString = driver.perform(post(URI_CONVITES + "/" + convite.getId() + "/aceitar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcessoConvidado", convidado.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            Convite conviteAtualizado = conviteRepository.findById(convite.getId()).orElseThrow();

            assertAll(
                    () -> assertEquals("O limite de participantes para este grupo já foi atingido!", resultado.getMessage()),
                    () -> assertEquals(StatusConvite.PENDENTE, conviteAtualizado.getStatus())
            );
        }

        @Test
        @DisplayName("Quando tenta aceitar convite com campeonato inativo")
        void quandoTentaAceitarConviteComCampeonatoInativo() throws Exception {
            campeonato.setAtivo(false);
            campeonatoRepository.save(campeonato);

            String responseJsonString = driver.perform(post(URI_CONVITES + "/" + convite.getId() + "/aceitar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcessoConvidado", convidado.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            Convite conviteAtualizado = conviteRepository.findById(convite.getId()).orElseThrow();
            Grupo grupoAtualizado = grupoRepository.findById(grupoPrivado.getId()).orElseThrow();

            assertAll(
                    () -> assertEquals("O campeonato associado a este grupo não está ativo!", resultado.getMessage()),
                    () -> assertEquals(StatusConvite.PENDENTE, conviteAtualizado.getStatus()),
                    () -> assertFalse(grupoAtualizado.getParticipantes().stream().anyMatch(p -> p.getId().equals(convidado.getId()))));
        }

        @Test
        @DisplayName("Quando convidado recusa um convite pendente")
        void quandoConvidadoRecusaConvite() throws Exception {
            String responseJsonString = driver.perform(post(URI_CONVITES + "/" + convite.getId() + "/recusar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcessoConvidado", convidado.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            ConviteResponseDTO resultado = objectMapper.readValue(responseJsonString, ConviteResponseDTO.class);

            assertAll(
                    () -> assertEquals(StatusConvite.RECUSADO, resultado.getStatus())
            );
        }

        @Test
        @DisplayName("Quando convidado ignora um convite pendente")
        void quandoConvidadoIgnoraConvite() throws Exception {
            String responseJsonString = driver.perform(post(URI_CONVITES + "/" + convite.getId() + "/ignorar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcessoConvidado", convidado.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            ConviteResponseDTO resultado = objectMapper.readValue(responseJsonString, ConviteResponseDTO.class);

            assertAll(
                    () -> assertEquals(StatusConvite.IGNORADO, resultado.getStatus())
            );
        }

        @Test
        @DisplayName("Quando tenta aceitar convite já processado")
        void quandoTentaAceitarConviteJaProcessado() throws Exception {
            driver.perform(post(URI_CONVITES + "/" + convite.getId() + "/recusar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcessoConvidado", convidado.getCodigo()))
                    .andExpect(status().isOk());

            String responseJsonString = driver.perform(post(URI_CONVITES + "/" + convite.getId() + "/aceitar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("codigoAcessoConvidado", convidado.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O convite já foi processado e não pode ser modificado!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando tenta recusar convite já processado")
        void quandoTentaRecusarConviteJaProcessado() throws Exception {
            driver.perform(post(URI_CONVITES + "/" + convite.getId() + "/aceitar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcessoConvidado", convidado.getCodigo()))
                    .andExpect(status().isOk());

            String responseJsonString = driver.perform(post(URI_CONVITES + "/" + convite.getId() + "/recusar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcessoConvidado", convidado.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O convite já foi processado e não pode ser modificado!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando tenta ignorar convite já processado")
        void quandoTentaIgnorarConviteJaProcessado() throws Exception {
            driver.perform(post(URI_CONVITES + "/" + convite.getId() + "/aceitar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcessoConvidado", convidado.getCodigo()))
                    .andExpect(status().isOk());

            String responseJsonString = driver.perform(post(URI_CONVITES + "/" + convite.getId() + "/ignorar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcessoConvidado", convidado.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O convite já foi processado e não pode ser modificado!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando tenta aceitar convite inexistente")
        void quandoTentaAceitarConviteInexistente() throws Exception {
            String responseJsonString = driver.perform(post(URI_CONVITES + "/999999/aceitar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcessoConvidado", convidado.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O convite não existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando tenta aceitar convite com código inválido")
        void quandoTentaAceitarConviteComCodigoInvalido() throws Exception {
            String responseJsonString = driver.perform(post(URI_CONVITES + "/" + convite.getId() + "/aceitar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcessoConvidado", "999999"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O usuário consultado é inválido para essa operação!", resultado.getMessage())
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de remoção de convites")
    class remocaoDeConvites {

        Convite convite;

        @BeforeEach
        void setupConvite() throws Exception {
            String responseJsonString = driver.perform(post(URI_CONVITES)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcesso", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(conviteDTO)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            ConviteResponseDTO dto = objectMapper.readValue(responseJsonString, ConviteResponseDTO.class);
            convite = conviteRepository.findById(dto.getId()).orElseThrow();
        }

        @Test
        @DisplayName("Quando organizador remove um convite")
        void quandoOrganizadorRemoveConvite() throws Exception {
            driver.perform(delete(URI_CONVITES + "/" + convite.getId() + "/remover")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcessoOrganizador", organizador.getCodigo()))
                    .andExpect(status().isNoContent())
                    .andDo(print());

            assertFalse(conviteRepository.findById(convite.getId()).isPresent());
        }

        @Test
        @DisplayName("Quando tenta remover convite inexistente")
        void quandoTentaRemoverConviteInexistente() throws Exception {
            String responseJsonString = driver.perform(delete(URI_CONVITES + "/999999/remover")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcessoOrganizador", organizador.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O convite não existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando tenta remover convite com código inválido")
        void quandoTentaRemoverConviteComCodigoInvalido() throws Exception {
            String responseJsonString = driver.perform(delete(URI_CONVITES + "/" + convite.getId() + "/remover")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcessoOrganizador", "999999"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O usuário consultado é inválido para essa operação!", resultado.getMessage())
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de listagem de convites pendentes")
    class listagemDeConvitesPendentes {

        @Test
        @DisplayName("Quando convidado lista seus convites pendentes")
        void quandoConvidadoListaConvitesPendentes() throws Exception {
            driver.perform(post(URI_CONVITES)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcesso", organizador.getCodigo())
                            .content(objectMapper.writeValueAsString(conviteDTO)))
                    .andExpect(status().isCreated());

            String responseJsonString = driver.perform(get(URI_CONVITES + "/usuario/" + convidado.getId() + "/pendentes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcessoConvidado", convidado.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            ConviteResponseDTO[] resultado = objectMapper.readValue(responseJsonString, ConviteResponseDTO[].class);

            assertAll(
                    () -> assertEquals(1, resultado.length),
                    () -> assertEquals(StatusConvite.PENDENTE, resultado[0].getStatus()),
                    () -> assertEquals(convidado.getId(), resultado[0].getConvidado())
            );
        }

        @Test
        @DisplayName("Quando convidado lista convites pendentes e não tem nenhum")
        void quandoConvidadoListaConvitesPendentesVazio() throws Exception {
            String responseJsonString = driver.perform(get(URI_CONVITES + "/usuario/" + convidado.getId() + "/pendentes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcessoConvidado", convidado.getCodigo()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            ConviteResponseDTO[] resultado = objectMapper.readValue(responseJsonString, ConviteResponseDTO[].class);

            assertAll(
                    () -> assertEquals(0, resultado.length)
            );
        }

        @Test
        @DisplayName("Quando tenta listar convites com código inválido")
        void quandoListaConvitesComCodigoInvalido() throws Exception {
            String responseJsonString = driver.perform(get(URI_CONVITES + "/usuario/" + convidado.getId() + "/pendentes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoAcessoConvidado", "999999"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O usuário consultado é inválido para essa operação!", resultado.getMessage())
            );
        }
    }
}