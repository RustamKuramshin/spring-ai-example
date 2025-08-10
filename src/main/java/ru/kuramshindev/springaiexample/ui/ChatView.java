package ru.kuramshindev.springaiexample.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ru.kuramshindev.springaiexample.ui.model.Conversation;
import ru.kuramshindev.springaiexample.ui.model.Message;
import ru.kuramshindev.springaiexample.ui.model.Role;

@Route("")
@PageTitle("AI Chat")
public class ChatView extends VerticalLayout {

    private final ConversationService conversationService;

    private final VerticalLayout conversationsPanel = new VerticalLayout();
    private final ListBox<Conversation> conversationsList = new ListBox<>();
    private final Button addConversationBtn = new Button("+ New");
    private final Button deleteConversationBtn = new Button("Delete");

    private final Scroller messagesScroller = new Scroller();
    private final VerticalLayout messagesContainer = new VerticalLayout();

    private final TextArea promptInput = new TextArea();
    private final Button sendBtn = new Button("Send");

    public ChatView(ConversationService conversationService) {
        this.conversationService = conversationService;
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Left panel: conversations
        conversationsPanel.setWidth("320px");
        conversationsPanel.setPadding(true);
        conversationsPanel.setSpacing(true);
        conversationsPanel.setAlignItems(Alignment.STRETCH);
        conversationsPanel.add(new H2("Conversations"));

        conversationsList.setItems(conversationService.getConversations());
        conversationService.getActiveConversation().ifPresent(conversationsList::setValue);
        conversationsList.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                conversationService.setActiveConversation(e.getValue().getId());
                refreshMessages();
            }
        });

        HorizontalLayout convActions = new HorizontalLayout(addConversationBtn, deleteConversationBtn);
        addConversationBtn.addClickListener(e -> {
            Conversation c = conversationService.addConversation();
            conversationsList.setItems(conversationService.getConversations());
            conversationsList.setValue(c);
        });
        deleteConversationBtn.addClickListener(e -> {
            Conversation selected = conversationsList.getValue();
            if (selected != null) {
                conversationService.removeConversation(selected.getId());
                conversationsList.setItems(conversationService.getConversations());
                conversationService.getActiveConversation().ifPresent(conversationsList::setValue);
                refreshMessages();
            }
        });

        conversationsPanel.add(conversationsList, convActions);

        // Right side: messages + input
        messagesContainer.setPadding(false);
        messagesContainer.setSpacing(true);
        messagesContainer.setWidthFull();

        messagesScroller.setContent(messagesContainer);
        messagesScroller.setSizeFull();

        promptInput.setWidthFull();
        promptInput.setMinHeight("80px");
        promptInput.setMaxHeight("200px");
        promptInput.setPlaceholder("Type your message and press Send…");
        sendBtn.addClickListener(e -> onSend());

        HorizontalLayout inputRow = new HorizontalLayout(promptInput, sendBtn);
        inputRow.setWidthFull();
        inputRow.setAlignItems(FlexComponent.Alignment.END);
        inputRow.setFlexGrow(1, promptInput);

        VerticalLayout right = new VerticalLayout(messagesScroller, inputRow);
        right.setSizeFull();
        right.setPadding(false);
        right.setSpacing(true);
        right.setAlignItems(Alignment.STRETCH);

        HorizontalLayout main = new HorizontalLayout(conversationsPanel, right);
        main.setSizeFull();
        main.setSpacing(false);
        main.setPadding(false);
        main.setFlexGrow(1, right);

        add(main);
        refreshMessages();
    }

    private void onSend() {
        String prompt = promptInput.getValue();
        if (prompt == null) prompt = "";
        prompt = prompt.trim();
        if (prompt.isEmpty()) {
            return;
        }
        conversationService.addUserMessage(prompt);
        promptInput.clear();
        refreshMessages();
        conversationService.generateAiResponse(prompt);
        refreshMessages();
    }

    private void refreshMessages() {
        messagesContainer.removeAll();
        conversationService.getActiveConversation().ifPresent(conv -> {
            for (Message m : conv.getMessages()) {
                Div bubble = new Div();
                bubble.getStyle().set("padding", "var(--lumo-space-m)");
                bubble.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
                bubble.getStyle().set("max-width", "70%");
                bubble.getStyle().set("white-space", "pre-wrap");
                bubble.setText(m.getContent());

                HorizontalLayout line = new HorizontalLayout();
                line.setWidthFull();
                line.setPadding(false);
                if (m.getRole() == Role.USER) {
                    line.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
                    bubble.getStyle().set("background", "var(--lumo-primary-color-10pct)");
                } else {
                    line.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
                    bubble.getStyle().set("background", "var(--lumo-contrast-10pct)");
                }
                line.add(bubble);
                messagesContainer.add(line);
            }
        });
    }
}
