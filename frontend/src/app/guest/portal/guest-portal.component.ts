import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService } from '../../core/api.service';
import { Booking, Property } from '../../shared/models';
import { GuestI18nService, GuestLang } from '../../core/guest-i18n.service';

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
  faqSearchTerm = '';
  selectedLanguage: GuestLang = 'en';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly apiService: ApiService,
    private readonly fb: FormBuilder,
    private readonly snackBar: MatSnackBar,
    readonly i18n: GuestI18nService
  ) {
    this.bookingReferenceForm = this.fb.group({
      bookingReference: ['', Validators.required],
      lastName: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.selectedLanguage = this.i18n.initializeFromBrowserLanguage();

    this.route.paramMap.subscribe((params) => {
      this.propertyId = params.get('propertyId') ?? '';
      this.loadProperty();
      this.handleAccessQuery(this.route.snapshot.queryParamMap);
    });

    this.route.queryParamMap.subscribe((params) => {
      this.handleAccessQuery(params);
    });
  }

  private handleAccessQuery(params: ParamMap): void {
    if (!this.propertyId) {
      return;
    }
    const token = params.get('token') ?? '';
    this.bookingId = params.get('bookingId') ?? '';
    if (token) {
      this.loadBookingByToken(token);
    } else if (this.bookingId) {
      this.loadBooking(this.bookingId);
    } else {
      this.isLoading = false;
    }
  }

  submitBookingReference(): void {
    if (this.bookingReferenceForm.invalid) {
      this.bookingReferenceForm.markAllAsTouched();
      return;
    }

    const reference = String(this.bookingReferenceForm.getRawValue().bookingReference ?? '');
    const lastName = String(this.bookingReferenceForm.getRawValue().lastName ?? '');
    this.loadBooking(reference, true, lastName);
  }

  revealRegistration(): void {
    this.showRegistrationForm = true;
  }

  handleRegistered(): void {
    this.showRegistrationForm = false;
    if (this.booking) {
      this.booking = { ...this.booking, isRegistrationCompleted: true };
    }
    this.snackBar.open(this.i18n.t('registrationDone'), 'Dismiss', { duration: 4000 });
  }

  setLanguage(language: GuestLang): void {
    this.selectedLanguage = language;
    this.i18n.setLanguage(language);
    this.loadProperty();
  }

  /** Returns a Material icon name appropriate for the contact label. */
  getContactIcon(label: string): string {
    const lower = (label ?? '').toLowerCase();
    if (lower.includes('police') || lower.includes('cop') || lower.includes('security')) return 'local_police';
    if (lower.includes('fire') || lower.includes('brigade')) return 'local_fire_department';
    if (lower.includes('medical') || lower.includes('ambulance') || lower.includes('hospital') || lower.includes('doctor')) return 'local_hospital';
    if (lower.includes('mobile') || lower.includes('host') || lower.includes('owner') || lower.includes('landlord')) return 'phone_iphone';
    if (lower.includes('maintenance') || lower.includes('repair') || lower.includes('technician')) return 'build';
    return 'phone';
  }

  format(text: string): string {
    if (text.includes('{{name}}') && this.booking?.guestName) {
      return text.replace('{{name}}', this.booking.guestName);
    }
    return text;
  }

  private loadProperty(): void {
    if (!this.propertyId) {
      return;
    }

    this.apiService.getPublicProperty(this.propertyId, this.selectedLanguage).subscribe({
      next: (property) => {
        this.property = property;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  private loadBooking(reference: string, byReference = false, lastName = ''): void {
    this.isLoading = true;
    const request = byReference
      ? this.apiService.getBookingByGate(this.propertyId, reference, lastName)
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
        this.snackBar.open(this.i18n.t('bookingNotFound'), 'Dismiss', { duration: 4000 });
      }
    });
  }

  private loadBookingByToken(token: string): void {
    this.isLoading = true;
    this.apiService.getBookingByAccessToken(this.propertyId, token).subscribe({
      next: (booking) => {
        this.booking = booking;
        this.bookingId = booking.id;
        this.isLoading = false;
      },
      error: () => {
        this.booking = null;
        this.bookingId = '';
        this.isLoading = false;
      }
    });
  }
}
