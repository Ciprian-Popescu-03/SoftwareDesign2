import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError, from } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = sessionStorage.getItem('jwtToken');

  const authReq = token ? req.clone({
    setHeaders: { Authorization: `Bearer ${token}` }
  }) : req;

  return next(authReq).pipe(
    catchError((error) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        !req.url.includes('/person/login') &&
        !req.url.includes('/person/refresh')
      ) {
        const refreshToken = sessionStorage.getItem('refreshToken');
        if (!refreshToken) {
          sessionStorage.clear();
          void router.navigate(['/login']);
          return throwError(() => error);
        }

        // Use native fetch instead of HttpClient to avoid circular injection
        const refreshCall = fetch('http://localhost:8081/person/refresh', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken })
        }).then(res => {
          if (!res.ok) throw new Error('Refresh failed');
          return res.json();
        });

        return from(refreshCall).pipe(
          switchMap((tokens: any) => {
            sessionStorage.setItem('jwtToken', tokens.token);
            sessionStorage.setItem('refreshToken', tokens.refreshToken);
            const retryReq = req.clone({
              setHeaders: { Authorization: `Bearer ${tokens.token}` }
            });
            return next(retryReq);
          }),
          catchError((refreshError) => {
            sessionStorage.clear();
            void router.navigate(['/login']);
            return throwError(() => refreshError);
          })
        );
      }
      return throwError(() => error);
    })
  );
};
