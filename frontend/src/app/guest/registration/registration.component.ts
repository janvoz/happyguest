import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService } from '../../core/api.service';

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html'
})
export class RegistrationComponent {
  @Input() bookingId = '';
  @Input() propertyId = '';
  @Output() registered = new EventEmitter<void>();

  readonly countries = ['United States', 'United Kingdom', 'France', 'Germany', 'Spain', 'Italy', 'Croatia', 'Other'];
  readonly form: FormGroup;
  isSubmitting = false;

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

  get primaryGuestGroup(): FormGroup {
    return this.form.controls['primaryGuest'] as FormGroup;
  }

  get additionalGuests(): FormArray<FormGroup> {
    return this.form.controls['additionalGuests'] as FormArray<FormGroup>;
  }

  addGuest(): void {
    this.additionalGuests.push(this.createGuestGroup());
  }

  removeGuest(index: number): void {
    this.additionalGuests.removeAt(index);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const guests = [this.normalizeGuest(this.primaryGuestGroup), ...this.additionalGuests.controls.map((group) => this.normalizeGuest(group))];
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
      citizenship: ['United States', Validators.required],
      citizenshipOther: [''],
      documentNumber: ['', Validators.required],
      address: ['', Validators.required]
    });
  }

  private normalizeGuest(group: FormGroup): { fullName: string; dateOfBirth: string; citizenship: string; documentNumber: string; address: string } {
    const value = group.getRawValue();
    return {
      fullName: String(value.fullName ?? ''),
      dateOfBirth: value.dateOfBirth instanceof Date ? value.dateOfBirth.toISOString() : '',
      citizenship: value.citizenship === 'Other' ? String(value.citizenshipOther ?? '') : String(value.citizenship ?? ''),
      documentNumber: String(value.documentNumber ?? ''),
      address: String(value.address ?? '')
    };
  }
}
