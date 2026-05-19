package com.ufcg.psoft.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.project.dto.UsuarioResponseDTO;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Usuarios")
public class UsuarioTestAula {

    final String URI_USUARIOS = "/usuario";

    @Autowired
    MockMvc driver;

    @Autowired
    UsuarioRepository usuarioRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    List<UsuarioResponseDTO> usuariosDTO = new ArrayList<>();

    @BeforeEach
    void setup() {
        // Object Mapper suporte para LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());
        Usuario usuario1 = usuarioRepository.save(Usuario.builder()
                .nome("Usuario")
                .endereco("Rua 123")
                .codigo("123456")
                .build()
        );

        Usuario usuario2 = usuarioRepository.save(Usuario.builder()
                .nome("Usuaria")
                .endereco("Rua 234")
                .codigo("123456")
                .build()
        );

        UsuarioResponseDTO r1 = UsuarioResponseDTO.builder()
                .nome(usuario1.getNome())
                .endereco(usuario1.getEndereco())
                .id(usuario1.getId())
                .build();

        usuariosDTO.add(r1);
    }

    @AfterEach
    void tearDown() {
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("Conjunto de casos de teste da aula")
    class UsuarioVerificacaoNome {

        @Test
        @DisplayName("Quando recuperamos usuarios")
        void quandoRecuperamosUsuariosValidos() throws Exception {

            String stringBusca = "Usuario";
            // Act
            String responseJsonString = driver.perform(get(URI_USUARIOS)
                            .param("nome", stringBusca))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            String expectedResult = objectMapper
                    .writeValueAsString(usuariosDTO);

            System.out.println(expectedResult);
            System.out.println(responseJsonString);
            // Assert
            assertEquals(expectedResult, responseJsonString);
        }

    }
}


