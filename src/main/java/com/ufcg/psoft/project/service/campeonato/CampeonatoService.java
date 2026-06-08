package com.ufcg.psoft.project.service.campeonato;

import java.util.*;

import com.ufcg.psoft.project.dto.campeonato.CampeonatoPostPutRequestDTO;
import com.ufcg.psoft.project.dto.campeonato.CampeonatoResponseDTO;

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
