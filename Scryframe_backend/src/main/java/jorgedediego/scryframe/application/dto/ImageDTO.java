package jorgedediego.scryframe.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor
public class ImageDTO {
    String id;
    String fileName;
    String extension;

    List<TagDTO> tags;
}
