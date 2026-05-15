package jorgedediego.scryframe.infrastructure.persistence.repositories;

import jorgedediego.scryframe.application.services.GeneralUtilsMapper;
import jorgedediego.scryframe.domain.aggregates.Tag;
import jorgedediego.scryframe.domain.repository.TagRepository;
import jorgedediego.scryframe.domain.valueObjects.TagId;
import jorgedediego.scryframe.infrastructure.persistence.entities.TagEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class TagRepositoryAdapter implements TagRepository {

    private final TagJpaRepository tagJpaRepository;

    @Override
    public List<Tag> findAll() {
        return tagJpaRepository.findAll()
                .stream()
                .map(GeneralUtilsMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Tag> findById(TagId tagId) {
        return tagJpaRepository.findById( tagId.getId() )
                .map(GeneralUtilsMapper::toDomain);
    }

    @Override
    public TagEntity save(Tag tag) {
        return tagJpaRepository.save( GeneralUtilsMapper.toEntity( tag ) );
    }

    @Override
    public void delete(TagId tagId) {
        tagJpaRepository.deleteById( tagId.getId() );
    }

    @Override
    public List<Tag> findAllById(List<TagId> tagIdList) {
        return tagJpaRepository.findAllById(
                tagIdList
                        .stream()
                        .map(TagId::getId)
                        .toList() )
                .stream()
                .map(GeneralUtilsMapper::toDomain)
                .toList();
    }
}
