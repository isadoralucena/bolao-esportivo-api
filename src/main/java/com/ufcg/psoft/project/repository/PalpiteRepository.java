package com.ufcg.psoft.project.repository;

import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PalpiteRepository extends JpaRepository<Palpite, Long> {

    List<Palpite> findByGrupoId(Long grupoId);

    List<Palpite> findByPartidaIdAndGrupoId(Long partidaId, Long grupoId);

    List<Palpite> findByPartidaId(Long partidaId);
    
    List<Palpite> findByUsuarioId(Long usuarioId);

    List<Usuario> findDistinctUsuarioByPartidaId(Long partidaId);

    boolean existsByUsuarioIdAndPartidaIdAndGrupoId(Long usuarioId, Long partidaId, Long grupoId);
}
