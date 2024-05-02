package ru.netology.mycloud.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.mycloud.dto.RenameFileDTO;
import ru.netology.mycloud.model.FileModel;
import ru.netology.mycloud.repository.FileRepository;
import ru.netology.mycloud.security.jwt.JwtTokenProvider;
import ru.netology.mycloud.service.FileService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class FileController {
    private FileService fileService;
    private FileRepository fileRepository;
    private JwtTokenProvider jwtTokenProvider;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    public FileController(FileRepository fileRepository, JwtTokenProvider jwtTokenProvider) {
        this.fileRepository = fileRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/file")
    public ResponseEntity uploadFile(@RequestHeader("auth-token") String token, @RequestBody MultipartFile file) throws IOException {
        return fileService.saveFileResponse(file, token);
    }

    @PutMapping("/file")
    public ResponseEntity rename(@RequestHeader("auth-token") String token,
                                 @RequestParam("filename") String fileName,
                                 @RequestBody RenameFileDTO renameFile) {
        return fileService.renameFile(token, fileName, renameFile.getNewName());
    }

    @GetMapping("/file")
    public ResponseEntity getFile(@RequestHeader("auth-token") String token, @RequestParam("filename") String fileName) {
        return fileService.getResponseWithFile(token, fileName);
    }

    @DeleteMapping("/file")
    public ResponseEntity deleteFile(@RequestHeader("auth-token") String token, @RequestParam("filename") String fileName) {
        return fileService.deleteFile(token, fileName);
    }

    @GetMapping("/list")
    public ResponseEntity getFilesList(@RequestHeader("auth-token") String token) {
        return fileService.findFilesByToken(token);
    }

    public ResponseEntity findFilesByToken(String token) {
        String username = jwtTokenProvider.getUsername(token);
        if (jwtTokenProvider.validateToken(token)) {
            List<FileModel> filesList = fileRepository.findFilesByUsername(username);
            return ResponseEntity.status(200).body(filesList);
        } else {
            return ResponseEntity.status(401).body("Unauthorized error");
        }
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
