package com.epam.esm.resourceservice.integration;

import org.testcontainers.containers.RabbitMQContainer;

import static org.assertj.core.api.Assertions.assertThat;

public final class RabbitMQContainerHelper {


   private RabbitMQContainerHelper() {
       super();
    }

    public static RabbitMQContainer construct(){
        RabbitMQContainer rabbitMQContainer =
                new RabbitMQContainer();
        rabbitMQContainer.start();
        System.setProperty("spring.rabbitmq.host",rabbitMQContainer.getHost());
        System.setProperty("spring.rabbitmq.port", rabbitMQContainer.getMappedPort(5672).toString());
   return rabbitMQContainer;
   }
}
