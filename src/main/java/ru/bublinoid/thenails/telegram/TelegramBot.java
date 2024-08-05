package ru.bublinoid.thenails.telegram;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.bublinoid.thenails.config.BotConfig;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.bublinoid.thenails.content.AboutUsInfoProvider;
import ru.bublinoid.thenails.content.BookingInfoProvider;
import ru.bublinoid.thenails.content.ServicesInfoProvider;
import ru.bublinoid.thenails.content.ContactsInfoProvider;
import ru.bublinoid.thenails.keyboard.InlineKeyboardMarkupBuilder;
import ru.bublinoid.thenails.service.BookingService;

@Component
@AllArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final BotConfig botConfig;
    private final InlineKeyboardMarkupBuilder inlineKeyboardMarkupBuilder;
    private final ServicesInfoProvider servicesInfoProvider;
    private final AboutUsInfoProvider aboutUsInfoProvider;
    private final ContactsInfoProvider contactsInfoProvider;
    private final BookingService bookingService;

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String firstName = update.getMessage().getChat().getFirstName();

            logger.info("Received message from chatId: {}, name: {}, text: {}", chatId, firstName, messageText);

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, firstName);
                    break;
                // Добавьте другие команды здесь, если необходимо
                default:
                    // Обработка ввода e-mail
                    bookingService.handleEmailInput(chatId, messageText);
                    sendMainMenu(chatId, firstName);
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String firstName = update.getCallbackQuery().getMessage().getChat().getFirstName();

            logger.info("Received callback query from chatId: {}, name: {}, data: {}", chatId, firstName, callbackData);

            switch (callbackData) {
                case "services":
                    sendServicesInfo(chatId, firstName);
                    break;
                case "book":
                    sendBookingInfo(chatId, firstName);
                    break;
                case "about_us":
                    sendAboutUsInfo(chatId, firstName);
                    break;
                case "contacts":
                    sendContactsInfo(chatId, firstName);
                    break;
                default:
                    logger.warn("Unknown callback data: {}", callbackData);
                    break;
            }
        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = "Здравствуйте, " + name + "!\n" +
                "Добро пожаловать в наш бот записи на маникюр! Здесь вы сможете легко и быстро записаться на маникюр.";
        logger.info("Sending start command response to chatId: {}, name: {}", chatId, name);
        sendMessageWithKeyboard(chatId, answer, inlineKeyboardMarkupBuilder.createMainMenuKeyboard());
    }

    private void sendServicesInfo(Long chatId, String name) {
        String servicesInfo = servicesInfoProvider.getServicesInfo();
        logger.info("Sending services info to chatId: {}, name: {}", chatId, name);
        sendMarkdownMessage(chatId, servicesInfo);
        sendMainMenu(chatId, name);
    }

    private void sendAboutUsInfo(Long chatId, String name) {
        String aboutUsInfo = aboutUsInfoProvider.getAboutUsInfo();
        logger.info("Sending about us info to chatId: {}, name: {}", chatId, name);
        sendMarkdownMessage(chatId, aboutUsInfo);
        sendMainMenu(chatId, name);
    }

    private void sendContactsInfo(Long chatId, String name) {
        String contactsInfo = contactsInfoProvider.getContactsInfo();
        logger.info("Sending contacts info to chatId: {}, name: {}", chatId, name);
        sendMarkdownMessage(chatId, contactsInfo);
        sendMainMenu(chatId, name);
    }

    private void sendMarkdownMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setParseMode("Markdown"); // Устанавливаем режим парсинга Markdown
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Error occurred while sending message: ", e);
        }
    }

    private void sendBookingInfo(Long chatId, String name) {
        String bookingInfo = bookingService.getRequestEmailMessage();
        logger.info("Sending booking info to chatId: {}, name: {}", chatId, name);
        sendMarkdownMessage(chatId, bookingInfo);

        bookingService.handleEmailInput(chatId, ""); // Это заглушка, так как email будет обрабатываться в onUpdateReceived
    }

    private void sendMessageWithKeyboard(Long chatId, String textToSend, InlineKeyboardMarkup keyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setReplyMarkup(keyboardMarkup);
        sendMessage.setParseMode("Markdown"); // Устанавливаем режим парсинга Markdown для сообщений с клавиатурой
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Error occurred while sending message: ", e);
        }
    }

    private void sendMainMenu(Long chatId, String name) {
        logger.info("Sending main menu to chatId: {}, name: {}", chatId, name);
        sendMessageWithKeyboard(chatId, "Что бы вы хотели сделать дальше?", inlineKeyboardMarkupBuilder.createMainMenuKeyboard());
    }
}
