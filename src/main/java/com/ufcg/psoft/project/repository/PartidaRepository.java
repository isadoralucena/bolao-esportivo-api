package com.ufcg.psoft.project.repository;

import com.ufcg.psoft.project.model.Partida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    List<Partida> findByCampeonatoId(Long campeonatoId);

    Optional<Partida> findByCampeonatoIdAndCodigoExterno(Long campeonatoId, Long codigoExterno);
}
