package com.ufcg.psoft.project.controller.grupo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.PrivacidadeGrupo;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes sobre o gerenciamento de participantes de grupos de bolão")
public class GrupoParticipantesControllerTest {

    final String URI_GRUPOS = "/grupos";
 
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
    Usuario participante;
    Campeonato campeonato;
    Grupo grupo;
 
    @BeforeEach
    void setup() {
        organizador = usuarioRepository.save(Usuario.builder()
                .nome("Organizador Teste")
                .email("organizador@teste.com")
                .username("organizador_teste")
                .endereco("Rua A, 123")
                .codigo("ORG12345")
                .build());
 
        participante = usuarioRepository.save(Usuario.builder()
                .nome("Participante Teste")
                .email("participante@teste.com")
                .username("participante_teste")
                .endereco("Rua B, 456")
                .codigo("PART12345")
                .build());
 
        campeonato = campeonatoRepository.save(Campeonato.builder()
                .nome("Campeonato Teste")
                .url("http://campeonato-teste.com")
                .codigo("CAMP12345")
                .ativo(true)
                .build());
 
        grupo = Grupo.builder()
                .nome("Grupo Teste")
                .descricao("Descrição do grupo de teste")
                .privacidade(PrivacidadeGrupo.PUBLICA)
                .limiteParticipantes(5)
                .campeonato(campeonato)
                .organizador(organizador)
                .participantes(new HashSet<>())
                .build();
        grupo.getParticipantes().add(organizador);
        grupo = grupoRepository.save(grupo);
    }
 
    @AfterEach
    void tearDown() {
        grupoRepository.deleteAll();
        usuarioRepository.deleteAll();
        campeonatoRepository.deleteAll();
    }

    @Test
    @DisplayName("Não deve listar participantes de um grupo inexistente")
    void quandoListarParticipantesDeGrupoInexistente() throws Exception {

        driver.perform(get(URI_GRUPOS + "/99999/participantes")
                .param("usuarioId", organizador.getId().toString())
                .param("codigoAcesso", organizador.getCodigo()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Não deve listar participantes quando o usuário informado não existe")
    void quandoListarParticipantesComUsuarioInexistente() throws Exception {

        driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/participantes")
                .param("usuarioId", "99999")
                .param("codigoAcesso", organizador.getCodigo()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Não deve permitir acesso com código inválido")
    void quandoListarParticipantesComCodigoInvalido() throws Exception {

        driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/participantes")
                .param("usuarioId", organizador.getId().toString())
                .param("codigoAcesso", "INVALIDO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Não deve permitir que usuário externo visualize participantes de grupo privado")
    void quandoUsuarioNaoMembroTentaListarParticipantesDeGrupoPrivado() throws Exception {

        Grupo grupoPrivado = Grupo.builder()
                .nome("Grupo privado")
                .descricao("Descrição privada")
                .privacidade(PrivacidadeGrupo.PRIVADA)
                .limiteParticipantes(5)
                .campeonato(campeonato)
                .organizador(organizador)
                .participantes(new HashSet<>())
                .build();

        grupoPrivado.getParticipantes().add(organizador);
        grupoRepository.save(grupoPrivado);


        driver.perform(get(URI_GRUPOS + "/" + grupoPrivado.getId() + "/participantes")
                .param("usuarioId", participante.getId().toString())
                .param("codigoAcesso", participante.getCodigo()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve listar todos os participantes pertencentes ao grupo")
    void quandoListarParticipantesDoGrupo() throws Exception {

        grupo.getParticipantes().add(participante);
        grupoRepository.save(grupo);

        driver.perform(get(URI_GRUPOS + "/" + grupo.getId() + "/participantes")
                .param("usuarioId", organizador.getId().toString())
                .param("codigoAcesso", organizador.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Deve permitir que o organizador remova um participante")
    void quandoRemoverParticipanteComSucesso() throws Exception {

        grupo.getParticipantes().add(participante);
        grupoRepository.save(grupo);


        driver.perform(delete(URI_GRUPOS + "/" + grupo.getId()
                + "/participantes/" + participante.getId())
                .param("usuarioId", organizador.getId().toString())
                .param("codigoAcesso", organizador.getCodigo()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Não deve permitir que o organizador remova a si próprio do grupo")
    void quandoOrganizadorTentaRemoverASiProprio() throws Exception {

        driver.perform(delete(URI_GRUPOS + "/" + grupo.getId()
                + "/participantes/" + organizador.getId())
                .param("usuarioId", organizador.getId().toString())
                .param("codigoAcesso", organizador.getCodigo()))
                .andExpect(status().isBadRequest());

        Grupo grupoAtualizado = grupoRepository.findById(grupo.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(
                grupoAtualizado.getParticipantes().contains(organizador));
    }
}