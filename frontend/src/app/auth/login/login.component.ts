import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent {
  readonly form: FormGroup;
  isSubmitting = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const { email, password } = this.form.getRawValue();
    this.authService.login(String(email ?? ''), String(password ?? '')).subscribe({
      next: () => {
        this.isSubmitting = false;
        void this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.isSubmitting = false;
        this.snackBar.open('Login failed. Please check your credentials.', 'Dismiss', { duration: 4000 });
      }
    });
  }
}
