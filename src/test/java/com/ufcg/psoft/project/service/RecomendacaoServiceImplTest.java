package com.ufcg.psoft.project.service;

import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.project.exception.RecomendacaoEstrategiaInvalidaException;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.partida.PartidaNaoExisteException;
import com.ufcg.psoft.project.exception.partida.PartidaNaoPertenceAoCampeonatoException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoExisteException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoPremiumException;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.service.grupo.GrupoAutorizacaoService;
import com.ufcg.psoft.project.service.recomendacao.RecomendacaoServiceImpl;
import com.ufcg.psoft.project.service.recomendacao.RecomendacaoStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Testes unitarios do RecomendacaoServiceImpl - US20")
class RecomendacaoServiceImplTest {

    @Mock
    private GrupoAutorizacaoService grupoAutorizacaoService;

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private GrupoRepository grupoRepository;

    @InjectMocks
    private RecomendacaoServiceImpl recomendacaoService;

    private Usuario usuarioPremium;
    private Usuario usuarioPadrao;
    private Campeonato campeonato;
    private Campeonato outroCampeonato;
    private Grupo grupo;
    private Partida partida;
    private RecomendacaoStrategy mockStrategy;

    @BeforeEach
    void setUp() {
        campeonato = Campeonato.builder()
                .id(1L).nome("Campeonato Teste")
                .url("http://campeonato-teste.com")
                .codigo("CAT001").ativo(true).build();

        outroCampeonato = Campeonato.builder()
                .id(2L).nome("Outro Campeonato")
                .url("http://outro-campeonato.com")
                .codigo("CAT002").ativo(true).build();

        usuarioPremium = Usuario.builder()
                .id(1L).nome("Usuario Premium")
                .email("premium@email.com").username("premium")
                .endereco("Rua A").codigo("111111")
                .perfil(PerfilUsuario.PREMIUM).build();

        usuarioPadrao = Usuario.builder()
                .id(2L).nome("Usuario Padrao")
                .email("padrao@email.com").username("padrao")
                .endereco("Rua B").codigo("222222")
                .perfil(PerfilUsuario.PADRAO).build();

        grupo = Grupo.builder()
                .id(1L).nome("Grupo Teste")
                .campeonato(campeonato).build();

        partida = Partida.builder()
                .id(1L).campeonato(campeonato)
                .codigoExterno(1L).mandante("Time A")
                .visitante("Time B").status(PartidaStatus.ABERTO).build();

        mockStrategy = mock(RecomendacaoStrategy.class);
        when(mockStrategy.getNome()).thenReturn("PLACAR_FREQUENTE");

        ReflectionTestUtils.setField(
                recomendacaoService,
                "estrategias",
                Map.of("PLACAR_FREQUENTE", mockStrategy)
        );
    }

    @Nested
    @DisplayName("Cenarios de sucesso")
    class CenariosDeSuccesso {

        @Test
        @DisplayName("Deve retornar recomendacao com estrategia PLACAR_FREQUENTE")
        void deveRetornarRecomendacaoComPlacarFrequente() {
            RecomendacaoResponseDTO esperada = RecomendacaoResponseDTO.builder()
                    .golsMandanteRecomendado(1).golsVisitanteRecomendado(0)
                    .estrategia("PLACAR_FREQUENTE").temHistorico(true).build();

            when(grupoAutorizacaoService.obterUsuarioValido(1L, "111111")).thenReturn(usuarioPremium);
            when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
            when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
            when(mockStrategy.recomendar(partida)).thenReturn(esperada);

            RecomendacaoResponseDTO resultado = recomendacaoService.recomendar(1L, 1L, 1L, "111111", "PLACAR_FREQUENTE");

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(1, resultado.getGolsMandanteRecomendado()),
                    () -> assertEquals(0, resultado.getGolsVisitanteRecomendado()),
                    () -> assertEquals("PLACAR_FREQUENTE", resultado.getEstrategia()),
                    () -> assertTrue(resultado.isTemHistorico())
            );
        }

        @Test
        @DisplayName("Deve retornar recomendacao com estrategia MEDIA_GOLS")
        void deveRetornarRecomendacaoComMediaGols() {
            RecomendacaoStrategy mediaGolsStrategy = mock(RecomendacaoStrategy.class);
            when(mediaGolsStrategy.getNome()).thenReturn("MEDIA_GOLS");
            ReflectionTestUtils.setField(recomendacaoService, "estrategias",
                    Map.of("PLACAR_FREQUENTE", mockStrategy, "MEDIA_GOLS", mediaGolsStrategy));

            RecomendacaoResponseDTO esperada = RecomendacaoResponseDTO.builder()
                    .golsMandanteRecomendado(2).golsVisitanteRecomendado(1)
                    .estrategia("MEDIA_GOLS").temHistorico(true).build();

            when(grupoAutorizacaoService.obterUsuarioValido(1L, "111111")).thenReturn(usuarioPremium);
            when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
            when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
            when(mediaGolsStrategy.recomendar(partida)).thenReturn(esperada);

            RecomendacaoResponseDTO resultado = recomendacaoService.recomendar(1L, 1L, 1L, "111111", "MEDIA_GOLS");

            assertAll(
                    () -> assertEquals(2, resultado.getGolsMandanteRecomendado()),
                    () -> assertEquals(1, resultado.getGolsVisitanteRecomendado()),
                    () -> assertEquals("MEDIA_GOLS", resultado.getEstrategia()),
                    () -> assertTrue(resultado.isTemHistorico())
            );
        }

