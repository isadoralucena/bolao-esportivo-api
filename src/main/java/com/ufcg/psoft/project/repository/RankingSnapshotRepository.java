package com.ufcg.psoft.project.repository;

import com.ufcg.psoft.project.model.RankingSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankingSnapshotRepository extends JpaRepository<RankingSnapshot, Long> {

    List<RankingSnapshot> findByGrupoIdAndUsuarioIdOrderByDataSnapshotAsc(Long grupoId, Long usuarioId);

    List<RankingSnapshot> findByGrupoIdOrderByDataSnapshotAscPosicaoAsc(Long grupoId);

    List<RankingSnapshot> findByGrupoIdAndPosicaoOrderByDataSnapshotAsc(Long grupoId, int posicao);

    List<RankingSnapshot> findByGrupoIdOrderByDataSnapshotDescPosicaoAsc(Long grupoId);

    boolean existsByGrupoIdAndPartidaId(Long grupoId, Long partidaId);
}