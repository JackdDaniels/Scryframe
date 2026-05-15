/**
 * Mirrors backend `TagDTO`. The string `id` IS the human-readable tag
 * (e.g. "cat_ears") — TagId is intentionally string-based on the backend.
 */
export interface Tag {
  id: string;
}
