package com.epam.esm.resourceservice.service;

import java.io.InputStream;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.epam.esm.resourceservice.exception.S3BucketNotExist;
import com.epam.esm.resourceservice.exception.S3ObjectNotFoundException;

@Service
@Slf4j
public class S3Service {
    private final AmazonS3 amazonS3;
    private final FileService fileService;

    @Value("${s3bucket.name}")
    private String bucketName;

    public S3Service(AmazonS3 amazonS3, FileService fileService) {
        this.amazonS3 = amazonS3;
        this.fileService = fileService;
    }

    public URL saveFile(InputStream file, String fileName) {
        checkBucket();
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.addUserMetadata("Name", fileName);
        objectMetadata.setContentType("audio/mpeg");
        amazonS3.putObject(bucketName, fileName, file, objectMetadata);
        return amazonS3.getUrl(bucketName, fileName);
    }

    public byte[] downloadFile(String fileName) {
        checkBucket();
        checkObjectExits(fileName);
        S3Object s3Object = amazonS3.getObject(bucketName, fileName);
        return fileService.readBytes(s3Object);
    }

    public void deleteFile(String fileName) {
        checkBucket();
        checkObjectExits(fileName);
        amazonS3.deleteObject(bucketName, fileName);
    }

    private void checkBucket() {
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            log.error(String.format("S3 bucket %s doesn't exist",bucketName));
            log.info(String.format("Creating bucket %s",bucketName));
            amazonS3.createBucket(bucketName);
            log.info(String.format("Bucket with name %s created",bucketName));

        }
    }

    private void checkObjectExits(String objectName) {
        if (!amazonS3.doesObjectExist(bucketName, objectName)) {
            throw new S3ObjectNotFoundException("File not found");
        }
    }
}
