package Core.Clients;

import Core.Models.exceptions.TicketException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

        testConsecutive();

        //testParalell();

        for (Event event : eventService.getAllEvents()) {
            System.out.println(
                event.getId() + " -> " + event.getTicketsSold().size()
            );
}

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

    private void testParalell() {
        long startTime = System.currentTimeMillis();
        List<Event> events = createEvents(amountEventsToBeCreated);
        long endTime = System.currentTimeMillis();
        System.out.println("Time to create " + amountEventsToBeCreated + " events: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();
        List<Customer> customers = createCustomers(amountCustomersToBeCreated);
        endTime = System.currentTimeMillis();
        System.out.println("Time to create " + amountCustomersToBeCreated + " customers: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();
        buyTicketsParallel(events, customers);
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
                10
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


    private List<Ticket> buyTicketsParallel(List<Event> events, List<Customer> customers) {
    int threads = Runtime.getRuntime().availableProcessors();
    ExecutorService executor = Executors.newFixedThreadPool(threads);

    List<Ticket> result = Collections.synchronizedList(new ArrayList<>());

    int chunkSize = customers.size() / threads;
    List<Thread> threadList = new ArrayList<>();

    for (int t = 0; t < threads; t++) {
        int start = t * chunkSize;
        int end = (t == threads - 1) ? customers.size() : (t + 1) * chunkSize;
        List<Customer> subCustomers = customers.subList(start, end);

        Thread thread = new Thread(() -> {
            for (Event event : events) {
                for (Customer customer : subCustomers) {
                    try {
                        result.add(ticketService.createTicket(customer.getId(), event.getId()));
                    } catch (TicketException e) {
                        break;
                    }
                }
            }
        });

        threadList.add(thread);
        thread.start();
    }

    for (Thread thread : threadList) {
        try {
            thread.join();  // wartet bis jeder Thread fertig ist
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    executor.shutdown();
    return result;
}

}
