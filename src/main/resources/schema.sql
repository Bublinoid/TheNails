-- Создание схемы the_nails
CREATE SCHEMA IF NOT EXISTS the_nails;

-- Создание таблицы email в схеме the_nails
CREATE TABLE IF NOT EXISTS the_nails.email (
id SERIAL PRIMARY KEY,
chat_id BIGINT NOT NULL,
email VARCHAR(255) NOT NULL,
confirmation_code VARCHAR(4),
insert_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
