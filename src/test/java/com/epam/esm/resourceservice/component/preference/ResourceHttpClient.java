package com.epam.esm.resourceservice.component.preference;

import com.epam.esm.resourceservice.entity.SaveResponse;
import com.epam.esm.resourceservice.exception.ApiExceptionResponse;
import io.cucumber.spring.CucumberTestContext;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Component
@Scope(CucumberTestContext.SCOPE_CUCUMBER_GLUE)
public class ResourceHttpClient {
    private final String SERVER_URL = "http://localhost";
    private final String ENDPOINT = "/resources";

    @LocalServerPort
    private int port;
    private final RestTemplate restTemplate = new RestTemplate();

    private String getEndpoint() {
        return SERVER_URL + ":" + port + ENDPOINT;
    }

    public ResponseEntity<?> sendFile(final File file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename(file.getName())
                .build();

        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(Files.readAllBytes(file.toPath()), fileMap);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        if (file.getName().matches(".+\\.mp3")) {
            return restTemplate.postForEntity(getEndpoint(), requestEntity, SaveResponse.class);
        } else {
            try {
               return  restTemplate.postForEntity(getEndpoint(), requestEntity, ApiExceptionResponse.class);
            } catch (HttpClientErrorException exception) {
                return new ResponseEntity<>(exception.getResponseBodyAsString(),exception.getStatusCode());
            }
        }
    }
}