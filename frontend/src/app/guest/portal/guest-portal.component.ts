import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService } from '../../core/api.service';
import { Booking, Property } from '../../shared/models';

@Component({
  selector: 'app-guest-portal',
  templateUrl: './guest-portal.component.html'
})
export class GuestPortalComponent implements OnInit {
  readonly bookingReferenceForm: FormGroup;
  propertyId = '';
  bookingId = '';
  property: Property | null = null;
  booking: Booking | null = null;
  showRegistrationForm = false;
  isLoading = true;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly apiService: ApiService,
    private readonly fb: FormBuilder,
    private readonly snackBar: MatSnackBar
  ) {
    this.bookingReferenceForm = this.fb.group({
      bookingReference: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.propertyId = params.get('propertyId') ?? '';
      this.loadProperty();
    });

    this.route.queryParamMap.subscribe((params) => {
      this.bookingId = params.get('bookingId') ?? '';
      if (this.bookingId) {
        this.loadBooking(this.bookingId);
      } else {
        this.isLoading = false;
      }
    });
  }

  submitBookingReference(): void {
    if (this.bookingReferenceForm.invalid) {
      this.bookingReferenceForm.markAllAsTouched();
      return;
    }

    const reference = String(this.bookingReferenceForm.getRawValue().bookingReference ?? '');
    this.loadBooking(reference, true);
  }

  revealRegistration(): void {
    this.showRegistrationForm = true;
  }

  handleRegistered(): void {
    this.showRegistrationForm = false;
    if (this.booking) {
      this.booking = { ...this.booking, isRegistrationCompleted: true };
    }
    this.snackBar.open('Registration completed. Your stay details are now unlocked.', 'Dismiss', { duration: 4000 });
  }

  private loadProperty(): void {
    if (!this.propertyId) {
      return;
    }

    this.apiService.getPublicProperty(this.propertyId).subscribe({
      next: (property) => {
        this.property = property;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  private loadBooking(reference: string, byReference = false): void {
    this.isLoading = true;
    const request = byReference
      ? this.apiService.getBookingByReference(this.propertyId, reference)
      : this.apiService.getPublicBooking(reference);

    request.subscribe({
      next: (booking) => {
        this.booking = booking;
        this.bookingId = booking.id;
        this.isLoading = false;
      },
      error: () => {
        this.booking = null;
        this.isLoading = false;
        this.snackBar.open('Booking reference not found. Please try again.', 'Dismiss', { duration: 4000 });
      }
    });
  }
}
