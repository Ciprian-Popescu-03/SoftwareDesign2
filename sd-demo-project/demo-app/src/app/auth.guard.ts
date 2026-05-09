import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

// Removed 'state' from the parameters here:
export const authGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const userRole = sessionStorage.getItem('userRole');
  const token = sessionStorage.getItem('jwtToken');

  if (!token) {
    void router.navigate(['/login']);
    return false;
  }

  if (route.data?.['role'] && route.data['role'] !== userRole) {
    void router.navigate(['/customer']);
    return false;
  }

  return true;
};
