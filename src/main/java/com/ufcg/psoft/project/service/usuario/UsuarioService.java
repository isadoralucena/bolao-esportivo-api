package com.ufcg.psoft.project.service.usuario;

import java.util.List;

import com.ufcg.psoft.project.dto.usuario.PromocaoPremiumResponseDTO;
import com.ufcg.psoft.project.dto.usuario.UsuarioPostPutRequestDTO;
import com.ufcg.psoft.project.dto.usuario.UsuarioResponseDTO;

public interface UsuarioService {

    UsuarioResponseDTO alterar(Long id, String codigoAcesso, UsuarioPostPutRequestDTO usuarioPostPutRequestDTO);

    List<UsuarioResponseDTO> listar();

    UsuarioResponseDTO recuperar(Long id);

    UsuarioResponseDTO criar(UsuarioPostPutRequestDTO usuarioPostPutRequestDTO);

    void remover(Long id, String codigoAcesso);

    List<UsuarioResponseDTO> listarPorNome(String nome);

    PromocaoPremiumResponseDTO obterPromocao(Long usuarioId);
}
