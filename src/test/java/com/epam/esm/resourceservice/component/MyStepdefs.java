package com.epam.esm.resourceservice.component;

import com.epam.esm.resourceservice.component.preference.ResourceHttpClient;
import com.epam.esm.resourceservice.entity.SaveResponse;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

public class MyStepdefs {
    @Autowired
    ResourceHttpClient client;
    private ResponseEntity<?> saveResponse;

    @When("^Client send (.+\\..{1,4}) to the resources endpoint$")
    public void client_send_file_to_the_resources(String fileName) throws IOException {
        File file = ResourceUtils.getFile( String.format("classpath:%s",fileName));
        saveResponse = client.sendFile(file);
    }
    @Then("Client received status code {int}")
    public void client_received_status_code(int code){
        Assertions.assertEquals(code,saveResponse.getStatusCodeValue());
    }
    @And("Client received response with id={long}")
    public void client_received_response_with_id(long id){
        SaveResponse saveResponse1 = (SaveResponse) saveResponse.getBody();
        Assertions.assertEquals(id,saveResponse1.getId());
    }
}
