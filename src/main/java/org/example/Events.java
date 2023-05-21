package org.example;

public class Events {
    int eventId;
    String eventType;
    String eventName;
    int eventPrice;
    int eventNumberOfTickets;

    public Events(int eventId, String eventType, String eventName, int eventPrice, int eventNumberOfTickets) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.eventName = eventName;
        this.eventPrice = eventPrice;
        this.eventNumberOfTickets = eventNumberOfTickets;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getEventPrice() {
        return eventPrice;
    }

    public void setEventPrice(int eventPrice) {
        this.eventPrice = eventPrice;
    }

    public int getEventNumberOfTickets() {
        return eventNumberOfTickets;
    }

    public void setEventNumberOfTickets(int eventNumberOfTickets) {
        this.eventNumberOfTickets = eventNumberOfTickets;
    }

    @Override
    public String toString() {
        return eventId +
                " " + eventType +
                " " + eventName +
                " " + eventPrice +
                " " + eventNumberOfTickets;
    }
}