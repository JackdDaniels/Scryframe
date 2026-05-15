package jorgedediego.scryframe.infrastructure.persistence.repositories;

import jorgedediego.scryframe.infrastructure.persistence.entities.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagJpaRepository extends JpaRepository<TagEntity, String> {
}
