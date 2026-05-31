import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { MinibarItem } from '../../../shared/models';

interface MinibarItemDialogData {
  propertyId: string;
  item?: MinibarItem;
}

@Component({
  selector: 'app-minibar-item-dialog',
  templateUrl: './minibar-item-dialog.component.html'
})
export class MinibarItemDialogComponent {
  readonly form: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    private readonly dialogRef: MatDialogRef<MinibarItemDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public readonly data: MinibarItemDialogData
  ) {
    this.form = this.fb.group({
      name: [this.data.item?.name ?? '', [Validators.required, Validators.minLength(2)]],
      price: [this.data.item?.price ?? 0, [Validators.required, Validators.min(0)]],
      stockCount: [this.data.item?.stockCount ?? 0, [Validators.required, Validators.min(0)]],
      imageUrl: [this.data.item?.imageUrl ?? '']
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.dialogRef.close({
      propertyId: this.data.propertyId,
      ...this.form.getRawValue()
    });
  }
}
