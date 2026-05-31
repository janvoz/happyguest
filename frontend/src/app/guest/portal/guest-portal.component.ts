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
  readonly labels: Record<string, { title: string; subtitle: string; bookingRef: string; lastName: string; continue: string }> = {
    en: { title: 'Enter your booking reference', subtitle: 'Use your booking reference and last name to unlock access.', bookingRef: 'Booking reference', lastName: 'Last name', continue: 'Continue' },
    cs: { title: 'Zadejte referenci rezervace', subtitle: 'Pro odemčení přístupu použijte číslo rezervace a příjmení.', bookingRef: 'Reference rezervace', lastName: 'Příjmení', continue: 'Pokračovat' },
    de: { title: 'Buchungsreferenz eingeben', subtitle: 'Nutzen Sie Referenz und Nachnamen für den Zugang.', bookingRef: 'Buchungsreferenz', lastName: 'Nachname', continue: 'Weiter' },
    pl: { title: 'Wpisz numer rezerwacji', subtitle: 'Użyj numeru rezerwacji i nazwiska, aby uzyskać dostęp.', bookingRef: 'Numer rezerwacji', lastName: 'Nazwisko', continue: 'Dalej' }
  };
  propertyId = '';
  bookingId = '';
  property: Property | null = null;
  booking: Booking | null = null;
  showRegistrationForm = false;
  isLoading = true;
  faqSearchTerm = '';
  selectedLanguage: 'en' | 'cs' | 'de' | 'pl' = 'en';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly apiService: ApiService,
    private readonly fb: FormBuilder,
    private readonly snackBar: MatSnackBar
  ) {
    this.bookingReferenceForm = this.fb.group({
      bookingReference: ['', Validators.required],
      lastName: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const language = (navigator.language || 'en').slice(0, 2).toLowerCase();
    this.selectedLanguage = ['cs', 'de', 'pl'].includes(language) ? language as 'cs' | 'de' | 'pl' : 'en';

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
    this.snackBar.open('Registration completed. Your stay details are now unlocked.', 'Dismiss', { duration: 4000 });
  }

  setLanguage(language: 'en' | 'cs' | 'de' | 'pl'): void {
    this.selectedLanguage = language;
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

  get filteredFaqList() {
    const faqs = this.property?.faqList ?? [];
    const term = this.faqSearchTerm.trim().toLowerCase();
    if (!term) {
      return faqs;
    }
    return faqs.filter((faq) =>
      faq.question.toLowerCase().includes(term) || faq.answer.toLowerCase().includes(term)
    );
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
        this.snackBar.open('Booking reference not found. Please try again.', 'Dismiss', { duration: 4000 });
      }
    });
  }
}
