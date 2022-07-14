package com.epam.esm.resourceservice.contract;

import com.epam.esm.resourceservice.ResourceServiceApplication;
import com.epam.esm.resourceservice.controller.SongsStoreController;
import com.epam.esm.resourceservice.service.Mp3Service;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest(classes = ResourceServiceApplication.class)
@RunWith(SpringRunner.class)
public class BaseClass {
    @Autowired
    SongsStoreController songsStoreController;
    @MockBean
    Mp3Service mp3Service;

    @BeforeEach
    public void setup() throws IOException {
        RestAssuredMockMvc.standaloneSetup(songsStoreController);
        File file = ResourceUtils.getFile( String.format("classpath:%s","file.mp3"));
        byte[] data = new FileInputStream(file).readAllBytes();
        Mockito.when(mp3Service.getSong(1)).thenReturn(data);
    }
}