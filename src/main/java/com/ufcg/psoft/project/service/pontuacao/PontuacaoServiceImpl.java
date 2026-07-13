package com.ufcg.psoft.project.service.pontuacao;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufcg.psoft.project.dto.pontuacao.PontuacaoPalpiteResponseDTO;
import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;
import com.ufcg.psoft.project.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.project.exception.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.PartidaNaoExisteException;
import com.ufcg.psoft.project.exception.UsuarioNaoExisteException;
import com.ufcg.psoft.project.exception.UsuarioNaoParticipanteException;
import com.ufcg.psoft.project.model.PontuacaoPalpite;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.model.RegraPontuacao;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.PontuacaoPalpiteRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PalpiteRepository;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.repository.RegraPontuacaoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import com.ufcg.psoft.project.service.notificacao.NotificacaoService;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Service
public class PontuacaoServiceImpl implements PontuacaoService {
    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private PalpiteRepository palpiteRepository;

    @Autowired
    private GrupoRepository grupoRepository;
    
    @Autowired
    private RegraPontuacaoRepository regraPontuacaoRepository;

    @Autowired
    private PontuacaoPalpiteRepository pontuacaoPalpiteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private List<Pontuador> pontuadoresDisponiveis;

    private Map<TipoRegraPontuacao, Pontuador> pontuadores;

    @PostConstruct
    public void inicializarPontuadores() {
        this.pontuadores = new EnumMap<>(TipoRegraPontuacao.class);

        for (Pontuador pontuador : pontuadoresDisponiveis) {
            this.pontuadores.put(pontuador.getTipo(), pontuador);
        }
    }

    @Override
    @Transactional
    public List<PontuacaoPalpiteResponseDTO> calcularPontuacoesAssociadasAPartida(Long partidaId) {
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(PartidaNaoExisteException::new);

        if (partida.getStatus() != PartidaStatus.FINALIZADO) {
            return List.of();
        }

        if (partida.getGolsMandante() == null || partida.getGolsVisitante() == null) {
            throw new IllegalArgumentException("Os gols de uma partida finalizada não podem ser nulos!");
        }

        List<Palpite> palpites = palpiteRepository.findByPartidaId(partidaId);

        // captura posicoes antes do calculo por grupo
        Map<Long, Map<Long, Integer>> posicoesAntesPorGrupo = new HashMap<>();
        palpites.stream()
                .map(p -> p.getGrupo().getId())
                .distinct()
                .forEach(grupoId -> {
                    Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
                    List<PontuacaoParticipanteResponseDTO> rankingAntes = new ArrayList<>();
                    for (Usuario participante : grupo.getParticipantes()) {
                        rankingAntes.add(calcularPontuacaoParticipanteNoGrupo(grupoId, participante.getId()));
                    }
                    rankingAntes.sort((a, b) -> Integer.compare(b.getPontuacao(), a.getPontuacao()));
                    Map<Long, Integer> posicoes = new HashMap<>();
                    for (int i = 0; i < rankingAntes.size(); i++) {
                        posicoes.put(rankingAntes.get(i).getUsuarioId(), i + 1);
                    }
                    posicoesAntesPorGrupo.put(grupoId, posicoes);
                });

        List<PontuacaoPalpite> pontuacoes = new ArrayList<>();
        for (Palpite palpite : palpites) {
            PontuacaoPalpite pontuacaoPalpite = buscarOuCriarPontuacaoPalpite(palpite);
            atualizarAcertos(pontuacaoPalpite);
            int pontuacao = calcularPontuacaoPalpite(pontuacaoPalpite);
            pontuacaoPalpite.setPontuacao(pontuacao);
            pontuacoes.add(pontuacaoPalpite);
        }

        List<PontuacaoPalpite> pontuacoesSalvas = pontuacaoPalpiteRepository.saveAll(pontuacoes);

        pontuacoesSalvas.stream()
                .map(p -> p.getPalpite().getGrupo().getId())
                .distinct()
                .forEach(grupoId -> {
                    notificacaoService.notificarAtualizacaoRanking(grupoId);
                    notificarMudancasDePosicao(grupoId, posicoesAntesPorGrupo.get(grupoId));
                });

        return pontuacoesSalvas.stream()
                .map(PontuacaoPalpiteResponseDTO::new)
                .toList();
    }

