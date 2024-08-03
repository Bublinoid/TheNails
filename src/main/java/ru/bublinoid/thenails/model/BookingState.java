package ru.bublinoid.thenails.model;

import lombok.Data;

@Data
public class BookingState {
    private String salonId;
    private String masterId;
    private String serviceId;
    private String date;
    private String time;
    private BookingStep step = BookingStep.SELECT_SALON;
}
