package com.ufcg.psoft.project.service.usuario;

import com.ufcg.psoft.project.exception.UsuarioNaoExisteException;
import com.ufcg.psoft.project.dto.usuario.UsuarioPostPutRequestDTO;
import com.ufcg.psoft.project.dto.usuario.UsuarioResponseDTO;
import com.ufcg.psoft.project.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.project.exception.EmailJaCadastradoException;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public UsuarioResponseDTO alterar(Long id, String codigoAcesso, UsuarioPostPutRequestDTO usuarioPostPutRequestDTO) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(UsuarioNaoExisteException::new);
        if (!usuario.getCodigo().equals(codigoAcesso)) {
            throw new CodigoDeAcessoInvalidoException();
        }

        String novoEmailNormalizado = usuarioPostPutRequestDTO.getEmail().trim().toLowerCase();
        String emailAtualNormalizado = usuario.getEmail().trim().toLowerCase();
        if (!emailAtualNormalizado.equals(novoEmailNormalizado) && usuarioRepository.existsByEmail(novoEmailNormalizado))  {
            throw new EmailJaCadastradoException();
        }

        modelMapper.map(usuarioPostPutRequestDTO, usuario);
        usuario.setEmail(emailAtualNormalizado);
        usuarioRepository.save(usuario);
        return modelMapper.map(usuario, UsuarioResponseDTO.class);
    }

    @Override
    public UsuarioResponseDTO criar(UsuarioPostPutRequestDTO usuarioPostPutRequestDTO) {
        String emailNormalizado = usuarioPostPutRequestDTO.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException();
        }

        Usuario usuario = modelMapper.map(usuarioPostPutRequestDTO, Usuario.class);
        usuario.setEmail(emailNormalizado);
        usuarioRepository.save(usuario);
        return modelMapper.map(usuario, UsuarioResponseDTO.class);
    }

    @Override
    public void remover(Long id, String codigoAcesso) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(UsuarioNaoExisteException::new);
        if (!usuario.getCodigo().equals(codigoAcesso)) {
            throw new CodigoDeAcessoInvalidoException();
        }
        usuarioRepository.delete(usuario);
    }

    @Override
    public List<UsuarioResponseDTO> listarPorNome(String nome) {
        List<Usuario> usuarios = usuarioRepository.findByNomeContaining(nome);
        return usuarios.stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioResponseDTO> listar() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioResponseDTO recuperar(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(UsuarioNaoExisteException::new);
        return new UsuarioResponseDTO(usuario);
    }
}