    @Override
    @Transactional
    public List<PontuacaoPalpiteResponseDTO> calcularPontuacoesDoGrupo(Long grupoId) {
        // usado para sincronizar automaticamente pontuacoes após a modificaçao do conjunto de regras do grupo. é chamado pelo service de grupos.

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(GrupoNaoExisteException::new);

        List<Palpite> palpites = palpiteRepository.findByGrupoId(grupo.getId());
        List<PontuacaoPalpite> pontuacoes = new ArrayList<>();

        for (Palpite palpite : palpites) {
            Partida partida = palpite.getPartida();

            if (partida.getStatus() == PartidaStatus.FINALIZADO) {
                if (partida.getGolsMandante() == null || partida.getGolsVisitante() == null) {
                    throw new IllegalArgumentException("Os gols de uma partida finalizada não podem ser nulos!");
                }

                PontuacaoPalpite pontuacaoPalpite = buscarOuCriarPontuacaoPalpite(palpite);

                atualizarAcertos(pontuacaoPalpite);
                int pontuacao = calcularPontuacaoPalpite(pontuacaoPalpite);
                pontuacaoPalpite.setPontuacao(pontuacao);

                pontuacoes.add(pontuacaoPalpite);
            }
        }

        List<PontuacaoPalpite> pontuacoesSalvas = pontuacaoPalpiteRepository.saveAll(pontuacoes);

        return pontuacoesSalvas.stream()
                .map(PontuacaoPalpiteResponseDTO::new)
                .toList();
    }

    @Override
    public PontuacaoParticipanteResponseDTO calcularPontuacaoParticipanteNoGrupo(Long grupoId, Long participanteId) {
        // obtem todos os palpites de um participante num grupo, suas respectivas pontuaçoes, e soma.
        
        Usuario participante = usuarioRepository.findById(participanteId)
                .orElseThrow(UsuarioNaoExisteException::new);

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(GrupoNaoExisteException::new);

        if (!grupo.getParticipantes().contains(participante)) {
            throw new UsuarioNaoParticipanteException();
        }

        List<PontuacaoPalpite> pontuacoes = pontuacaoPalpiteRepository.findByPalpite_Grupo_IdAndPalpite_Usuario_Id(grupoId, participanteId);

        return calcularPontuacaoParticipante(grupoId, participante, pontuacoes);
    }

    @Override
    public List<PontuacaoParticipanteResponseDTO> listarPontuacoesParticipantesDoGrupo(Long grupoId, Long usuarioId, String codigoAcesso) {
        Usuario usuario = obterUsuarioValido(usuarioId, codigoAcesso);

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(GrupoNaoExisteException::new);

        if (!grupo.getParticipantes().contains(usuario)) {
            throw new UsuarioNaoParticipanteException();
        }

        List<PontuacaoParticipanteResponseDTO> pontuacoes = new ArrayList<>();
        for (Usuario participante : grupo.getParticipantes()) {
            PontuacaoParticipanteResponseDTO pontuacaoParticipante = calcularPontuacaoParticipanteNoGrupo(grupoId, participante.getId());
            pontuacoes.add(pontuacaoParticipante);
        }

        return pontuacoes;
    }

    @Override
    public PontuacaoParticipanteResponseDTO calcularPontuacaoGlobalDoParticipante(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(UsuarioNaoExisteException::new);
        List<PontuacaoPalpite> pontuacoes = pontuacaoPalpiteRepository.findByPalpite_Usuario_Id(usuarioId);
        return calcularPontuacaoParticipante(null, usuario, pontuacoes);
    }

    @Override
    public List<PontuacaoParticipanteResponseDTO> listarPontuacoesGlobais() {
        return usuarioRepository.findAll().stream()
            .map(usuario -> calcularPontuacaoGlobalDoParticipante(usuario.getId()))
            .toList();
    }

