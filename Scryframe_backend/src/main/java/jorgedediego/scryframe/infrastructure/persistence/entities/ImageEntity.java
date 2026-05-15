package jorgedediego.scryframe.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name= "images")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ImageEntity {
    @Id
    @Column(name = "image_id")
    private UUID id;

    private String fileName;
    private String fileExtension;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name="tagged_images",
            joinColumns = @JoinColumn(name="image_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    List<TagEntity> tags;
}
