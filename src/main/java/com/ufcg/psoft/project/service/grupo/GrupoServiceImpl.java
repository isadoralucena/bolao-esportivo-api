package com.ufcg.psoft.project.service.grupo;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufcg.psoft.project.dto.grupo.GrupoPostRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoPutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.dto.grupo.ParticipantePostRequestDTO;
import com.ufcg.psoft.project.dto.usuario.UsuarioResponseDTO;
import com.ufcg.psoft.project.exception.CampeonatoNaoExisteException;
import com.ufcg.psoft.project.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.project.exception.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.LimiteDeParticipantesAtingidoException;
import com.ufcg.psoft.project.exception.PermissaoNegadaException;
import com.ufcg.psoft.project.exception.UsuarioNaoExisteException;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.PrivacidadeGrupo;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import com.ufcg.psoft.project.exception.UsuarioJaParticipanteException;
import com.ufcg.psoft.project.exception.CampeonatoInativoException;

@Service
public class GrupoServiceImpl implements GrupoService {
    @Autowired
    GrupoRepository grupoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CampeonatoRepository campeonatoRepository;
    @Autowired
    ModelMapper modelMapper;

    public GrupoResponseDTO criar(Long usuarioId, String codigoAcesso, GrupoPostRequestDTO grupoPostRequestDto) {
        Usuario usuarioLogado = obterUsuarioValido(usuarioId, codigoAcesso);

        Campeonato campeonato = campeonatoRepository.findById(grupoPostRequestDto.getCampeonatoId())
            .orElseThrow(CampeonatoNaoExisteException::new);
        
        Grupo grupo = modelMapper.map(grupoPostRequestDto, Grupo.class);
        grupo.setCampeonato(campeonato);
        grupo.setOrganizador(usuarioLogado);

        grupoRepository.save(grupo);
        return modelMapper.map(grupo, GrupoResponseDTO.class);
    }

    public GrupoResponseDTO recuperar(Long usuarioId, String codigoAcesso, Long id) {
        Usuario usuarioLogado = obterUsuarioValido(usuarioId, codigoAcesso);
        Grupo grupo = grupoRepository.findById(id).orElseThrow(GrupoNaoExisteException::new);

        if (grupo.getPrivacidade() == PrivacidadeGrupo.PRIVADA) {
            boolean isMembroOuDono = grupo.getParticipantes().contains(usuarioLogado) || grupo.getOrganizador().equals(usuarioLogado);
            if (!isMembroOuDono) {
                throw new PermissaoNegadaException();
            }
        }

        return modelMapper.map(grupo, GrupoResponseDTO.class);
    }

    public List<GrupoResponseDTO> listar(Long usuarioId, String codigoAcesso) {
        Usuario usuarioLogado = obterUsuarioValido(usuarioId, codigoAcesso);
        List<Grupo> grupos = grupoRepository.findAll();
        
        return grupos.stream()
                .filter(g -> g.getPrivacidade() == PrivacidadeGrupo.PUBLICA || g.getParticipantes().contains(usuarioLogado))
                .map(grupo -> modelMapper.map(grupo, GrupoResponseDTO.class))
                .collect(Collectors.toList());
    }

    public GrupoResponseDTO alterar(Long usuarioId, String codigoAcesso, Long id, GrupoPutRequestDTO grupoPutRequestDto) {
        Grupo grupo = grupoRepository.findById(id).orElseThrow(GrupoNaoExisteException::new);

        Usuario usuarioLogado = obterUsuarioValido(usuarioId, codigoAcesso);
        if (!grupo.getOrganizador().equals(usuarioLogado)) {
            throw new PermissaoNegadaException();
        }
        
        modelMapper.map(grupoPutRequestDto, grupo);
        grupo = grupoRepository.save(grupo);
        return modelMapper.map(grupo, GrupoResponseDTO.class);
    }

