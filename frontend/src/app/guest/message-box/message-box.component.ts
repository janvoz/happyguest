import { Component, Input } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService } from '../../core/api.service';

@Component({
  selector: 'app-message-box',
  templateUrl: './message-box.component.html'
})
export class MessageBoxComponent {
  @Input({ required: true }) propertyId = '';
  @Input({ required: true }) bookingId = '';
  @Input() guestName = 'Guest';

  readonly form: FormGroup;
  expanded = false;
  isSubmitting = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly apiService: ApiService,
    private readonly snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      content: ['', [Validators.required, Validators.minLength(5)]]
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.apiService.submitGuestMessage({
      propertyId: this.propertyId,
      bookingId: this.bookingId,
      guestName: this.guestName,
      messageType: 'ISSUE',
      content: String(this.form.getRawValue().content ?? '')
    }).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.expanded = false;
        this.form.reset({ content: '' });
        this.snackBar.open('Message sent successfully.', 'Dismiss', { duration: 3000 });
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }
}
