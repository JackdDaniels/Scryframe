import { PredictedTag } from './predicted-tag.model';

/**
 * Mirrors backend `EvaluateResponseDTO`. Returned by `POST /images/upload`
 * after the file has been saved on disk and DeepDanbooru has evaluated it.
 *
 * The image bytes are persisted at this point but the metadata entity is
 * NOT — the frontend should let the user confirm/edit `predictedTags` and
 * then call `POST /images` (re-using `imageId`) to persist the entity.
 */
export interface EvaluateResponse {
  imageId: string;
  fileName: string;
  extension: string;
  predictedTags: PredictedTag[];
}
