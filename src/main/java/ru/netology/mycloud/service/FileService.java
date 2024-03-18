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

    public ResponseEntity findFilesByToken(String token) {
        String username = jwtTokenProvider.getUsername(token);
        if (jwtTokenProvider.validateToken(token)) {
            List<FileModel> filesList = fileRepository.findFilesByUsername(username);
            return ResponseEntity.status(200).body(filesList);
        } else {
            return ResponseEntity.status(401).body("Unauthorized error");
        }
    }


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

    public ResponseEntity saveFileResponse(MultipartFile multipartFile, String token) throws IOException {
        if (jwtTokenProvider.validateToken(token)) {
            if (saveFile(multipartFile, token)) {
                return ResponseEntity.ok("Success upload");
            } else {
                return ResponseEntity.status(400).body("Error input data");
            }
        } else {
            return ResponseEntity.status(401).body("Unauthorized error");
        }
    }

    public ResponseEntity deleteFile(String token, String fileName) {
        if (jwtTokenProvider.validateToken(token)) {
            var file = new File(uploadPath + fileName);
            if (file.exists()) {
                file.delete();
                fileRepository.deleteFileModelByFilename(fileName);
                log.info("IN deleteFile was deleted file {}", fileName);
                return ResponseEntity.ok("Success deleted");
            }
            return ResponseEntity.status(400).body("Error input data");
        } else {
            return ResponseEntity.status(401).body("Unauthorized error");
        }
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

    public ResponseEntity getResponseWithFile(String token, String fileName) {
        if (jwtTokenProvider.validateToken(token)) {
            File file = null;
            try {
                file = getFile(fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Path path = Paths.get(file.getAbsolutePath());
            byte[] bytes = new byte[0];
            String probeContentType = null;
            try {
                bytes = Files.readAllBytes(path);
                probeContentType = Files.probeContentType(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("IN getResponseWithFile was response 200, file {}", fileName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(file.getName()).build().toString())
                    .contentType(probeContentType != null ? MediaType.valueOf(probeContentType) : MediaType.APPLICATION_OCTET_STREAM)
                    .body(bytes);
        } else {
            return ResponseEntity.status(401).body("Unauthorized error");
        }
    }


    public ResponseEntity renameFile(String token, String fileName, String newName) {
        if (jwtTokenProvider.validateToken(token)) {
            var username = jwtTokenProvider.getUsername(token);
            var file = new File(uploadPath + fileName);
            if (!file.exists()) {
                return ResponseEntity.status(400).body("File not found");
            } else {
                FileModel renamedFile = new FileModel()
                        .setFilename(newName)
                        .setSize(file.length())
                        .setUsername(username);
                fileRepository.deleteFileModelByFilename(fileName);
                fileRepository.save(renamedFile);
                log.info("IN renameFile file:{} was renamed into: {}", fileName, newName);
                if (file.renameTo(new File(uploadPath + newName))) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }
        } else {
            return ResponseEntity.status(401).body("Unauthorized error");
        }
    }
}
