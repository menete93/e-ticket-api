package mz.co.mozbuy.e_ticket.event.core.dto;

public record EventDto(
        Long id,
        String name,
        String description,
        String status,
        EventLocationDto location
) {}