        @Test
        @DisplayName("Deve retornar recomendacao sem historico")
        void deveRetornarRecomendacaoSemHistorico() {
            RecomendacaoResponseDTO semHistorico = RecomendacaoResponseDTO.builder()
                    .golsMandanteRecomendado(0).golsVisitanteRecomendado(0)
                    .estrategia("PLACAR_FREQUENTE").temHistorico(false).build();

            when(grupoAutorizacaoService.obterUsuarioValido(1L, "111111")).thenReturn(usuarioPremium);
            when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
            when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
            when(mockStrategy.recomendar(partida)).thenReturn(semHistorico);

            RecomendacaoResponseDTO resultado = recomendacaoService.recomendar(1L, 1L, 1L, "111111", "PLACAR_FREQUENTE");

            assertAll(
                    () -> assertEquals(0, resultado.getGolsMandanteRecomendado()),
                    () -> assertEquals(0, resultado.getGolsVisitanteRecomendado()),
                    () -> assertFalse(resultado.isTemHistorico())
            );
        }
    }

    @Nested
    @DisplayName("Validacao de usuario")
    class ValidacaoDeUsuario {

        @Test
        @DisplayName("Deve lancar excecao quando usuario nao existe")
        void deveLancarExcecaoQuandoUsuarioNaoExiste() {
            when(grupoAutorizacaoService.obterUsuarioValido(99L, "111111"))
                    .thenThrow(UsuarioNaoExisteException.class);

            assertThrows(UsuarioNaoExisteException.class,
                    () -> recomendacaoService.recomendar(1L, 1L, 99L, "111111", "PLACAR_FREQUENTE"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando codigo invalido")
        void deveLancarExcecaoQuandoCodigoInvalido() {
            when(grupoAutorizacaoService.obterUsuarioValido(1L, "ERRADO"))
                    .thenThrow(CodigoDeAcessoInvalidoException.class);

            assertThrows(CodigoDeAcessoInvalidoException.class,
                    () -> recomendacaoService.recomendar(1L, 1L, 1L, "ERRADO", "PLACAR_FREQUENTE"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando usuario nao e Premium")
        void deveLancarExcecaoQuandoUsuarioNaoPremium() {
            when(grupoAutorizacaoService.obterUsuarioValido(2L, "222222")).thenReturn(usuarioPadrao);

            assertThrows(UsuarioNaoPremiumException.class,
                    () -> recomendacaoService.recomendar(1L, 1L, 2L, "222222", "PLACAR_FREQUENTE"));
        }
    }

    @Nested
    @DisplayName("Validacao de partida e grupo")
    class ValidacaoDePartidaEGrupo {

        @Test
        @DisplayName("Deve lancar excecao quando partida nao existe")
        void deveLancarExcecaoQuandoPartidaNaoExiste() {
            when(grupoAutorizacaoService.obterUsuarioValido(1L, "111111")).thenReturn(usuarioPremium);
            when(partidaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(PartidaNaoExisteException.class,
                    () -> recomendacaoService.recomendar(1L, 99L, 1L, "111111", "PLACAR_FREQUENTE"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando grupo nao existe")
        void deveLancarExcecaoQuandoGrupoNaoExiste() {
            when(grupoAutorizacaoService.obterUsuarioValido(1L, "111111")).thenReturn(usuarioPremium);
            when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
            when(grupoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(GrupoNaoExisteException.class,
                    () -> recomendacaoService.recomendar(99L, 1L, 1L, "111111", "PLACAR_FREQUENTE"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando partida nao pertence ao campeonato do grupo")
        void deveLancarExcecaoQuandoPartidaNaoPertenceAoCampeonato() {
            Partida partidaOutroCampeonato = Partida.builder()
                    .id(2L).campeonato(outroCampeonato)
                    .codigoExterno(2L).mandante("Time C")
                    .visitante("Time D").status(PartidaStatus.ABERTO).build();

            when(grupoAutorizacaoService.obterUsuarioValido(1L, "111111")).thenReturn(usuarioPremium);
            when(partidaRepository.findById(2L)).thenReturn(Optional.of(partidaOutroCampeonato));
            when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));

            assertThrows(PartidaNaoPertenceAoCampeonatoException.class,
                    () -> recomendacaoService.recomendar(1L, 2L, 1L, "111111", "PLACAR_FREQUENTE"));
        }
    }

    @Nested
    @DisplayName("Validacao de estrategia")
    class ValidacaoDeEstrategia {

        @Test
        @DisplayName("Deve lancar excecao quando estrategia invalida")
        void deveLancarExcecaoQuandoEstrategiaInvalida() {
            when(grupoAutorizacaoService.obterUsuarioValido(1L, "111111")).thenReturn(usuarioPremium);
            when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
            when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));

            assertThrows(RecomendacaoEstrategiaInvalidaException.class,
                    () -> recomendacaoService.recomendar(1L, 1L, 1L, "111111", "INEXISTENTE"));
        }
    }
}