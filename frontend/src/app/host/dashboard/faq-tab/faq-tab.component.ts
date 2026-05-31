import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, switchMap, filter } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService } from '../../../core/api.service';
import { CurrentPropertyService } from '../../../core/current-property.service';
import { HostI18nService } from '../../../core/host-i18n.service';
import { PropertyFaqItem } from '../../../shared/models';
import { FaqItemDialogComponent } from './faq-item-dialog.component';

@Component({
  selector: 'app-faq-tab',
  template: `
<div class="flex items-center justify-between mb-4">
  <h2 class="text-xl font-semibold">{{ i18n.t('faq') }}</h2>
  <button mat-raised-button color="primary" (click)="openDialog()">
    <mat-icon>add</mat-icon> {{ i18n.t('addFaq') }}
  </button>
</div>
<mat-card *ngFor="let item of faqList; let i = index" class="mb-3">
  <mat-card-content>
    <div class="flex items-start justify-between gap-2">
      <div class="flex-1">
        <p class="font-medium">{{ item.question }}</p>
        <p class="text-sm text-gray-500" *ngIf="item.questionCs">{{ item.questionCs }}</p>
        <p class="mt-1 text-gray-700">{{ item.answer }}</p>
        <p class="text-sm text-gray-500" *ngIf="item.answerCs">{{ item.answerCs }}</p>
      </div>
      <div class="flex gap-1 flex-shrink-0">
        <button mat-icon-button (click)="openDialog(item, i)" [matTooltip]="i18n.t('edit')">
          <mat-icon>edit</mat-icon>
        </button>
        <button mat-icon-button color="warn" (click)="deleteItem(i)" [matTooltip]="i18n.t('delete')">
          <mat-icon>delete</mat-icon>
        </button>
      </div>
    </div>
  </mat-card-content>
</mat-card>
<p *ngIf="faqList.length === 0" class="text-gray-400 italic">{{ i18n.t('noData') }}</p>
  `,
})
export class FaqTabComponent implements OnInit, OnDestroy {
  faqList: PropertyFaqItem[] = [];
  private propertyId = '';
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly api: ApiService,
    private readonly currentProperty: CurrentPropertyService,
    private readonly dialog: MatDialog,
    private readonly snack: MatSnackBar,
    readonly i18n: HostI18nService,
  ) {}

  ngOnInit(): void {
    this.currentProperty.selectedPropertyId$.pipe(
      takeUntil(this.destroy$),
      filter(id => !!id),
      switchMap(id => {
        this.propertyId = id;
        return this.api.getFaq(id);
      })
    ).subscribe({
      next: list => this.faqList = list ?? [],
      error: () => this.faqList = [],
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  openDialog(item?: PropertyFaqItem, index?: number): void {
    const ref = this.dialog.open(FaqItemDialogComponent, {
      width: '560px',
      data: item ? { ...item } : null,
    });
    ref.afterClosed().subscribe((result: PropertyFaqItem | undefined) => {
      if (!result) return;
      if (index !== undefined) {
        this.api.updateFaqItem(this.propertyId, index, result).subscribe({
          next: list => { this.faqList = list; this.snack.open('FAQ updated', 'OK', { duration: 2000 }); },
          error: () => this.snack.open('Error saving FAQ', 'OK', { duration: 3000 }),
        });
      } else {
        this.api.addFaqItem(this.propertyId, result).subscribe({
          next: list => { this.faqList = list; this.snack.open('FAQ added', 'OK', { duration: 2000 }); },
          error: () => this.snack.open('Error saving FAQ', 'OK', { duration: 3000 }),
        });
      }
    });
  }

  deleteItem(index: number): void {
    if (!confirm('Delete this FAQ item?')) return;
    this.api.deleteFaqItem(this.propertyId, index).subscribe({
      next: () => {
        this.faqList.splice(index, 1);
        this.snack.open('FAQ deleted', 'OK', { duration: 2000 });
      },
      error: () => this.snack.open('Error deleting FAQ', 'OK', { duration: 3000 }),
    });
  }
}
