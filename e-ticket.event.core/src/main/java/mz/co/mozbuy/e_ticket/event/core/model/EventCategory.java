package mz.co.mozbuy.e_ticket.event.core.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event_categories")
@Getter
@Setter
public class EventCategory extends AuditableEntity<Long, String> {


    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "color_code", length = 7)
    private String colorCode;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Event> events = new ArrayList<>();

    public EventCategory() {}

    public EventCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public EventCategory(String name, String description, String colorCode, String iconUrl) {
        this.name = name;
        this.description = description;
        this.colorCode = colorCode;
        this.iconUrl = iconUrl;
    }
}