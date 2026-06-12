package com.ufcg.psoft.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ufcg.psoft.project.model.Convite;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.StatusConvite;
import com.ufcg.psoft.project.model.Usuario;

public interface ConviteRepository extends JpaRepository<Convite, Long> {

    List<Convite> findByConvidadoIdAndStatus(Long convidadoId, StatusConvite statusConvite);
    boolean existsByGrupoAndConvidadoAndStatus(Grupo grupo, Usuario convidado, StatusConvite status);
}
