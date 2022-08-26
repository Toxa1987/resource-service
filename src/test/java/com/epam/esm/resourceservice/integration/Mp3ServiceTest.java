package com.epam.esm.resourceservice.integration;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.epam.esm.resourceservice.dto.SaveSongDto;
import com.epam.esm.resourceservice.dto.StorageDto;
import com.epam.esm.resourceservice.entity.SaveResponse;
import com.epam.esm.resourceservice.entity.StorageType;
import com.epam.esm.resourceservice.exception.ResourceNotFoundException;
import com.epam.esm.resourceservice.service.Mp3Service;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class Mp3ServiceTest {

    private Mp3Service mp3Service;
    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    @Container
    static LocalStackContainer container = S3ContainerHelper.construct();
    @Container
    static RabbitMQContainer rabbitMQContainer = RabbitMQContainerHelper.construct();

    @Autowired
    public Mp3ServiceTest(Mp3Service mp3Service, RestTemplate restTemplate) {
        this.mp3Service = mp3Service;
        this.restTemplate = restTemplate;
        this.mockServer = MockRestServiceServer.bindTo(restTemplate)
                .bufferContent()
                .build();
    }
    @Order(1)
    @Test
    void saveSong_ShouldReturnSaveResponse() throws IOException {
        //Given
        SaveResponse expected = new SaveResponse(1);
        //When
        mockServer.expect(
                        MockRestRequestMatchers.requestTo("http://gateway/sts/storages"))
                .andRespond(
                        MockRestResponseCreators.withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("[{\"id\": 1,\"storageType\": \"STAGING\",\"bucket\": \"songsbucket\"}]"));
        File file = ResourceUtils.getFile("classpath:file.mp3");
        FileInputStream fis = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(), "audio/mpeg", IOUtils.toByteArray(fis));
        SaveSongDto songDto = SaveSongDto.builder()
                .file(multipartFile)
                .build();
        SaveResponse actual = mp3Service.saveSong(songDto,"");
        //Then
        Assertions.assertEquals(actual, expected);
    }

    @Order(2)
    @Test
    void DownloadExistFile_ShouldReturnByteArray() throws IOException {
        //Given
        File file = ResourceUtils.getFile("classpath:file.mp3");
        byte[] expected = new FileInputStream(file).readAllBytes();
        //When
        mockServer.expect(
                        MockRestRequestMatchers.requestTo("http://gateway/sts/storages"))
                .andRespond(
                        MockRestResponseCreators.withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("[{\"id\": 1,\"storageType\": \"STAGING\",\"bucket\": \"songsbucket\"}]"));
        byte[] actual = mp3Service.getSong(1);
        //Then
        Assertions.assertArrayEquals(actual, expected);
    }

    @Order(3)
    @Test
    void deleteFiles_GivenTwoIds_ShouldReturnOneId() {
        //Given
        List<Long> expectedIds = new ArrayList<>();
        expectedIds.add(1L);
        long[] ids = {1, 4};
        //When
        mockServer.expect(
                        MockRestRequestMatchers.requestTo("http://gateway/sts/storages"))
                .andRespond(
                        MockRestResponseCreators.withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("[{\"id\": 1,\"storageType\": \"STAGING\",\"bucket\": \"songsbucket\"}]"));
        List<Long> actual = mp3Service.deleteSongs(ids);
        //Then
        Assertions.assertTrue(expectedIds.containsAll(actual));
        Assertions.assertEquals(actual.size(), expectedIds.size());
    }

    @Test
    void DownloadFile_ShouldThrowException() throws IOException {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> mp3Service.getSong(2));
    }

    @AfterAll
    static void afterAll() {
        container.close();
        rabbitMQContainer.close();
    }
}
