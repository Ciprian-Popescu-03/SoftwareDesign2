import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { PersonService, AuthResponse } from '../../services/person.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
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
      next: (response: AuthResponse) => {
        sessionStorage.setItem('jwtToken', response.token);

        const role = response.person.role || 'CUSTOMER';
        const userEmail = response.person.email || '';
        const userId = response.person.id ? response.person.id.toString() : '';

        sessionStorage.setItem('userRole', role);
        sessionStorage.setItem('userEmail', userEmail);
        sessionStorage.setItem('userId', userId);

        if (role === 'ADMIN') {
          void this.router.navigate(['/people']);
        } else {
          void this.router.navigate(['/customer']);
        }
      },
      error: (err: HttpErrorResponse) => {
        const errorBody = err.error as { error?: string };
        const finalMessage = errorBody?.error ?? 'Invalid email or password';
        this.errorMessage.set(finalMessage);
      }
    });
  }
}
