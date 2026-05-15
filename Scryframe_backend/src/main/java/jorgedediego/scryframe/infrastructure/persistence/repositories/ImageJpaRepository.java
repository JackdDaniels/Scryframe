package jorgedediego.scryframe.infrastructure.persistence.repositories;

import jorgedediego.scryframe.infrastructure.persistence.entities.ImageEntity;
import jorgedediego.scryframe.infrastructure.persistence.entities.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImageJpaRepository extends JpaRepository<ImageEntity, UUID> {
    List<ImageEntity> searchAllByTagsContaining(TagEntity tagEntity);

    @Query("""
    SELECT i
    FROM ImageEntity i
    JOIN i.tags t
    WHERE t IN :tags
    GROUP BY i
    HAVING COUNT(DISTINCT t) = :tagCount
""")
    List<ImageEntity> searchImagesWithAllTags(
            @Param("tags") List<TagEntity> tags,
            @Param("tagCount") long tagCount
    );

}
