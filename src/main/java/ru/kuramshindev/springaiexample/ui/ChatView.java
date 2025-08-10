package ru.kuramshindev.springaiexample.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.Key;
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
    private final Button sendBtn = new Button(new Icon(VaadinIcon.ARROW_UP));

    public ChatView(ConversationService conversationService) {
        this.conversationService = conversationService;
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Left panel: conversations
        conversationsPanel.setWidth("320px");
        conversationsPanel.setHeightFull();
        conversationsPanel.setPadding(true);
        conversationsPanel.setSpacing(true);
        conversationsPanel.setAlignItems(Alignment.STRETCH);
        conversationsPanel.getStyle().set("background-color", "#181818");
        conversationsPanel.getStyle().set("color", "#f0f0f0");
        conversationsPanel.getStyle().set("border-right", "1px solid #2a2a2a");
        H2 convTitle = new H2("Conversations");
        convTitle.getStyle().set("color", "#f0f0f0");
        conversationsPanel.add(convTitle);

        conversationsList.setItems(conversationService.getConversations());
        conversationsList.getStyle().set("background", "transparent");
        conversationsList.getStyle().set("color", "#f0f0f0");
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
        messagesContainer.setPadding(true);
        messagesContainer.setSpacing(true);
        messagesContainer.setWidthFull();
        messagesContainer.getStyle().set("padding", "24px");

        messagesScroller.setContent(messagesContainer);
        messagesScroller.setSizeFull();

        promptInput.setWidthFull();
        promptInput.setMinHeight("80px");
        promptInput.setMaxHeight("200px");
        promptInput.setPlaceholder("Спросите что-нибудь...");
        // Dark style and rounded corners for the input
        promptInput.getStyle().set("background-color", "#303030");
        promptInput.getStyle().set("color", "#f0f0f0");
        promptInput.getStyle().set("border-radius", "12px");
        promptInput.getStyle().set("border", "1px solid #3a3a3a");
        promptInput.getStyle().set("overflow", "auto");
        // Placeholder color
        promptInput.getStyle().set("--vaadin-input-field-placeholder-color", "#A7A7A7");
        // Ensure text doesn't go under the send button: extra right padding on the native textarea
        // and auto-resize behavior remains
        promptInput.getElement().executeJs(
                "const ta=$0.inputElement; const max=200; const adjust=()=>{ta.style.height='auto'; ta.style.height=Math.min(ta.scrollHeight,max)+'px'; ta.style.overflowY=(ta.scrollHeight>max)?'auto':'hidden';}; ta.style.paddingRight='56px'; ta.addEventListener('input', adjust); requestAnimationFrame(adjust);",
                promptInput.getElement());

        // Send button styling: circular, icon-only, inside the input at right center
        // Make the button white with a black arrow
        // Remove primary theme to avoid blue background
        // and use custom styles instead
        sendBtn.getStyle().set("position", "absolute");
        sendBtn.getStyle().set("right", "16px");
        sendBtn.getStyle().set("top", "50%");
        sendBtn.getStyle().set("transform", "translateY(-50%)");
        sendBtn.getStyle().set("width", "36px");
        sendBtn.getStyle().set("height", "36px");
        sendBtn.getStyle().set("border-radius", "50%");
        sendBtn.getStyle().set("padding", "0");
        sendBtn.getStyle().set("min-width", "36px");
        sendBtn.getStyle().set("min-height", "36px");
        sendBtn.getStyle().set("background-color", "#FFFFFF");
        sendBtn.getStyle().set("color", "#000000");
        sendBtn.addClickListener(e -> onSend());

        // Enter key sends the message when pressing Enter
        // Note: Shift+Enter might also trigger send depending on Vaadin modifiers support
        promptInput.addKeyDownListener(Key.ENTER, e -> onSend());

        // Wrap the text area and the send button to place the button inside the field
        Div inputWrapper = new Div(promptInput, sendBtn);
        inputWrapper.setWidthFull();
        inputWrapper.getStyle().set("position", "relative");

        HorizontalLayout inputRow = new HorizontalLayout(inputWrapper);
        inputRow.setWidthFull();
        inputRow.setAlignItems(FlexComponent.Alignment.END);
        inputRow.setFlexGrow(1, inputWrapper);
        inputRow.getStyle().set("padding", "0 24px 24px 24px");

        // Make the chat dialog and input more compact (focused): 1/3 of page width
        messagesScroller.setWidthFull();
        VerticalLayout chatWrapper = new VerticalLayout(messagesScroller, inputRow);
        chatWrapper.setWidth("33vw");
        chatWrapper.setHeightFull();
        chatWrapper.setPadding(false);
        chatWrapper.setSpacing(false);
        chatWrapper.setAlignItems(Alignment.STRETCH);
        chatWrapper.getStyle().set("margin", "0 auto");
        chatWrapper.expand(messagesScroller);

        VerticalLayout right = new VerticalLayout(chatWrapper);
        right.setSizeFull();
        right.setPadding(true);
        right.setSpacing(true);
        right.setAlignItems(Alignment.STRETCH);
        right.getStyle().set("background-color", "#212121");
        right.getStyle().set("color", "#f0f0f0");

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
                HorizontalLayout line = new HorizontalLayout();
                line.setWidthFull();
                line.setPadding(false);

                if (m.getRole() == Role.USER) {
                    Div bubble = new Div();
                    bubble.getStyle().set("padding", "var(--lumo-space-m)");
                    bubble.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
                    bubble.getStyle().set("max-width", "70%");
                    bubble.getStyle().set("white-space", "pre-wrap");
                    // Ensure long words/URLs wrap inside the bubble and do not overflow
                    bubble.getStyle().set("overflow-wrap", "anywhere");
                    bubble.getStyle().set("word-break", "break-word");
                    bubble.getStyle().set("background-color", "#2E2E2E");
                    bubble.getStyle().set("color", "#FFFFFF");
                    bubble.setText(m.getContent());

                    line.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
                    line.add(bubble);
                } else {
                    Div text = new Div();
                    text.getStyle().set("white-space", "pre-wrap");
                    // Ensure long words/URLs wrap and stay within container
                    text.getStyle().set("overflow-wrap", "anywhere");
                    text.getStyle().set("word-break", "break-word");
                    text.getStyle().set("color", "#FFFFFF");
                    text.setText(m.getContent());

                    line.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
                    line.add(text);
                }

                messagesContainer.add(line);
            }
        });
    }
}
