package com.example.JourneyHub.event;

import com.example.JourneyHub.model.entity.Ticket;
import org.springframework.context.ApplicationEvent;

public class TicketChangeEvent extends ApplicationEvent {

    private final Ticket ticket;

    public TicketChangeEvent(Object source, Ticket ticket) {
        super(source);
        this.ticket = ticket;
    }

    public Ticket getTicket() {
        return ticket;
    }
}