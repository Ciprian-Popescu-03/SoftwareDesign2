import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import {PersonService} from './services/person.service';


@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './forgot-password.html',
  styleUrls: ['./forgot-password.scss']
})
export class ForgotPasswordComponent {
  email = signal('');
  code = signal('');
  newPassword = signal('');
  confirmPassword = signal('');

  // Step 1: Ask for email. Step 2: Ask for code and new password
  step = signal<1 | 2>(1);
  message = signal('');
  errorMessage = signal('');

  private personService = inject(PersonService);
  private router = inject(Router);

  onRequestCode() {
    this.errorMessage.set('');
    this.message.set('');

    this.personService.requestPasswordReset(this.email()).subscribe({
      next: () => {
        this.message.set('Code sent successfully (check your backend console)!');
        this.step.set(2);
      },
      // FIX 1: Removed 'err' completely to fix the "err is defined but never used" ESLint error
      error: () => this.errorMessage.set('Failed to send code. Make sure the email exists.')
    });
  }

  onResetPassword() {
    this.errorMessage.set('');
    this.message.set('');

    if (this.newPassword() !== this.confirmPassword()) {
      this.errorMessage.set('Passwords do not match.');
      return;
    }

    this.personService.resetPassword(this.email(), this.code(), this.newPassword()).subscribe({
      next: () => {
        this.message.set('Password reset successfully! Redirecting to login...');
        setTimeout(() => void this.router.navigate(['/login']), 2000);
      },
      // FIX 2: Safely cast the error body to fix "Unsafe member access .error on an 'any' value"
      error: (err: HttpErrorResponse) => {
        const errorBody = err.error as { error?: string };
        this.errorMessage.set(errorBody?.error || 'Invalid or expired code.');
      }
    });
  }
}
