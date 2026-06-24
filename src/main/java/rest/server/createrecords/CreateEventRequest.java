package rest.server.createrecords;

import java.time.LocalDateTime;

public record CreateEventRequest(
        String name,
        String location,
        LocalDateTime time,
        int ticketsAvailable
) {}
