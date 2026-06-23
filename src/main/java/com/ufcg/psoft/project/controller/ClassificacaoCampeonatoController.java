package com.ufcg.psoft.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufcg.psoft.project.dto.campeonato.ClassificacaoCampeonatoResponseDTO;
import com.ufcg.psoft.project.service.campeonato.ClassificacaoCampeonatoService;

@RestController
@RequestMapping("/campeonatos")
public class ClassificacaoCampeonatoController {

    @Autowired
    private ClassificacaoCampeonatoService classificacaoCampeonatoService;

    @GetMapping("/{campeonatoId}/classificacao")
    public List<ClassificacaoCampeonatoResponseDTO> listarClassificacao(@PathVariable Long campeonatoId) {
        return classificacaoCampeonatoService.listarPorCampeonato(campeonatoId);
    }

    @PostMapping("/{campeonatoId}/classificacao/sincronizar")
    public void sincronizarClassificacao(@PathVariable Long campeonatoId) {
        classificacaoCampeonatoService.sincronizarClassificacao(campeonatoId);
    }
}