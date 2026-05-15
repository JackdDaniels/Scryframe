package jorgedediego.scryframe.application.services;

import jorgedediego.scryframe.application.dto.ImageDTO;
import jorgedediego.scryframe.application.dto.TagDTO;
import jorgedediego.scryframe.domain.aggregates.Image;
import jorgedediego.scryframe.domain.aggregates.Tag;
import jorgedediego.scryframe.domain.valueObjects.ImageId;
import jorgedediego.scryframe.domain.valueObjects.TagId;
import jorgedediego.scryframe.infrastructure.persistence.entities.ImageEntity;
import jorgedediego.scryframe.infrastructure.persistence.entities.TagEntity;

import java.util.UUID;
import java.util.stream.Collectors;

public abstract class GeneralUtilsMapper {

    // TO DTO
    public static ImageDTO toDTO (ImageEntity imageEntity){

        return ImageDTO.builder()
                .id( String.valueOf(imageEntity.getId()) )
                .fileName( imageEntity.getFileName() )
                .extension( imageEntity.getFileExtension() )
                .tags( imageEntity.getTags().stream().map(GeneralUtilsMapper::toDTO).toList() )
                .build();

    }

    public static TagDTO toDTO (TagEntity tagEntity){
        return TagDTO.builder()
                .id( tagEntity.getId() )
                .build();
    }

    public static ImageDTO toDTO (Image image){

        return ImageDTO.builder()
                .id( String.valueOf(image.getImageId().getId()) )
                .fileName( image.getFileName() )
                .extension( image.getFileExtension() )
                .tags( image.getTags().stream().map(GeneralUtilsMapper::toDTO).toList() )
                .build();

    }

    public static TagDTO toDTO (Tag tag){
        return TagDTO.builder()
                .id( tag.getTagId().getId() )
                .build();
    }

    // TO DOMAIN
    public static Image toDomain (ImageEntity imageEntity){
        return Image.builder()
                .imageId(
                        ImageId.builder()
                                .id( imageEntity.getId() )
                                .build())
                .fileName( imageEntity.getFileName() )
                .fileExtension( imageEntity.getFileExtension() )
                .tags( imageEntity.getTags().stream().map(GeneralUtilsMapper::toDomain).collect(Collectors.toList()) )
                .build();
    }

    public static Tag toDomain (TagEntity tagEntity){
        return Tag.builder()
                .tagId(
                        TagId.builder()
                                .id( tagEntity.getId() )
                                .build()
                )
                .build();
    }

    public static Image toDomain (ImageDTO imageDTO){
        return Image.builder()
                .imageId(
                        ImageId.builder()
                                .id(UUID.fromString(imageDTO.getId()))
                                .build())
                .fileName( imageDTO.getFileName() )
                .fileExtension( imageDTO.getExtension() )
                .tags( imageDTO.getTags().stream().map(GeneralUtilsMapper::toDomain).collect(Collectors.toList()) )
                .build();
    }

    public static Tag toDomain (TagDTO tagDTO){
        return Tag.builder()
                .tagId(
                        TagId.builder()
                                .id( tagDTO.getId() )
                                .build()
                )
                .build();
    }

    // TO ENTITY

    public static ImageEntity toEntity (Image image){
        return ImageEntity.builder()
                .id( image.getImageId().getId() )
                .fileName( image.getFileName() )
                .fileExtension( image.getFileExtension() )
                .tags( image.getTags().stream().map(GeneralUtilsMapper::toEntity).toList() )
                .build();
    }

    public static TagEntity toEntity (Tag tag){
        return TagEntity.builder()
                .id( tag.getTagId().getId() )
                .build();
    }
}
