import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { Booking, Property } from '../../../shared/models';

interface BookingDialogData {
  properties: Property[];
  propertyId?: string;
  booking?: Booking;
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
    const b = this.data.booking;
    this.form = this.fb.group({
      propertyId: [b?.propertyId ?? this.data.propertyId ?? '', Validators.required],
      guestName: [b?.guestName ?? '', [Validators.required, Validators.minLength(2)]],
      guestEmail: [b?.guestEmail ?? '', [Validators.required, Validators.email]],
      checkIn: [b?.checkIn ? new Date(b.checkIn) : null as Date | null, Validators.required],
      checkOut: [b?.checkOut ? new Date(b.checkOut) : null as Date | null, Validators.required]
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
