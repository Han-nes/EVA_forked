package Core.Clients;

import Core.Models.exceptions.TicketException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import Core.Models.Customer;
import Core.Models.Event;
import Core.Models.Ticket;
import Core.Services.CustomerService;
import Core.Services.EventService;
import Core.Services.TicketService;


public class PerformanceClient {

    private final EventService eventService;
    private final CustomerService customerService;
    private final TicketService ticketService;

    private final int amountEventsToBeCreated = 100;
    private final int amountCustomersToBeCreated = 100;


    public PerformanceClient() {
        this.ticketService = new TicketService();
        this.customerService = new CustomerService(ticketService);
        this.eventService = new EventService(ticketService);
        ticketService.setCustomerService(customerService);
        ticketService.setEventService(eventService);
    }

    public void run() {
        System.out.println("Test consecutive");
        testConsecutive();

    }

    private List<UUID> getIdsFromEvents(List<Event> events){
        List<UUID> idsFromEvents = new ArrayList<>();
        for(Event event : events){
            idsFromEvents.add(event.getId());
        }
        return idsFromEvents;
    }

    private List<UUID> getIdsFromCustomer(List<Customer> customers){
        List<UUID> idsFromCustomers = new ArrayList<>();
        for(Customer customer : customers){
            idsFromCustomers.add(customer.getId());
        }
        return idsFromCustomers;
    }

    private List<UUID> getIdsFromTickets(List<Ticket> tickets){
        List<UUID> idsFromTickets = new ArrayList<>();
        for(Ticket ticket : tickets){
            idsFromTickets.add(ticket.getId());
        }
        return idsFromTickets;
    }


    private void testConsecutive() {
        long startTime = System.currentTimeMillis();
        List<Event> events = createEvents(amountEventsToBeCreated);
        long endTime = System.currentTimeMillis();
        System.out.println("Time to create " + amountEventsToBeCreated + " events: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();
        List<Customer> customers = createCustomers(amountCustomersToBeCreated);
        endTime = System.currentTimeMillis();
        System.out.println("Time to create " + amountCustomersToBeCreated + " customers: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();
        buyTickets(events, customers);
        endTime = System.currentTimeMillis();
        System.out.println(
            "Time to buy tickets: " + (endTime - startTime) + "ms"
        );
    }

    private List<Event> createEvents(int amount) {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Event new_event = eventService.createEvent(
                "Event" + i,
                "Location" + i,
                LocalDateTime.now().plusDays(7 + i),
                1000
            );
            events.add(new_event);
        }

        return events;
    }

    private List<Customer> createCustomers(int amount) {
        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Customer newCustomer = customerService.createCustomer(
                "User" + i,
                "user" + i + "@test.org",
                LocalDate.now().minusYears(18 + i)
            );
            customers.add(newCustomer);
        }
        return customers;
    }

    private List<Ticket> buyTickets(
        List<Event> events,
        List<Customer> customers
    ) {
        List<Ticket> tickets = new ArrayList<>();
        for (Event event : events) {
            for (Customer customer : customers) {
                try {
                    tickets.add(ticketService.createTicket(customer.getId(), event.getId()));
                } catch (TicketException e) {
                    break;
                }
            }
        }
        return tickets;
    }

}
