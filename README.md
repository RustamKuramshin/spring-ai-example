# Spring AI + Vaadin — Demo Application

This repository demonstrates how to use **Spring AI** (Spring Boot 3.5) together with **Vaadin 24** to build a simple chat interface with **conversation context memory** (Chat Memory).
The UI is built with Vaadin, the backend with Spring Boot, and the default model is **OpenAI GPT-4o**.

---

## Highlights

* **Spring AI ChatClient** with context persistence via **Chat Memory Repository** (JDBC, H2 schema included)
* Minimal Vaadin UI: conversation list, message sending, user/AI message blocks
* **Actuator** (with Prometheus support via Micrometer)
* Fast local build & run

**Important:** Use the exact command below for production builds to ensure the Vaadin frontend compiles correctly:

```bash
./gradlew clean bootJar -Pvaadin.productionMode
```

---

## Table of Contents

* Features
* Architecture & Tech Stack
* Requirements
* Environment Setup
* Build
* Run
* Development Mode
* Configuration (application.properties)
* Database / Chat Memory
* Metrics & Actuator
* FAQ
* License

---

## Features

* Multi-window conversations in the left panel, with active conversation switching
* Chat with AI on the main page (`/`)
* Conversation context persistence for higher-quality model responses
* Minimal UI — no external CSS/JS, all built with Vaadin components

---

## Architecture & Tech Stack

* Java 21, Gradle Kotlin DSL
* Spring Boot 3.5.x
* Vaadin 24.8.x
* Spring AI 1.0.x (OpenAI starter + JDBC Chat Memory Repository)
* H2 in-memory database (default) with bundled memory repository schema
* Micrometer + Prometheus registry

**Key Classes:**

* `ru.kuramshindev.springaiexample.ui.ChatView` — main Vaadin view (`/`)
* `ru.kuramshindev.springaiexample.ui.ConversationService` — conversation storage, ChatClient & Chat Memory integration
* `ru.kuramshindev.springaiexample.ui.model.*` — conversation, message, and role models

---

## Requirements

* Java 21+
* Node.js **not** required — the Vaadin Gradle plugin builds the frontend automatically
* A valid **OpenAI API key** (`OPENAI_API_KEY` environment variable)

---

## Environment Setup

1. Set your OpenAI API key:

    * macOS/Linux:

      ```bash
      export OPENAI_API_KEY=sk-...
      ```
    * Windows (PowerShell):

      ```powershell
      $env:OPENAI_API_KEY="sk-..."
      ```

2. Or create a `.env` file in the project root (do not commit real keys):

   ```env
   OPENAI_API_KEY=sk-...
   ```

`.env` is already in `.gitignore`.

---

## Build

Production build (required for correct Vaadin frontend compilation):

```bash
./gradlew clean bootJar -Pvaadin.productionMode
```

The final JAR will be at:

```
build/libs/spring-ai-example-<version>.jar
```

---

## Run

After building, start the application:

```bash
./run.sh
```

or directly:

```bash
java -jar build/libs/spring-ai-example-*.jar
```

Default URL: [http://localhost:8080](http://localhost:8080)

---

## Development Mode

For quick startup without a production build:

```bash
./gradlew bootRun
```

Vaadin will run in DevMode.
**Note:** For final builds and deployment, use the production build command from the **Build** section.

---

## Configuration (`application.properties`)

Key default settings (`src/main/resources/application.properties`):

* **Spring AI**:

  ```properties
  spring.ai.openai.api-key=${OPENAI_API_KEY}
  spring.ai.openai.chat.options.model=gpt-4o
  spring.ai.openai.chat.options.temperature=0.2
  spring.ai.chat.memory.repository.jdbc.schema=classpath:ai/chat/memory/repository/jdbc/schema-h2.sql
  ```
* **H2 in-memory DB & console**:

  ```properties
  spring.datasource.url=jdbc:h2:mem:testdb
  spring.h2.console.enabled=true
  ```

  Console available at `/h2-console`.

Override values via environment variables or Spring profiles as needed.

---

## Database / Chat Memory

Uses H2 in-memory by default.
The schema is loaded from:

```
src/main/resources/ai/chat/memory/repository/jdbc/schema-h2.sql
```

---

## Metrics & Actuator

Includes `spring-boot-starter-actuator` and `micrometer-registry-prometheus`.
Available endpoints:

* `/actuator/health`
* `/actuator/info`
* `/actuator/metrics`
* `/actuator/prometheus` (if enabled)

---

## How It Works (Briefly)

1. UI (Vaadin) renders the conversation list and messages.
2. On sending a message, `ConversationService` adds the user’s message and calls `ChatClient`.
3. `ChatClient` receives `ChatMemory.CONVERSATION_ID` to link message history to the active conversation.
4. AI response is added to the conversation and rendered in the UI.

---

## FAQ

* **Where is the model set?** In `application.properties`:
  `spring.ai.openai.chat.options.model=gpt-4o`
* **How to enable/disable SQL & Spring AI logging?** Same file via `logging.level.*`
* **How to access the H2 console?** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
* **Error “API key not set”?** Check `OPENAI_API_KEY` env var or `.env` file.

---

## License

This project is for demonstration purposes.
Feel free to use and adapt it for your own needs.