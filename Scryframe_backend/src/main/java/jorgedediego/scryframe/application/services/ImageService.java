package jorgedediego.scryframe.application.services;

import jorgedediego.scryframe.application.dto.EvaluateResponseDTO;
import jorgedediego.scryframe.application.dto.ImageDTO;
import jorgedediego.scryframe.application.dto.PredictedTagDTO;
import jorgedediego.scryframe.application.dto.TagListDTO;
import jorgedediego.scryframe.domain.aggregates.Image;
import jorgedediego.scryframe.domain.aggregates.Tag;
import jorgedediego.scryframe.domain.repository.ImageRepository;
import jorgedediego.scryframe.domain.repository.TagRepository;
import jorgedediego.scryframe.domain.valueObjects.ImageId;
import jorgedediego.scryframe.domain.valueObjects.TagId;
import jorgedediego.scryframe.infrastructure.persistence.entities.ImageEntity;
import jorgedediego.scryframe.infrastructure.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageService {

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    DeepDanbooruService deepDanbooruService;

    @Autowired
    FileStorageService fileStorageService;

    // GET list of all images
    public List<ImageDTO> getImages(){
        return imageRepository.findAll()
                .stream()
                .map(GeneralUtilsMapper::toDTO)
                .toList();
    }

    //GET By Id
    public Optional<ImageDTO> getImageById(String id){
        return imageRepository.findById(ImageId.builder().id(UUID.fromString(id)).build())
                .map(GeneralUtilsMapper::toDTO);
    }

    /**
     * Resolve where the image bytes for {@code id} live on disk, based on the
     * extension stored with the entity. Returns empty if no entity with that
     * id exists. The caller is responsible for checking that the file still
     * exists on disk before reading it.
     */
    public Optional<Path> getFilePath(String id){
        return imageRepository.findById(ImageId.builder().id(UUID.fromString(id)).build())
                .map(image -> fileStorageService.pathFor(id, image.getFileExtension()));
    }

    public String saveImage(ImageDTO imageDTO) {
        if (imageDTO.getId() == null || imageDTO.getId().isBlank())
            imageDTO = ImageDTO.builder()
                    .id( String.valueOf(UUID.randomUUID()) )
                    .fileName( imageDTO.getFileName() )
                    .extension( imageDTO.getExtension() )
                    .tags( imageDTO.getTags() )
                    .build();

        ImageEntity savedImage = imageRepository.save(
                GeneralUtilsMapper.toDomain(imageDTO)
        );
        return "Image saved with id: " + savedImage.getId().toString();
    }

    /**
     * MVP upload flow:
     * <ol>
     *   <li>Generate a fresh imageId.</li>
     *   <li>Save the uploaded bytes to disk as {@code <imageId>.<ext>}.</li>
     *   <li>Run DeepDanbooru on the saved file.</li>
     *   <li>Return the predicted tags so the user can confirm them.</li>
     * </ol>
     * The {@link ImageEntity} itself is NOT persisted here — the caller is
     * expected to follow up with {@code POST /images} once the user confirms.
     */
    public EvaluateResponseDTO uploadAndEvaluate(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        String imageId = UUID.randomUUID().toString();
        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);

        Path savedPath = fileStorageService.save(file, imageId, extension);
        List<PredictedTagDTO> predictions = deepDanbooruService.evaluate(savedPath);

        return EvaluateResponseDTO.builder()
                .imageId(imageId)
                .fileName(originalName)
                .extension(extension)
                .predictedTags(predictions)
                .build();
    }

    private static String extractExtension(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Uploaded file has no name");
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            throw new IllegalArgumentException("Uploaded file has no extension: " + filename);
        }
        return filename.substring(dot + 1).toLowerCase();
    }

    // DELETE By Id
    public String deleteImage( String id ){
        ImageId imageId = ImageId.builder().id( UUID.fromString( id ) ).build();
        if(
                imageRepository.findById( imageId ).isPresent()
        ){
            imageRepository.delete( imageId );
            return "Image with id " + id + " succesfully deleted.";
        }
        return "Image with id " + id + " was not found.";
    }

    // PATCH : ADD TAG
    public String addTag(String imageId, String tagId) {
        Optional<Image> image = imageRepository.findById(ImageId.builder().id(UUID.fromString(imageId)).build());
        if (image.isEmpty())
            return "Image with id " + imageId + " not found";

        Optional<Tag> tag = tagRepository.findById(TagId.builder().id( tagId ).build());
        if (tag.isEmpty())
            return "Tag with id " + tagId + " not found";

        image.get().addTag( tag.get() );

        ImageEntity updatedImage =
                imageRepository.save( image.get() );

        return "Added tag: " + updatedImage.getTags().getLast().getId() + " to Image with id: " + updatedImage.getId().toString();
    }

    // PATCH : ADD TAG LIST, BULK UPDATE
    public String addTagList(String imageId, TagListDTO tagIdList) {
        Optional<Image> image = imageRepository.findById(ImageId.builder().id(UUID.fromString(imageId)).build());
        if (image.isEmpty())
            return "Image with id " + imageId + " not found";

        List<Tag> tagList = tagRepository.findAllById(
                        tagIdList.getTagList()
                                .stream()
                                .map(GeneralUtilsMapper::toDomain)
                                .map( Tag::getTagId )
                                .toList() );

        int currentTagsSize = image.get().getTags().size();
        image.get().addTagList( tagList );

        ImageEntity updatedImage =
                imageRepository.save( image.get() );

        int tagsAdded = updatedImage.getTags().size() - currentTagsSize;
        return "Added " + tagsAdded + " tag/s to Image with id: " + updatedImage.getId().toString();
    }

    //SEARCH BY TAG
    public List<ImageDTO> searchByTag( String tagId ){

        Optional<Tag> tag = tagRepository.findById(TagId.builder().id( tagId ).build());
        if (tag.isEmpty())
            throw new IllegalArgumentException("Tag with id " + tagId + " not found");

        return imageRepository.searchByTag( tag.get() )
                .stream()
                .map(GeneralUtilsMapper::toDTO)
                .toList();
    }

    public List<ImageDTO> searchByMultipleTags( List<String> tagIds ){
        List<Tag> tagList = tagRepository.findAllById(
                tagIds.stream()
                        .map(id -> TagId.builder().id(id).build())
                        .toList() );

        return imageRepository.searchByMultipleTags( tagList )
                .stream()
                .map(GeneralUtilsMapper::toDTO)
                .toList();
    }
}
