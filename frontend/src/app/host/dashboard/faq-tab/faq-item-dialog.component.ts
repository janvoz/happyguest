import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { HostI18nService } from '../../../core/host-i18n.service';
import { PropertyFaqItem } from '../../../shared/models';

@Component({
  selector: 'app-faq-item-dialog',
  template: `
<h2 mat-dialog-title>{{ item ? i18n.t('editFaq') : i18n.t('addFaq') }}</h2>
<form [formGroup]="form" (ngSubmit)="submit()">
  <mat-dialog-content class="flex flex-col gap-3 !pt-2">
    <mat-form-field appearance="outline" class="w-full">
      <mat-label>{{ i18n.t('question') }}</mat-label>
      <input matInput formControlName="question" />
    </mat-form-field>
    <mat-form-field appearance="outline" class="w-full">
      <mat-label>{{ i18n.t('answer') }}</mat-label>
      <textarea matInput formControlName="answer" rows="3"></textarea>
    </mat-form-field>
    <mat-form-field appearance="outline" class="w-full">
      <mat-label>{{ i18n.t('questionCs') }}</mat-label>
      <input matInput formControlName="questionCs" />
    </mat-form-field>
    <mat-form-field appearance="outline" class="w-full">
      <mat-label>{{ i18n.t('answerCs') }}</mat-label>
      <textarea matInput formControlName="answerCs" rows="3"></textarea>
    </mat-form-field>
  </mat-dialog-content>
  <mat-dialog-actions align="end">
    <button mat-button type="button" [mat-dialog-close]="null">{{ i18n.t('cancel') }}</button>
    <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">{{ i18n.t('save') }}</button>
  </mat-dialog-actions>
</form>
  `,
})
export class FaqItemDialogComponent {
  form: FormGroup;
  item?: PropertyFaqItem;

  constructor(
    fb: FormBuilder,
    private readonly ref: MatDialogRef<FaqItemDialogComponent>,
    @Inject(MAT_DIALOG_DATA) data: PropertyFaqItem | null,
    readonly i18n: HostI18nService,
  ) {
    this.item = data ?? undefined;
    this.form = fb.group({
      question: [data?.question ?? '', Validators.required],
      answer: [data?.answer ?? '', Validators.required],
      questionCs: [data?.questionCs ?? ''],
      answerCs: [data?.answerCs ?? ''],
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.ref.close(this.form.value as PropertyFaqItem);
  }
}
