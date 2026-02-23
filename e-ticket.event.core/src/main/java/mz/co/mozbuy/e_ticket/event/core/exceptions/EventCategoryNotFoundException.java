package mz.co.mozbuy.e_ticket.event.core.exceptions;

public class EventCategoryNotFoundException extends RuntimeException {
    public EventCategoryNotFoundException(Long id) {
        super("Category not found with id: " + id);
    }
}
