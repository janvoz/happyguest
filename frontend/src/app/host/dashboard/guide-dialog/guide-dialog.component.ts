import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { GuideItem } from '../../../shared/models';

interface GuideDialogData {
  propertyId: string;
  guide?: GuideItem;
}

@Component({
  selector: 'app-guide-dialog',
  templateUrl: './guide-dialog.component.html'
})
export class GuideDialogComponent {
  private slugManuallyEdited = false;
  readonly form: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    private readonly dialogRef: MatDialogRef<GuideDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public readonly data: GuideDialogData
  ) {
    this.form = this.fb.group({
      title: [this.data.guide?.title ?? '', [Validators.required, Validators.minLength(2)]],
      slug: [this.data.guide?.slug ?? '', Validators.required],
      contentMarkdown: [this.data.guide?.contentMarkdown ?? '', Validators.required],
      videoUrl: [this.data.guide?.videoUrl ?? ''],
      displayOrder: [this.data.guide?.displayOrder ?? 0, [Validators.required, Validators.min(0)]]
    });

    this.form.get('title')?.valueChanges.subscribe((title) => {
      if (!this.slugManuallyEdited) {
        this.form.get('slug')?.setValue(this.slugify(String(title ?? '')), { emitEvent: false });
      }
    });
    this.form.get('slug')?.valueChanges.subscribe(() => {
      this.slugManuallyEdited = true;
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

  private slugify(value: string): string {
    return value
      .toLowerCase()
      .trim()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
  }
}
