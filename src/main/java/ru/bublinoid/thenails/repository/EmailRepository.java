package ru.bublinoid.thenails.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bublinoid.thenails.model.Email;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
}
