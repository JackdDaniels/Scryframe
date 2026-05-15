package jorgedediego.scryframe.domain.aggregates;

import jorgedediego.scryframe.domain.valueObjects.ImageId;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
public class Image {
    private final ImageId imageId;
    private String fileName;
    private String fileExtension;

    private List<Tag> tags = new ArrayList<>();

    public void addTag( Tag tag ){
        this.tags.add( tag );
    }

    public void addTagList( List<Tag> tagList ){
        this.tags.addAll( tagList );
    }

}
