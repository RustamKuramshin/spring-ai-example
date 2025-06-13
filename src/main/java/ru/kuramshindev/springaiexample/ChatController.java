package ru.kuramshindev.springaiexample;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class ChatController {

    private final ChatClient chatClient;

    @PostMapping("/generation")
    public String generation(@RequestBody UserInputDto userInputDto) {
        return chatClient.prompt()
                .user(userInputDto.getPrompt())
                .call()
                .content();
    }

    @GetMapping("/generation/stream")
    public Flux<String> generation(@RequestParam String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }

}
