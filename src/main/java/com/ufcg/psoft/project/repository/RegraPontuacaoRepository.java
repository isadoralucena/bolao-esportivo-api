package com.ufcg.psoft.project.repository;

import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.RegraPontuacao;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegraPontuacaoRepository extends JpaRepository<RegraPontuacao, Long> {
    boolean existsByGrupoAndTipoRegraPontuacao(Grupo grupo, TipoRegraPontuacao tipo);
    List<RegraPontuacao> findByGrupoId(Long grupoId);
}
