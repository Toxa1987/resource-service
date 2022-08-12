package com.epam.esm.resourceservice.integration;

import org.testcontainers.containers.localstack.LocalStackContainer;

import java.io.IOException;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

public final class S3ContainerHelper {

    private S3ContainerHelper() {
        super();
    }

    public static LocalStackContainer construct() {
        LocalStackContainer s3Container = new LocalStackContainer().withServices(S3);
        s3Container.start();
        System.setProperty("aws.accessKeyId", s3Container.getDefaultCredentialsProvider().getCredentials().getAWSAccessKeyId());
        System.setProperty("aws.secretKey", s3Container.getDefaultCredentialsProvider().getCredentials().getAWSSecretKey());
        System.setProperty("s3bucket.endpoint", s3Container.getEndpointConfiguration(S3).getServiceEndpoint());
        System.setProperty("s3bucket.region", s3Container.getEndpointConfiguration(S3).getSigningRegion());
        try {
            s3Container.execInContainer("awslocal", "s3", "mb", "s3://" + "songsbucket");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return s3Container;
    }
}
