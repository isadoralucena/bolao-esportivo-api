package com.ufcg.psoft.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ufcg.psoft.project.model.Estatisticas;

public interface EstatisticasRepository extends JpaRepository<Estatisticas, Long> {
    Estatisticas findFirstByUsuarioIdOrderByDataRegistroDesc(Long usuarioId);
    List<Estatisticas> findByUsuarioId(Long usuarioId);
}
