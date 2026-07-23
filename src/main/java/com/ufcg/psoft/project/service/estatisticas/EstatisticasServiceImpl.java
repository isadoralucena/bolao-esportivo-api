package com.ufcg.psoft.project.service.estatisticas;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ufcg.psoft.project.dto.estatisticas.EstatisticasResponseDTO;
import com.ufcg.psoft.project.event.PartidaConsolidadaEvent;
import com.ufcg.psoft.project.model.Estatisticas;
import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.EstatisticasRepository;
import com.ufcg.psoft.project.repository.PalpiteRepository;
import com.ufcg.psoft.project.service.grupo.GrupoAutorizacaoService;

import java.util.stream.Collectors;

@Service
public class EstatisticasServiceImpl implements EstatisticasService {

    @Autowired
    PalpiteRepository palpiteRepository;

    @Autowired
    EstatisticasRepository estatisticasRepository;

    @Autowired
    GrupoAutorizacaoService grupoAutorizacaoService;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public List<EstatisticasResponseDTO> calcularEstatisticasAssociadasAConsolidacaoDePartida(PartidaConsolidadaEvent event) {
        Partida p = event.getPartida();

        List<Usuario> usuarios = palpiteRepository.findDistinctUsuarioByPartidaId(p.getId());
        List<Estatisticas> estatisticas = new ArrayList<>();
        for (Usuario u : usuarios) {
            Estatisticas e = calcularEstatisticasUsuario(u);
            estatisticas.add(e);
            estatisticasRepository.save(e);
        }
        
        return estatisticas.stream()
            .map(EstatisticasResponseDTO::new)
            .collect(Collectors.toList());
    }

    @Override
    public EstatisticasResponseDTO obterEstatisticaMaisRecente(Long usuarioId, String codigoAcesso) {
        Usuario usuario = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);
        Estatisticas estatisticaAtual = estatisticasRepository.findFirstByUsuarioIdOrderByDataRegistroDesc(usuario.getId());
        return new EstatisticasResponseDTO(estatisticaAtual);
    }

    @Override
    public List<EstatisticasResponseDTO> obterEvolucaoEstatisticas(Long usuarioId, String codigoAcesso) {
        Usuario usuario = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);

        return estatisticasRepository.findByUsuarioId(usuario.getId()).stream()
            .map(EstatisticasResponseDTO::new)
            .collect(Collectors.toList());
    }

    private Estatisticas calcularEstatisticasUsuario(Usuario u) {
        return new Estatisticas(); // todo
    }
}
