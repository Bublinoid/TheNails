package ru.bublinoid.thenails.service;

import org.springframework.stereotype.Service;
import ru.bublinoid.thenails.content.BookingInfoProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class BookingService {

    private final BookingInfoProvider bookingInfoProvider;
    private final Map<Long, String> userEmails = new HashMap<>();
    private final Map<Long, String> userVerificationCodes = new HashMap<>();
    private final Map<Long, String> userStates = new HashMap<>();

    public BookingService(BookingInfoProvider bookingInfoProvider) {
        this.bookingInfoProvider = bookingInfoProvider;
    }

    public String getRequestEmailMessage() {
        return bookingInfoProvider.getRequestEmailMessage();
    }

    public String getVerificationCodeMessage() {
        return bookingInfoProvider.getVerificationCodeMessage();
    }

    public String getEmailConfirmedMessage() {
        return bookingInfoProvider.getEmailConfirmedMessage();
    }

    public String getInvalidCodeMessage() {
        return bookingInfoProvider.getInvalidCodeMessage();
    }

    public void startEmailProcess(long chatId) {
        userStates.put(chatId, "awaiting_email");
    }

    public void handleEmailInput(long chatId, String email) {
        userEmails.put(chatId, email);
        String code = generateConfirmationCode();
        userVerificationCodes.put(chatId, code);

        // Simulate sending confirmation code to email
        // You would integrate with an actual email service here
        System.out.println("Sending confirmation code " + code + " to email " + email);

        userStates.put(chatId, "awaiting_verification_code");
    }

    public boolean verifyCode(long chatId, String code) {
        String expectedCode = userVerificationCodes.get(chatId);
        return expectedCode != null && expectedCode.equals(code);
    }

    public void clearUserData(long chatId) {
        userEmails.remove(chatId);
        userVerificationCodes.remove(chatId);
        userStates.remove(chatId);
    }

    private String generateConfirmationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Generate a 6-digit code
        return String.valueOf(code);
    }

    public String getUserState(long chatId) {
        return userStates.get(chatId);
    }
}
