package mz.co.mozbuy.e_ticket.event.auth.feignClient.service;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.auth.dto.OrganizerUpgradeRequest;
import mz.co.mozbuy.e_ticket.event.auth.enums.RoleType;
import mz.co.mozbuy.e_ticket.event.auth.exception.*;
import mz.co.mozbuy.e_ticket.event.auth.feignClient.TicketServiceClient;
import mz.co.mozbuy.e_ticket.event.auth.feignClient.dto.OrganizerProfileRequest;
import mz.co.mozbuy.e_ticket.event.auth.feignClient.dto.OrganizerProfileResponse;
import mz.co.mozbuy.e_ticket.event.auth.model.Role;
import mz.co.mozbuy.e_ticket.event.auth.model.User;
import mz.co.mozbuy.e_ticket.event.auth.repository.RoleRepository;
import mz.co.mozbuy.e_ticket.event.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserUpgradeService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TicketServiceClient ticketServiceClient;

    @Transactional
    public User upgradeToOrganizer(OrganizerUpgradeRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

        if (Boolean.TRUE.equals(user.getIsOrganizer())) {
            throw new AlreadyOrganizerException(request.getUserId());
        }

// Chamada reativa ao ticket_service
        OrganizerProfileResponse profile = createOrganizerProfileWithRetry(user, request)
                .block(); // bloqueia aqui só para pegar o resultado



// Atualizar user
        user.setIsOrganizer(true);
        assert profile != null;
        user.setOrganizerReferenceId(profile.getOrganizerReferenceId());

        Role organizerRole = roleRepository.findByName(RoleType.ORGANIZER.getValue())
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ORGANIZER.getValue())));
        user.getRoles().add(organizerRole);

        User savedUser = userRepository.save(user);
        log.info("User {} upgraded to organizer. Reference: {}", request.getUserId(), profile.getOrganizerReferenceId());

        return savedUser;
    }
    public Mono<OrganizerProfileResponse> createOrganizerProfileWithRetry(
            User user,
            OrganizerUpgradeRequest request
    ) {
        int maxAttempts = 1;
        Duration backoff = Duration.ofSeconds(2);

        return ticketServiceClient.createOrganizerProfile(
                        OrganizerProfileRequest.builder()
                                .userId(user.getId())
                                .email(user.getEmail())
                                .name(user.getUsefFullName())
                                .companyName(request.getCompanyName())
                                .nuit(request.getNuit())
                                .build()
                )
                .doOnSubscribe(sub ->
                        log.info("Creating organizer profile for user {}...", user.getId())
                )
                .onErrorMap(WebClientResponseException.class, ex -> {
                    int status = ex.getRawStatusCode();
                    return switch (status) {
                        case 400 -> new ApiException.BadRequestException("Invalid request to Ticket Service");
                        case 404 -> new ApiException.ResourceNotFoundException("Resource not found in Ticket Service");
                        case 409 -> new ApiException.ConflictException("Conflict in Ticket Service");
                        case 503 -> new ApiException.ServiceUnavailableException("Ticket Service unavailable");
                        default -> new ApiException(status, ex.getMessage());
                    };
                })
                .retryWhen(Retry.backoff(maxAttempts - 1, backoff)
                        .doBeforeRetry(retry -> log.warn(
                                "Attempt {} failed for user {}: {}",
                                retry.totalRetries() + 1,
                                user.getId(),
                                retry.failure().getMessage()
                        ))
                );
    }


}