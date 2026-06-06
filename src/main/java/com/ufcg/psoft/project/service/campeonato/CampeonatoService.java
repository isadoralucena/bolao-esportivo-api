package com.ufcg.psoft.project.service.campeonato;

import com.ufcg.psoft.project.dto.CampeonatoPostPutRequestDTO;
import com.ufcg.psoft.project.dto.CampeonatoResponseDTO;

import java.util.*;

public interface CampeonatoService {
	List<CampeonatoResponseDTO> sincronizar(String codigo);

	CampeonatoResponseDTO criar(String codigo, CampeonatoPostPutRequestDTO campeonatoPostPutRequestDTO);
	void remover(String codigo, Long id);

	List<CampeonatoResponseDTO> listar();
	CampeonatoResponseDTO recuperar(Long id);
	List<CampeonatoResponseDTO> recuperarNome(String nome);

	CampeonatoResponseDTO ativar(String codigo, Long id);
	CampeonatoResponseDTO desativar(String codigo, Long id);
}
