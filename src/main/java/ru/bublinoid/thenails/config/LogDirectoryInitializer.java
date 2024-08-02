package ru.bublinoid.thenails.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class LogDirectoryInitializer {

    @PostConstruct
    public void init() {
        File logDir = new File("logs");
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            if (created) {
                System.out.println("Директория logs была создана.");
            } else {
                System.out.println("Не удалось создать директорию logs.");
            }
        } else {
            System.out.println("Директория logs уже существует.");
        }
    }
}
