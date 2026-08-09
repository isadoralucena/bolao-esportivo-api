package com.ufcg.psoft.project.repository;

import com.ufcg.psoft.project.model.Campeonato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampeonatoRepository extends JpaRepository<Campeonato, Long> {
	List<Campeonato> findByNomeContainingIgnoreCase(String nome);
	Optional<Campeonato> findByCodigoIgnoreCase(String codigo);
	Optional<Campeonato> findByCodigo(String codigo);
	List<Campeonato> findByNomeContaining(String nome);
    List<Campeonato> findByAtivoTrue();
}
