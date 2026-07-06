package com.ufcg.psoft.project.service.pontuacao;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
        // usado para sincronizar automaticamente pontuacoes após a sincronizaçao de uma partida. é chamado pelo service de partida.
        
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(PartidaNaoExisteException::new);

        if (partida.getStatus() != PartidaStatus.FINALIZADO) {
            return List.of();
        }

        if (partida.getGolsMandante() == null || partida.getGolsVisitante() == null) {
            throw new IllegalArgumentException("Os gols de uma partida finalizada não podem ser nulos!");
        }

        List<Palpite> palpites = palpiteRepository.findByPartidaId(partidaId);
        List<PontuacaoPalpite> pontuacoes = new ArrayList<>();

        for (Palpite palpite : palpites) {
            PontuacaoPalpite pontuacaoPalpite = buscarOuCriarPontuacaoPalpite(palpite);

            atualizarAcertos(pontuacaoPalpite);
            int pontuacao = calcularPontuacaoPalpite(pontuacaoPalpite);
            pontuacaoPalpite.setPontuacao(pontuacao);
            
            pontuacoes.add(pontuacaoPalpite);
        }

        List<PontuacaoPalpite> pontuacoesSalvas = pontuacaoPalpiteRepository.saveAll(pontuacoes);

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

        int pontuacaoTotal = 0;
        int acertosVencedor = 0;
        int acertosEmpate = 0;
        int placaresExatos = 0;

        for (PontuacaoPalpite pontuacaoPalpite : pontuacoes) {
            pontuacaoTotal += pontuacaoPalpite.getPontuacao();

            if (Boolean.TRUE.equals(pontuacaoPalpite.getAcertouVencedor())) {
                acertosVencedor++;
            }

            if (Boolean.TRUE.equals(pontuacaoPalpite.getAcertouEmpate())) {
                acertosEmpate++;
            }

            if (Boolean.TRUE.equals(pontuacaoPalpite.getAcertouPlacarExato())) {
                placaresExatos++;
            }
        }

        return new PontuacaoParticipanteResponseDTO(
            grupoId,
            participante,
            pontuacaoTotal,
            acertosVencedor,
            acertosEmpate,
            placaresExatos
        );
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
}