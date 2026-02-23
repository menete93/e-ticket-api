package mz.co.mozbuy.e_ticket.event.auth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
//
//    @ExceptionHandler(UsernameAlreadyExistsException.class)
//    public ResponseEntity<ErrorResponse> handleUsernameExists(
//            UsernameAlreadyExistsException ex,
//            WebRequest request) {
//
//        // Log apenas a mensagem importante (sem stack trace completo)
//        log.warn("Validation error: {}", ex.getMessage());
//
//        ErrorResponse error = new ErrorResponse(
//                "USERNAME_ALREADY_EXISTS",
//                ex.getMessage(), // "Username 'kiko_menete' already exists"
//                LocalDateTime.now(),
//                request.getDescription(false).replace("uri=", "")
//        );
//
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
//    }
//
//    @ExceptionHandler(EmailAlreadyExistsException.class)
//    public ResponseEntity<ErrorResponse> handleEmailExists(
//            EmailAlreadyExistsException ex,
//            WebRequest request) {
//
//        log.warn("Validation error: {}", ex.getMessage());
//
//        ErrorResponse error = new ErrorResponse(
//                "EMAIL_ALREADY_EXISTS",
//                ex.getMessage(),
//                LocalDateTime.now(),
//                request.getDescription(false).replace("uri=", "")
//        );
//
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
//    }
}