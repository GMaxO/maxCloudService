package ru.netology.mycloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.mycloud.dto.RenameFileDTO;
import ru.netology.mycloud.service.FileService;

import java.io.IOException;

@RestController
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
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
}
