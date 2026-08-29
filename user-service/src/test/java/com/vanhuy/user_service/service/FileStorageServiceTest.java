package com.vanhuy.user_service.service;

import com.vanhuy.user_service.config.FileStorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest {
    @TempDir
    Path tempDirectory;

    @Test
    void loadFileRejectsPathOutsideUploadDirectory() throws Exception {
        Path uploadDirectory = Files.createDirectory(tempDirectory.resolve("uploads"));
        Path outsideFile = Files.writeString(tempDirectory.resolve("outside.txt"), "outside");
        FileStorageService service = createService(uploadDirectory);

        assertThrows(IllegalArgumentException.class,
                () -> service.loadFileAsResource("../outside.txt"));
        assertThrows(IllegalArgumentException.class,
                () -> service.loadFileAsResource(outsideFile.toAbsolutePath().toString()));
    }

    @Test
    void deleteFileRejectsPathOutsideUploadDirectory() throws Exception {
        Path uploadDirectory = Files.createDirectory(tempDirectory.resolve("uploads"));
        Path outsideFile = Files.writeString(tempDirectory.resolve("outside.txt"), "outside");
        FileStorageService service = createService(uploadDirectory);

        assertThrows(IllegalArgumentException.class,
                () -> service.deleteFile("../outside.txt"));
        assertTrue(Files.exists(outsideFile));
    }

    @Test
    void validFileCanBeLoadedAndDeleted() throws Exception {
        Path uploadDirectory = Files.createDirectory(tempDirectory.resolve("uploads"));
        Path storedFile = Files.writeString(uploadDirectory.resolve("profile.jpg"), "image");
        FileStorageService service = createService(uploadDirectory);

        Resource resource = service.loadFileAsResource("profile.jpg");
        assertTrue(resource.exists());

        service.deleteFile("profile.jpg");
        assertFalse(Files.exists(storedFile));
    }

    private FileStorageService createService(Path uploadDirectory) {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setUploadDir(uploadDirectory.toString());
        return new FileStorageService(properties);
    }
}
