package com.ufcg.psoft.project.repository;

import java.util.List;

import com.ufcg.psoft.project.model.ClassificacaoCampeonato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassificacaoCampeonatoRepository extends JpaRepository<ClassificacaoCampeonato, Long> {
    List<ClassificacaoCampeonato> findByCampeonatoIdOrderByPosicaoAsc(Long campeonatoId);
    void deleteByCampeonatoId(Long campeonatoId);
}