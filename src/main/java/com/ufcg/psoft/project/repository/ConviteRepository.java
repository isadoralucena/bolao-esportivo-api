package com.ufcg.psoft.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ufcg.psoft.project.model.Convite;
import com.ufcg.psoft.project.model.StatusConvite;

public interface ConviteRepository extends JpaRepository<Convite, Long> {

    List<Convite> findByConvidadoIdAndStatus(Long convidadoId, StatusConvite statusConvite);

}
