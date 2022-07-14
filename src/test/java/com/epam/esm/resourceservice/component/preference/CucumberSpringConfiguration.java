package com.epam.esm.resourceservice.component.preference;

import com.epam.esm.resourceservice.integration.RabbitMQContainerHelper;
import com.epam.esm.resourceservice.integration.S3ContainerHelper;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@Testcontainers(disabledWithoutDocker = true)
public class CucumberSpringConfiguration {
    @Container
    static LocalStackContainer container = S3ContainerHelper.construct();
    @Container
    static RabbitMQContainer rabbitMQContainer = RabbitMQContainerHelper.construct();
}
