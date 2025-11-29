import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: '',
    renderMode: RenderMode.Prerender // Home page can be prerendered
  },
  {
    path: 'customers',
    renderMode: RenderMode.Client // Client-side rendering for data fetching
  },
  {
    path: 'customers/new',
    renderMode: RenderMode.Client
  },
  {
    path: 'customers/edit/:id',
    renderMode: RenderMode.Client
  },
  {
    path: 'products',
    renderMode: RenderMode.Client // Client-side rendering for data fetching
  },
  {
    path: 'products/new',
    renderMode: RenderMode.Client
  },
  {
    path: 'products/edit/:id',
    renderMode: RenderMode.Client
  },
  {
    path: 'bills',
    renderMode: RenderMode.Client // Client-side rendering for data fetching
  },
  {
    path: 'bills/new',
    renderMode: RenderMode.Client
  },
  {
    path: 'bills/:id',
    renderMode: RenderMode.Client
  },
  {
    path: '**',
    renderMode: RenderMode.Client
  }
];
