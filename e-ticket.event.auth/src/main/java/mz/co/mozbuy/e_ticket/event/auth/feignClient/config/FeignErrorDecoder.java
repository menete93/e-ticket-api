package mz.co.mozbuy.e_ticket.event.auth.feignClient.config;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.ws.rs.ServiceUnavailableException;

import mz.co.mozbuy.e_ticket.event.auth.exception.ApiException;
import org.apache.coyote.BadRequestException;

public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 400:
                return new BadRequestException("Invalid request to Ticket Service");
            case 404:
                return new ApiException.ResourceNotFoundException("Resource not found in Ticket Service");
            case 409:
                return new ApiException.ConflictException("Conflict in Ticket Service");
            case 503:
                return new ServiceUnavailableException("Ticket Service unavailable");
            default:
                return FeignException.errorStatus(methodKey, response);


        }
    }
}
