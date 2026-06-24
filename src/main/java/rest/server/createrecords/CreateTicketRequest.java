package rest.server.createrecords;

public record CreateTicketRequest(
        long customerId,
        long eventId
) {}
