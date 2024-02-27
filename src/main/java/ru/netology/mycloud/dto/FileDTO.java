package ru.netology.mycloud.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDTO {
    private String filename;
    private long size;
    private String username;
}

