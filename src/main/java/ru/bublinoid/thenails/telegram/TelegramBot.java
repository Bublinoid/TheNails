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

@Component
@AllArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final BotConfig botConfig;
    private final InlineKeyboardMarkupBuilder inlineKeyboardMarkupBuilder;
    private final ServicesInfoProvider servicesInfoProvider;

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
                    // Здесь можно добавить обработку других сообщений, если это потребуется в будущем
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch (callbackData) {
                case "services":
                    sendServicesInfo(chatId);
                    break;
                case "book":
                    // Add booking info method here
                    break;
                case "about_us":
                    // Add about us info method here
                    break;
                case "contacts":
                    // Add contacts info method here
                    break;
                default:
                    break;
            }
        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = "Здравствуйте, " + name + "!\n" +
                "Добро пожаловать в наш бот записи на маникюр! Здесь вы сможете легко и быстро записаться на маникюр.";
        sendMessageWithKeyboard(chatId, answer, inlineKeyboardMarkupBuilder.createMainMenuKeyboard());
    }

    private void sendServicesInfo(Long chatId) {
        String servicesInfo = servicesInfoProvider.getServicesInfo();
        sendMarkdownMessage(chatId, servicesInfo);
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
}