    public void remover(Long usuarioId, String codigoAcesso, Long id) {
        Grupo grupo = grupoRepository.findById(id).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuarioLogado = obterUsuarioValido(usuarioId, codigoAcesso);
        if (!grupo.getOrganizador().equals(usuarioLogado)) {
            throw new PermissaoNegadaException();
        }

        grupoRepository.delete(grupo);
    }

    public GrupoResponseDTO adicionarParticipante(Long usuarioId, String codigoAcesso, Long grupoId, ParticipantePostRequestDTO participantePostRequestDto) {
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuarioLogado = obterUsuarioValido(usuarioId, codigoAcesso);

        if (grupo.getLimiteParticipantes() != null && grupo.getParticipantes().size() >= grupo.getLimiteParticipantes()) {
            throw new LimiteDeParticipantesAtingidoException(); 
        }

        Usuario participanteParaAdicionar = usuarioRepository.findById(participantePostRequestDto.getUsuarioId())
                .orElseThrow(UsuarioNaoExisteException::new);

        if (!grupo.getOrganizador().equals(usuarioLogado)) {
            throw new PermissaoNegadaException();
        }

        grupo.getParticipantes().add(participanteParaAdicionar);
        grupoRepository.save(grupo);
        return modelMapper.map(grupo, GrupoResponseDTO.class);
    }

    public void removerParticipante(Long usuarioId, String codigoAcesso, Long grupoId, Long participanteId) {
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuarioLogado = obterUsuarioValido(usuarioId, codigoAcesso);

        Usuario participante = usuarioRepository.findById(participanteId)
                .orElseThrow(UsuarioNaoExisteException::new);

        if (!grupo.getOrganizador().equals(usuarioLogado)) {
            throw new PermissaoNegadaException();
        }

        grupo.getParticipantes().remove(participante);
        grupoRepository.save(grupo);
    }

    public Set<UsuarioResponseDTO> listarParticipantes(Long usuarioId, String codigoAcesso, Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuarioLogado = obterUsuarioValido(usuarioId, codigoAcesso);

        if (grupo.getPrivacidade() == PrivacidadeGrupo.PRIVADA) {
            boolean isMembroOuDono = grupo.getParticipantes().contains(usuarioLogado) || grupo.getOrganizador().equals(usuarioLogado);
            if (!isMembroOuDono) {
                throw new PermissaoNegadaException();
            }
        }

        return grupo.getParticipantes().stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toSet());
    }

    public GrupoResponseDTO entrarEmGrupoPublico(Long grupoId, Long usuarioId, String codigoAcesso) {
            Grupo grupo = grupoRepository.findById(grupoId)
                    .orElseThrow(GrupoNaoExisteException::new);
            
            Usuario usuarioLogado = obterUsuarioValido(usuarioId, codigoAcesso);
            
            validarEntradaEmGrupoPublico(grupo, usuarioLogado);
            
            grupo.getParticipantes().add(usuarioLogado);
            grupoRepository.save(grupo);
            return modelMapper.map(grupo, GrupoResponseDTO.class);
        }

    private void validarEntradaEmGrupoPublico(Grupo grupo, Usuario usuario) {
        if (grupo.getPrivacidade() == PrivacidadeGrupo.PRIVADA) {
            throw new PermissaoNegadaException();
        }
        if (!grupo.getCampeonato().getAtivo()) {
            throw new CampeonatoInativoException();
        }
        if (grupo.getParticipantes().contains(usuario)) {
            throw new UsuarioJaParticipanteException();
        }
        if (grupo.getLimiteParticipantes() != null &&
            grupo.getParticipantes().size() >= grupo.getLimiteParticipantes()) {
            throw new LimiteDeParticipantesAtingidoException();
        }
    }

    private Usuario obterUsuarioValido(Long usuarioId, String codigo) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(UsuarioNaoExisteException::new);
        if (!usuario.getCodigo().equals(codigo)) {
            throw new CodigoDeAcessoInvalidoException();
        }
        return usuario;
    }
}

