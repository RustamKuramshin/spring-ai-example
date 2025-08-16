package ru.kuramshindev.springaiexample.ui.aichat.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Message {
    private final Role role;
    private final String content;
    private final LocalDateTime timestamp;

    public Message(Role role, String content, LocalDateTime timestamp) {
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

}