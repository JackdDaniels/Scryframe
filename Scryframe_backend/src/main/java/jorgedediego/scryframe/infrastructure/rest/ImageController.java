package jorgedediego.scryframe.infrastructure.rest;

import jorgedediego.scryframe.application.dto.EvaluateResponseDTO;
import jorgedediego.scryframe.application.dto.ImageDTO;
import jorgedediego.scryframe.application.dto.TagListDTO;
import jorgedediego.scryframe.application.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/images")
@CrossOrigin(origins = "http://localhost:4200")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @GetMapping("/list")
    public List<ImageDTO> getImages(){
        return imageService.getImages();
    }

    @GetMapping("/{id}")
    public Optional<ImageDTO> getImageById(
            @PathVariable String id
    ){
        return imageService.getImageById(id) ;
    }

    /**
     * Stream the raw image bytes for the given id. Used by the frontend to
     * actually display the image in the gallery; metadata-only access goes
     * through {@code GET /images/{id}}.
     */
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getImageFile(
            @PathVariable String id
    ) throws IOException {
        Optional<Path> pathOpt = imageService.getFilePath(id);
        if (pathOpt.isEmpty() || !Files.exists(pathOpt.get())) {
            return ResponseEntity.notFound().build();
        }
        Path path = pathOpt.get();
        Resource resource = new UrlResource(path.toUri());
        String contentType = Files.probeContentType(path);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(resource);
    }

    @PostMapping("")
    public String saveImage(
            @RequestBody ImageDTO image
    ){
        return imageService.saveImage( image );
    }

    /**
     * MVP image upload + tag prediction.
     * <p>
     * Multipart form field name: {@code file}.
     * Returns the imageId the file was stored under, plus the tags
     * DeepDanbooru predicted. The frontend should let the user confirm/edit
     * the tags and then call {@code POST /images} with the chosen subset
     * (re-using the same imageId) to persist the metadata.
     */
    @PostMapping("/upload")
    public EvaluateResponseDTO uploadImage(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return imageService.uploadAndEvaluate(file);
    }

    @DeleteMapping("/{id}")
    public String deleteImage(
            @PathVariable String id
    ){
        return imageService.deleteImage( id );
    }

    @PatchMapping("/addTag")
    public String addTag(
            @RequestParam String imageId,
            @RequestParam String tagId
    ){
        return imageService.addTag( imageId, tagId);
    }

    @PatchMapping("/addTags/{imageId}")
    public String addTag(
            @PathVariable String imageId,
            @RequestBody TagListDTO tagIdList
    ){
        return imageService.addTagList( imageId, tagIdList);
    }

    @GetMapping("")
    public List<ImageDTO> searchByTag(
            @RequestParam String tag
    ){
        return imageService.searchByTag ( tag );
    }

    /**
     * Search by multiple tags using repeated query params:
     * {@code GET /images/search?tag=cat&tag=ears&tag=blaze}
     */
    @GetMapping("/search")
    public List<ImageDTO> searchByMultipleTags(
            @RequestParam("tag") List<String> tags
    ){
        return imageService.searchByMultipleTags(tags);
    }
}
