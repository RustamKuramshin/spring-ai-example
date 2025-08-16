package ru.kuramshindev.springaiexample;

import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@RequiredArgsConstructor
@SpringBootApplication
public class SpringAiExampleApplication {

    private final Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(SpringAiExampleApplication.class, args);
    }

    @EventListener
    public void printApplicationUrl(final ApplicationStartedEvent event) {
        LoggerFactory.getLogger(SpringAiExampleApplication.class).info("Application started at "
                + "http://localhost:"
                + environment.getProperty("local.server.port"));
    }

}
