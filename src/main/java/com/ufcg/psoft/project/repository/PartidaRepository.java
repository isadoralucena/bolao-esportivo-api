package com.ufcg.psoft.project.repository;

import com.ufcg.psoft.project.model.Partida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import com.ufcg.psoft.project.model.PartidaStatus;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    List<Partida> findByCampeonatoId(Long campeonatoId);

    Optional<Partida> findByCampeonatoIdAndCodigoExterno(Long campeonatoId, Long codigoExterno);

    boolean existsByCampeonatoIdAndStatusIn(Long campeonatoId, List<PartidaStatus> statuses);

    boolean existsByCampeonatoId(Long campeonatoId);

    void deleteByCampeonatoId(Long campeonatoId);

    List<Partida> findByDataAfterAndStatus(LocalDateTime data, PartidaStatus status);
}
