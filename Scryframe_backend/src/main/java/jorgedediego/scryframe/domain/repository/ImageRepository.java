package jorgedediego.scryframe.domain.repository;

import jorgedediego.scryframe.domain.aggregates.Image;
import jorgedediego.scryframe.domain.aggregates.Tag;
import jorgedediego.scryframe.domain.valueObjects.ImageId;
import jorgedediego.scryframe.infrastructure.persistence.entities.ImageEntity;

import java.util.List;
import java.util.Optional;

public interface ImageRepository {
    List<Image> findAll();
    Optional<Image> findById(ImageId imageId);
    ImageEntity save(Image image );
    void delete( ImageId imageId);

    List<Image> searchByTag(Tag tag);
    List<Image> searchByMultipleTags(List<Tag> tagList);
}
