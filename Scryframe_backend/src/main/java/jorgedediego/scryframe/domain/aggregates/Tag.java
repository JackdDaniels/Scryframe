package jorgedediego.scryframe.domain.aggregates;

import jorgedediego.scryframe.domain.valueObjects.TagId;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Tag {
    private final TagId tagId;
}
