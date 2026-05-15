package jorgedediego.scryframe.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * A single tag prediction returned by the DeepDanbooru model
 * along with its confidence score (0.0 - 1.0).
 */
@Value
@Builder
@AllArgsConstructor
public class PredictedTagDTO {
    String tag;
    double confidence;
}
