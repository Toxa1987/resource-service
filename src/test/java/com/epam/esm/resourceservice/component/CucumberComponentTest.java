package com.epam.esm.resourceservice.component;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features/",
        glue = "com.epam.esm.resourceservice.component"
)
public class CucumberComponentTest {

}