    private PontuacaoParticipanteResponseDTO calcularPontuacaoParticipante(Long grupoId, Usuario participante, List<PontuacaoPalpite> pontuacoes) {
        int pontuacaoTotal = 0;
        int erros = 0;
        int acertosVencedor = 0;
        int acertosEmpate = 0;
        int placaresExatos = 0;

        for (PontuacaoPalpite pontuacaoPalpite : pontuacoes) {
            boolean acertouAlgo = false;
            pontuacaoTotal += pontuacaoPalpite.getPontuacao();

            if (pontuacaoPalpite.isAcertouVencedor()) {
                acertosVencedor++;
                acertouAlgo = true;
            }

            if (pontuacaoPalpite.isAcertouEmpate()) {
                acertosEmpate++;
                acertouAlgo = true;
            }

            if (pontuacaoPalpite.isAcertouPlacarExato()) {
                placaresExatos++;
                acertouAlgo = true;
            }

            if (!acertouAlgo) {
                erros += 1;
            }
        }

        return new PontuacaoParticipanteResponseDTO(
            grupoId,
            participante,
            pontuacaoTotal,
            erros,
            acertosVencedor,
            acertosEmpate,
            placaresExatos
        );
    }

    private PontuacaoPalpite buscarOuCriarPontuacaoPalpite(Palpite palpite) {
        return pontuacaoPalpiteRepository.findByPalpiteId(palpite.getId())
                .orElse(PontuacaoPalpite.builder()
                        .palpite(palpite)
                        .pontuacao(0)
                        .acertouVencedor(false)
                        .acertouEmpate(false)
                        .acertouPlacarExato(false)
                        .build());
    }

    private void atualizarAcertos(PontuacaoPalpite pontuacaoPalpite) {
        Palpite palpite = pontuacaoPalpite.getPalpite();
        Partida partida = palpite.getPartida();

        int resultadoReal = Integer.compare(
                partida.getGolsMandante(),
                partida.getGolsVisitante()
        );

        int resultadoPalpite = Integer.compare(
                palpite.getGolsMandante(),
                palpite.getGolsVisitante()
        );

        boolean acertouPlacarExato =
                partida.getGolsMandante().equals(palpite.getGolsMandante()) &&
                partida.getGolsVisitante().equals(palpite.getGolsVisitante());

        boolean acertouEmpate = resultadoReal == 0 && resultadoPalpite == 0;
        boolean acertouVencedor = resultadoReal != 0 && resultadoReal == resultadoPalpite;

        pontuacaoPalpite.setAcertouPlacarExato(acertouPlacarExato);
        pontuacaoPalpite.setAcertouEmpate(acertouEmpate);
        pontuacaoPalpite.setAcertouVencedor(acertouVencedor);
    }

    private int calcularPontuacaoPalpite(PontuacaoPalpite pontuacaoPalpite) {
        Long grupoId = pontuacaoPalpite.getPalpite().getGrupo().getId();

        List<RegraPontuacao> regras = regraPontuacaoRepository.findByGrupoId(grupoId);

        int total = 0;

        for (RegraPontuacao regra : regras) {
            total += calcularPontuacaoDaRegra(pontuacaoPalpite, regra);
        }

        return total;
    }

    private int calcularPontuacaoDaRegra(PontuacaoPalpite pontuacaoPalpite, RegraPontuacao regra) {
        Pontuador pontuador = pontuadores.get(regra.getTipoRegraPontuacao());
        return pontuador.calcular(pontuacaoPalpite, regra);
    }

    private Usuario obterUsuarioValido(Long usuarioId, String codigo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(UsuarioNaoExisteException::new);

        if (!usuario.getCodigo().equals(codigo)) {
            throw new CodigoDeAcessoInvalidoException();
        }

        return usuario;
    }

    private void notificarMudancasDePosicao(Long grupoId, Map<Long, Integer> posicoesAntes) {
        if (posicoesAntes == null || posicoesAntes.isEmpty()) return;

        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);

        List<PontuacaoParticipanteResponseDTO> rankingAtual = new ArrayList<>();
        for (Usuario participante : grupo.getParticipantes()) {
            rankingAtual.add(calcularPontuacaoParticipanteNoGrupo(grupoId, participante.getId()));
        }
        rankingAtual.sort((a, b) -> Integer.compare(b.getPontuacao(), a.getPontuacao()));

        for (int i = 0; i < rankingAtual.size(); i++) {
            PontuacaoParticipanteResponseDTO participante = rankingAtual.get(i);
            int posicaoAtual = i + 1;
            Integer posicaoAnterior = posicoesAntes.get(participante.getUsuarioId());

            if (posicaoAnterior != null && posicaoAnterior != posicaoAtual) {
                notificacaoService.notificarMudancaDePosicao(
                        participante.getUsuarioNome(),
                        posicaoAnterior,
                        posicaoAtual,
                        grupoId
                );
            }
        }
    }
}