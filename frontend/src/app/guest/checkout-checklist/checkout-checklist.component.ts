import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

import { Property } from '../../shared/models';
import { ReviewDialogComponent } from '../review-dialog/review-dialog.component';

@Component({
  selector: 'app-checkout-checklist',
  templateUrl: './checkout-checklist.component.html'
})
export class CheckoutChecklistComponent implements OnChanges {
  @Input({ required: true }) property!: Property;
  @Input() bookingId = '';

  checkedItems: boolean[] = [];

  constructor(private readonly dialog: MatDialog) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['property']?.currentValue) {
      this.checkedItems = this.property.checkoutChecklist.map(() => false);
    }
  }

  get progress(): number {
    const completed = this.checkedItems.filter(Boolean).length;
    return this.checkedItems.length ? (completed / this.checkedItems.length) * 100 : 0;
  }

  get allChecked(): boolean {
    return this.checkedItems.length > 0 && this.checkedItems.every(Boolean);
  }

  openReview(): void {
    this.dialog.open(ReviewDialogComponent, {
      width: '560px',
      data: { bookingId: this.bookingId, property: this.property }
    });
  }
}
