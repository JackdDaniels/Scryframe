package jorgedediego.scryframe.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Response returned after an image is uploaded and evaluated by DeepDanbooru.
 * <p>
 * The image bytes have already been persisted on disk at this point, but the
 * {@link jorgedediego.scryframe.infrastructure.persistence.entities.ImageEntity}
 * is NOT yet persisted — it will be created by a follow-up
 * {@code POST /images} call once the user confirms the chosen tags.
 */
@Value
@Builder
@AllArgsConstructor
public class EvaluateResponseDTO {
    String imageId;
    String fileName;
    String extension;
    List<PredictedTagDTO> predictedTags;
}
