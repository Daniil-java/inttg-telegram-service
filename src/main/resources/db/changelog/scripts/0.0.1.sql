--liquibase formatted sql

--changeset DanielK:1


CREATE TABLE IF NOT EXISTS telegram_users (
                                              telegram_id BIGINT NOT NULL PRIMARY KEY,
                                              user_id BIGINT,
                                              username TEXT,
                                              firstname TEXT,
                                              lastname TEXT,
                                              language_code TEXT,
                                              bot_state TEXT,
                                              actual_ai_conversation_id BIGINT,
                                              actual_topic_id BIGINT,
                                              updated TIMESTAMP,
                                              created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

