package com.ufcg.psoft.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ufcg.psoft.project.model.PontuacaoPalpite;

public interface PontuacaoPalpiteRepository extends JpaRepository<PontuacaoPalpite, Long> {
    Optional<PontuacaoPalpite> findByPalpiteId(Long palpiteId);
    List<PontuacaoPalpite> findByPalpite_Partida_Id(Long partidaId);
    List<PontuacaoPalpite> findByPalpite_Grupo_Id(Long grupoId);
    List<PontuacaoPalpite> findByPalpite_Grupo_IdAndPalpite_Usuario_Id(Long grupoId, Long usuarioId);
    List<PontuacaoPalpite> findByPalpite_Usuario_Id(Long usuarioId);
}