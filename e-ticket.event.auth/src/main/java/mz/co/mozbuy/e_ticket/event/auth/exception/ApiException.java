package mz.co.mozbuy.e_ticket.event.auth.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public static class BadRequestException extends ApiException {
        public BadRequestException(String message) {
            super(400, message);
        }
    }

    public static class ResourceNotFoundException extends ApiException {
        public ResourceNotFoundException(String message) {
            super(404, message);
        }
    }

    public static class ConflictException extends ApiException {
        public ConflictException(String message) {
            super(409, message);
        }
    }

    public static class ServiceUnavailableException extends ApiException {
        public ServiceUnavailableException(String message) {
            super(503, message);
        }
    }
}
