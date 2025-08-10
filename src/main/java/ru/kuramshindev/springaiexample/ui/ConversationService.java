package ru.kuramshindev.springaiexample.ui;

import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;
import ru.kuramshindev.springaiexample.ui.model.Conversation;
import ru.kuramshindev.springaiexample.ui.model.Message;
import ru.kuramshindev.springaiexample.ui.model.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@UIScope
public class ConversationService {

    private final ChatClient chatClient;

    @Getter
    private final List<Conversation> conversations = new ArrayList<>();
    private UUID activeConversationId;

    public ConversationService(ChatClient chatClient) {
        this.chatClient = chatClient;
        // initialize with one empty conversation
        Conversation initial = new Conversation("Conversation 1");
        conversations.add(initial);
        activeConversationId = initial.getId();
    }

    public Optional<Conversation> getActiveConversation() {
        return conversations.stream().filter(c -> c.getId().equals(activeConversationId)).findFirst();
    }

    public Conversation addConversation() {
        int idx = conversations.size() + 1;
        Conversation c = new Conversation("Conversation " + idx);
        conversations.add(c);
        activeConversationId = c.getId();
        return c;
    }

    public void removeConversation(UUID id) {
        conversations.removeIf(c -> c.getId().equals(id));
        if (conversations.isEmpty()) {
            Conversation c = new Conversation("Conversation 1");
            conversations.add(c);
            activeConversationId = c.getId();
        } else if (activeConversationId != null && conversations.stream().noneMatch(c -> c.getId().equals(activeConversationId))) {
            activeConversationId = conversations.get(0).getId();
        }
    }

    public void setActiveConversation(UUID id) {
        this.activeConversationId = id;
    }

    public Message addUserMessage(String content) {
        Conversation conv = getActiveConversation().orElseThrow();
        Message msg = new Message(Role.USER, content, LocalDateTime.now());
        conv.getMessages().add(msg);
        // Update conversation title from first user message if still default
        if (conv.getMessages().size() == 1 && conv.getName().startsWith("Conversation ")) {
            String title = content;
            if (title.length() > 30) title = title.substring(0, 30) + "…";
            conv.setName(title);
        }
        return msg;
    }

    public Message generateAiResponse(String prompt) {
        String ai = chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, activeConversationId))
                .call()
                .content();
        Conversation conv = getActiveConversation().orElseThrow();
        Message msg = new Message(Role.AI, ai, LocalDateTime.now());
        conv.getMessages().add(msg);
        return msg;
    }
}
