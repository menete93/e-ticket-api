package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCategoryDTO {
    private Long id;
    private String name;
    private String description;
    private String colorCode;
    private String iconUrl;
    private Integer eventsCount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Construtor simplificado para casos básicos
    public EventCategoryDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Construtor para casos com cores e ícones
    public EventCategoryDTO(Long id, String name, String description, String colorCode, String iconUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.colorCode = colorCode;
        this.iconUrl = iconUrl;
    }
}