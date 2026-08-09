package com.ufcg.psoft.project.repository;

import com.ufcg.psoft.project.model.PromocaoPremium;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromocaoPremiumRepository extends JpaRepository<PromocaoPremium, Long> {

    Optional<PromocaoPremium> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}
