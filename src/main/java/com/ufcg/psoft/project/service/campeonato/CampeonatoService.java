package com.ufcg.psoft.project.service.campeonato;

import java.util.*;

import com.ufcg.psoft.project.dto.campeonato.CampeonatoPostPutRequestDTO;
import com.ufcg.psoft.project.dto.campeonato.CampeonatoResponseDTO;

public interface CampeonatoService {
    void sincronizarCampeonato(Long campeonatoId, Long usuarioId, String codigo);
    void sincronizarCampeonatoAutomaticamente(Long campeonatoId);

	CampeonatoResponseDTO criar(Long userId, String codigo, CampeonatoPostPutRequestDTO campeonatoPostPutRequestDTO);
	void remover(Long userId, String codigo, Long id);

	List<CampeonatoResponseDTO> listar();
	CampeonatoResponseDTO recuperar(Long id);
	List<CampeonatoResponseDTO> recuperarNome(String nome);

	CampeonatoResponseDTO ativar(Long userId, String codigo, Long id);
	CampeonatoResponseDTO desativar(Long userId, String codigo, Long id);
}
