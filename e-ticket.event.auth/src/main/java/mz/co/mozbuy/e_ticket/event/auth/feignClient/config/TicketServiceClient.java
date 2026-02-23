//package mz.co.mozbuy.e_ticket.event.auth.feignClient.config;
//
//import mz.co.mozbuy.e_ticket.event.auth.exception.ApiException;
//import mz.co.mozbuy.e_ticket.event.auth.feignClient.dto.OrganizerProfileRequest;
//import mz.co.mozbuy.e_ticket.event.auth.feignClient.dto.OrganizerProfileResponse;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//import reactor.util.retry.Retry;
//
//import java.time.Duration;
//
//@Service
//public class TicketServiceClient {
//
//    private final WebClient webClient;
//
//    public TicketServiceClient(WebClient ticketServiceWebClient) {
//        this.webClient = ticketServiceWebClient;
//    }
//
//    public Mono<OrganizerProfileResponse> createOrganizerProfile(OrganizerProfileRequest request) {
//        return webClient.post()
//                .uri("/e-ticket/api/organizers")
//                .bodyValue(request)
//                .retrieve()
//                .onStatus(status -> status.value() == 400,
//                        res -> Mono.error(new ApiException.BadRequestException("Invalid request to Ticket Service")))
//                .onStatus(status -> status.value() == 404,
//                        res -> Mono.error(new ApiException.ResourceNotFoundException("Resource not found")))
//                .onStatus(status -> status.value() == 409,
//                        res -> Mono.error(new ApiException.ConflictException("Conflict in Ticket Service")))
//                .onStatus(status -> status.value() == 503,
//                        res -> Mono.error(new ApiException.ServiceUnavailableException("Ticket Service unavailable")))
//                .bodyToMono(OrganizerProfileResponse.class)
//                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)));
//    }
//}
