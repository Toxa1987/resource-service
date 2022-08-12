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

    public S3Service(AmazonS3 amazonS3, FileService fileService) {
        this.amazonS3 = amazonS3;
        this.fileService = fileService;
    }

    public URL saveFile(InputStream file, String fileName, String bucketName) {
        checkBucket(bucketName);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.addUserMetadata("Name", fileName);
        objectMetadata.setContentType("audio/mpeg");
        amazonS3.putObject(bucketName, fileName, file, objectMetadata);
        return amazonS3.getUrl(bucketName, fileName);
    }

    public byte[] downloadFile(String fileName, String bucketName) {
        checkBucket(bucketName);
        checkObjectExits(fileName,bucketName);
        S3Object s3Object = amazonS3.getObject(bucketName, fileName);
        return fileService.readBytes(s3Object);
    }

    public void deleteFile(String fileName, String bucketName) {
        checkBucket(bucketName);
        checkObjectExits(fileName,bucketName);
        amazonS3.deleteObject(bucketName, fileName);
    }

    private void checkBucket(String bucketName) {
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            log.error(String.format("S3 bucket %s doesn't exist",bucketName));
            throw new S3BucketNotExist(String.format("S3 bucket %s doesn't exist",bucketName));
        }
    }

    private void checkObjectExits(String objectName, String bucketName) {
        if (!amazonS3.doesObjectExist(bucketName, objectName)) {
            throw new S3ObjectNotFoundException("File not found");
        }
    }
}
