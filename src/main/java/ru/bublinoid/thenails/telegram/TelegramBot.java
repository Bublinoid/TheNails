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
import ru.bublinoid.thenails.service.YClientsService;
import ru.bublinoid.thenails.service.UserStatesService;
import ru.bublinoid.thenails.model.BookingState;
import ru.bublinoid.thenails.model.BookingStep;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final BotConfig botConfig;
    private final YClientsService yClientsService;
    private final UserStatesService userStatesService;

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
                case "/book_manicure":
                    userStatesService.resetState(chatId);
                    askForSalon(chatId);
                    break;
                case "да":
                    handleBookingProcess(chatId, messageText);
                    break;
                case "нет":
                    sendMessage(chatId, "Бронирование отменено.");
                    userStatesService.resetState(chatId);
                    break;
                default:
                    handleBookingProcess(chatId, messageText);
                    break;
            }
        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = "Привет, " + name + ", приятно познакомиться!" + "\n" +
                "Для записи на маникюр введите команду /book_manicure.";
        sendMessage(chatId, answer);
    }

    private void askForSalon(Long chatId) {
        List<Map<String, Object>> salons = yClientsService.getSalons();
        String salonsMessage = salons.stream()
                .map(salon -> salon.get("id") + ": " + salon.get("title"))
                .collect(Collectors.joining("\n"));
        sendMessage(chatId, "Выберите салон:\n" + salonsMessage);
    }

    private void askForMaster(Long chatId, String salonId) {
        List<Map<String, Object>> masters = yClientsService.getMasters(salonId);
        String mastersMessage = masters.stream()
                .map(master -> master.get("id") + ": " + master.get("name"))
                .collect(Collectors.joining("\n"));
        sendMessage(chatId, "Выберите мастера:\n" + mastersMessage);
    }

    private void askForService(Long chatId, String salonId, String masterId) {
        List<Map<String, Object>> services = yClientsService.getServices(salonId, masterId);
        String servicesMessage = services.stream()
                .map(service -> service.get("id") + ": " + service.get("title"))
                .collect(Collectors.joining("\n"));
        sendMessage(chatId, "Выберите услугу:\n" + servicesMessage);
    }

    private void askForDate(Long chatId, String salonId, String masterId, String serviceId) {
        List<String> dates = yClientsService.getAvailableDates(salonId, masterId, serviceId);
        String datesMessage = String.join("\n", dates);
        sendMessage(chatId, "Выберите дату:\n" + datesMessage);
    }

    private void askForTime(Long chatId, String salonId, String masterId, String serviceId, String date) {
        List<String> times = yClientsService.getAvailableTimes(salonId, masterId, serviceId, date);
        String timesMessage = String.join("\n", times);
        sendMessage(chatId, "Выберите время:\n" + timesMessage);
    }

    private void sendServiceCost(Long chatId, String salonId, String serviceId) {
        Map<String, Object> serviceCost = yClientsService.getServiceCost(salonId, serviceId);
        String costMessage = "Стоимость услуги: " + serviceCost.get("cost") + " рублей";
        sendMessage(chatId, costMessage);
    }

    private void handleBookingProcess(Long chatId, String messageText) {
        BookingState state = userStatesService.getState(chatId);

        switch (state.getStep()) {
            case SELECT_SALON:
                state.setSalonId(messageText);
                state.setStep(BookingStep.SELECT_MASTER);
                askForMaster(chatId, state.getSalonId());
                break;
            case SELECT_MASTER:
                state.setMasterId(messageText);
                state.setStep(BookingStep.SELECT_SERVICE);
                askForService(chatId, state.getSalonId(), state.getMasterId());
                break;
            case SELECT_SERVICE:
                state.setServiceId(messageText);
                state.setStep(BookingStep.SELECT_DATE);
                askForDate(chatId, state.getSalonId(), state.getMasterId(), state.getServiceId());
                break;
            case SELECT_DATE:
                state.setDate(messageText);
                state.setStep(BookingStep.SELECT_TIME);
                askForTime(chatId, state.getSalonId(), state.getMasterId(), state.getServiceId(), state.getDate());
                break;
            case SELECT_TIME:
                state.setTime(messageText);
                state.setStep(BookingStep.CONFIRMATION);
                confirmBooking(chatId);
                break;
            case CONFIRMATION:
                if (messageText.equalsIgnoreCase("да")) {
                    bookService(chatId, state);
                    state.setStep(BookingStep.COMPLETED);
                    userStatesService.resetState(chatId);
                } else if (messageText.equalsIgnoreCase("нет")) {
                    sendMessage(chatId, "Бронирование отменено.");
                    userStatesService.resetState(chatId);
                } else {
                    sendMessage(chatId, "Введите 'да' для подтверждения или 'нет' для отмены.");
                }
                break;
            case COMPLETED:
                sendMessage(chatId, "Произошла ошибка. Попробуйте снова.");
                userStatesService.resetState(chatId);
                break;
        }
    }

    private void confirmBooking(Long chatId) {
        String confirmationMessage = "Для подтверждения бронирования введите 'да' или 'нет'.";
        sendMessage(chatId, confirmationMessage);
    }

    private void bookService(Long chatId, BookingState state) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(state.getDate() + "T" + state.getTime());
            Map<String, Object> response = yClientsService.bookService(
                    1, // ID бронирования можно изменить
                    "Имя клиента", // имя клиента
                    "79161234567", // телефон клиента
                    "email@example.com", // email клиента
                    Integer.parseInt(state.getMasterId()),
                    dateTime,
                    Integer.parseInt(state.getServiceId()),
                    "Комментарий"
            );
            if (response.containsKey("error")) {
                sendMessage(chatId, "Не удалось забронировать услугу: " + response.get("error"));
            } else {
                sendMessage(chatId, "Услуга успешно забронирована!");
            }
        } catch (Exception e) {
            sendMessage(chatId, "Произошла ошибка: " + e.getMessage());
        }
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
