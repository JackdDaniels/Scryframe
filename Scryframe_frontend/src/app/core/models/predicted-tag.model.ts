/**
 * Mirrors backend `PredictedTagDTO`. A single tag prediction returned by
 * the DeepDanbooru model with a confidence score in the range 0.0 – 1.0.
 */
export interface PredictedTag {
  tag: string;
  confidence: number;
}
