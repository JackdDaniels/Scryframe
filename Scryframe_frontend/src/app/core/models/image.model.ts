import { Tag } from './tag.model';

/**
 * Mirrors backend `ImageDTO`. Used for both reading existing images and
 * persisting a new one (after the user has confirmed predicted tags).
 */
export interface Image {
  id: string;
  fileName: string;
  extension: string;
  tags: Tag[];
}
