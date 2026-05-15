package jorgedediego.scryframe.infrastructure.persistence.repositories;

import jorgedediego.scryframe.application.services.GeneralUtilsMapper;
import jorgedediego.scryframe.domain.aggregates.Image;
import jorgedediego.scryframe.domain.aggregates.Tag;
import jorgedediego.scryframe.domain.repository.ImageRepository;
import jorgedediego.scryframe.domain.valueObjects.ImageId;
import jorgedediego.scryframe.infrastructure.persistence.entities.ImageEntity;
import jorgedediego.scryframe.infrastructure.persistence.entities.TagEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class ImageRepositoryAdapter implements ImageRepository {

    private final ImageJpaRepository imageJpaRepository;

    @Override
    public List<Image> findAll() {
        return imageJpaRepository.findAll()
                .stream()
                .map(GeneralUtilsMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Image> findById(ImageId imageId) {
        return imageJpaRepository.findById( imageId.getId() )
                .map(GeneralUtilsMapper::toDomain);
    }

    @Override
    public ImageEntity save(Image image) {
        return imageJpaRepository.save(
                GeneralUtilsMapper.toEntity( image )
        );    }

    @Override
    public void delete(ImageId imageId) {
        imageJpaRepository.deleteById(
                imageId.getId()
        );
    }

    @Override
    public List<Image> searchByTag(Tag tag) {
        return imageJpaRepository.searchAllByTagsContaining( GeneralUtilsMapper.toEntity(tag) )
                .stream()
                .map( GeneralUtilsMapper::toDomain)
                .toList();
    }

    @Override
    public List<Image> searchByMultipleTags(List<Tag> tagList) {
        List<TagEntity> tagEntityList = tagList
                .stream()
                .map( GeneralUtilsMapper::toEntity)
                .toList();
        return imageJpaRepository
                .searchImagesWithAllTags(
                        tagEntityList,
                        tagEntityList.size()
                )
                .stream()
                .map( GeneralUtilsMapper::toDomain)
                .toList();
    }

}
