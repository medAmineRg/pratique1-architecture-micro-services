import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'customers/edit/:id',
    renderMode: RenderMode.Client
  },
  {
    path: 'products/edit/:id',
    renderMode: RenderMode.Client
  },
  {
    path: 'bills/:id',
    renderMode: RenderMode.Client
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
