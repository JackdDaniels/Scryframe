package jorgedediego.scryframe.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name= "tags")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TagEntity {
    @Id
    @Column(name = "tag_id")
    private String id;

    @ManyToMany(mappedBy = "tags")
    List<ImageEntity> images;
}
