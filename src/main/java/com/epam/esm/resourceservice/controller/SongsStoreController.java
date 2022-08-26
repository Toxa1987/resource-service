package com.epam.esm.resourceservice.controller;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.epam.esm.resourceservice.dto.SaveSongDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.epam.esm.resourceservice.entity.DeleteResponse;
import com.epam.esm.resourceservice.entity.SaveResponse;
import com.epam.esm.resourceservice.service.Mp3Service;

@RestController
@RequestMapping("/resources")
@Validated
public class SongsStoreController {
    private final Mp3Service mp3Service;

    public SongsStoreController(Mp3Service mp3Service) {
        this.mp3Service = mp3Service;
    }

    @PostMapping()
    public ResponseEntity<SaveResponse> saveSong(@RequestHeader(name = "traceId", required = false) String traceId,
                                                 @SessionAttribute(name = "traceId", required = false) String sessionTraceId,
                                                 @ModelAttribute SaveSongDto saveSongDto) {
        if (traceId == null) {
            traceId = sessionTraceId;
        }
        return new ResponseEntity<>(mp3Service.saveSong(saveSongDto, traceId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getSong(
            @PathVariable @NotNull @Min(value = 1, message = "Id value should be 1 or more") long id) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put("Content-Type", List.of("audio/mpeg"));
        return new ResponseEntity<>(mp3Service.getSong(id), headers, HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity<DeleteResponse> deleteSongs(
            @RequestParam @NotNull long[] id) {
        return new ResponseEntity<>(new DeleteResponse(mp3Service.deleteSongs(id)), HttpStatus.OK);
    }
}
