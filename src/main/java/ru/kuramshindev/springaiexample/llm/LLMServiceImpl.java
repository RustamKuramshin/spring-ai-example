package ru.kuramshindev.springaiexample.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import ru.kuramshindev.springaiexample.llm.tool.CommonTools;
import ru.kuramshindev.springaiexample.llm.tool.GradleTools;
import ru.kuramshindev.springaiexample.llm.tool.MavenTools;
import ru.kuramshindev.springaiexample.llm.tool.WorkspaceReadOnlyTools;
import ru.kuramshindev.springaiexample.llm.tool.WorkspaceWriteTools;

import java.util.ArrayList;
import java.util.List;

import static ru.kuramshindev.springaiexample.llm.AgentPrompts.SYSTEM;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMServiceImpl implements LLMService {

    private final ChatClient chatClient;

    private final WorkspaceReadOnlyTools roWorkspace;
    private final WorkspaceWriteTools wrWorkspace;
    private final MavenTools maven;
    private final GradleTools gradle;
    private final CommonTools commonTools;

    @Override
    public String talkToChatGPT(String conversationId, SystemMessage systemMessage, UserMessage userMessage) {
        List<Message> promptMessages = new ArrayList<>();
        promptMessages.add(systemMessage);
        promptMessages.add(userMessage);

        String fullResponse = chatClient
                .prompt(new Prompt(promptMessages))
                .advisors(advisor -> advisor
                        .param("chat_memory_conversation_id", conversationId)
                        .param("chat_memory_response_size", 1000))
                .call()
                .content();

        return fullResponse;
    }

    @Override
    public <T> T structuredTalkToChatGPT(String conversationId, SystemMessage systemMessage, UserMessage userMessage, Class<T> classType) {
        List<Message> promptMessages = new ArrayList<>();
        promptMessages.add(systemMessage);
        promptMessages.add(userMessage);

        T fullResponse = chatClient
                .prompt(new Prompt(promptMessages))
                .advisors(advisor -> advisor
                        .param("chat_memory_conversation_id", conversationId)
                        .param("chat_memory_response_size", 1000))
                .call()
                .entity(classType);

        return fullResponse;
    }

    @Override
    public String generateAiResponse(String conversationId, String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    @Override
    public String agentRunOnce(String conversationId, UserMessage userMessage, AgentMode mode) {

        List<Message> promptMessages = new ArrayList<>();
        promptMessages.add(new SystemMessage(SYSTEM));
        promptMessages.add(userMessage);

        if (mode == AgentMode.ASK) {
            return chatClient
                    .prompt(new Prompt(promptMessages))
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .tools(roWorkspace)
                    .call()
                    .content();
        } else {
            // CODE mode: read + write + build tools
            return chatClient
                    .prompt(new Prompt(promptMessages))
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .tools(roWorkspace, wrWorkspace, maven, gradle, commonTools)
                    .call()
                    .content();
        }
    }
}
