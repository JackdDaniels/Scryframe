package jorgedediego.scryframe.infrastructure.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Persists uploaded image bytes to the local filesystem under
 * {@code scryframe.storage.path}. Files are named {@code <imageId>.<ext>} so
 * that the {@code imageId} alone is enough to locate the file later.
 */
@Service
public class FileStorageService {

    @Value("${scryframe.storage.path}")
    private String storagePath;

    private Path storageRoot;

    @PostConstruct
    void init() throws IOException {
        this.storageRoot = Path.of(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(this.storageRoot);
    }

    /**
     * Save the given multipart file to disk as {@code <imageId>.<extension>}.
     *
     * @param file       the uploaded file
     * @param imageId    UUID-style identifier the file will be stored under
     * @param extension  file extension WITHOUT the leading dot (e.g. "jpg")
     * @return absolute path to the saved file
     */
    public Path save(MultipartFile file, String imageId, String extension) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        Path destination = storageRoot.resolve(imageId + "." + extension);
        try (var in = file.getInputStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        return destination;
    }

    /**
     * Resolve where an image WOULD live on disk given its id and extension.
     * Does not check that the file exists.
     */
    public Path pathFor(String imageId, String extension) {
        return storageRoot.resolve(imageId + "." + extension);
    }
}
