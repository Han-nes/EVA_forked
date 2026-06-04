package Core.Services;

import Core.Models.exceptions.CustomerException;
import Core.Models.exceptions.EventException;
import Core.Models.exceptions.TicketException;
import Core.Interfaces.TicketServiceInterface;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDate;
import java.util.*;
import Core.Models.Ticket;
import Core.Models.Event;

public class TicketService implements TicketServiceInterface {

    private final Map<UUID, Ticket> ticketsById = new ConcurrentHashMap<>();
    private CustomerService customerService;
    private EventService eventService;
    private final ConcurrentHashMap<UUID, Object> eventLocks = new ConcurrentHashMap<>();
    public void setCustomerService(CustomerService customerService){
        this.customerService = customerService;
    }

    public void setEventService(EventService eventService){
        this.eventService = eventService;
    }
    /* 
    @Override
    public Ticket createTicket(UUID customerId, UUID eventId) throws TicketException, EventException, CustomerException {
        Ticket newTicket = new Ticket(
                UUID.randomUUID(),
                LocalDate.now(),
                customerId,
                eventId
        );
        validateTicket(newTicket);
        saveTicket(newTicket);
        return newTicket;
    }
    */
    @Override
    public Ticket createTicket(UUID customerId, UUID eventId) throws TicketException, EventException, CustomerException {
        Object lock = eventLocks.computeIfAbsent(eventId, k -> new Object());
        
        synchronized (lock) {
            Ticket newTicket = new Ticket(
                UUID.randomUUID(),
                LocalDate.now(),
                customerId,
                eventId
            );
            validateTicket(newTicket);
            saveTicket(newTicket);
            return newTicket;
        }
    }


    private void saveTicket(Ticket ticket){
        ticketsById.put(ticket.getId(), new Ticket(ticket));
        eventService.ticketSoldForEvent(ticket);
        customerService.addTicketToCustomer(ticket);
    }

    @Override
    public Ticket getTicketById(UUID id) throws TicketException {
        if(!ticketsById.containsKey(id)){
            throw TicketException.ticketDoesNotExist();
        }
        return new Ticket(ticketsById.get(id));
    }

    @Override
    public List<Ticket> getAllTickets() {
        return new ArrayList<>(ticketsById.values());
    }

    @Override
    public void deleteTicket(UUID ticketId) throws IllegalArgumentException {
        if(ticketId == null){
            throw new IllegalArgumentException("Ticket ID cannot be null");
        }
        ticketsById.remove(ticketId);
    }

    @Override
    public void deleteAllTickets() {
        ticketsById.clear();
    }

    private void validateTicket(Ticket ticket) throws TicketException, CustomerException, EventException {
        if(ticket.getCustomerId() == null) {
            throw CustomerException.customerDoesNotExist();
        }
        else if(ticket.getEventId() == null){
            throw EventException.eventDoesNotExist();
        }
        else if(!eventService.getEventById(ticket.getEventId()).hasAvailableTickets()){
            throw TicketException.noTicketsAvailable();
        }

        //check for max number of tickets per event
        int count = 0;
        for(UUID idTicketsCustomer : customerService.getCustomerById(ticket.getCustomerId()).getTicketsBought()){
            Ticket ticketsCustomer = ticketsById.get(idTicketsCustomer);
            if(ticketsCustomer.getEventId().equals(ticket.getEventId())){
                count+=1;
            }
        }
        if(count >= 5){
            throw TicketException.maximumNumberOfTickets();
        }
    }

    @Override
    public boolean verifyTicket(UUID id) {
        return ticketsById.containsKey(id);
    }

}
