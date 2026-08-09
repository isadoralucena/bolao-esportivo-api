package com.ufcg.psoft.project.service;

import static com.ufcg.psoft.project.config.TestClockConfig.FIXED_CLOCK;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.ufcg.psoft.project.exception.usuario.UsuarioNaoExisteException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoPromovidoException;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.*;
import com.ufcg.psoft.project.service.premium.ContadorRequisicoes;
import com.ufcg.psoft.project.service.premium.PromocaoPremiumServiceImpl;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Testes unitarios do PromocaoPremiumServiceImpl")
class PromocaoPremiumServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PalpiteRepository palpiteRepository;

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private PontuacaoPalpiteRepository pontuacaoPalpiteRepository;

    @Mock
    private PromocaoPremiumRepository promocaoPremiumRepository;

    @Mock
    private ContadorRequisicoes contadorRequisicoes;

    private PromocaoPremiumServiceImpl promocaoPremiumService;

    private Usuario usuarioPadrao;

    @BeforeEach
    void setUp() {
        promocaoPremiumService = new PromocaoPremiumServiceImpl(
                usuarioRepository,
                palpiteRepository,
                grupoRepository,
                pontuacaoPalpiteRepository,
                promocaoPremiumRepository,
                contadorRequisicoes,
                FIXED_CLOCK
        );
        ReflectionTestUtils.setField(promocaoPremiumService, "minPalpites", 50);
        ReflectionTestUtils.setField(promocaoPremiumService, "minGrupos", 3);
        ReflectionTestUtils.setField(promocaoPremiumService, "minRequisicoes", 100);
        ReflectionTestUtils.setField(promocaoPremiumService, "minAcertos", 2);

        usuarioPadrao = Usuario.builder()
                .id(1L)
                .nome("Usuario Teste")
                .email("teste@email.com")
                .username("teste")
                .endereco("Rua Teste")
                .codigo("123456")
                .perfil(PerfilUsuario.PADRAO)
                .build();
    }

    @Nested
    @DisplayName("Criterios de avaliacao de promocao")
    class AvaliacaoPromocao {

        @Test
        @DisplayName("Deve promover usuario quando atinge todos os minimos")
        void devePromoverUsuarioQuandoAtingeTodosOsMinimos() {
            // Arrange
            when(usuarioRepository.findAll()).thenReturn(List.of(usuarioPadrao));
            when(promocaoPremiumRepository.existsByUsuarioId(1L)).thenReturn(false);
            when(palpiteRepository.findByUsuarioId(1L)).thenReturn(Collections.nCopies(50, mock(Palpite.class)));

            Grupo grupo1 = Grupo.builder().id(1L).participantes(Set.of(usuarioPadrao)).build();
            Grupo grupo2 = Grupo.builder().id(2L).participantes(Set.of(usuarioPadrao)).build();
            Grupo grupo3 = Grupo.builder().id(3L).participantes(Set.of(usuarioPadrao)).build();
            when(grupoRepository.findAll()).thenReturn(List.of(grupo1, grupo2, grupo3));

            when(contadorRequisicoes.getContagem(1L)).thenReturn(100L);

            PontuacaoPalpite pp1 = PontuacaoPalpite.builder().acertouVencedor(true).build();
            PontuacaoPalpite pp2 = PontuacaoPalpite.builder().acertouEmpate(true).build();
            when(pontuacaoPalpiteRepository.findByPalpite_Usuario_Id(1L)).thenReturn(List.of(pp1, pp2));

            // Act
            promocaoPremiumService.avaliarPromocoes();

            // Assert
            assertEquals(PerfilUsuario.PREMIUM, usuarioPadrao.getPerfil());
            verify(usuarioRepository).save(usuarioPadrao);
            verify(promocaoPremiumRepository).save(argThat(promocao ->
                    LocalDateTime.now(FIXED_CLOCK).equals(promocao.getData())));
            verify(contadorRequisicoes).resetar(1L);
        }

        @Test
        @DisplayName("Nao deve promover usuario quando nao atinge minimos de palpites")
        void naoDevePromoverUsuarioQuandoNaoAtingeMinimoPalpites() {
            // Arrange
            when(usuarioRepository.findAll()).thenReturn(List.of(usuarioPadrao));
            when(promocaoPremiumRepository.existsByUsuarioId(1L)).thenReturn(false);
            when(palpiteRepository.findByUsuarioId(1L)).thenReturn(Collections.nCopies(10, mock(Palpite.class)));
            when(grupoRepository.findAll()).thenReturn(List.of(
                    Grupo.builder().id(1L).participantes(Set.of(usuarioPadrao)).build(),
                    Grupo.builder().id(2L).participantes(Set.of(usuarioPadrao)).build(),
                    Grupo.builder().id(3L).participantes(Set.of(usuarioPadrao)).build()));
            when(contadorRequisicoes.getContagem(1L)).thenReturn(100L);
            when(pontuacaoPalpiteRepository.findByPalpite_Usuario_Id(1L)).thenReturn(List.of(
                    PontuacaoPalpite.builder().acertouVencedor(true).build()));

            // Act
            promocaoPremiumService.avaliarPromocoes();

            // Assert
            assertEquals(PerfilUsuario.PADRAO, usuarioPadrao.getPerfil());
            verify(usuarioRepository, never()).save(any());
            verify(promocaoPremiumRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nao deve promover usuario quando nao atinge minimo de grupos")
        void naoDevePromoverUsuarioQuandoNaoAtingeMinimoGrupos() {
            // Arrange
            when(usuarioRepository.findAll()).thenReturn(List.of(usuarioPadrao));
            when(promocaoPremiumRepository.existsByUsuarioId(1L)).thenReturn(false);
            when(palpiteRepository.findByUsuarioId(1L)).thenReturn(Collections.nCopies(50, mock(Palpite.class)));
            when(grupoRepository.findAll()).thenReturn(List.of(
                    Grupo.builder().id(1L).participantes(Set.of(usuarioPadrao)).build()));
            when(contadorRequisicoes.getContagem(1L)).thenReturn(100L);
            when(pontuacaoPalpiteRepository.findByPalpite_Usuario_Id(1L)).thenReturn(List.of(
                    PontuacaoPalpite.builder().acertouVencedor(true).build()));

            // Act
            promocaoPremiumService.avaliarPromocoes();

            // Assert
            assertEquals(PerfilUsuario.PADRAO, usuarioPadrao.getPerfil());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nao deve promover usuario quando nao atinge minimo de requisicoes")
        void naoDevePromoverUsuarioQuandoNaoAtingeMinimoRequisicoes() {
            // Arrange
            when(usuarioRepository.findAll()).thenReturn(List.of(usuarioPadrao));
            when(promocaoPremiumRepository.existsByUsuarioId(1L)).thenReturn(false);
            when(palpiteRepository.findByUsuarioId(1L)).thenReturn(Collections.nCopies(50, mock(Palpite.class)));
            when(grupoRepository.findAll()).thenReturn(List.of(
                    Grupo.builder().id(1L).participantes(Set.of(usuarioPadrao)).build(),
                    Grupo.builder().id(2L).participantes(Set.of(usuarioPadrao)).build(),
                    Grupo.builder().id(3L).participantes(Set.of(usuarioPadrao)).build()));
            when(contadorRequisicoes.getContagem(1L)).thenReturn(50L);
            when(pontuacaoPalpiteRepository.findByPalpite_Usuario_Id(1L)).thenReturn(List.of(
                    PontuacaoPalpite.builder().acertouVencedor(true).build()));

            // Act
            promocaoPremiumService.avaliarPromocoes();

            // Assert
            assertEquals(PerfilUsuario.PADRAO, usuarioPadrao.getPerfil());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nao deve promover usuario quando nao atinge minimo de acertos")
        void naoDevePromoverUsuarioQuandoNaoAtingeMinimoAcertos() {
            // Arrange
            when(usuarioRepository.findAll()).thenReturn(List.of(usuarioPadrao));
            when(promocaoPremiumRepository.existsByUsuarioId(1L)).thenReturn(false);
            when(palpiteRepository.findByUsuarioId(1L)).thenReturn(Collections.nCopies(50, mock(Palpite.class)));
            when(grupoRepository.findAll()).thenReturn(List.of(
                    Grupo.builder().id(1L).participantes(Set.of(usuarioPadrao)).build(),
                    Grupo.builder().id(2L).participantes(Set.of(usuarioPadrao)).build(),
                    Grupo.builder().id(3L).participantes(Set.of(usuarioPadrao)).build()));
            when(contadorRequisicoes.getContagem(1L)).thenReturn(100L);
            when(pontuacaoPalpiteRepository.findByPalpite_Usuario_Id(1L)).thenReturn(List.of(
                    PontuacaoPalpite.builder().acertouVencedor(false).acertouEmpate(false).acertouPlacarExato(false).build()));

            // Act
            promocaoPremiumService.avaliarPromocoes();

            // Assert
            assertEquals(PerfilUsuario.PADRAO, usuarioPadrao.getPerfil());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nao deve promover usuario que ja esta premium")
        void naoDevePromoverUsuarioQueJaEstaPremium() {
            // Arrange
            Usuario usuarioPremium = Usuario.builder()
                    .id(2L)
                    .nome("Usuario Premium")
                    .email("premium@email.com")
                    .username("premium")
                    .endereco("Rua Premium")
                    .codigo("654321")
                    .perfil(PerfilUsuario.PREMIUM)
                    .build();

            when(usuarioRepository.findAll()).thenReturn(List.of(usuarioPremium));

            // Act
            promocaoPremiumService.avaliarPromocoes();

            // Assert
            verify(palpiteRepository, never()).findByUsuarioId(anyLong());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nao deve promover usuario ja promovido (com registro existente)")
        void naoDevePromoverUsuarioJaPromovidoComRegistroExistente() {
            // Arrange
            when(usuarioRepository.findAll()).thenReturn(List.of(usuarioPadrao));
            when(promocaoPremiumRepository.existsByUsuarioId(1L)).thenReturn(true);

            // Act
            promocaoPremiumService.avaliarPromocoes();

            // Assert
            verify(palpiteRepository, never()).findByUsuarioId(anyLong());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve registrar motivo com todas as metricas na promocao")
        void deveRegistrarMotivoComTodasAsMetricas() {
            // Arrange
            when(usuarioRepository.findAll()).thenReturn(List.of(usuarioPadrao));
            when(promocaoPremiumRepository.existsByUsuarioId(1L)).thenReturn(false);
            when(palpiteRepository.findByUsuarioId(1L)).thenReturn(Collections.nCopies(55, mock(Palpite.class)));
            when(grupoRepository.findAll()).thenReturn(List.of(
                    Grupo.builder().id(1L).participantes(Set.of(usuarioPadrao)).build(),
                    Grupo.builder().id(2L).participantes(Set.of(usuarioPadrao)).build(),
                    Grupo.builder().id(3L).participantes(Set.of(usuarioPadrao)).build(),
                    Grupo.builder().id(4L).participantes(Set.of(usuarioPadrao)).build()));
            when(contadorRequisicoes.getContagem(1L)).thenReturn(200L);

            PontuacaoPalpite pp1 = PontuacaoPalpite.builder().acertouVencedor(true).build();
            PontuacaoPalpite pp2 = PontuacaoPalpite.builder().acertouPlacarExato(true).build();
            PontuacaoPalpite pp3 = PontuacaoPalpite.builder().acertouEmpate(true).build();
            when(pontuacaoPalpiteRepository.findByPalpite_Usuario_Id(1L)).thenReturn(List.of(pp1, pp2, pp3));

            // Act
            promocaoPremiumService.avaliarPromocoes();

            // Assert
            verify(promocaoPremiumRepository).save(argThat(promocao ->
                    promocao.getPalpites() == 55
                    && promocao.getGruposParticipa() == 4
                    && promocao.getRequisicoes() == 200
                    && promocao.getAcertos() == 3
                    && promocao.getMotivo().contains("55 palpites")
                    && promocao.getMotivo().contains("4 grupos")
                    && promocao.getMotivo().contains("200 requisicoes")
                    && promocao.getMotivo().contains("3 acertos")
            ));
        }

        @Test
        @DisplayName("Nao deve contar acertos quando pontuacao nao tem nenhum acerto")
        void naoDeveContarAcertosQuandoNaoTemNenhumAcerto() {
            // Arrange
            when(usuarioRepository.findAll()).thenReturn(List.of(usuarioPadrao));
            when(promocaoPremiumRepository.existsByUsuarioId(1L)).thenReturn(false);
            when(palpiteRepository.findByUsuarioId(1L)).thenReturn(Collections.nCopies(50, mock(Palpite.class)));
            when(grupoRepository.findAll()).thenReturn(List.of(
                    Grupo.builder().id(1L).participantes(Set.of(usuarioPadrao)).build(),
                    Grupo.builder().id(2L).participantes(Set.of(usuarioPadrao)).build(),
                    Grupo.builder().id(3L).participantes(Set.of(usuarioPadrao)).build()));
            when(contadorRequisicoes.getContagem(1L)).thenReturn(100L);

            PontuacaoPalpite pp1 = PontuacaoPalpite.builder()
                    .acertouVencedor(false).acertouEmpate(false).acertouPlacarExato(false).build();
            when(pontuacaoPalpiteRepository.findByPalpite_Usuario_Id(1L)).thenReturn(List.of(pp1));

            // Act
            promocaoPremiumService.avaliarPromocoes();

            // Assert
            assertEquals(PerfilUsuario.PADRAO, usuarioPadrao.getPerfil());
            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Consulta de promocao")
    class ConsultaPromocao {

        @Test
        @DisplayName("Deve retornar promocao quando usuario foi promovido")
        void deveRetornarPromocaoQuandoUsuarioFoiPromovido() {
            // Arrange
            PromocaoPremium promocao = PromocaoPremium.builder()
                    .id(1L)
                    .usuario(usuarioPadrao)
                    .motivo("Promovido por atingir os criterios")
                    .palpites(50)
                    .gruposParticipa(3)
                    .requisicoes(100)
                    .acertos(10)
                    .build();

            when(usuarioRepository.existsById(1L)).thenReturn(true);
            when(promocaoPremiumRepository.findByUsuarioId(1L)).thenReturn(Optional.of(promocao));

            // Act
            var resultado = promocaoPremiumService.obterPromocao(1L);

            // Assert
            assertAll(
                    () -> assertEquals(1L, resultado.getId()),
                    () -> assertEquals(1L, resultado.getUsuarioId()),
                    () -> assertEquals(50, resultado.getPalpites()),
                    () -> assertEquals(3, resultado.getGruposParticipa()),
                    () -> assertEquals(100, resultado.getRequisicoes()),
                    () -> assertEquals(10, resultado.getAcertos())
            );
        }

        @Test
        @DisplayName("Deve lancar excecao quando usuario nao foi promovido")
        void deveLancarExcecaoQuandoUsuarioNaoFoiPromovido() {
            // Arrange
            when(usuarioRepository.existsById(1L)).thenReturn(true);
            when(promocaoPremiumRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(UsuarioNaoPromovidoException.class,
                    () -> promocaoPremiumService.obterPromocao(1L));
        }

        @Test
        @DisplayName("Deve lancar excecao quando usuario nao existe")
        void deveLancarExcecaoQuandoUsuarioNaoExiste() {
            // Arrange
            when(usuarioRepository.existsById(99L)).thenReturn(false);

            // Act & Assert
            assertThrows(UsuarioNaoExisteException.class,
                    () -> promocaoPremiumService.obterPromocao(99L));
        }
    }
}
