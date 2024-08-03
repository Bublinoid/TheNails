package ru.bublinoid.thenails.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.bublinoid.thenails.config.YClientsConfig;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YClientsService {

    private final YClientsConfig yClientsConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "https://api.yclients.com/api/v1/";

    @Autowired
    public YClientsService(YClientsConfig yClientsConfig) {
        this.yClientsConfig = yClientsConfig;
    }

    private String getAuthHeader() {
        return "Bearer " + yClientsConfig.getApiKey();
    }

    public List<Map<String, Object>> getSalons() {
        String url = BASE_URL + "companies/" + yClientsConfig.getCompanyId();
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpClientErrorException e) {
            System.err.println("Error fetching salons: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public List<Map<String, Object>> getMasters(String salonId) {
        String url = BASE_URL + "company/" + salonId + "/staff";
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpClientErrorException e) {
            System.err.println("Error fetching masters: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public List<Map<String, Object>> getServices(String salonId, String masterId) {
        String url = BASE_URL + "company/" + salonId + "/services/" + masterId;
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpClientErrorException e) {
            System.err.println("Error fetching services: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public List<String> getAvailableDates(String salonId, String masterId, String serviceId) {
        String url = BASE_URL + "record/" + salonId + "/available-dates?staff_id=" + masterId + "&service_id=" + serviceId;
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpClientErrorException e) {
            System.err.println("Error fetching available dates: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public List<String> getAvailableTimes(String salonId, String masterId, String serviceId, String date) {
        String url = BASE_URL + "record/" + salonId + "/available-times?staff_id=" + masterId + "&service_id=" + serviceId + "&date=" + date;
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (HttpClientErrorException e) {
            System.err.println("Error fetching available times: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public Map<String, Object> getServiceCost(String salonId, String serviceId) {
        String url = BASE_URL + "company/" + salonId + "/services/" + serviceId;
        try {
            return restTemplate.getForObject(url, Map.class);
        } catch (HttpClientErrorException e) {
            System.err.println("Error fetching service cost: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public Map<String, Object> bookService(int bookingId, String fullname, String phone, String email, int staffId, LocalDateTime dateTime, int serviceId, String comment) {
        String url = String.format("https://n%s.yclients.com/api/v1/book_record/%s/", yClientsConfig.getCompanyId(), bookingId);
        String dateTimeStr = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        Map<String, Object> payload = new HashMap<>();
        payload.put("phone", phone);
        payload.put("fullname", fullname);
        payload.put("email", email);
        payload.put("comment", comment);
        payload.put("notify_by_email", 0);
        Map<String, Object> appointment = new HashMap<>();
        appointment.put("id", bookingId);
        appointment.put("services", new int[]{serviceId});
        appointment.put("staff_id", staffId);
        appointment.put("datetime", dateTimeStr);
        payload.put("appointments", new Map[]{appointment});

        try {
            return restTemplate.postForObject(url, payload, Map.class);
        } catch (HttpClientErrorException e) {
            System.err.println("Error booking service: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        }
    }
}
