package com.epam.esm.resourceservice.service;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.epam.esm.resourceservice.dto.SaveSongDto;
import com.epam.esm.resourceservice.dto.StorageDto;
import com.epam.esm.resourceservice.entity.Message;
import com.epam.esm.resourceservice.entity.SaveResponse;
import com.epam.esm.resourceservice.entity.StorageType;
import com.epam.esm.resourceservice.exception.S3BucketNotExist;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.epam.esm.resourceservice.dao.SongsRowRepository;
import com.epam.esm.resourceservice.entity.SongRow;
import com.epam.esm.resourceservice.exception.ResourceNotFoundException;
import com.epam.esm.resourceservice.validator.FileValidator;

@Service
@Transactional
@Slf4j
public class Mp3Service {
    private static final String STORAGE_ENDPOINT = "storages";
    @Value("${storage-service.endpointHost}")
    private String storageServiceHost;
    private final S3Service s3Service;
    private final FileService fileService;
    private final FileValidator fileValidator;
    private final SongsRowRepository songsRowRepository;
    private final MessageService messageService;
    private final RestTemplate restTemplate;
    private final CircuitBreakerRegistry registry;

    private ResponseEntity<StorageDto[]> savedEntity = new ResponseEntity<StorageDto[]>(new StorageDto[]{}, HttpStatus.NO_CONTENT);


    public Mp3Service(S3Service s3Service, FileService fileService, FileValidator fileValidator, SongsRowRepository songsRowRepository, MessageService messageService, RestTemplate restTemplate, CircuitBreakerRegistry registry) {
        this.s3Service = s3Service;
        this.fileService = fileService;
        this.fileValidator = fileValidator;
        this.songsRowRepository = songsRowRepository;
        this.messageService = messageService;
        this.restTemplate = restTemplate;
        this.registry = registry;
    }

    @Transactional
    public SaveResponse saveSong(SaveSongDto saveSongDto, String traceId) {
        MultipartFile file = saveSongDto.getFile();
        long resourceId = saveSongDto.getResourceId();
        long storageId = saveSongDto.getStorageId();

        if (resourceId > 0 & storageId > 0) {
            SongRow songRow = songsRowRepository
                    .findById(resourceId)
                    .orElseThrow(() -> new ResourceNotFoundException(String.format("Resource doesn't exist with given id = %d", resourceId)));
            String fileName = songRow.getSongFileName();
            fileValidator.validate(fileName);
            StorageDto storageDto = getStorage(storageId);
            URL url = s3Service.saveFile(fileService.getInputStream(file), fileName, storageDto.getBucket());
            songRow.setLocation(String.valueOf(url));
            songRow.setStorageId(storageId);
            songsRowRepository.save(songRow);
            return new SaveResponse(songRow.getId());
        } else {
            String fileName = file.getOriginalFilename();
            fileValidator.validate(fileName);
            StorageDto storageDto = getStorage();
            URL url = s3Service.saveFile(fileService.getInputStream(file), fileName, storageDto.getBucket());
            SongRow row = SongRow.builder()
                    .songFileName(fileName)
                    .location(String.valueOf(url))
                    .storageId(storageDto.getId())
                    .build();
            songsRowRepository.save(row);
            SaveResponse sr = new SaveResponse(row.getId());
            Message message = Message.builder()
                    .id(row.getId())
                    .traceId(traceId)
                    .build();
            messageService.send(message);
            return sr;
        }
    }


    public byte[] getSong(long id) {
        SongRow songRow = songsRowRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Resource doesn't exist with given id = %d", id)));
        StorageDto storageDto = getStorage(songRow.getStorageId());
        return s3Service.downloadFile(songRow.getSongFileName(), storageDto.getBucket());
    }

    @Transactional
    public List<Long> deleteSongs(long[] ids) {
        List<Long> listOfIds = Arrays.stream(ids)
                .boxed()
                .collect(Collectors.toList());
        Iterator<Long> iterator = listOfIds.iterator();
        while (iterator.hasNext()) {
            Long currentId = iterator.next();
            if (songsRowRepository.existsById(currentId)) {
                SongRow songRow = songsRowRepository.findById(currentId).get();
                StorageDto storageDto = getStorage(songRow.getStorageId());
                songsRowRepository.delete(songRow);
                s3Service.deleteFile(songRow.getSongFileName(), storageDto.getBucket());
            } else {
                iterator.remove();
            }
        }
        return listOfIds;
    }

    private StorageDto getStorage() {
        log.info(String.format("Call endpoint: %s", STORAGE_ENDPOINT));
        CircuitBreaker circuitBreaker = registry.circuitBreaker("storage");
        Supplier<ResponseEntity<StorageDto[]>> responseEntitySupplier = () -> restTemplate.exchange(storageServiceHost + STORAGE_ENDPOINT, HttpMethod.GET, new HttpEntity<>(null), StorageDto[].class);
        ResponseEntity<StorageDto[]> storageDtos = Try.ofSupplier(circuitBreaker.decorateSupplier(responseEntitySupplier))
                .recover(throwable -> savedEntity)
                .get();
        saveResponseEntity(storageDtos);
        List<StorageDto> stagingStorages = Arrays.stream(storageDtos.getBody())
                .filter(storageDto -> storageDto.getStorageType().equals(StorageType.STAGING))
                .collect(Collectors.toList());
        Random random = new Random();
        if (stagingStorages.size() != 0) {
            return stagingStorages.get(random.nextInt(stagingStorages.size()));
        } else {
            throw new S3BucketNotExist("Buckets in storage service don't exists");
        }
    }

    private StorageDto getStorage(long id) {
        log.info(String.format("Call endpoint: %s", STORAGE_ENDPOINT));
        CircuitBreaker circuitBreaker = registry.circuitBreaker("storage");
        Supplier<ResponseEntity<StorageDto[]>> responseEntitySupplier = () -> restTemplate.exchange(storageServiceHost + STORAGE_ENDPOINT, HttpMethod.GET, new HttpEntity<>(null), StorageDto[].class);
        ResponseEntity<StorageDto[]> storageDtos = Try.ofSupplier(circuitBreaker.decorateSupplier(responseEntitySupplier))
                .recover(throwable -> {
                    log.error("Storage service unavailable, returned cashed result");
                    return savedEntity;
                })
                .get();
        saveResponseEntity(storageDtos);
        return Arrays.stream(storageDtos.getBody())
                .filter(storageDto -> storageDto.getId() == id)
                .findAny()
                .orElseThrow(() -> new S3BucketNotExist(String.format("Bucket in storage service with current id = %d doesn't exist", id)));
    }

    private void saveResponseEntity(ResponseEntity<StorageDto[]> storageDtos) {
        if (storageDtos.getStatusCodeValue() == 200) {
            savedEntity = new ResponseEntity<StorageDto[]>(storageDtos.getBody(), HttpStatus.NO_CONTENT);
        }
    }
}
