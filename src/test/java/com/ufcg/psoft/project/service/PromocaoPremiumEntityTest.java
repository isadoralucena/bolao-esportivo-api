package com.ufcg.psoft.project.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ufcg.psoft.project.dto.usuario.PromocaoPremiumResponseDTO;
import com.ufcg.psoft.project.model.PromocaoPremium;
import com.ufcg.psoft.project.model.Usuario;

@DisplayName("Testes de integridade da entidade PromocaoPremium e DTO")
class PromocaoPremiumEntityTest {

    @Test
    @DisplayName("Deve construir entidade via builder e ler campos via getters")
    void deveConstruirEntidadeViaBuilderELerCampos() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Teste")
                .email("teste@email.com")
                .username("teste")
                .endereco("Rua")
                .codigo("123456")
                .build();

        PromocaoPremium promocao = PromocaoPremium.builder()
                .id(10L)
                .usuario(usuario)
                .data(LocalDateTime.of(2026, 7, 22, 10, 0))
                .motivo("Motivo teste")
                .palpites(50)
                .gruposParticipa(3)
                .requisicoes(100)
                .acertos(10)
                .build();

        assertAll(
                () -> assertEquals(10L, promocao.getId()),
                () -> assertEquals(1L, promocao.getUsuario().getId()),
                () -> assertEquals("Motivo teste", promocao.getMotivo()),
                () -> assertEquals(50, promocao.getPalpites()),
                () -> assertEquals(3, promocao.getGruposParticipa()),
                () -> assertEquals(100, promocao.getRequisicoes()),
                () -> assertEquals(10, promocao.getAcertos())
        );
    }

    @Test
    @DisplayName("Deve construir DTO via construtor da entidade")
    void deveConstruirDTOViaConstrutorDaEntidade() {
        Usuario usuario = Usuario.builder()
                .id(2L)
                .nome("Teste DTO")
                .email("dto@email.com")
                .username("dto")
                .endereco("Rua DTO")
                .codigo("654321")
                .build();

        PromocaoPremium promocao = PromocaoPremium.builder()
                .id(20L)
                .usuario(usuario)
                .data(LocalDateTime.of(2026, 7, 22, 15, 30))
                .motivo("Motivo DTO")
                .palpites(55)
                .gruposParticipa(4)
                .requisicoes(200)
                .acertos(15)
                .build();

        PromocaoPremiumResponseDTO dto = new PromocaoPremiumResponseDTO(promocao);

        assertAll(
                () -> assertEquals(20L, dto.getId()),
                () -> assertEquals(2L, dto.getUsuarioId()),
                () -> assertEquals(55, dto.getPalpites()),
                () -> assertEquals(4, dto.getGruposParticipa()),
                () -> assertEquals(200, dto.getRequisicoes()),
                () -> assertEquals(15, dto.getAcertos()),
                () -> assertEquals("Motivo DTO", dto.getMotivo())
        );
    }

    @Test
    @DisplayName("Deve construir DTO via builder vazio")
    void deveConstruirDTOViaBuilderVazio() {
        PromocaoPremiumResponseDTO dto = PromocaoPremiumResponseDTO.builder().build();
        assertAll(
                () -> assertNull(dto.getId()),
                () -> assertNull(dto.getUsuarioId()),
                () -> assertNull(dto.getMotivo()),
                () -> assertNull(dto.getPalpites()),
                () -> assertNull(dto.getGruposParticipa()),
                () -> assertNull(dto.getRequisicoes()),
                () -> assertNull(dto.getAcertos())
        );
    }

    @Test
    @DisplayName("Deve construir entidade via construtor padrao e setters")
    void deveConstruirEntidadeViaConstrutorPadraoESetters() {
        Usuario usuario = Usuario.builder().id(3L).build();
        PromocaoPremium promocao = new PromocaoPremium();
        promocao.setId(30L);
        promocao.setUsuario(usuario);
        promocao.setData(LocalDateTime.of(2026, 7, 22, 20, 0));
        promocao.setMotivo("Setters test");
        promocao.setPalpites(60);
        promocao.setGruposParticipa(5);
        promocao.setRequisicoes(300);
        promocao.setAcertos(20);

        assertAll(
                () -> assertEquals(30L, promocao.getId()),
                () -> assertEquals(3L, promocao.getUsuario().getId()),
                () -> assertEquals("Setters test", promocao.getMotivo()),
                () -> assertEquals(60, promocao.getPalpites()),
                () -> assertEquals(5, promocao.getGruposParticipa()),
                () -> assertEquals(300, promocao.getRequisicoes()),
                () -> assertEquals(20, promocao.getAcertos())
        );
    }
}
