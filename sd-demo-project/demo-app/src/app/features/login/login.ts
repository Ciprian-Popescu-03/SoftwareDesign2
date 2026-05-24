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
  mode = signal<'login' | 'register'>('login');

  email = signal('');
  password = signal('');
  name = signal('');
  age = signal(0);

  errorMessage = signal('');
  successMessage = signal('');

  private readonly personService = inject(PersonService);
  private readonly router = inject(Router);

  onLogin() {
    this.errorMessage.set('');

    this.personService.login({ email: this.email(), password: this.password() }).subscribe({
      next: (response: AuthResponse) => {
        sessionStorage.setItem('jwtToken', response.token);
        sessionStorage.setItem('refreshToken', response.refreshToken);
        sessionStorage.setItem('userRole', response.person.role || 'CUSTOMER');
        sessionStorage.setItem('userEmail', response.person.email || '');
        sessionStorage.setItem('userId', response.person.id?.toString() || '');

        if (response.person.role === 'ADMIN') {
          void this.router.navigate(['/people']);
        } else {
          void this.router.navigate(['/customer']);
        }
      },
      error: (err: HttpErrorResponse) => {
        const errorBody = err.error as { error?: string };
        this.errorMessage.set(errorBody?.error ?? 'Invalid email or password');
      }
    });
  }

  onRegister() {
    this.errorMessage.set('');
    this.successMessage.set('');

    this.personService.create({
      name: this.name(),
      email: this.email(),
      password: this.password(),
      age: this.age()
    }).subscribe({
      next: () => {
        this.successMessage.set('Account created! You can now sign in.');
        this.name.set('');
        this.age.set(0);
        this.password.set('');
        setTimeout(() => {
          this.mode.set('login');
          this.successMessage.set('');
        }, 2000);
      },
      error: (err: HttpErrorResponse) => {
        const errorBody = err.error as { error?: string };
        this.errorMessage.set(errorBody?.error ?? 'Registration failed. Please try again.');
      }
    });
  }
}
