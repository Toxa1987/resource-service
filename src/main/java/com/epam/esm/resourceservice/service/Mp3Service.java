package com.epam.esm.resourceservice.service;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.epam.esm.resourceservice.entity.SaveResponse;
import com.epam.esm.resourceservice.exception.BrokerUnavailableException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.epam.esm.resourceservice.dao.SongsRowRepository;
import com.epam.esm.resourceservice.entity.SongRow;
import com.epam.esm.resourceservice.exception.ResourceNotFoundException;
import com.epam.esm.resourceservice.validator.FileValidator;

@Service
@Transactional
public class Mp3Service {
    private final S3Service s3Service;
    private final FileService fileService;
    private final FileValidator fileValidator;
    private final SongsRowRepository songsRowRepository;
    private final MessageService messageService;

    public Mp3Service(S3Service s3Service, FileService fileService, FileValidator fileValidator, SongsRowRepository songsRowRepository, MessageService messageService) {
        this.s3Service = s3Service;
        this.fileService = fileService;
        this.fileValidator = fileValidator;
        this.songsRowRepository = songsRowRepository;
        this.messageService = messageService;
    }

    public SaveResponse saveSong(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        fileValidator.validate(fileName);
        URL url = s3Service.saveFile(fileService.getInputStream(file),fileName);
        SongRow row = SongRow.builder()
                .songFileName(fileName)
                .location(String.valueOf(url))
                .build();
        songsRowRepository.save(row);
        SaveResponse sr = new SaveResponse(row.getId());
        messageService.send(sr);
        return sr;
    }


    public byte[] getSong(long id) {
        SongRow songRow = songsRowRepository
                .findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(String.format("Resource doesn't exist with given id = %d",id)));
        return s3Service.downloadFile(songRow.getSongFileName());
    }

    public List<Long> deleteSongs(long[] ids){
        List<Long> listOfIds = Arrays.stream(ids)
                .boxed()
                .collect(Collectors.toList());
        Iterator<Long> iterator = listOfIds.iterator();
        while (iterator.hasNext()){
            Long currentId = iterator.next();
            if(songsRowRepository.existsById(currentId)){
                SongRow songRow = songsRowRepository.findById(currentId).get();
                songsRowRepository.delete(songRow);
                s3Service.deleteFile(songRow.getSongFileName());
            }else{
                iterator.remove();
            }
        }
        return listOfIds;
    }
}
