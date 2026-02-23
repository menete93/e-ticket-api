

package mz.co.mozbuy.e_ticket.event.auth.feignClient;

import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.auth.feignClient.dto.OrganizerProfileRequest;
import mz.co.mozbuy.e_ticket.event.auth.feignClient.dto.OrganizerProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class TicketServiceClient {

    private final WebClient webClient;

    public TicketServiceClient(WebClient ticketServiceWebClient) {
        this.webClient = ticketServiceWebClient;
    }

    public Mono<OrganizerProfileResponse> createOrganizerProfile(OrganizerProfileRequest request) {
        log.info("Creating organizer profile for user: {}", request.getUserId());

        return webClient.post()
                .uri("/e-ticket/api/organizers")
                .bodyValue(request)
                .exchangeToMono(response -> {
                    // LOG DA RESPOSTA
                    log.info("Response Status: {}", response.statusCode());
                    log.info("Response Headers: {}", response.headers().asHttpHeaders());

                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Error Response Body: {}", errorBody);
                                    return Mono.error(new RuntimeException(
                                            "Ticket Service error: " + response.statusCode() +
                                                    " - Body: " + errorBody));
                                });
                    }

                    return response.bodyToMono(OrganizerProfileResponse.class);
                })
                .doOnSubscribe(subscription ->
                        log.debug("Making request to /e-ticket/api/organizers"))
                .doOnError(error ->
                        log.error("Request failed: {}", error.getMessage(), error));
    }
}