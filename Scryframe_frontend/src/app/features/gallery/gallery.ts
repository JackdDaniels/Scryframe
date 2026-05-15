import { Component, computed, inject, signal } from '@angular/core';

import { ImagesApiService } from '../../core/services/images-api.service';
import { Image } from '../../core/models/image.model';

/** Number of images per page in the grid (4 columns × 4 rows). */
const PAGE_SIZE = 16;

@Component({
  selector: 'app-gallery',
  templateUrl: './gallery.html',
  styleUrl: './gallery.scss',
})
export class Gallery {
  private readonly imagesApi = inject(ImagesApiService);

  /** Tags currently used to filter results. Empty = show everything. */
  protected readonly searchTags = signal<string[]>([]);
  /** What the user is typing into the tag input (not yet committed). */
  protected readonly tagInput = signal<string>('');

  /** Results from the most recent backend call. */
  protected readonly images = signal<Image[]>([]);
  protected readonly loading = signal<boolean>(false);
  protected readonly errorMessage = signal<string | null>(null);

  /** Zero-indexed current page. */
  protected readonly currentPage = signal<number>(0);

  protected readonly totalPages = computed(() =>
    Math.max(1, Math.ceil(this.images().length / PAGE_SIZE)),
  );

  protected readonly pagedImages = computed<Image[]>(() => {
    const start = this.currentPage() * PAGE_SIZE;
    return this.images().slice(start, start + PAGE_SIZE);
  });

  /** Human-readable "1–16 of 42" string for the pagination footer. */
  protected readonly pageInfo = computed(() => {
    const total = this.images().length;
    if (total === 0) return '0 results';
    const start = this.currentPage() * PAGE_SIZE + 1;
    const end = Math.min(start + PAGE_SIZE - 1, total);
    return `${start}–${end} of ${total}`;
  });

  protected readonly pageSize = PAGE_SIZE;

  constructor() {
    // Load the unfiltered catalog on first render.
    this.loadImages();
  }

  // --- Tag chip management ---

  onTagInputChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.tagInput.set(value);
  }

  onTagInputKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.commitTag();
    } else if (event.key === 'Backspace' && this.tagInput() === '') {
      // Remove the last chip when backspacing in an empty input.
      const tags = this.searchTags();
      if (tags.length > 0) {
        this.searchTags.set(tags.slice(0, -1));
        this.loadImages();
      }
    }
  }

  /** Commit whatever's in the input box as a chip, if it's a valid tag. */
  commitTag(): void {
    const raw = this.tagInput().trim();
    if (raw === '') return;
    // Backend rejects tag ids with spaces; reject early to give clear feedback.
    if (/\s/.test(raw)) {
      this.errorMessage.set('Tags cannot contain spaces.');
      return;
    }
    const current = this.searchTags();
    if (current.includes(raw)) {
      // Already filtering by this tag — just clear the input.
      this.tagInput.set('');
      return;
    }
    this.searchTags.set([...current, raw]);
    this.tagInput.set('');
    this.errorMessage.set(null);
    this.loadImages();
  }

  removeTag(tag: string): void {
    this.searchTags.set(this.searchTags().filter((t) => t !== tag));
    this.loadImages();
  }

  clearAllTags(): void {
    if (this.searchTags().length === 0) return;
    this.searchTags.set([]);
    this.loadImages();
  }

  refresh(): void {
    this.loadImages();
  }

  // --- Data loading ---

  private loadImages(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.currentPage.set(0);

    const tags = this.searchTags();
    const request$ =
      tags.length === 0 ? this.imagesApi.list() : this.imagesApi.searchByTags(tags);

    request$.subscribe({
      next: (results) => {
        this.images.set(results ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Gallery load failed', err);
        this.images.set([]);
        this.loading.set(false);
        this.errorMessage.set(this.formatError(err, 'Failed to load images.'));
      },
    });
  }

  // --- Pagination ---

  goToPage(page: number): void {
    const clamped = Math.max(0, Math.min(page, this.totalPages() - 1));
    this.currentPage.set(clamped);
  }

  nextPage(): void {
    this.goToPage(this.currentPage() + 1);
  }

  prevPage(): void {
    this.goToPage(this.currentPage() - 1);
  }

  // --- Helpers used in the template ---

  imageUrl(image: Image): string {
    return this.imagesApi.imageFileUrl(image.id);
  }

  trackById(_index: number, image: Image): string {
    return image.id;
  }

  private formatError(err: unknown, fallback: string): string {
    if (err && typeof err === 'object' && 'message' in err) {
      const msg = (err as { message?: string }).message;
      return msg ? `${fallback} ${msg}` : fallback;
    }
    return fallback;
  }
}
