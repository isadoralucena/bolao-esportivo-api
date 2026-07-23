package com.ufcg.psoft.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ufcg.psoft.project.model.Estatisticas;

public interface EstatisticasRepository extends JpaRepository<Estatisticas, Long> {
    
}
