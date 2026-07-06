package com.ufcg.psoft.project.repository;

import com.ufcg.psoft.project.model.Palpite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PalpiteRepository extends JpaRepository<Palpite, Long> {

    List<Palpite> findByGrupoId(Long grupoId);

    List<Palpite> findByPartidaIdAndGrupoId(Long partidaId, Long grupoId);

    List<Palpite> findByPartidaId(Long partidaId);
    
    List<Palpite> findByUsuarioId(Long usuarioId);

    Optional<Palpite> findByUsuarioIdAndPartidaIdAndGrupoId(Long usuarioId, Long partidaId, Long grupoId);

    boolean existsByUsuarioIdAndPartidaIdAndGrupoId(Long usuarioId, Long partidaId, Long grupoId);
}
