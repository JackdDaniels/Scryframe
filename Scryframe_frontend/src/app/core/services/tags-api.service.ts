import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Tag } from '../models/tag.model';

@Injectable({ providedIn: 'root' })
export class TagsApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/tags`;

  list(): Observable<Tag[]> {
    return this.http.get<Tag[]>(`${this.baseUrl}/list`);
  }

  getById(id: string): Observable<Tag> {
    return this.http.get<Tag>(`${this.baseUrl}/${id}`);
  }

  /** POST /tags — create a tag. The id IS the tag name (e.g. "cat_ears"). */
  save(tag: Tag): Observable<string> {
    return this.http.post(`${this.baseUrl}`, tag, { responseType: 'text' });
  }

  delete(id: string): Observable<string> {
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' });
  }
}
