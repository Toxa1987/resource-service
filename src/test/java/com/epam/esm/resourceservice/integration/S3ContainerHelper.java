package com.epam.esm.resourceservice.integration;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.util.ResourceUtils;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.io.FileNotFoundException;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

public final class S3ContainerHelper {

    private S3ContainerHelper() {
        super();
    }

    public static LocalStackContainer construct() {
        LocalStackContainer s3Container = new LocalStackContainer().withServices(S3);
        s3Container.start();
        System.setProperty("s3bucket.endpoint", s3Container.getEndpointConfiguration(S3).getServiceEndpoint());
        AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(s3Container.getEndpointConfiguration(S3))
                .withCredentials(s3Container.getDefaultCredentialsProvider())
                .build();
        s3.createBucket("songs-bucket");
        return s3Container;
    }
}
