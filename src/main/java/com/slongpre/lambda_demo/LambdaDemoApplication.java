package com.slongpre.lambda_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Function;

@SpringBootApplication
public class LambdaDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LambdaDemoApplication.class, args);
    }

    @Bean
    public Function<Message<JiraConfig>, Message<String>> function() {
        return m -> {
            final var worklogs = new JiraCloudConnector(m.getPayload()).getAllWorklogs();
            final var payload = new JiraReporter(m.getPayload()).printReport(worklogs);
            return MessageBuilder.createMessage(payload, m.getHeaders());
        };
    }
}
