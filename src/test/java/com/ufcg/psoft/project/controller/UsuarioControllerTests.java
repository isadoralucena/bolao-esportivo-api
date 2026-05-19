package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.project.dto.UsuarioPostPutRequestDTO;
import com.ufcg.psoft.project.dto.UsuarioResponseDTO;
import com.ufcg.psoft.project.exception.CustomErrorType;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Usuários")
public class UsuarioControllerTests {

    final String URI_USUARIOS = "/usuario";

    @Autowired
    MockMvc driver;

    @Autowired
    UsuarioRepository usuarioRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    Usuario usuario;

    UsuarioPostPutRequestDTO usuarioPostPutRequestDTO;

    @BeforeEach
    void setup() {
        // Object Mapper suporte para LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());
        usuario = usuarioRepository.save(Usuario.builder()
                .nome("usuario Um da Silva")
                .endereco("Rua dos Testes, 123")
                .codigo("123456")
                .build()
        );
        usuarioPostPutRequestDTO = UsuarioPostPutRequestDTO.builder()
                .nome(usuario.getNome())
                .endereco(usuario.getEndereco())
                .codigo(usuario.getCodigo())
                .build();
    }

    @AfterEach
    void tearDown() {
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação de nome")
    class usuarioVerificacaoNome {

        @Test
        @DisplayName("Quando recuperamos um usuario com dados válidos")
        void quandoRecuperamosNomeDousuarioValido() throws Exception {

            // Act
            String responseJsonString = driver.perform(get(URI_USUARIOS + "/" + usuario.getId()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Usuario resultado = objectMapper.readValue(responseJsonString, Usuario.UsuarioBuilder.class).build();

            // Assert
            assertEquals("usuario Um da Silva", resultado.getNome());
        }

        @Test
        @DisplayName("Quando alteramos o nome do usuario com dados válidos")
        void quandoAlteramosNomeDousuarioValido() throws Exception {
            // Arrange
            usuarioPostPutRequestDTO.setNome("usuario Um Alterado");

            // Act
            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Usuario resultado = objectMapper.readValue(responseJsonString, Usuario.UsuarioBuilder.class).build();

            // Assert
            assertEquals("usuario Um Alterado", resultado.getNome());
        }

        @Test
        @DisplayName("Quando alteramos o nome do usuario nulo")
        void quandoAlteramosNomeDousuarioNulo() throws Exception {
            // Arrange
            usuarioPostPutRequestDTO.setNome(null);

            // Act
            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Nome obrigatorio", resultado.getErrors().get(0))
            );
        }

        @Test
        @DisplayName("Quando alteramos o nome do usuario vazio")
        void quandoAlteramosNomeDousuarioVazio() throws Exception {
            // Arrange
            usuarioPostPutRequestDTO.setNome("");

            // Act
            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Nome obrigatorio", resultado.getErrors().get(0))
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação do endereço")
    class usuarioVerificacaoEndereco {

        @Test
        @DisplayName("Quando alteramos o endereço do usuario com dados válidos")
        void quandoAlteramosEnderecoDousuarioValido() throws Exception {
            // Arrange
            usuarioPostPutRequestDTO.setEndereco("Endereco Alterado");

            // Act
            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            UsuarioResponseDTO resultado = objectMapper.readValue(responseJsonString, UsuarioResponseDTO.UsuarioResponseDTOBuilder.class).build();

            // Assert
            assertEquals("Endereco Alterado", resultado.getEndereco());
        }

        @Test
        @DisplayName("Quando alteramos o endereço do usuario nulo")
        void quandoAlteramosEnderecoDousuarioNulo() throws Exception {
            // Arrange
            usuarioPostPutRequestDTO.setEndereco(null);

            // Act
            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Endereco obrigatorio", resultado.getErrors().get(0))
            );
        }

        @Test
        @DisplayName("Quando alteramos o endereço do usuario vazio")
        void quandoAlteramosEnderecoDousuarioVazio() throws Exception {
            // Arrange
            usuarioPostPutRequestDTO.setEndereco("");

            // Act
            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Endereco obrigatorio", resultado.getErrors().get(0))
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação do código de acesso")
    class usuarioVerificacaoCodigoAcesso {

        @Test
        @DisplayName("Quando alteramos o código de acesso do usuario nulo")
        void quandoAlteramosCodigoAcessoDousuarioNulo() throws Exception {
            // Arrange
            usuarioPostPutRequestDTO.setCodigo(null);

            // Act
            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Codigo de acesso obrigatorio", resultado.getErrors().get(0))
            );
        }

        @Test
        @DisplayName("Quando alteramos o código de acesso do usuario mais de 6 digitos")
        void quandoAlteramosCodigoAcessoDousuarioMaisDe6Digitos() throws Exception {
            // Arrange
            usuarioPostPutRequestDTO.setCodigo("1234567");

            // Act
            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Codigo de acesso deve ter exatamente 6 digitos numericos", resultado.getErrors().get(0))
            );
        }

        @Test
        @DisplayName("Quando alteramos o código de acesso do usuario menos de 6 digitos")
        void quandoAlteramosCodigoAcessoDousuarioMenosDe6Digitos() throws Exception {
            // Arrange
            usuarioPostPutRequestDTO.setCodigo("12345");

            // Act
            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Codigo de acesso deve ter exatamente 6 digitos numericos", resultado.getErrors().get(0))
            );
        }

        @Test
        @DisplayName("Quando alteramos o código de acesso do usuario caracteres não numéricos")
        void quandoAlteramosCodigoAcessoDousuarioCaracteresNaoNumericos() throws Exception {
            // Arrange
            usuarioPostPutRequestDTO.setCodigo("a*c4e@");

            // Act
            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Codigo de acesso deve ter exatamente 6 digitos numericos", resultado.getErrors().get(0))
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação dos fluxos básicos API Rest")
    class usuarioVerificacaoFluxosBasicosApiRest {

        @Test
        @DisplayName("Quando buscamos por todos usuarios salvos")
        void quandoBuscamosPorTodosusuarioSalvos() throws Exception {
            // Arrange
            // Vamos ter 3 usuarios no banco
            Usuario usuario1 = usuario.builder()
                    .nome("usuario Dois Almeida")
                    .endereco("Av. da Pits A, 100")
                    .codigo("246810")
                    .build();
            Usuario usuario2 = usuario.builder()
                    .nome("usuario Três Lima")
                    .endereco("Distrito dos Testadores, 200")
                    .codigo("135790")
                    .build();
            usuarioRepository.saveAll(Arrays.asList(usuario1, usuario2));

            // Act
            String responseJsonString = driver.perform(get(URI_USUARIOS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isOk()) // Codigo 200
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<Usuario> resultado = objectMapper.readValue(responseJsonString, new TypeReference<>() {
            });

            // Assert
            assertAll(
                    () -> assertEquals(3, resultado.size())
            );
        }

        @Test
        @DisplayName("Quando buscamos um usuario salvo pelo id")
        void quandoBuscamosPorUmusuarioSalvo() throws Exception {
            // Arrange
            // nenhuma necessidade além do setup()

            // Act
            String responseJsonString = driver.perform(get(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isOk()) // Codigo 200
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            UsuarioResponseDTO resultado = objectMapper.readValue(responseJsonString, new TypeReference<>() {});

            // Assert
            assertAll(
                    () -> assertEquals(usuario.getId().longValue(), resultado.getId().longValue()),
                    () -> assertEquals(usuario.getNome(), resultado.getNome())
            );
        }

        @Test
        @DisplayName("Quando buscamos um usuario inexistente")
        void quandoBuscamosPorUmusuarioInexistente() throws Exception {
            // Arrange
            // nenhuma necessidade além do setup()

            // Act
            String responseJsonString = driver.perform(get(URI_USUARIOS + "/" + 999999999)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest()) // Codigo 400
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("O Usuario consultado nao existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando criamos um novo usuario com dados válidos")
        void quandoCriarusuarioValido() throws Exception {
            // Arrange
            // nenhuma necessidade além do setup()

            // Act
            String responseJsonString = driver.perform(post(URI_USUARIOS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isCreated()) // Codigo 201
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Usuario resultado = objectMapper.readValue(responseJsonString, Usuario.UsuarioBuilder.class).build();

            // Assert
            assertAll(
                    () -> assertNotNull(resultado.getId()),
                    () -> assertEquals(usuarioPostPutRequestDTO.getNome(), resultado.getNome())
            );

        }

        @Test
        @DisplayName("Quando alteramos o usuario com dados válidos")
        void quandoAlteramosusuarioValido() throws Exception {
            // Arrange
            Long usuarioId = usuario.getId();

            // Act
            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isOk()) // Codigo 200
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Usuario resultado = objectMapper.readValue(responseJsonString, Usuario.UsuarioBuilder.class).build();

            // Assert
            assertAll(
                    () -> assertEquals(resultado.getId().longValue(), usuarioId),
                    () -> assertEquals(usuarioPostPutRequestDTO.getNome(), resultado.getNome())
            );
        }

        @Test
        @DisplayName("Quando alteramos o usuario inexistente")
        void quandoAlteramosusuarioInexistente() throws Exception {
            // Arrange
            // nenhuma necessidade além do setup()

            // Act
            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + 99999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest()) // Codigo 400
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("O Usuario consultado nao existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando alteramos o usuario passando código de acesso inválido")
        void quandoAlteramosusuarioCodigoAcessoInvalido() throws Exception {
            // Arrange
            Long usuarioId = usuario.getId();

            // Act
            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuarioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", "invalido")
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest()) // Codigo 400
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Codigo de acesso invalido!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando excluímos um usuario salvo")
        void quandoExcluimosusuarioValido() throws Exception {
            // Arrange
            // nenhuma necessidade além do setup()

            // Act
            String responseJsonString = driver.perform(delete(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", usuario.getCodigo()))
                    .andExpect(status().isNoContent()) // Codigo 204
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            // Assert
            assertTrue(responseJsonString.isBlank());
        }

        @Test
        @DisplayName("Quando excluímos um usuario inexistente")
        void quandoExcluimosusuarioInexistente() throws Exception {
            // Arrange
            // nenhuma necessidade além do setup()

            // Act
            String responseJsonString = driver.perform(delete(URI_USUARIOS + "/" + 999999)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", usuario.getCodigo()))
                    .andExpect(status().isBadRequest()) // Codigo 400
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("O Usuario consultado nao existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando excluímos um usuario salvo passando código de acesso inválido")
        void quandoExcluimosusuarioCodigoAcessoInvalido() throws Exception {
            // Arrange
            // nenhuma necessidade além do setup()

            // Act
            String responseJsonString = driver.perform(delete(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", "invalido"))
                    .andExpect(status().isBadRequest()) // Codigo 400
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Codigo de acesso invalido!", resultado.getMessage())
            );
        }
    }
}
