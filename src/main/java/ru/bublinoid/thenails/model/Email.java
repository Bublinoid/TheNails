package ru.bublinoid.thenails.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "email", schema = "the_nails")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "confirmation_code")
    private String confirmationCode;

    @Column(name = "insert_dt", nullable = false)
    private LocalDateTime insertDt = LocalDateTime.now();
}
