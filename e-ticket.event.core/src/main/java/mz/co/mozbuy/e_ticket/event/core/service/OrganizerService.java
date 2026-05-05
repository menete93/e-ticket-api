package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.dto.OrganizerRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.OrganizerResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.OrganizerStatsDTO;
import mz.co.mozbuy.e_ticket.event.core.model.Organizer;
import mz.co.mozbuy.e_ticket.event.core.repository.OrganizerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizerService {

    private final OrganizerRepository organizerRepository;

    @Transactional
    public OrganizerResponseDTO createOrganizer(OrganizerRequestDTO requestDTO) {
        // Verificar se email já existe
        if (organizerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new RuntimeException("Organizer with email " + requestDTO.getEmail() + " already exists");
        }

        // Verificar se NUIT/CNPJ já existe
        if (requestDTO.getNuit() != null && organizerRepository.existsByNuit(requestDTO.getNuit())) {
            throw new RuntimeException("Organizer with tax ID " + requestDTO.getNuit() + " already exists");
        }

        Organizer organizer = new Organizer();
        organizer.setUserId(requestDTO.getUserId());
        organizer.setName(requestDTO.getName());
        organizer.setEmail(requestDTO.getEmail());
        organizer.setPhoneNumber(requestDTO.getPhoneNumber());
        organizer.setCompanyName(requestDTO.getCompanyName());
        organizer.setNuit(requestDTO.getNuit());

        // Configuração de pricing personalizada
        if (requestDTO.getCommissionRate() != null) {
            organizer.setCommissionRate(requestDTO.getCommissionRate());
        }
        if (requestDTO.getFlatFeePerTicket() != null) {
            organizer.setFlatFeePerTicket(requestDTO.getFlatFeePerTicket());
        }
        if (requestDTO.getTrialEventsRemaining() != null) {
            organizer.setTrialEventsRemaining(requestDTO.getTrialEventsRemaining());
        }

        Organizer savedOrganizer = organizerRepository.save(organizer);
        log.info("Created organizer: {} ({})", organizer.getName(), organizer.getEmail());

        return toResponseDTO(savedOrganizer);
    }

    public OrganizerResponseDTO getOrganizerById(Long id) {
        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organizer not found with id: " + id));
        return toResponseDTO(organizer);
    }

    public OrganizerResponseDTO getOrganizerByEmail(String email) {
        Organizer organizer = organizerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Organizer not found with email: " + email));
        return toResponseDTO(organizer);
    }

    @Transactional
    public OrganizerResponseDTO updateOrganizer(Long id, OrganizerRequestDTO requestDTO) {
        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organizer not found with id: " + id));

        // Atualizar campos básicos
        if (requestDTO.getName() != null) {
            organizer.setName(requestDTO.getName());
        }
        if (requestDTO.getPhoneNumber() != null) {
            organizer.setPhoneNumber(requestDTO.getPhoneNumber());
        }
        if (requestDTO.getCompanyName() != null) {
            organizer.setCompanyName(requestDTO.getCompanyName());
        }

        // Atualizar configuração de pricing
        if (requestDTO.getCommissionRate() != null) {
            organizer.setCommissionRate(requestDTO.getCommissionRate());
        }
        if (requestDTO.getFlatFeePerTicket() != null) {
            organizer.setFlatFeePerTicket(requestDTO.getFlatFeePerTicket());
        }
        if (requestDTO.getTrialEventsRemaining() != null) {
            organizer.setTrialEventsRemaining(requestDTO.getTrialEventsRemaining());
        }

        Organizer updatedOrganizer = organizerRepository.save(organizer);
        log.info("Updated organizer: {} ({})", organizer.getName(), organizer.getEmail());

        return toResponseDTO(updatedOrganizer);
    }

    public OrganizerStatsDTO getOrganizerStats(Long id) {
        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organizer not found with id: " + id));

        OrganizerStatsDTO stats = new OrganizerStatsDTO();
        stats.setOrganizerId(organizer.getId());
        stats.setOrganizerName(organizer.getName());
        stats.setTotalEventsCreated(organizer.getTotalEventsCreated() != null ? organizer.getTotalEventsCreated() : 0);
        stats.setTotalTicketsSold(organizer.getTotalTicketsSold() != null ? organizer.getTotalTicketsSold() : 0);
        stats.setTotalEarnings(organizer.getTotalEarnings() != null ? organizer.getTotalEarnings() : BigDecimal.ZERO);
        stats.setTotalCommissionPaid(organizer.getTotalCommissionPaid() != null ? organizer.getTotalCommissionPaid() : BigDecimal.ZERO);
        stats.setAccountBalance(organizer.getAccountBalance() != null ? organizer.getAccountBalance() : BigDecimal.ZERO);
        stats.setTrialEventsRemaining(organizer.getTrialEventsRemaining() != null ? organizer.getTrialEventsRemaining() : 0);
        stats.setTrialEventsUsed(organizer.getTrialUsedCount() != null ? organizer.getTrialUsedCount() : 0);

        // Calcular net earnings
        BigDecimal netEarnings = stats.getTotalEarnings().subtract(stats.getTotalCommissionPaid());
        stats.setNetEarnings(netEarnings);

        return stats;
    }

    public List<OrganizerResponseDTO> getActiveOrganizers() {
        List<Organizer> organizers = organizerRepository.findByState(LifeCycleState.ACTIVE);
        return organizers.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrganizerResponseDTO> getTopEarners(BigDecimal minEarnings) {
        List<Organizer> organizers = organizerRepository.findTopEarners(minEarnings);
        return organizers.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivateOrganizer(Long id) {
        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organizer not found with id: " + id));

        organizer.setState(LifeCycleState.INACTIVE);
        organizerRepository.save(organizer);
        log.info("Deactivated organizer: {} ({})", organizer.getName(), organizer.getEmail());
    }

    @Transactional
    public void activateOrganizer(Long id) {
        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organizer not found with id: " + id));

        organizer.setState(LifeCycleState.ACTIVE);
        organizerRepository.save(organizer);
        log.info("Activated organizer: {} ({})", organizer.getName(), organizer.getEmail());
    }

    private OrganizerResponseDTO toResponseDTO(Organizer organizer) {
        OrganizerResponseDTO dto = new OrganizerResponseDTO();
        dto.setId(organizer.getId());
        dto.setName(organizer.getName());
        dto.setEmail(organizer.getEmail());
        dto.setPhoneNumber(organizer.getPhoneNumber());
        dto.setCompanyName(organizer.getCompanyName());
        dto.setNuit(organizer.getNuit());
        dto.setCommissionRate(organizer.getCommissionRate());
        dto.setFlatFeePerTicket(organizer.getFlatFeePerTicket());
        dto.setTrialEventsRemaining(organizer.getTrialEventsRemaining());
        dto.setTrialUsedCount(organizer.getTrialUsedCount() != null ? organizer.getTrialUsedCount() : 0);
        dto.setLifeCycleState(organizer.getState());
        dto.setAccountBalance(organizer.getAccountBalance());
        dto.setTotalEarnings(organizer.getTotalEarnings());
        dto.setTotalCommissionPaid(organizer.getTotalCommissionPaid());
        dto.setTotalTicketsSold(organizer.getTotalTicketsSold());
        dto.setTotalEventsCreated(organizer.getTotalEventsCreated());
        dto.setCreatedAt(organizer.getCreatedAt());
        dto.setUpdatedAt(organizer.getUpdatedAt());
        dto.setOrganizerReferenceId(organizer.getReferenceId());
        return dto;
    }
}