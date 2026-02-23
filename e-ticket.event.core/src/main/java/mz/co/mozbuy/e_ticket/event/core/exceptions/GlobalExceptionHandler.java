package mz.co.mozbuy.e_ticket.event.core.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryNotFound(CategoryNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 404);
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(404).body(response);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", 500);
//        response.put("error", "Internal Server Error");
//        response.put("message", "Ocorreu um erro inesperado. Contacte o administrador.");
//        response.put("timestamp", System.currentTimeMillis());
//
//        return ResponseEntity.status(500).body(response);
//    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEventNotFound(EventNotFoundException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", 404);
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(EventCategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEventCategoryNotFound(EventNotFoundException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", 404);
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(404).body(response);
    }

}
