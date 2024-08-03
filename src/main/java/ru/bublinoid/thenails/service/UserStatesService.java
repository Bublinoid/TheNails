package ru.bublinoid.thenails.service;

import org.springframework.stereotype.Component;
import ru.bublinoid.thenails.model.BookingState;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStatesService {
    private final ConcurrentHashMap<Long, BookingState> states = new ConcurrentHashMap<>();

    public BookingState getState(Long userId) {
        return states.computeIfAbsent(userId, k -> new BookingState());
    }

    public void resetState(Long userId) {
        states.remove(userId);
    }
}
