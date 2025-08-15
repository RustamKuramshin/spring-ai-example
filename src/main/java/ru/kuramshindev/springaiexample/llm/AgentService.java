package ru.kuramshindev.springaiexample.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import ru.kuramshindev.springaiexample.llm.tool.CommonTools;
import ru.kuramshindev.springaiexample.llm.tool.MavenTools;
import ru.kuramshindev.springaiexample.llm.tool.WorkspaceTools;

public class AgentService {

    private final ChatClient client;
    private final WorkspaceTools workspace;
    private final MavenTools maven;

    private final CommonTools commonTools;

    public AgentService(ChatModel chatModel, WorkspaceTools workspace, MavenTools maven, CommonTools commonTools) {
        this.client = ChatClient.builder(chatModel)
                .defaultSystem(AgentPrompts.SYSTEM)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.workspace = workspace;
        this.maven = maven;
        this.commonTools = commonTools;
    }

    /**
     * Запускает один reasoning-проход: модель может вызывать несколько инструментов
     * (просмотр/чтение/запись файлов, запуск maven), после чего вернёт итог.
     */
    public String runOnce(String developerRequest) {
        return client.prompt()
                .user(developerRequest)
                .tools(workspace, maven, commonTools)
                .call()
                .content();
    }
}