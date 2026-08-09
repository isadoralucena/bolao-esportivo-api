package com.ufcg.psoft.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ufcg.psoft.project.model.Grupo;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    List<Grupo> findByParticipantes_Id(Long usuarioId);
}
