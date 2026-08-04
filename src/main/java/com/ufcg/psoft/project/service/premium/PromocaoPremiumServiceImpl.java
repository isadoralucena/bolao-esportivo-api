package com.ufcg.psoft.project.service.premium;

import com.ufcg.psoft.project.dto.usuario.PromocaoPremiumResponseDTO;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoExisteException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoPromovidoException;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromocaoPremiumServiceImpl implements PromocaoPremiumService {

    private final UsuarioRepository usuarioRepository;

    private final PalpiteRepository palpiteRepository;

    private final GrupoRepository grupoRepository;

    private final PontuacaoPalpiteRepository pontuacaoPalpiteRepository;

    private final PromocaoPremiumRepository promocaoPremiumRepository;

    private final ContadorRequisicoes contadorRequisicoes;

    @Value("${project.premium.min-palpites:50}")
    private int minPalpites;

    @Value("${project.premium.min-grupos:3}")
    private int minGrupos;

    @Value("${project.premium.min-requisicoes:100}")
    private int minRequisicoes;

    @Value("${project.premium.min-acertos:10}")
    private int minAcertos;

    @Scheduled(fixedDelayString = "${project.premium.scheduler-delay-ms:300000}", initialDelay = 60000)
    @Override
    public void avaliarPromocoes() {
        List<Usuario> usuariosPadrao = usuarioRepository.findAll().stream()
                .filter(u -> u.getPerfil() == PerfilUsuario.PADRAO)
                .toList();

        for (Usuario usuario : usuariosPadrao) {
            if (promocaoPremiumRepository.existsByUsuarioId(usuario.getId())) {
                continue;
            }

            long palpites = palpiteRepository.findByUsuarioId(usuario.getId()).size();

            long gruposParticipa = grupoRepository.findAll().stream()
                    .filter(g -> g.getParticipantes().contains(usuario))
                    .count();

            long requisicoes = contadorRequisicoes.getContagem(usuario.getId());

            List<PontuacaoPalpite> pontuacoes = pontuacaoPalpiteRepository.findByPalpite_Usuario_Id(usuario.getId());
            long acertos = pontuacoes.stream()
                    .filter(p -> p.isAcertouVencedor() || p.isAcertouEmpate() || p.isAcertouPlacarExato())
                    .count();

            if (palpites >= minPalpites
                    && gruposParticipa >= minGrupos
                    && requisicoes >= minRequisicoes
                    && acertos >= minAcertos) {

                usuario.setPerfil(PerfilUsuario.PREMIUM);
                usuarioRepository.save(usuario);

                String motivo = String.format(
                        "Promovido por atingir os criterios automaticos: %d palpites, %d grupos, %d requisicoes, %d acertos",
                        palpites, gruposParticipa, requisicoes, acertos);

                PromocaoPremium promocao = PromocaoPremium.builder()
                        .usuario(usuario)
                        .data(LocalDateTime.now())
                        .motivo(motivo)
                        .palpites((int) palpites)
                        .gruposParticipa((int) gruposParticipa)
                        .requisicoes((int) requisicoes)
                        .acertos((int) acertos)
                        .build();

                promocaoPremiumRepository.save(promocao);
                contadorRequisicoes.resetar(usuario.getId());
            }
        }
    }

    @Override
    public PromocaoPremiumResponseDTO obterPromocao(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new UsuarioNaoExisteException();
        }
        PromocaoPremium promocao = promocaoPremiumRepository.findByUsuarioId(usuarioId)
                .orElseThrow(UsuarioNaoPromovidoException::new);
        return new PromocaoPremiumResponseDTO(promocao);
    }
}
