import { Routes } from '@angular/router';

import { Upload } from './features/upload/upload';
import { Gallery } from './features/gallery/gallery';

export const routes: Routes = [
  { path: '', redirectTo: 'upload', pathMatch: 'full' },
  { path: 'upload', component: Upload, title: 'Upload — Scryframe' },
  { path: 'gallery', component: Gallery, title: 'Gallery — Scryframe' },
  { path: '**', redirectTo: 'upload' },
];
