import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatChipInputEvent } from '@angular/material/chips';

import { Property, PropertyMapMarker, PropertyQuickContact } from '../../../shared/models';

@Component({
  selector: 'app-property-dialog',
  templateUrl: './property-dialog.component.html'
})
export class PropertyDialogComponent {
  readonly separatorKeysCodes = [ENTER, COMMA] as const;
  readonly form: FormGroup;
  icalUrls: string[];
  checkoutChecklist: string[];
  mapMarkers: PropertyMapMarker[];
  quickContacts: PropertyQuickContact[];

  get basicsGroup(): FormGroup {
    return this.form.controls['basics'] as FormGroup;
  }

  get listsGroup(): FormGroup {
    return this.form.controls['lists'] as FormGroup;
  }

  constructor(
    private readonly fb: FormBuilder,
    private readonly dialogRef: MatDialogRef<PropertyDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public readonly data: Property | null
  ) {
    this.form = this.fb.group({
      basics: this.fb.group({
        name: [this.data?.name ?? '', [Validators.required, Validators.minLength(2)]],
        address: [this.data?.address ?? '', Validators.required],
        wifiName: [this.data?.wifiName ?? '', Validators.required],
        wifiPassword: [this.data?.wifiPassword ?? '', Validators.required],
        airbnbReviewUrl: [this.data?.airbnbReviewUrl ?? ''],
        bookingReviewUrl: [this.data?.bookingReviewUrl ?? '']
      }),
      lists: this.fb.group({
        faqJson: [JSON.stringify(this.data?.faqList ?? [], null, 2)]
      })
    });
    this.icalUrls = [...(this.data?.icalUrls ?? [])];
    this.checkoutChecklist = [...(this.data?.checkoutChecklist ?? [])];
    this.mapMarkers = (this.data?.mapMarkers ?? []).map((m) => ({ ...m }));
    this.quickContacts = (this.data?.quickContacts ?? []).map((c) => ({ ...c }));
  }

  addIcal(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();
    if (value) {
      this.icalUrls = [...this.icalUrls, value];
    }
    event.chipInput?.clear();
  }

  removeIcal(index: number): void {
    this.icalUrls = this.icalUrls.filter((_, i) => i !== index);
  }

  addChecklist(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();
    if (value) {
      this.checkoutChecklist = [...this.checkoutChecklist, value];
    }
    event.chipInput?.clear();
  }

  removeChecklist(index: number): void {
    this.checkoutChecklist = this.checkoutChecklist.filter((_, i) => i !== index);
  }

  addMarker(): void {
    this.mapMarkers = [
      ...this.mapMarkers,
      { title: '', category: 'Restaurants', description: '', latitude: 0, longitude: 0 }
    ];
  }

  removeMarker(index: number): void {
    this.mapMarkers = this.mapMarkers.filter((_, i) => i !== index);
  }

  addContact(): void {
    this.quickContacts = [...this.quickContacts, { label: 'Host Mobile', phone: '' }];
  }

  removeContact(index: number): void {
    this.quickContacts = this.quickContacts.filter((_, i) => i !== index);
  }

  save(): void {
    if (this.basicsGroup.invalid) {
      this.basicsGroup.markAllAsTouched();
      return;
    }

    const basics = this.basicsGroup.getRawValue();
    const lists = this.listsGroup.getRawValue();
    this.dialogRef.close({
      name: basics.name,
      address: basics.address,
      wifiName: basics.wifiName,
      wifiPassword: basics.wifiPassword,
      airbnbReviewUrl: basics.airbnbReviewUrl,
      bookingReviewUrl: basics.bookingReviewUrl,
      icalUrls: this.icalUrls,
      checkoutChecklist: this.checkoutChecklist,
      faqList: this.parseJsonArray(lists.faqJson),
      mapMarkers: this.mapMarkers.filter((m) => m.title.trim()),
      quickContacts: this.quickContacts.filter((c) => c.label && c.phone.trim())
    });
  }

  private parseJsonArray(raw: string): unknown[] {
    try {
      const parsed = JSON.parse(raw ?? '[]');
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }
}
