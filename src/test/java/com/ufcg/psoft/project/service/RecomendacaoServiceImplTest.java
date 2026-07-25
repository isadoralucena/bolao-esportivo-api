package com.ufcg.psoft.project.service;

import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.exception.CodigoDeAcessoInvalidoException;
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

    @Mock
    private RecomendacaoStrategy placarFrequente;

    @Mock
    private RecomendacaoStrategy mediaGols;

    @InjectMocks
    private RecomendacaoServiceImpl recomendacaoService;

    private Usuario usuarioPremium;
    private Usuario usuarioPadrao;
    private Campeonato campeonato;
    private Campeonato outroCampeonato;
    private Grupo grupo;
    private Partida partida;

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

        ReflectionTestUtils.setField(recomendacaoService, "placarFrequente", placarFrequente);
        ReflectionTestUtils.setField(recomendacaoService, "mediaGols", mediaGols);
    }

    @Nested
    @DisplayName("Cenarios de sucesso")
    class CenariosDeSuccesso {

        @Test
        @DisplayName("Deve retornar recomendacao quando PLACAR_FREQUENTE tem historico")
        void deveRetornarRecomendacaoComPlacarFrequente() {
            RecomendacaoResponseDTO esperada = RecomendacaoResponseDTO.builder()
                    .golsMandanteRecomendado(1).golsVisitanteRecomendado(0)
                    .estrategia("PLACAR_FREQUENTE").build();

            when(grupoAutorizacaoService.obterUsuarioValido(1L, "111111")).thenReturn(usuarioPremium);
            when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
            when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
            when(placarFrequente.recomendar(partida)).thenReturn(Optional.of(esperada));

            RecomendacaoResponseDTO resultado = recomendacaoService.recomendar(1L, 1L, 1L, "111111");

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(1, resultado.getGolsMandanteRecomendado()),
                    () -> assertEquals(0, resultado.getGolsVisitanteRecomendado()),
                    () -> assertEquals("PLACAR_FREQUENTE", resultado.getEstrategia()),
                    () -> assertEquals(partida.getId(), resultado.getPartidaId()),
                    () -> assertEquals("Time A", resultado.getMandante()),
                    () -> assertEquals("Time B", resultado.getVisitante())
            );
        }

        @Test
        @DisplayName("Deve cair para MEDIA_GOLS quando PLACAR_FREQUENTE nao tem historico")
        void deveCairParaMediaGolsQuandoPlacarFrequenteVazio() {
            RecomendacaoResponseDTO esperada = RecomendacaoResponseDTO.builder()
                    .golsMandanteRecomendado(2).golsVisitanteRecomendado(1)
                    .estrategia("MEDIA_GOLS").build();

            when(grupoAutorizacaoService.obterUsuarioValido(1L, "111111")).thenReturn(usuarioPremium);
            when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
            when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
            when(placarFrequente.recomendar(partida)).thenReturn(Optional.empty());
            when(mediaGols.recomendar(partida)).thenReturn(Optional.of(esperada));

            RecomendacaoResponseDTO resultado = recomendacaoService.recomendar(1L, 1L, 1L, "111111");

            assertAll(
                    () -> assertEquals(2, resultado.getGolsMandanteRecomendado()),
                    () -> assertEquals(1, resultado.getGolsVisitanteRecomendado()),
                    () -> assertEquals("MEDIA_GOLS", resultado.getEstrategia())
            );
        }

        @Test
        @DisplayName("Deve retornar sem recomendacao quando nenhuma estrategia tem historico")
        void deveRetornarFallbackQuandoSemHistorico() {
            when(grupoAutorizacaoService.obterUsuarioValido(1L, "111111")).thenReturn(usuarioPremium);
            when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
            when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
            when(placarFrequente.recomendar(partida)).thenReturn(Optional.empty());
            when(mediaGols.recomendar(partida)).thenReturn(Optional.empty());

            RecomendacaoResponseDTO resultado = recomendacaoService.recomendar(1L, 1L, 1L, "111111");

            assertAll(
                () -> assertFalse(resultado.isTemRecomendacao()),
                () -> assertNull(resultado.getGolsMandanteRecomendado()),
                () -> assertNull(resultado.getGolsVisitanteRecomendado())
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
                    () -> recomendacaoService.recomendar(1L, 1L, 99L, "111111"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando codigo invalido")
        void deveLancarExcecaoQuandoCodigoInvalido() {
            when(grupoAutorizacaoService.obterUsuarioValido(1L, "ERRADO"))
                    .thenThrow(CodigoDeAcessoInvalidoException.class);

            assertThrows(CodigoDeAcessoInvalidoException.class,
                    () -> recomendacaoService.recomendar(1L, 1L, 1L, "ERRADO"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando usuario nao e Premium")
        void deveLancarExcecaoQuandoUsuarioNaoPremium() {
            when(grupoAutorizacaoService.obterUsuarioValido(2L, "222222")).thenReturn(usuarioPadrao);

            assertThrows(UsuarioNaoPremiumException.class,
                    () -> recomendacaoService.recomendar(1L, 1L, 2L, "222222"));
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
                    () -> recomendacaoService.recomendar(1L, 99L, 1L, "111111"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando grupo nao existe")
        void deveLancarExcecaoQuandoGrupoNaoExiste() {
            when(grupoAutorizacaoService.obterUsuarioValido(1L, "111111")).thenReturn(usuarioPremium);
            when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
            when(grupoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(GrupoNaoExisteException.class,
                    () -> recomendacaoService.recomendar(99L, 1L, 1L, "111111"));
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
                    () -> recomendacaoService.recomendar(1L, 2L, 1L, "111111"));
        }
    }
        @Nested
        @DisplayName("Sobrecarga sem grupoId")
        class SobreargaSemGrupoId {

        @Test
        @DisplayName("Deve retornar recomendacao sem validar grupo")
        void deveRetornarRecomendacaoSemGrupo() {
                RecomendacaoResponseDTO esperada = RecomendacaoResponseDTO.builder()
                        .golsMandanteRecomendado(1).golsVisitanteRecomendado(0)
                        .estrategia("PLACAR_FREQUENTE").temRecomendacao(true)
                        .mensagem("Recomendação baseada no placar mais frequente do campeonato: 1x0")
                        .build();

                when(grupoAutorizacaoService.obterUsuarioValido(1L, "111111")).thenReturn(usuarioPremium);
                when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
                when(placarFrequente.recomendar(partida)).thenReturn(Optional.of(esperada));

                RecomendacaoResponseDTO resultado = recomendacaoService.recomendar(1L, 1L, "111111");

                assertAll(
                        () -> assertNotNull(resultado),
                        () -> assertEquals(1, resultado.getGolsMandanteRecomendado()),
                        () -> assertEquals(0, resultado.getGolsVisitanteRecomendado()),
                        () -> assertEquals(partida.getId(), resultado.getPartidaId()),
                        () -> assertEquals("Time A", resultado.getMandante()),
                        () -> assertEquals("Time B", resultado.getVisitante()),
                        () -> assertTrue(resultado.isTemRecomendacao())
                );
        }

        @Test
        @DisplayName("Deve retornar fallback sem grupoId quando sem historico")
        void deveRetornarFallbackSemGrupo() {
                when(grupoAutorizacaoService.obterUsuarioValido(1L, "111111")).thenReturn(usuarioPremium);
                when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
                when(placarFrequente.recomendar(partida)).thenReturn(Optional.empty());
                when(mediaGols.recomendar(partida)).thenReturn(Optional.empty());

                RecomendacaoResponseDTO resultado = recomendacaoService.recomendar(1L, 1L, "111111");

                assertAll(
                        () -> assertFalse(resultado.isTemRecomendacao()),
                        () -> assertNull(resultado.getGolsMandanteRecomendado()),
                        () -> assertNull(resultado.getGolsVisitanteRecomendado())
                );
        }

        @Test
        @DisplayName("Deve lancar excecao quando usuario nao e Premium na sobrecarga")
        void deveLancarExcecaoQuandoNaoPremiumSemGrupo() {
                when(grupoAutorizacaoService.obterUsuarioValido(2L, "222222")).thenReturn(usuarioPadrao);

                assertThrows(UsuarioNaoPremiumException.class,
                        () -> recomendacaoService.recomendar(1L, 2L, "222222"));
        }

        @Test
        @DisplayName("Deve lancar excecao quando partida nao existe na sobrecarga")
        void deveLancarExcecaoQuandoPartidaNaoExisteSemGrupo() {
                when(grupoAutorizacaoService.obterUsuarioValido(1L, "111111")).thenReturn(usuarioPremium);
                when(partidaRepository.findById(99L)).thenReturn(Optional.empty());

                assertThrows(PartidaNaoExisteException.class,
                        () -> recomendacaoService.recomendar(99L, 1L, "111111"));
        }
        }
}