import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService } from '../../core/api.service';
import { IsoCountry, ISO_COUNTRIES } from '../../shared/iso-countries';
import { isoCountryCodeValidator } from '../../shared/validators/iso-country.validator';

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html'
})
export class RegistrationComponent implements OnInit {
  @Input() bookingId = '';
  @Input() propertyId = '';
  @Output() registered = new EventEmitter<void>();

  readonly countries = ISO_COUNTRIES;
  readonly form: FormGroup;
  isSubmitting = false;

  /**
   * Separate filtered country lists keyed by a field identifier so that
   * typing in one autocomplete does not affect any other open autocomplete.
   * Key pattern: "primary_citizenship", "primary_issuingCountry",
   *              "guest_<index>_citizenship", "guest_<index>_issuingCountry"
   */
  filteredCountryMap: Record<string, IsoCountry[]> = {};

  constructor(
    private readonly fb: FormBuilder,
    private readonly apiService: ApiService,
    private readonly snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      primaryGuest: this.createGuestGroup(),
      additionalGuests: this.fb.array<FormGroup>([])
    });
  }

  ngOnInit(): void {
    // Pre-populate filtered lists for the primary guest
    this.resetFilter('primary_citizenship');
    this.resetFilter('primary_issuingCountry');
  }

  get primaryGuestGroup(): FormGroup {
    return this.form.controls['primaryGuest'] as FormGroup;
  }

  get additionalGuests(): FormArray<FormGroup> {
    return this.form.controls['additionalGuests'] as FormArray<FormGroup>;
  }

  addGuest(): void {
    const index = this.additionalGuests.length;
    this.additionalGuests.push(this.createGuestGroup());
    this.resetFilter(`guest_${index}_citizenship`);
    this.resetFilter(`guest_${index}_issuingCountry`);
  }

  removeGuest(index: number): void {
    this.additionalGuests.removeAt(index);
  }

  /** Returns the filtered country list for a given field key, defaulting to full list. */
  getFiltered(key: string): IsoCountry[] {
    return this.filteredCountryMap[key] ?? this.countries;
  }

  filterCountries(key: string, query: string): void {
    const normalized = (query ?? '').toLowerCase().trim();
    this.filteredCountryMap[key] = !normalized
      ? [...this.countries]
      : this.countries.filter(
          (c) => c.name.toLowerCase().includes(normalized) || c.code.toLowerCase().includes(normalized)
        );
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const guests = [
      this.normalizeGuest(this.primaryGuestGroup),
      ...this.additionalGuests.controls.map((group) => this.normalizeGuest(group))
    ];
    this.apiService.submitGuestRegistration({
      bookingId: this.bookingId,
      propertyId: this.propertyId,
      guests
    }).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.snackBar.open('Registration submitted successfully.', 'Dismiss', { duration: 3000 });
        this.registered.emit();
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }

  private createGuestGroup(): FormGroup {
    return this.fb.group({
      fullName: ['', Validators.required],
      dateOfBirth: [null as Date | null, Validators.required],
      citizenship: ['', [Validators.required, isoCountryCodeValidator()]],
      documentType: ['PASSPORT', Validators.required],
      documentNumber: ['', Validators.required],
      documentIssuingCountry: ['', isoCountryCodeValidator()],
      documentExpiry: [null as Date | null],
      gender: [''],
      address: ['', Validators.required]
    });
  }

  private resetFilter(key: string): void {
    this.filteredCountryMap[key] = [...this.countries];
  }

  private normalizeGuest(group: FormGroup): {
    fullName: string; dateOfBirth: string; citizenship: string;
    documentType: string; documentNumber: string; documentIssuingCountry: string;
    documentExpiry: string; gender: string; address: string
  } {
    const value = group.getRawValue();
    const dobDate = value.dateOfBirth instanceof Date ? value.dateOfBirth : null;
    const expiryDate = value.documentExpiry instanceof Date ? value.documentExpiry : null;
    const formatDate = (d: Date | null): string =>
      d
        ? `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
        : '';
    // Extract bare ISO code if user selected from autocomplete (may be stored as code or display string)
    const extractCode = (v: string): string => {
      const m = v.match(/\(([A-Za-z]{2})\)\s*$/);
      return m ? m[1].toUpperCase() : v.toUpperCase().trim();
    };
    return {
      fullName: String(value.fullName ?? ''),
      dateOfBirth: formatDate(dobDate),
      citizenship: extractCode(String(value.citizenship ?? '')),
      documentType: String(value.documentType ?? 'PASSPORT'),
      documentNumber: String(value.documentNumber ?? ''),
      documentIssuingCountry: extractCode(String(value.documentIssuingCountry ?? '')),
      documentExpiry: formatDate(expiryDate),
      gender: String(value.gender ?? ''),
      address: String(value.address ?? '')
    };
  }
}
