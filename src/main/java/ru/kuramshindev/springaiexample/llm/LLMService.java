package ru.kuramshindev.springaiexample.llm;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

public interface LLMService {
    String talkToChatGPT(String conversationId, SystemMessage systemMessage, UserMessage userMessage);

    <T> T structuredTalkToChatGPT(String conversationId, SystemMessage systemMessage, UserMessage userMessage, Class<T> classType);

    String generateAiResponse(String conversationId, String prompt);

    String agentRunOnce(String conversationId, UserMessage userMessage);
}
