package com.epam.esm.resourceservice.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.epam.esm.resourceservice.exception.FileServiceException;

@Service
public class FileService {

    public byte[] readBytes(S3Object s3Object) {
        byte[] objectBytes = null;
        try {
            S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
            objectBytes = IOUtils.toByteArray(s3ObjectInputStream);
        } catch (IOException e) {
            throw new FileServiceException("Can't read bytes from object", e);
        }
        return objectBytes;
    }

    public String getExtension(String fileName) {
        Optional<String> extension = Optional.of(fileName)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(fileName.lastIndexOf(".") + 1));
        if (!extension.isPresent()) {
            throw new FileServiceException("File without extension");
        }
        return extension.get();
    }

    public InputStream getInputStream(MultipartFile file) {
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            throw new FileServiceException("Invalid file", e);
        }
        return inputStream;
    }
}
