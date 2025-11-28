package com.management.restaurant.event;

import com.management.restaurant.event.model.UserEvent;
import com.management.restaurant.event.model.BookingEvent;

public interface OutboxEventService {
    void saveBookingEvent(BookingEvent event);
    void saveUserEvent(UserEvent event);
    void processPendingEvents();
}
