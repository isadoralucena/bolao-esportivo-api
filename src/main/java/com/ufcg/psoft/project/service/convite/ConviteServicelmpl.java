package com.ufcg.psoft.project.service.convite;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.ufcg.psoft.project.dto.convite.ConvitePostPutRequestDTO;
import com.ufcg.psoft.project.dto.convite.ConviteResponseDTO;
import com.ufcg.psoft.project.exception.ConviteDuplicadoException;
import com.ufcg.psoft.project.exception.ConviteNaoExisteException;
import com.ufcg.psoft.project.exception.ConviteJaProcessadoException;
import com.ufcg.psoft.project.exception.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.OrganizadorInvalidoException;
import com.ufcg.psoft.project.exception.PrivacidadeInvalidaException;
import com.ufcg.psoft.project.exception.UsuarioInvalidoException;
import com.ufcg.psoft.project.exception.UsuarioNaoExisteException;
import com.ufcg.psoft.project.model.Convite;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Privacidade;
import com.ufcg.psoft.project.model.StatusConvite;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.ConviteRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;

public class ConviteServicelmpl implements ConviteService {

    @Autowired
    private ConviteRepository conviteRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired 
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ConviteResponseDTO criar(ConvitePostPutRequestDTO convitePostPutRequestDTO) {
        Usuario organizador = obterUsuario(convitePostPutRequestDTO.getOrganizador());
        Usuario convidado = obterUsuario(convitePostPutRequestDTO.getConvidado());
        Grupo grupo = obterGrupo(convitePostPutRequestDTO.getGrupo());

        if (!organizador.equals(grupo.getOrganizador())) throw new OrganizadorInvalidoException();

        if (grupo.getPrivacidade() == Privacidade.PUBLICA) throw new PrivacidadeInvalidaException();

        if (grupo.getParticipantes().contains(convidado)) throw new ConviteDuplicadoException();

        Convite convite = modelMapper.map(convitePostPutRequestDTO, Convite.class);
        convite.setConvidado(convidado);
        convite.setOrganizador(organizador);
        
        this.conviteRepository.save(convite);
        notificarConvidado(convite, "criado");

        return modelMapper.map(convite, ConviteResponseDTO.class);
    }

    @Override
    public void remover(Long id, Long idOrganizador) {
        Convite convite = obterConvite(id);
        Usuario organizador = convite.getOrganizador();

        if (organizador.getId() != idOrganizador) {
            throw new OrganizadorInvalidoException();
        }

        this.conviteRepository.delete(convite);
    }

    @Override
    public ConviteResponseDTO aceitar(Long id, Long idConvidado) {
        Convite convite = obterConvite(id);

        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new ConviteJaProcessadoException();
        }

        if (convite.getConvidado().getId() != idConvidado) {
            throw new UsuarioInvalidoException();
        }

        convite.setStatus(StatusConvite.ACEITO);
        
        Usuario convidado = convite.getConvidado();
        Grupo grupo = convite.getGrupo();
        
        grupo.getParticipantes().add(convidado);
        
        this.conviteRepository.save(convite);
        this.grupoRepository.save(grupo);
        notificarConvidado(convite, "aceito");

        return modelMapper.map(convite, ConviteResponseDTO.class);
    }

    @Override
    public ConviteResponseDTO recusar(Long id, Long idConvidado) {
        Convite convite = obterConvite(id);
        
        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new ConviteJaProcessadoException();
        }

        if (convite.getConvidado().getId() != idConvidado) {
            throw new UsuarioInvalidoException();
        }

        convite.setStatus(StatusConvite.RECUSADO);
        this.conviteRepository.save(convite);
        notificarConvidado(convite, "recusado");

        return modelMapper.map(convite, ConviteResponseDTO.class);
    }

    @Override
    public ConviteResponseDTO ignorar(Long id, Long idConvidado) {
        Convite convite = obterConvite(id);
        
        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new ConviteJaProcessadoException();
        }

        if (convite.getConvidado().getId() != idConvidado) {
            throw new UsuarioInvalidoException();
        }

        convite.setStatus(StatusConvite.IGNORADO);
        this.conviteRepository.save(convite);
        notificarConvidado(convite, "ignorado");

        return modelMapper.map(convite, ConviteResponseDTO.class);
    }

    @Override
    public List<ConviteResponseDTO> listarConvitesPendentesPorConvidado(Long convidadoId) {
        obterUsuario(convidadoId);

        List<Convite> convitesPendentes = conviteRepository.findByConvidadoIdAndStatus(convidadoId, StatusConvite.PENDENTE);

        return convitesPendentes.stream()
                .map(convite -> modelMapper.map(convite, ConviteResponseDTO.class))
                .toList();
    }
    
    private Usuario obterUsuario(Long id) {
        return this.usuarioRepository.findById(id)
                .orElseThrow(UsuarioNaoExisteException::new);
    }

    private Grupo obterGrupo(Long id) {
        return this.grupoRepository.findById(id)
                .orElseThrow(GrupoNaoExisteException::new);
    }

    private Convite obterConvite(Long id) {
        return this.conviteRepository.findById(id)
                .orElseThrow(ConviteNaoExisteException::new);
    }

    private void notificarConvidado(Convite convite, String status) {
        String mensagem = String.format("Notificação: O convite para %s participar do grupo %s foi %s.", 
                convite.getConvidado().getNome(), 
                convite.getGrupo().getNome(), 
                status);
        System.out.println(mensagem);
    }
}
