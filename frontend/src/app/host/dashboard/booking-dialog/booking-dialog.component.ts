import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { Property } from '../../../shared/models';

interface BookingDialogData {
  properties: Property[];
  propertyId?: string;
}

@Component({
  selector: 'app-booking-dialog',
  templateUrl: './booking-dialog.component.html'
})
export class BookingDialogComponent {
  readonly form: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    private readonly dialogRef: MatDialogRef<BookingDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public readonly data: BookingDialogData
  ) {
    this.form = this.fb.group({
      propertyId: [this.data.propertyId ?? '', Validators.required],
      guestName: ['', [Validators.required, Validators.minLength(2)]],
      guestEmail: ['', [Validators.required, Validators.email]],
      checkIn: [null as Date | null, Validators.required],
      checkOut: [null as Date | null, Validators.required]
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.dialogRef.close({
      propertyId: value.propertyId,
      guestName: value.guestName,
      guestEmail: value.guestEmail,
      checkIn: value.checkIn?.toISOString(),
      checkOut: value.checkOut?.toISOString()
    });
  }
}
