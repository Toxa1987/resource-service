package com.epam.esm.resourceservice.integration;

import com.epam.esm.resourceservice.exception.S3ObjectNotFoundException;
import com.epam.esm.resourceservice.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ResourceUtils;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Slf4j
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles(profiles = "integration")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class S3Test {
    private static final String S3_BUCKET_NAME = "songsbucket";
    @Container
    static LocalStackContainer container = S3ContainerHelper.construct();
    @Autowired
    private S3Service s3Service;

    @Test
    void SaveValidFile_ShouldReturn_NotNullUrl() throws FileNotFoundException {
        String host = container.getEndpointConfiguration(S3).getServiceEndpoint();
        String expected = host + "/" + S3_BUCKET_NAME + "/file.mp3";
        File file = ResourceUtils.getFile("classpath:file.mp3");
        URL url = s3Service.saveFile(new FileInputStream(file), file.getName(),S3_BUCKET_NAME);
        Assertions.assertNotNull(url);
        Assertions.assertEquals(expected, url.toString());
    }

    @Test
    void DownloadExistFile_ShouldReturnByteArray() throws IOException {
        File file = ResourceUtils.getFile("classpath:file.mp3");
        byte[] expected = new FileInputStream(file).readAllBytes();
        byte[] actual = s3Service.downloadFile("file.mp3",S3_BUCKET_NAME);
        Assertions.assertArrayEquals(actual, expected);
    }

    @Test
    void DownloadFile_ShouldThrowException() throws IOException {
        Assertions.assertThrows(S3ObjectNotFoundException.class, () -> s3Service.downloadFile("missed.mp3",S3_BUCKET_NAME));
    }

    @Test
    void DeleteExistFile_ShouldExecuteWithoutException() {
        s3Service.deleteFile("file.mp3",S3_BUCKET_NAME);
    }
    @Test
    void DeleteFile_ShouldThrowException() {
        Assertions.assertThrows(S3ObjectNotFoundException.class, () -> s3Service.deleteFile("missed.mp3",S3_BUCKET_NAME));
    }
}
