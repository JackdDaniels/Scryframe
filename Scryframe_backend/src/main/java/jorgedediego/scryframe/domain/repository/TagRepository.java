package jorgedediego.scryframe.domain.repository;

import jorgedediego.scryframe.domain.aggregates.Tag;
import jorgedediego.scryframe.domain.valueObjects.TagId;
import jorgedediego.scryframe.infrastructure.persistence.entities.TagEntity;

import java.util.List;
import java.util.Optional;

public interface TagRepository {
    List<Tag> findAll();
    Optional<Tag> findById(TagId tagId);
    TagEntity save(Tag tag );
    void delete( TagId tagId);

    List<Tag> findAllById( List<TagId> tagIdList);
}
