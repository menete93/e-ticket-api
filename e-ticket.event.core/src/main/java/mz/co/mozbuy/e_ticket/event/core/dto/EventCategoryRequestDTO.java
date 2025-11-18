package mz.co.mozbuy.e_ticket.event.core.dto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCategoryRequestDTO {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 7, message = "Color code must be in HEX format (e.g., #FF0000)")
    private String colorCode;

    @Size(max = 500, message = "Icon URL must not exceed 500 characters")
    private String iconUrl;
}