import { AfterViewInit, Component, ElementRef, Inject, ViewChild } from '@angular/core';
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
export class TemplateDialogComponent implements AfterViewInit {
  @ViewChild('sourceTextarea') sourceTextarea?: ElementRef<HTMLTextAreaElement>;
  @ViewChild('wysiwygEditor') wysiwygEditor?: ElementRef<HTMLDivElement>;

  readonly form: FormGroup;
  readonly placeholders = ['{{guest_name}}', '{{check_in_date}}', '{{check_out_date}}', '{{door_code}}', '{{portal_link}}', '{{property_name}}'];
  readonly triggerOptions: Array<{ value: 'PRE_ARRIVAL' | 'POST_DEPARTURE'; label: string }> = [
    { value: 'PRE_ARRIVAL', label: 'Pre-arrival' },
    { value: 'POST_DEPARTURE', label: 'Post-departure' }
  ];
  showSourceView = false;
  private lastSelectionRange: Range | null = null;

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

  ngAfterViewInit(): void {
    this.syncEditorFromControl();
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

  toggleSourceView(): void {
    if (this.showSourceView) {
      this.syncControlFromEditor();
      this.showSourceView = false;
      queueMicrotask(() => this.syncEditorFromControl());
      return;
    }

    this.showSourceView = true;
    queueMicrotask(() => {
      this.sourceTextarea?.nativeElement.focus();
      const current = this.sourceTextarea?.nativeElement.value ?? '';
      this.sourceTextarea?.nativeElement.setSelectionRange(current.length, current.length);
    });
  }

  appendPlaceholder(token: string): void {
    if (this.showSourceView && this.sourceTextarea) {
      this.insertIntoTextarea(this.sourceTextarea.nativeElement, token);
      return;
    }

    const editor = this.wysiwygEditor?.nativeElement;
    if (!editor) {
      this.setBodyControl(`${this.form.controls['htmlBody'].value ?? ''}${token}`);
      return;
    }

    editor.focus();
    const selection = window.getSelection();
    if (selection && this.lastSelectionRange) {
      selection.removeAllRanges();
      selection.addRange(this.lastSelectionRange);
      this.insertAtSelection(token);
      this.captureEditorSelection();
    } else {
      editor.innerHTML += token;
    }
    this.syncControlFromEditor();
  }

  onEditorInput(): void {
    this.syncControlFromEditor();
  }

  captureEditorSelection(): void {
    const editor = this.wysiwygEditor?.nativeElement;
    const selection = window.getSelection();
    if (!editor || !selection || selection.rangeCount === 0) {
      return;
    }
    const range = selection.getRangeAt(0);
    if (editor.contains(range.commonAncestorContainer)) {
      this.lastSelectionRange = range.cloneRange();
    }
  }

  private syncEditorFromControl(): void {
    if (this.showSourceView) {
      return;
    }
    const editor = this.wysiwygEditor?.nativeElement;
    if (!editor) {
      return;
    }
    editor.innerHTML = String(this.form.controls['htmlBody'].value ?? '');
  }

  private syncControlFromEditor(): void {
    const editor = this.wysiwygEditor?.nativeElement;
    if (!editor) {
      return;
    }
    this.setBodyControl(editor.innerHTML);
  }

  private setBodyControl(value: string): void {
    const control = this.form.controls['htmlBody'];
    control.setValue(value);
    control.markAsDirty();
    control.markAsTouched();
  }

  private insertIntoTextarea(textarea: HTMLTextAreaElement, token: string): void {
    const start = textarea.selectionStart ?? textarea.value.length;
    const end = textarea.selectionEnd ?? textarea.value.length;
    const current = textarea.value;
    const updated = `${current.slice(0, start)}${token}${current.slice(end)}`;
    textarea.value = updated;
    const caret = start + token.length;
    textarea.setSelectionRange(caret, caret);
    textarea.focus();
    this.setBodyControl(updated);
  }

  private insertAtSelection(token: string): void {
    const selection = window.getSelection();
    if (!selection || selection.rangeCount === 0) {
      return;
    }
    const range = selection.getRangeAt(0);
    range.deleteContents();
    const textNode = document.createTextNode(token);
    range.insertNode(textNode);
    range.setStartAfter(textNode);
    range.collapse(true);
    selection.removeAllRanges();
    selection.addRange(range);
  }
}
