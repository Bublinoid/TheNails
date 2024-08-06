package ru.bublinoid.thenails.service;

import org.springframework.stereotype.Service;
import ru.bublinoid.thenails.content.BookingInfoProvider;
import ru.bublinoid.thenails.model.Email;
import ru.bublinoid.thenails.repository.EmailRepository;
import ru.bublinoid.thenails.telegram.TelegramBot;
import ru.bublinoid.thenails.utils.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Service
public class BookingService {

    private final BookingInfoProvider bookingInfoProvider;
    private final EmailRepository emailRepository;
    private final TelegramBot telegramBot;
    private final Map<Long, String> userEmails = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    public BookingService(BookingInfoProvider bookingInfoProvider, EmailRepository emailRepository, @Lazy TelegramBot telegramBot) {
        this.bookingInfoProvider = bookingInfoProvider;
        this.emailRepository = emailRepository;
        this.telegramBot = telegramBot;
    }

    public String getRequestEmailMessage() {
        return bookingInfoProvider.getRequestEmailMessage();
    }

    public void handleEmailInput(long chatId, String email) {
        if (EmailValidator.isValid(email)) {
            userEmails.put(chatId, email);
            logger.info("Received valid email: {} from chatId: {}", email, chatId);

            // Создание и сохранение email в базу данных
            Email emailEntity = new Email();
            emailEntity.setChatId(chatId);
            emailEntity.setEmail(email);
            emailRepository.save(emailEntity);

            logger.info("Email saved to database: {} for chatId: {}", email, chatId);
            telegramBot.sendEmailConfirmedMessage(chatId);
        } else {
            logger.warn("Received invalid email: {} from chatId: {}", email, chatId);
            telegramBot.sendInvalidEmailMessage(chatId, bookingInfoProvider.getInvalidEmailMessage());
        }
    }
}
