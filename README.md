# Spring AI + Vaadin — демонстрационное приложение

Этот репозиторий показывает, как использовать Spring AI (Spring Boot 3.5) вместе с Vaadin 24 для построения простого чат‑интерфейса с поддержкой контекста диалога (Chat Memory). UI написан на Vaadin, серверная часть — Spring Boot, модель по умолчанию — OpenAI (gpt‑4o).

Основные моменты:
- Spring AI ChatClient с хранением контекста через Chat Memory Repository (JDBC, схема H2 в комплекте)
- Простое UI на Vaadin: список диалогов, отправка сообщений, блоки сообщений пользователя и ИИ
- Actuator (включая поддержку Prometheus через Micrometer)
- Быстрая локальная сборка и запуск

Важно: сборку производите именно этой командой, чтобы корректно собрать фронтенд Vaadin в production‑режиме:

./gradlew clean bootJar -Pvaadin.productionMode


## Содержание
- Возможности
- Архитектура и технологии
- Требования
- Настройка окружения
- Сборка
- Запуск
- Режим разработки
- Конфигурация (application.properties)
- База данных / Chat Memory
- Метрики и Actuator
- Частые вопросы


## Возможности
- Многооконные «беседы» (conversations) в левой панели, переключение активной беседы
- Чат с ИИ на главной странице (маршрут "/")
- Сохранение контекста беседы для лучшего качества ответов модели
- Минимальный UI без внешних зависимостей CSS/JS — всё на компонентах Vaadin


## Архитектура и технологии
- Java 21, Gradle Kotlin DSL
- Spring Boot 3.5.x
- Vaadin 24.8.x
- Spring AI 1.0.x (OpenAI starter + JDBC Chat Memory Repository)
- H2 в памяти (по умолчанию) — для примера, вместе со схемой репозитория памяти
- Micrometer + Prometheus Registry

Ключевые классы:
- ru.kuramshindev.springaiexample.ui.ChatView — основное Vaadin‑представление (маршрут "/")
- ru.kuramshindev.springaiexample.ui.ConversationService — хранение списка бесед, интеграция с ChatClient и Chat Memory
- ru.kuramshindev.springaiexample.ui.model.* — модели «беседа», «сообщение», «роль»


## Требования
- Java 21+
- Node.js не требуется устанавливать вручную — Vaadin плагин в Gradle соберёт фронтенд автоматически
- Действующий ключ OpenAI API (переменная среды OPENAI_API_KEY)


## Настройка окружения
1) Установите переменную среды с вашим ключом OpenAI:
   - macOS/Linux: export OPENAI_API_KEY=sk-...
   - Windows (PowerShell): $env:OPENAI_API_KEY="sk-..."

2) Либо создайте файл .env в корне (не храните реальные ключи в VCS):
   OPENAI_API_KEY=sk-...

В репозитории уже добавлена запись .env в .gitignore.


## Сборка
Production‑сборка (обязательно для корректной сборки фронтенда Vaadin):

./gradlew clean bootJar -Pvaadin.productionMode

Итоговый JAR появится в build/libs/spring-ai-example-<version>.jar


## Запуск
После сборки запустите приложение:

./run.sh

или напрямую:

java -jar build/libs/spring-ai-example-*.jar

По умолчанию приложение доступно на http://localhost:8080


## Режим разработки
Для быстрого старта без production‑сборки можно использовать bootRun (Vaadin включит DevMode):

./gradlew bootRun

Учтите, что для финальной сборки фронтенда и публикации используйте production‑команду из раздела «Сборка».


## Конфигурация (application.properties)
Основные параметры по умолчанию находятся в src/main/resources/application.properties. Важно:
- Spring AI:
  - spring.ai.openai.api-key=${OPENAI_API_KEY}
  - spring.ai.openai.chat.options.model=gpt-4o
  - spring.ai.openai.chat.options.temperature=0.2
  - spring.ai.chat.memory.repository.jdbc.schema=classpath:ai/chat/memory/repository/jdbc/schema-h2.sql
- БД H2 (в памяти) и консоль H2:
  - spring.datasource.url=jdbc:h2:mem:testdb
  - spring.h2.console.enabled=true (консоль на /h2-console)

Вы можете переопределять значения через переменные окружения или профили Spring.


## База данных / Chat Memory
Для примера используется H2 in-memory, схема репозитория памяти подгружается из
src/main/resources/ai/chat/memory/repository/jdbc/schema-h2.sql

## Метрики и Actuator
Подключён spring-boot-starter-actuator и micrometer‑registry‑prometheus. После запуска доступны стандартные эндпоинты:
- /actuator/health
- /actuator/info
- /actuator/metrics
- /actuator/prometheus (если включено)


## Как это работает (коротко)
- UI (Vaadin) рендерит список бесед и сообщения
- При отправке запроса ConversationService добавляет сообщение пользователя и вызывает ChatClient
- В вызове ChatClient передаётся параметр ChatMemory.CONVERSATION_ID, чтобы Spring AI мог связать историю сообщений с конкретной беседой
- Ответ ИИ добавляется в текущую беседу и отображается в UI


## Частые вопросы
- Где задаётся модель? В application.properties: spring.ai.openai.chat.options.model=gpt-4o
- Где включить/выключить логирование SQL и Spring AI? Там же: logging.level.*
- Как открыть H2 консоль? http://localhost:8080/h2-console
- Ошибка «API key not set»: проверьте переменную окружения OPENAI_API_KEY или .env


## Лицензия
Проект предназначен для демонстрационных целей. Используйте и адаптируйте под свои задачи.