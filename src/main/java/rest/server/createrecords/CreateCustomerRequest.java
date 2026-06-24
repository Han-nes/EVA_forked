package rest.server.createrecords;

import java.time.LocalDate;

public record CreateCustomerRequest(
        String username,
        String email,
        LocalDate dateOfBirth
) {}
