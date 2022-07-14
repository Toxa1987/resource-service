package com.epam.esm.resourceservice.service;

import com.epam.esm.resourceservice.entity.SaveResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routingKey}")
    private String routingKey;

    @Retryable(maxAttemptsExpression = "${messageService.maxRetries}", value = RuntimeException.class, backoff = @Backoff(1000))
    public boolean send(SaveResponse saveResponse) {
        log.info(String.format("Sending message to the queue with data: %s", saveResponse));
        rabbitTemplate.convertAndSend(exchange, routingKey, saveResponse);
        log.info("Successfully sent message with data: " + saveResponse);
        return true;
    }

    @Recover
    private boolean recoverSend(RuntimeException exception) {
        log.error("Can't establish connection with message broker server");
        return false;
    }
}
