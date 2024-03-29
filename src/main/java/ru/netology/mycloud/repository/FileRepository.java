package ru.netology.mycloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.mycloud.model.FileModel;
import ru.netology.mycloud.model.User;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileModel, Long> {
    List<FileModel> findFilesByUsername(String username);
    FileModel findFileByFilenameAndUser(String filename, User user);

    FileModel save(FileModel file);

    @Transactional
    Integer deleteFileModelByFilename(String filename);
}
