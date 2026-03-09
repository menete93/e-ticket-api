package mz.co.mozbuy.e_ticket.event.core.service.pricing;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.dto.BulkStrategyAssignmentDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.PricingStrategyRequestDTO;
import mz.co.mozbuy.e_ticket.event.core.dto.PricingStrategyResponseDTO;
import mz.co.mozbuy.e_ticket.event.core.exceptions.StrategyCreationException;
import mz.co.mozbuy.e_ticket.event.core.factory.StrategyFactory;
import mz.co.mozbuy.e_ticket.event.core.repository.PricingStrategyRepository;
import mz.co.mozbuy.e_ticket.event.core.service.PricingStrategyService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingStrategyRouterService {

    private final PricingStrategyService strategyService;
    private final StrategyFactory strategyFactory;
    private final PricingStrategyRepository strategyRepository;

    /**
     * Roteia a criação da estratégia para o método específico baseado no tipo
     */
    public PricingStrategyResponseDTO createStrategyByType(PricingStrategyRequestDTO request) {
        log.info("🔄 Routing strategy creation for type: {}", request.getStrategyType());
        return strategyService.createStrategyByType(request);
    }

    @Transactional
    public List<PricingStrategyResponseDTO> createStrategiesFromAssignment(BulkStrategyAssignmentDTO request) {
        // Converte para DTOs completos
        List<PricingStrategyRequestDTO> fullRequests = strategyFactory.buildFullRequests(request);

        List<PricingStrategyResponseDTO> responses = new ArrayList<>();

        for (PricingStrategyRequestDTO fullRequest : fullRequests) {
            try {
                // Valida a estratégia
                if (!fullRequest.isValid()) {
                    throw new ValidationException("Estratégia inválida: " + fullRequest.getName());
                }

                // Cria a estratégia (reutiliza o método existente)
                PricingStrategyResponseDTO response = createStrategyByType(fullRequest);
                responses.add(response);

                log.debug("Estratégia criada: {} para categoria {}",
                        response.getName(), response.getSpecificCategory());

            } catch (Exception e) {
                log.error("Erro ao criar estratégia: {}", e.getMessage());
                throw new StrategyCreationException("Falha ao criar estratégia", e);
            }
        }

        return responses;
    }


    /**
     * Roteia a atualização da estratégia (se necessário comportamento específico)
     */
    public PricingStrategyResponseDTO updateStrategyByType(Long id, PricingStrategyRequestDTO request) {
        log.info("🔄 Routing strategy update for ID: {}, type: {}", id, request.getStrategyType());

        // Se precisar de comportamentos diferentes para update baseado no tipo,
        // implemente lógica similar ao create. Por enquanto, usa o método genérico
        return strategyService.updateStrategy(id, request);
    }
}