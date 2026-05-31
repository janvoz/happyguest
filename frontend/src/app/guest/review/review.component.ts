import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { ApiService } from '../../core/api.service';
import { Booking, Property } from '../../shared/models';

@Component({
  selector: 'app-review',
  templateUrl: './review.component.html'
})
export class ReviewComponent implements OnInit {
  readonly stars = [1, 2, 3, 4, 5];
  readonly form: FormGroup;

  bookingId = '';
  booking: Booking | null = null;
  property: Property | null = null;
  rating = 0;
  submitted = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly apiService: ApiService,
    private readonly fb: FormBuilder
  ) {
    this.form = this.fb.group({
      feedback: ['', [Validators.required, Validators.minLength(5)]]
    });
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.bookingId = params.get('bookingId') ?? '';
      if (this.bookingId) {
        this.apiService.getPublicBooking(this.bookingId).subscribe((booking) => {
          this.booking = booking;
          this.apiService.getPublicProperty(booking.propertyId).subscribe((property) => {
            this.property = property;
          });
        });
      }
    });
  }

  setRating(value: number): void {
    this.rating = value;
    if (value >= 4 && this.property) {
      setTimeout(() => {
        window.location.href = this.property?.airbnbReviewUrl || this.property?.bookingReviewUrl || '/';
      }, 2000);
    }
  }

  submit(): void {
    if (this.rating < 1 || this.rating > 3 || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.apiService.submitReview({
      bookingId: this.bookingId,
      rating: this.rating,
      feedback: String(this.form.getRawValue().feedback ?? '')
    }).subscribe(() => {
      this.submitted = true;
    });
  }
}
