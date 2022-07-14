package com.epam.esm.resourceservice.validator;

import org.springframework.stereotype.Component;

import com.epam.esm.resourceservice.exception.ResourceValidationException;
import com.epam.esm.resourceservice.service.FileService;

@Component
public class FileValidator {
    private final FileService fileService;

    public FileValidator(FileService fileService) {
        this.fileService = fileService;
    }

    public void validate(String filename){
      if (!fileService.getExtension(filename).equals("mp3"))
          throw new ResourceValidationException("Invalid file");
 }
}
