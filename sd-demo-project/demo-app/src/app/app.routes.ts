import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/login/login').then((m) => m.LoginComponent),
  },
  {
    path: 'people',
    loadComponent: () =>
      import('./features/person-list/person-list-page.component').then((m) => m.PersonListPageComponent),
  },
  {
    path: 'customer',
    loadComponent: () =>
      import('./features/customer/customer').then((m) => m.CustomerComponent),
  },
  {
    path: 'error',
    loadComponent: () =>
      import('./features/not-found/not-found-page.component').then((m) => m.NotFoundPageComponent),
  },
  {
    path: 'products',
    loadComponent: () =>
      import('./features/product-list/product-list').then((m) => m.ProductListComponent),
  },
  {
    path: '**',
    redirectTo: 'error',
  },
];
