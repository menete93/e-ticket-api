package mz.co.mozbuy.e_ticket.event.core.enums;

public enum TicketCategory {
    GENERAL_ADMISSION("General Admission", "Standard access to the event"),
    VIP("VIP", "VIP access with exclusive benefits"),
    V_VIP("VVIP", "Very VIP access with premium benefits"),
    EARLY_BIRD("Early Bird", "Early booking discount tickets"),
    STUDENT("Student", "Discounted tickets for students"),
    GROUP("Group", "Discounted tickets for groups"),
    CORPORATE("Corporate", "Corporate packages"),
    INVITATION("Invitation", "Complimentary tickets"),
    BACKSTAGE("Backstage", "Backstage access passes"),
    MEET_GREET("Meet & Greet", "Meet and greet with performers"),
    TABLE_BOOKING("Table Booking", "Reserved table booking"),
    PREMIUM("Premium", "Premium experience packages");

    private final String displayName;
    private final String description;

    TicketCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}