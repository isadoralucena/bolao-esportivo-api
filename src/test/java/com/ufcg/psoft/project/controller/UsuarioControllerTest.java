package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.project.dto.usuario.UsuarioPostPutRequestDTO;
import com.ufcg.psoft.project.dto.usuario.UsuarioResponseDTO;
import com.ufcg.psoft.project.dto.usuario.PromocaoPremiumResponseDTO;
import com.ufcg.psoft.project.exception.CustomErrorType;
import com.ufcg.psoft.project.model.PromocaoPremium;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.PromocaoPremiumRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.TestConstructor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.time.Clock;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.charset.StandardCharsets;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Usuários")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class UsuarioControllerTest {

    final String URI_USUARIOS = "/usuarios";

    MockMvc driver;

    final UsuarioRepository usuarioRepository;

    final PromocaoPremiumRepository promocaoPremiumRepository;

    final WebApplicationContext webApplicationContext;

    final Clock clock;

    ObjectMapper objectMapper = new ObjectMapper();

    Usuario usuario;

    UsuarioPostPutRequestDTO usuarioPostPutRequestDTO;

    @BeforeEach
        void setup() {
        driver = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();

        objectMapper.registerModule(new JavaTimeModule());
        usuario = usuarioRepository.save(Usuario.builder()
                .nome("usuario Um da Silva")
                .endereco("Rua dos Testes, 123")
                .username("usuarioUmDaSilva")
                .email("usuario.um.da.silva@email.com")
                .codigo("123456")
                .build()
        );
        usuarioPostPutRequestDTO = UsuarioPostPutRequestDTO.builder()
                .nome(usuario.getNome())
                .endereco(usuario.getEndereco())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
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
                            .param("codigoUsuario", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Usuario resultado = objectMapper.readValue(responseJsonString, Usuario.UsuarioBuilder.class).build();

            // Assert
            assertEquals("usuario Um Alterado", resultado.getNome());
        }

        @ParameterizedTest(name = "Nome inválido: {0}")
        @NullAndEmptySource
        @DisplayName("Quando alteramos o nome para um valor obrigatório inválido")
        void quandoAlteramosNomeParaValorInvalido(String nomeInvalido) throws Exception {
            usuarioPostPutRequestDTO.setNome(nomeInvalido);

            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoUsuario", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals(LocalDateTime.now(clock), resultado.getTimestamp()),
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
                            .param("codigoUsuario", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            UsuarioResponseDTO resultado = objectMapper.readValue(responseJsonString, UsuarioResponseDTO.UsuarioResponseDTOBuilder.class).build();

            // Assert
            assertEquals("Endereco Alterado", resultado.getEndereco());
        }

        @ParameterizedTest(name = "Endereço inválido: {0}")
        @NullAndEmptySource
        @DisplayName("Quando alteramos o endereço para um valor obrigatório inválido")
        void quandoAlteramosEnderecoParaValorInvalido(String enderecoInvalido) throws Exception {
            usuarioPostPutRequestDTO.setEndereco(enderecoInvalido);

            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoUsuario", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

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
            usuarioPostPutRequestDTO.setCodigo(null);

            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigoUsuario", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Codigo de acesso obrigatorio", resultado.getErrors().get(0))
            );
        }

        @ParameterizedTest(name = "Código inválido: \"{0}\"")
        @ValueSource(strings = {
            "1234567",
            "12345",
            "a*c4e@"
        })
        @DisplayName("Quando alteramos o código de acesso para um valor inválido")
        void quandoAlteramosCodigoAcessoParaValorInvalido(String codigoInvalido) throws Exception {

            usuarioPostPutRequestDTO.setCodigo(codigoInvalido);

            String responseJsonString = driver.perform(
                            put(URI_USUARIOS + "/" + usuario.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .param("codigoUsuario", usuario.getCodigo())
                                    .content(objectMapper.writeValueAsString(
                                            usuarioPostPutRequestDTO
                                    )))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(
                    responseJsonString,
                    CustomErrorType.class
            );

            assertAll(
                    () -> assertEquals(
                            "Erros de validacao encontrados",
                            resultado.getMessage()
                    ),
                    () -> assertEquals(
                            "Codigo de acesso deve ter exatamente 6 digitos numericos",
                            resultado.getErrors().get(0)
                    )
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação do email")
    class usuarioVerificacaoEmail {
        @ParameterizedTest(name = "E-mail inválido: {0}")
        @NullAndEmptySource
        @DisplayName("Quando criamos usuário sem informar um e-mail")
        void quandoCriamosUsuarioSemEmail(String emailInvalido) throws Exception {
            usuarioPostPutRequestDTO.setEmail(emailInvalido);

            String responseJsonString = driver.perform(post(URI_USUARIOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertTrue(resultado.getErrors().contains("Email obrigatorio"))
            );
        }

        @Test
        @DisplayName("Quando criamos usuario com email invalido")
        void quandoCriamosUsuarioComEmailInvalido() throws Exception {
            // Arrange
            usuarioPostPutRequestDTO.setEmail("email-invalido");

            // Act
            String responseJsonString = driver.perform(post(URI_USUARIOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertTrue(resultado.getErrors().contains("Email invalido"))
            );
        }

        @Test
        @DisplayName("Quando criamos usuario com email já cadastrado")
        void quandoCriamosUsuarioComEmailJaCadastrado() throws Exception {
            // Arrange
            usuarioPostPutRequestDTO.setEmail(usuario.getEmail().toUpperCase());

            // Act
            String responseJsonString = driver.perform(post(URI_USUARIOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
						.andExpect(status().isBadRequest())
						.andDo(print())
						.andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Já existe outro usuário com esse email cadastrado!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando alteramos usuario usando email de outro usuario")
        void quandoAlteramosUsuarioComEmailDuplicado() throws Exception {
            // Arrange
            Usuario outroUsuario = usuarioRepository.save(Usuario.builder()
                    .nome("usuario Dois Almeida")
                    .endereco("Rua dos Testes, 456")
                    .username("usuarioDoisAlmeida")
                    .email("usuario.dois.almeida@email.com")
                    .codigo("654321")
                    .build());

            usuarioPostPutRequestDTO.setEmail(outroUsuario.getEmail());

            // Act
            String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("codigoUsuario", usuario.getCodigo())
                        .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Já existe outro usuário com esse email cadastrado!", resultado.getMessage())
            );
        }
    }

    @Test
    @DisplayName("Quando alteramos o email do usuario com dados válidos")
    void quandoAlteramosEmailDousuarioValido() throws Exception {
        // Arrange
        String novoEmail = "usuario.alterado@email.com";
        usuarioPostPutRequestDTO.setEmail(novoEmail);

        // Act
        String responseJsonString = driver.perform(put(URI_USUARIOS + "/" + usuario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .param("codigoUsuario", usuario.getCodigo())
                .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        UsuarioResponseDTO resultado = objectMapper.readValue(responseJsonString, UsuarioResponseDTO.class);

        Usuario usuarioSalvo = usuarioRepository.findById(usuario.getId()).orElseThrow();

        // Assert
        assertAll(
                () -> assertEquals(novoEmail, resultado.getEmail()),
                () -> assertEquals(novoEmail, usuarioSalvo.getEmail()));
    }
    
    @Nested
    @DisplayName("Conjunto de casos de verificação do perfil")
    class usuarioVerificacaoPerfil {
        @Test
        @DisplayName("Quando criamos um usuário, ele deve ter o perfil padrão")
        void quandoCriamosUsuarioEleTemPerfilPadrao() throws Exception {
            // Arrange
            UsuarioPostPutRequestDTO novoUsuarioDTO = UsuarioPostPutRequestDTO.builder()
                    .nome("usuario Dois Almeida")
                    .endereco("Rua dos Testes, 456")
                    .username("usuarioDoisAlmeida")
                    .email("usuario.dois.almeida@email.com")
                    .codigo("654321")
                    .build();

            // Act
            String responseJsonString = driver.perform(post(URI_USUARIOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoUsuarioDTO)))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            UsuarioResponseDTO resultado = objectMapper.readValue(responseJsonString, UsuarioResponseDTO.class);

            // Assert
            assertAll(
                    () -> assertNotNull(resultado.getId()),
                    () -> assertEquals("PADRAO", resultado.getPerfil().toString())
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação do username")
    class usuarioVerificacaoUsername {
        @ParameterizedTest(name = "Username inválido: {0}")
        @NullAndEmptySource
        @DisplayName("Quando criamos usuário com username obrigatório inválido")
        void quandoCriamosUsuarioComUsernameInvalido(String usernameInvalido) throws Exception {
            usuarioPostPutRequestDTO.setUsername(usernameInvalido);

            String responseJsonString = driver.perform(post(URI_USUARIOS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertTrue(resultado.getErrors().contains("Username obrigatorio"))
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
                    .username("user2")
                    .email("user2@email.com")
                    .endereco("Av. da Pits A, 100")
                    .codigo("246810")
                    .build();
            Usuario usuario2 = usuario.builder()
                    .nome("usuario Três Lima")
                    .username("user3")
                    .email("user3@email.com")
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
        @DisplayName("Quando buscamos usuarios filtrando pelo nome")
        void quandoBuscamosUsuariosFiltrandoPeloNome() throws Exception {
            // Arrange
            usuarioRepository.save(Usuario.builder()
                    .nome("Maria Buscada")
                    .username("maria")
                    .email("maria@email.com")
                    .endereco("Rua Maria")
                    .codigo("246810")
                    .build());
            usuarioRepository.save(Usuario.builder()
                    .nome("Carlos Outro")
                    .username("carlos")
                    .email("carlos@email.com")
                    .endereco("Rua Carlos")
                    .codigo("135790")
                    .build());

            // Act
            String responseJsonString = driver.perform(get(URI_USUARIOS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("nome", "Maria"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<Usuario> resultado = objectMapper.readValue(responseJsonString, new TypeReference<>() {
            });

            // Assert
            assertAll(
                    () -> assertEquals(1, resultado.size()),
                    () -> assertEquals("Maria Buscada", resultado.get(0).getNome())
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
                    () -> assertEquals("O usuário consultado não existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando criamos um novo usuario com dados válidos")
        void quandoCriarusuarioValido() throws Exception {
            // Arrange
            UsuarioPostPutRequestDTO novoUsuarioDTO = UsuarioPostPutRequestDTO.builder()
                            .nome("usuario Dois Almeida")
                            .endereco("Rua Nova, 456")
                            .username("usuarioDoisAlmeida")
                            .email("usuario.dois.almeida@email.com")
                            .codigo("654321")
                            .build();

            // Act
            String responseJsonString = driver.perform(post(URI_USUARIOS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(novoUsuarioDTO)))
                    .andExpect(status().isCreated()) // Codigo 201
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Usuario resultado = objectMapper.readValue(responseJsonString, Usuario.UsuarioBuilder.class).build();

            // Assert
            assertAll(
                    () -> assertNotNull(resultado.getId()),
                    () -> assertEquals(novoUsuarioDTO.getNome(), resultado.getNome())
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
                            .param("codigoUsuario", usuario.getCodigo())
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
                            .param("codigoUsuario", usuario.getCodigo())
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest()) // Codigo 400
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("O usuário consultado não existe!", resultado.getMessage())
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
                            .param("codigoUsuario", "invalido")
                            .content(objectMapper.writeValueAsString(usuarioPostPutRequestDTO)))
                    .andExpect(status().isBadRequest()) // Codigo 400
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Código de acesso inválido!", resultado.getMessage())
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
                            .param("codigoUsuario", usuario.getCodigo()))
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
                            .param("codigoUsuario", usuario.getCodigo()))
                    .andExpect(status().isBadRequest()) // Codigo 400
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("O usuário consultado não existe!", resultado.getMessage())
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
                            .param("codigoUsuario", "invalido"))
                    .andExpect(status().isBadRequest()) // Codigo 400
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Código de acesso inválido!", resultado.getMessage())
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação da promoção Premium")
    class usuarioVerificacaoPromocaoPremium {

        @AfterEach
        void tearDownPromocao() {
            promocaoPremiumRepository.deleteAll();
        }

        @Test
        @DisplayName("Quando consultamos promocao de usuario que foi promovido")
        void quandoConsultamosPromocaoDeUsuarioPromovido() throws Exception {
            // Arrange
            PromocaoPremium promocao = PromocaoPremium.builder()
                    .usuario(usuario)
                    .data(LocalDateTime.now(clock))
                    .motivo("Promovido por atingir os criterios")
                    .palpites(50)
                    .gruposParticipa(3)
                    .requisicoes(100)
                    .acertos(10)
                    .build();
            promocaoPremiumRepository.save(promocao);

            // Act
            String responseJsonString = driver.perform(get(URI_USUARIOS + "/" + usuario.getId() + "/promocao-premium"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            PromocaoPremiumResponseDTO resultado = objectMapper.readValue(responseJsonString, PromocaoPremiumResponseDTO.class);

            // Assert
            assertAll(
                    () -> assertNotNull(resultado.getId()),
                    () -> assertEquals(usuario.getId(), resultado.getUsuarioId()),
                    () -> assertEquals(50, resultado.getPalpites()),
                    () -> assertEquals(3, resultado.getGruposParticipa()),
                    () -> assertEquals(100, resultado.getRequisicoes()),
                    () -> assertEquals(10, resultado.getAcertos()),
                    () -> assertEquals("Promovido por atingir os criterios", resultado.getMotivo())
            );
        }

        @Test
        @DisplayName("Quando consultamos promocao de usuario que nao foi promovido")
        void quandoConsultamosPromocaoDeUsuarioNaoPromovido() throws Exception {
            // Act
            String responseJsonString = driver.perform(get(URI_USUARIOS + "/" + usuario.getId() + "/promocao-premium"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertEquals("O usuário não foi promovido ao plano Premium!", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando consultamos promocao de usuario inexistente")
        void quandoConsultamosPromocaoDeUsuarioInexistente() throws Exception {
            // Act
            String responseJsonString = driver.perform(get(URI_USUARIOS + "/" + 999999 + "/promocao-premium"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertEquals("O usuário consultado não existe!", resultado.getMessage());
        }
    }
}
