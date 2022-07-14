package com.epam.esm.resourceservice.integration;

import com.epam.esm.resourceservice.entity.SaveResponse;
import com.epam.esm.resourceservice.exception.ResourceNotFoundException;
import com.epam.esm.resourceservice.service.Mp3Service;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ResourceUtils;
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

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class Mp3ServiceTest {
    @Autowired
    Mp3Service mp3Service;
    @Container
    static LocalStackContainer container = S3ContainerHelper.construct();
    @Container
    static RabbitMQContainer rabbitMQContainer = RabbitMQContainerHelper.construct();

    @Order(1)
    @Test
    void saveSong_ShouldReturnSaveResponse() throws IOException {
        //Given
        SaveResponse expected = new SaveResponse(1);
        //When
        File file = ResourceUtils.getFile("classpath:file.mp3");
        FileInputStream fis = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(), "audio/mpeg", IOUtils.toByteArray(fis));
        SaveResponse actual = mp3Service.saveSong(multipartFile);
        //Then
        Assertions.assertEquals(actual, expected);
    }

    @Order(2)
    @Test
    void DownloadExistFile_ShouldReturnByteArray() throws IOException {
        File file = ResourceUtils.getFile("classpath:file.mp3");
        byte[] expected = new FileInputStream(file).readAllBytes();
        byte[] actual = mp3Service.getSong(1);
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
    }
}
