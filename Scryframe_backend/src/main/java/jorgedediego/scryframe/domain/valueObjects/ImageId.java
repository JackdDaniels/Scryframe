package jorgedediego.scryframe.domain.valueObjects;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Builder
@Value
public class ImageId {
    UUID id;

    public ImageId(UUID id){
        this.id = validateImageId(id);
    }

    private UUID validateImageId(UUID id){
        if( id==null ){
            throw new IllegalArgumentException("ID of the Image cannot be empty");
        }
        return id;
    }
}
