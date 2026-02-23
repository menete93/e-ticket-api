package mz.co.mozbuy.e_ticket.event.auth.feignClient.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class TicketServiceWebClientConfig {

    @Value("${ticket-service.url}")
    private String ticketServiceUrl;

    @Bean
    public WebClient ticketServiceWebClient(AuthTokenService authTokenService) {
        return WebClient.builder()
                .baseUrl(ticketServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter((request, next) -> {
                    log.debug("Making request to: {} {}", request.method(), request.url());

                    return authTokenService.getToken()
                            .flatMap(token -> {
                                ClientRequest newRequest = ClientRequest.from(request)
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                        .build();

                                log.debug("Added Authorization header with token");
                                return next.exchange(newRequest);
                            });
                })
                .build();
    }
}