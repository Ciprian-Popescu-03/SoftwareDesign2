import { Routes } from '@angular/router';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./features/login/login').then(m => m.LoginComponent) },
  { path: 'forgot-password', loadComponent: () => import('./features/forgot-password/forgot-password').then(m => m.ForgotPasswordComponent) },
  { path: 'people', loadComponent: () => import('./features/person-list/person-list-page.component').then(m => m.PersonListPageComponent), canActivate: [authGuard], data: { role: 'ADMIN' } },
  { path: 'products', loadComponent: () => import('./features/product-list/product-list').then(m => m.ProductListComponent), canActivate: [authGuard] },
  { path: 'customer', loadComponent: () => import('./features/customer/customer').then(m => m.CustomerComponent), canActivate: [authGuard], data: { role: 'CUSTOMER' } },
  { path: '**', loadComponent: () => import('./features/not-found/not-found-page.component').then(m => m.NotFoundPageComponent) }
];
