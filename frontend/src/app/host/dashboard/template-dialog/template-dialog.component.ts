import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { EmailTemplate } from '../../../shared/models';

export interface TemplateDialogData {
  propertyId: string;
  template?: EmailTemplate;
}

@Component({
  selector: 'app-template-dialog',
  templateUrl: './template-dialog.component.html'
})
export class TemplateDialogComponent {
  readonly form: FormGroup;
  readonly placeholders = ['{{guest_name}}', '{{check_in_date}}', '{{check_out_date}}', '{{door_code}}', '{{portal_link}}', '{{property_name}}'];
  readonly triggerOptions: Array<{ value: 'PRE_ARRIVAL' | 'POST_DEPARTURE'; label: string }> = [
    { value: 'PRE_ARRIVAL', label: 'Pre-arrival' },
    { value: 'POST_DEPARTURE', label: 'Post-departure' }
  ];

  constructor(
    private readonly fb: FormBuilder,
    private readonly dialogRef: MatDialogRef<TemplateDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public readonly data: TemplateDialogData
  ) {
    this.form = this.fb.group({
      triggerType: [this.data.template?.triggerType ?? 'PRE_ARRIVAL', Validators.required],
      subject: [this.data.template?.subject ?? '', [Validators.required, Validators.minLength(2)]],
      htmlBody: [this.data.template?.htmlBody ?? '', Validators.required]
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

  appendPlaceholder(token: string): void {
    const control = this.form.controls['htmlBody'];
    const existing = String(control.value ?? '');
    control.setValue(`${existing}${existing.endsWith(' ') || !existing ? '' : ' '}${token}`);
    control.markAsDirty();
  }
}
