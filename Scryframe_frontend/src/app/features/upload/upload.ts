import { Component, computed, inject, signal } from '@angular/core';

import { ImagesApiService } from '../../core/services/images-api.service';
import { EvaluateResponse } from '../../core/models/evaluate-response.model';
import { Image } from '../../core/models/image.model';
import { PredictedTag } from '../../core/models/predicted-tag.model';

/**
 * Tags whose confidence is at least this value are pre-checked in the
 * confirmation list. Tweak as needed once we see how DeepDanbooru scores
 * real images.
 */
const DEFAULT_CONFIDENCE_THRESHOLD = 0.75;

type Status = 'idle' | 'selected' | 'analyzing' | 'tagging' | 'saving' | 'saved' | 'error';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.html',
  styleUrl: './upload.scss',
})
export class Upload {
  private readonly imagesApi = inject(ImagesApiService);

  // --- selection / preview state ---
  protected readonly file = signal<File | null>(null);
  protected readonly previewUrl = signal<string | null>(null);

  // --- workflow state ---
  protected readonly status = signal<Status>('idle');
  protected readonly errorMessage = signal<string | null>(null);

  // --- evaluate / tag confirmation state ---
  protected readonly evaluateResponse = signal<EvaluateResponse | null>(null);
  protected readonly selectedTags = signal<Set<string>>(new Set());

  /** Tags from the model, sorted by confidence descending for nicer review. */
  protected readonly sortedPredictions = computed<PredictedTag[]>(() => {
    const r = this.evaluateResponse();
    if (!r) return [];
    return [...r.predictedTags].sort((a, b) => b.confidence - a.confidence);
  });

  protected readonly selectedCount = computed(() => this.selectedTags().size);
  protected readonly hasFile = computed(() => this.file() !== null);

  // --- Step 1: file selection ---

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const f = input.files?.[0] ?? null;
    this.resetPreview();
    if (!f) {
      this.file.set(null);
      this.status.set('idle');
      return;
    }
    this.file.set(f);
    this.previewUrl.set(URL.createObjectURL(f));
    this.evaluateResponse.set(null);
    this.selectedTags.set(new Set());
    this.errorMessage.set(null);
    this.status.set('selected');
  }

  // --- Step 2: upload + analyze ---

  onUpload(): void {
    const f = this.file();
    if (!f) return;
    this.status.set('analyzing');
    this.errorMessage.set(null);

    this.imagesApi.upload(f).subscribe({
      next: (response) => {
        this.evaluateResponse.set(response);
        // Pre-select tags above the threshold so the common case is one click.
        const preselected = new Set(
          response.predictedTags
            .filter((t) => t.confidence >= DEFAULT_CONFIDENCE_THRESHOLD)
            .map((t) => t.tag),
        );
        this.selectedTags.set(preselected);
        this.status.set('tagging');
      },
      error: (err) => {
        console.error('Upload failed', err);
        this.errorMessage.set(this.formatError(err, 'Upload failed.'));
        this.status.set('error');
      },
    });
  }

  // --- Step 3: tag confirmation ---

  isSelected(tag: string): boolean {
    return this.selectedTags().has(tag);
  }

  toggleTag(tag: string): void {
    const next = new Set(this.selectedTags());
    if (next.has(tag)) {
      next.delete(tag);
    } else {
      next.add(tag);
    }
    this.selectedTags.set(next);
  }

  selectAll(): void {
    const all = new Set(this.sortedPredictions().map((t) => t.tag));
    this.selectedTags.set(all);
  }

  selectNone(): void {
    this.selectedTags.set(new Set());
  }

  // --- Step 4: persist ---

  onSave(): void {
    const response = this.evaluateResponse();
    if (!response) return;
    this.status.set('saving');
    this.errorMessage.set(null);

    const image: Image = {
      id: response.imageId,
      fileName: response.fileName,
      extension: response.extension,
      tags: [...this.selectedTags()].map((id) => ({ id })),
    };

    this.imagesApi.save(image).subscribe({
      next: () => {
        this.status.set('saved');
      },
      error: (err) => {
        console.error('Save failed', err);
        this.errorMessage.set(this.formatError(err, 'Save failed.'));
        this.status.set('error');
      },
    });
  }

  // --- Reset for another upload ---

  reset(): void {
    this.resetPreview();
    this.file.set(null);
    this.evaluateResponse.set(null);
    this.selectedTags.set(new Set());
    this.errorMessage.set(null);
    this.status.set('idle');
  }

  private resetPreview(): void {
    const current = this.previewUrl();
    if (current) {
      URL.revokeObjectURL(current);
    }
    this.previewUrl.set(null);
  }

  private formatError(err: unknown, fallback: string): string {
    if (err && typeof err === 'object' && 'message' in err) {
      const msg = (err as { message?: string }).message;
      return msg ? `${fallback} ${msg}` : fallback;
    }
    return fallback;
  }

  // Format a confidence as a percent, e.g. 0.876 → "87.6%".
  protected formatConfidence(confidence: number): string {
    return `${(confidence * 100).toFixed(1)}%`;
  }
}
