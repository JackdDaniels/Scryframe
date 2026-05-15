package jorgedediego.scryframe.domain.valueObjects;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class TagId {
    String id;

    public TagId(String id){
        this.id = validateTagId(id);
    }

    private String validateTagId(String id){
        if( id==null || id.isBlank() )
            throw new IllegalArgumentException("ID of the Tag cannot be empty");
        if( id.contains(" "))
            throw new IllegalArgumentException("ID of the Tag cannot contain spaces");
        return id;
    }
}
