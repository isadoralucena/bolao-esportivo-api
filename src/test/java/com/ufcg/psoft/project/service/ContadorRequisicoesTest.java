package com.ufcg.psoft.project.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ufcg.psoft.project.service.premium.ContadorRequisicoes;

@DisplayName("Testes unitarios do ContadorRequisicoes")
class ContadorRequisicoesTest {

    private ContadorRequisicoes contador;

    @BeforeEach
    void setUp() {
        contador = new ContadorRequisicoes();
    }

    @Nested
    @DisplayName("Incrementar")
    class Incrementar {

        @Test
        @DisplayName("Deve incrementar de 0 para 1 quando primeira chamada")
        void deveIncrementarDe0Para1QuandoPrimeiraChamada() {
            contador.incrementar(1L);
            assertEquals(1L, contador.getContagem(1L));
        }

        @Test
        @DisplayName("Deve incrementar de 1 para 2 quando segunda chamada")
        void deveIncrementarDe1Para2QuandoSegundaChamada() {
            contador.incrementar(1L);
            contador.incrementar(1L);
            assertEquals(2L, contador.getContagem(1L));
        }

        @Test
        @DisplayName("Deve manter contagens separadas por usuario")
        void deveManterContagensSeparadasPorUsuario() {
            contador.incrementar(1L);
            contador.incrementar(1L);
            contador.incrementar(2L);

            assertAll(
                    () -> assertEquals(2L, contador.getContagem(1L)),
                    () -> assertEquals(1L, contador.getContagem(2L))
            );
        }
    }

    @Nested
    @DisplayName("GetContagem")
    class GetContagem {

        @Test
        @DisplayName("Deve retornar 0 quando usuario nunca incrementou")
        void deveRetornar0QuandoUsuarioNuncaIncrementou() {
            assertEquals(0L, contador.getContagem(99L));
        }

        @Test
        @DisplayName("Deve retornar 0 para usuario apos resetar")
        void deveRetornar0ParaUsuarioAposResetar() {
            contador.incrementar(1L);
            contador.resetar(1L);
            assertEquals(0L, contador.getContagem(1L));
        }
    }

    @Nested
    @DisplayName("Resetar")
    class Resetar {

        @Test
        @DisplayName("Deve resetar contagem de usuario especifico sem afetar outros")
        void deveResetarContagemDeUsuarioEspecificoSemAfetarOutros() {
            contador.incrementar(1L);
            contador.incrementar(1L);
            contador.incrementar(2L);

            contador.resetar(1L);

            assertAll(
                    () -> assertEquals(0L, contador.getContagem(1L)),
                    () -> assertEquals(1L, contador.getContagem(2L))
            );
        }

        @Test
        @DisplayName("Nao deve lancar excecao quando usuario nao existe")
        void naoDeveLancarExcecaoQuandoUsuarioNaoExiste() {
            assertDoesNotThrow(() -> contador.resetar(99L));
        }
    }

    @Nested
    @DisplayName("ResetarTodos")
    class ResetarTodos {

        @Test
        @DisplayName("Deve resetar todas as contagens")
        void deveResetarTodasAsContagens() {
            contador.incrementar(1L);
            contador.incrementar(2L);
            contador.incrementar(3L);

            contador.resetarTodos();

            assertAll(
                    () -> assertEquals(0L, contador.getContagem(1L)),
                    () -> assertEquals(0L, contador.getContagem(2L)),
                    () -> assertEquals(0L, contador.getContagem(3L))
            );
        }

        @Test
        @DisplayName("Nao deve lancar excecao quando mapa esta vazio")
        void naoDeveLancarExcecaoQuandoMapaEstaVazio() {
            assertDoesNotThrow(() -> contador.resetarTodos());
        }
    }
}
