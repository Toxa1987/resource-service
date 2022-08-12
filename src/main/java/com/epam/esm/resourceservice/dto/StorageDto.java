package com.epam.esm.resourceservice.dto;

import com.epam.esm.resourceservice.entity.StorageType;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class StorageDto {
    private long id;
    private StorageType storageType;
    private String bucket;
}
