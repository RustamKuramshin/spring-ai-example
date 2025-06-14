package ru.kuramshindev.springaiexample;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient getChatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder
                .build();
    }
}
