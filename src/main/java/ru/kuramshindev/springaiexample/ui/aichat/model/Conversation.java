package ru.kuramshindev.springaiexample.ui.aichat.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Conversation {
    private final UUID id;
    @Setter
    private String name;
    private final List<Message> messages = new ArrayList<>();

    public Conversation(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}