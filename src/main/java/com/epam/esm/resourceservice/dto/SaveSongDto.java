package com.epam.esm.resourceservice.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SaveSongDto {
    @NotNull
    private MultipartFile file;
    private long storageId;
    private long resourceId;
}
