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

@Component
@AllArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final BotConfig botConfig;

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
                default:
                    sendMessage(chatId, "Вы отправили: " + messageText);
            }
        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = "Привет, " + name + ", приятно познакомиться!" + "\n" +
                "Введите любой текст, и я повторю его обратно.";
        sendMessage(chatId, answer);
    }

    private void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Error occurred while sending message: ", e);
        }
    }
}