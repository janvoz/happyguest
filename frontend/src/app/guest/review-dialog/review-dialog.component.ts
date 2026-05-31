import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { ApiService } from '../../core/api.service';
import { Property } from '../../shared/models';

interface ReviewDialogData {
  bookingId: string;
  property: Property;
}

@Component({
  selector: 'app-review-dialog',
  templateUrl: './review-dialog.component.html'
})
export class ReviewDialogComponent {
  readonly stars = [1, 2, 3, 4, 5];
  readonly form: FormGroup;
  rating = 0;
  submitted = false;
  redirecting = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly apiService: ApiService,
    private readonly dialogRef: MatDialogRef<ReviewDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public readonly data: ReviewDialogData
  ) {
    this.form = this.fb.group({
      feedback: ['', [Validators.required, Validators.minLength(5)]]
    });
  }

  setRating(value: number): void {
    this.rating = value;
    if (value >= 4) {
      this.redirecting = true;
      setTimeout(() => {
        window.location.href = this.data.property.airbnbReviewUrl || this.data.property.bookingReviewUrl;
      }, 2000);
    }
  }

  submit(): void {
    if (this.rating < 1 || this.rating > 3 || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.apiService.submitReview({
      bookingId: this.data.bookingId,
      rating: this.rating,
      feedback: String(this.form.getRawValue().feedback ?? '')
    }).subscribe(() => {
      this.submitted = true;
      setTimeout(() => this.dialogRef.close(), 1500);
    });
  }
}
