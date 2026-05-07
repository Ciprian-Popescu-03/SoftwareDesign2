import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http'; // <-- Imported this for the error type
import { PersonService } from '../../services/person.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  email = signal('');
  password = signal('');
  errorMessage = signal('');

  private readonly personService = inject(PersonService);
  private readonly router = inject(Router);

  onLogin() {
    this.errorMessage.set('');

    this.personService.login({ email: this.email(), password: this.password() }).subscribe({
      next: (user) => {
        localStorage.setItem('userRole', user.role || 'CUSTOMER');
        localStorage.setItem('userEmail', user.email);
        localStorage.setItem('userId', user.id);

        if (user.role === 'ADMIN') {
          void this.router.navigate(['/people']);
        } else {
          void this.router.navigate(['/customer']);
        }
      },
      error: (err: HttpErrorResponse) => {
        // 1. Explicitly tell TypeScript the shape of the backend error object
        const errorBody = err.error as { message?: string };

        // 2. Safely grab the message (TypeScript now knows this is a string or undefined)
        const finalMessage = errorBody?.message ?? 'Wrong password or email';

        // 3. Set the signal with a guaranteed string
        this.errorMessage.set(finalMessage);
      }
    });
  }
}
