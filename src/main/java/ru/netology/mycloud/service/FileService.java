package ru.netology.mycloud.service;


import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.mycloud.model.FileModel;
import ru.netology.mycloud.repository.FileRepository;
import ru.netology.mycloud.security.jwt.JwtTokenProvider;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
public class FileService {
    private final FileRepository fileRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public FileService(FileRepository fileRepository, JwtTokenProvider jwtTokenProvider) {
        this.fileRepository = fileRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Value("${upload.path}")
    private String uploadPath;

    public boolean saveFile(MultipartFile multipartFile, String token) throws IOException {
        String username = jwtTokenProvider.getUsername(token);
        String uuidFile = UUID.randomUUID().toString();

        String resultFilename = uuidFile + "." + multipartFile.getOriginalFilename();
        var file = new File(uploadPath + resultFilename);
        if (file.exists() || multipartFile.isEmpty()) return false;

        var checkPath = Paths.get(uploadPath);
        if (!Files.exists(checkPath)) {
            var dir = new java.io.File(uploadPath);
            dir.mkdir();
        }
        log.info("IN saveFile user: {} saved file: {}", username, multipartFile.getOriginalFilename());
        byte[] bytes = multipartFile.getBytes();
        @Cleanup BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        stream.write(bytes);
        FileModel uploadedFile = new FileModel()
                .setFilename(resultFilename)
                .setSize(multipartFile.getSize())
                .setUsername(username);
        fileRepository.save(uploadedFile);
        log.info("IN save(database) user:{} saved file {}", username, resultFilename);
        return true;
    }

    public File getFile(String fileName) throws FileNotFoundException {
        var file = new File(uploadPath + fileName);
        if (file.exists()) {
            log.info("IN getFile was uploaded file {}", fileName);
            return file;
        } else {
            throw new FileNotFoundException("File not found");
        }
    }
}
