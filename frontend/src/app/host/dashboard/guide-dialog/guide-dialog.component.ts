import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService } from '../../../core/api.service';
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
  uploadingMedia = false;
  uploadedUrls: string[] = [];

  constructor(
    private readonly fb: FormBuilder,
    private readonly dialogRef: MatDialogRef<GuideDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public readonly data: GuideDialogData,
    private readonly api: ApiService,
    private readonly snack: MatSnackBar,
  ) {
    this.uploadedUrls = [...(this.data.guide?.photoUrls ?? [])];
    this.form = this.fb.group({
      title: [this.data.guide?.title ?? '', [Validators.required, Validators.minLength(2)]],
      slug: [this.data.guide?.slug ?? '', Validators.required],
      contentMarkdown: [this.data.guide?.contentMarkdown ?? '', Validators.required],
      contentMarkdownCs: [this.data.guide?.contentMarkdownCs ?? ''],
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

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!this.data.guide?.id) {
      this.snack.open('Save the guide first before uploading media.', 'OK', { duration: 4000 });
      return;
    }
    this.uploadingMedia = true;
    this.api.uploadGuideMedia(this.data.guide.id, file).subscribe({
      next: res => {
        this.uploadedUrls.push(res.url);
        this.uploadingMedia = false;
        this.snack.open('Media uploaded', 'OK', { duration: 2000 });
      },
      error: () => {
        this.uploadingMedia = false;
        this.snack.open('Upload failed', 'OK', { duration: 3000 });
      },
    });
    input.value = '';
  }

  removeMedia(url: string): void {
    this.uploadedUrls = this.uploadedUrls.filter(u => u !== url);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.dialogRef.close({
      propertyId: this.data.propertyId,
      photoUrls: this.uploadedUrls,
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

