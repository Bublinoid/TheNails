package ru.bublinoid.thenails.service;

import org.springframework.stereotype.Service;
import ru.bublinoid.thenails.content.BookingInfoProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class BookingService {

    private final BookingInfoProvider bookingInfoProvider;
    private final Map<Long, String> userEmails = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    public BookingService(BookingInfoProvider bookingInfoProvider) {
        this.bookingInfoProvider = bookingInfoProvider;
    }

    public String getRequestEmailMessage() {
        return bookingInfoProvider.getRequestEmailMessage();
    }

    public void handleEmailInput(long chatId, String email) {
        userEmails.put(chatId, email);
        logger.info("Received email: {} from chatId: {}", email, chatId);
    }
}
