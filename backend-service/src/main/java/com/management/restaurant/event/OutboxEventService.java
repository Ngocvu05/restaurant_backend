package com.management.restaurant.event;

import com.management.restaurant.event.model.BookingEvent;
import com.management.restaurant.event.model.UserEvent;

public interface OutboxEventService {
    void saveBookingEvent(BookingEvent event);
    void saveUserEvent(UserEvent event);
    void processPendingEvents();
}
