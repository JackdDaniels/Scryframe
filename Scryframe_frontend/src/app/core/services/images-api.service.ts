import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { EvaluateResponse } from '../models/evaluate-response.model';
import { Image } from '../models/image.model';
import { Tag } from '../models/tag.model';

/**
 * Backend payload for endpoints that take a list of tags (e.g. addTags).
 * Mirrors backend `TagListDTO`.
 */
interface TagListPayload {
  tagList: Tag[];
}

@Injectable({ providedIn: 'root' })
export class ImagesApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/images`;

  /**
   * MVP step 1: upload an image file to the backend, which saves it to disk
   * and runs DeepDanbooru on it. Returns the predicted tags + the imageId
   * the file was stored under.
   */
  upload(file: File): Observable<EvaluateResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<EvaluateResponse>(`${this.baseUrl}/upload`, formData);
  }

  /**
   * MVP step 2: persist the image entity with the user-confirmed tags.
   * The `id` here should be the `imageId` returned by `upload()` so the
   * stored file and the entity stay linked.
   */
  save(image: Image): Observable<string> {
    return this.http.post(`${this.baseUrl}`, image, { responseType: 'text' });
  }

  list(): Observable<Image[]> {
    return this.http.get<Image[]>(`${this.baseUrl}/list`);
  }

  getById(id: string): Observable<Image> {
    return this.http.get<Image>(`${this.baseUrl}/${id}`);
  }

  /**
   * URL the browser can use directly in <img src="..."> to fetch the actual
   * image bytes. The backend serves them via GET /images/{id}/file.
   */
  imageFileUrl(id: string): string {
    return `${this.baseUrl}/${id}/file`;
  }

  delete(id: string): Observable<string> {
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' });
  }

  /** PATCH /images/addTag?imageId=...&tagId=... — append a single tag. */
  addTag(imageId: string, tagId: string): Observable<string> {
    const params = new HttpParams().set('imageId', imageId).set('tagId', tagId);
    return this.http.patch(`${this.baseUrl}/addTag`, null, {
      params,
      responseType: 'text',
    });
  }

  /** PATCH /images/addTags/{imageId} — bulk append. */
  addTags(imageId: string, tagIds: string[]): Observable<string> {
    const body: TagListPayload = { tagList: tagIds.map((id) => ({ id })) };
    return this.http.patch(`${this.baseUrl}/addTags/${imageId}`, body, {
      responseType: 'text',
    });
  }

  /** GET /images?tag=... — search by a single tag. */
  searchByTag(tag: string): Observable<Image[]> {
    const params = new HttpParams().set('tag', tag);
    return this.http.get<Image[]>(`${this.baseUrl}`, { params });
  }

  /**
   * GET /images/search?tag=cat&tag=ears&tag=blaze — search by multiple tags
   * (intersection). Uses repeated query params; HttpParams handles arrays
   * by emitting one entry per value.
   */
  searchByTags(tags: string[]): Observable<Image[]> {
    let params = new HttpParams();
    for (const t of tags) {
      params = params.append('tag', t);
    }
    return this.http.get<Image[]>(`${this.baseUrl}/search`, { params });
  }
}
