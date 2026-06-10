package com.ufcg.psoft.project.service.campeonato;

import com.ufcg.psoft.project.dto.CampeonatoPostPutRequestDTO;
import com.ufcg.psoft.project.dto.CampeonatoResponseDTO;

import java.util.*;

public interface CampeonatoService {
	List<CampeonatoResponseDTO> sincronizar(Long userId, String codigo);

	CampeonatoResponseDTO criar(Long userId, String codigo, CampeonatoPostPutRequestDTO campeonatoPostPutRequestDTO);
	void remover(Long userId, String codigo, Long id);

	List<CampeonatoResponseDTO> listar();
	CampeonatoResponseDTO recuperar(Long id);
	List<CampeonatoResponseDTO> recuperarNome(String nome);

	CampeonatoResponseDTO ativar(Long userId, String codigo, Long id);
	CampeonatoResponseDTO desativar(Long userId, String codigo, Long id);
}
