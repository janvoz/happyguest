import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService } from '../../../core/api.service';
import { CurrentPropertyService } from '../../../core/current-property.service';
import { Booking, MinibarOrder, Property } from '../../../shared/models';

interface OrderRow extends MinibarOrder {
  guestName: string;
}

@Component({
  selector: 'app-finances-tab',
  templateUrl: './finances-tab.component.html'
})
export class FinancesTabComponent implements OnInit {
  readonly displayedColumns = ['createdAt', 'guestName', 'totalAmount', 'applicationFee', 'hostPayoutAmount', 'status', 'paymentMethod', 'variableSymbol'];
  readonly plans = [
    {
      tier: 'FREE' as const,
      price: '$0/mo',
      features: ['1 property', 'Guest portal', 'Digital logbook']
    },
    {
      tier: 'STANDARD' as const,
      price: '$9/mo',
      features: ['Unlimited properties', 'Minibar upsells', 'Automated messaging']
    },
    {
      tier: 'PRO' as const,
      price: '$29/mo',
      features: ['Custom domains', 'Advanced analytics', 'Priority support']
    }
  ];
  properties: Property[] = [];
  selectedPropertyId = '';
  orderRows: OrderRow[] = [];
  totalRevenue = 0;
  totalFees = 0;
  hostNetPayout = 0;
  isSavingBank = false;

  readonly bankForm: FormGroup;

  constructor(
    private readonly apiService: ApiService,
    private readonly currentPropertyService: CurrentPropertyService,
    private readonly snackBar: MatSnackBar,
    private readonly fb: FormBuilder
  ) {
    this.bankForm = this.fb.group({
      iban: [''],
      swift: ['']
    });
  }

  ngOnInit(): void {
    this.currentPropertyService.properties$.subscribe((properties) => {
      this.properties = properties;
    });
    this.currentPropertyService.selectedPropertyId$.subscribe((propertyId) => {
      this.selectedPropertyId = propertyId;
      this.loadOrders();
    });
    this.apiService.getProfile().subscribe((profile) => {
      this.bankForm.patchValue({ iban: profile.iban ?? '', swift: profile.swift ?? '' });
    });
  }

  loadOrders(): void {
    if (!this.selectedPropertyId) {
      this.orderRows = [];
      this.calculateSummary();
      return;
    }

    this.apiService.getOrders(this.selectedPropertyId).subscribe((orders) => {
      this.apiService.getBookings(this.selectedPropertyId).subscribe((bookings) => {
        const bookingLookup = new Map(bookings.map((booking) => [booking.id, booking]));
        this.orderRows = orders.map((order) => ({
          ...order,
          guestName: bookingLookup.get(order.bookingId)?.guestName ?? 'Guest'
        }));
        this.calculateSummary();
      });
    });
  }

  saveBankDetails(): void {
    this.isSavingBank = true;
    this.apiService.updateProfile(this.bankForm.value).subscribe({
      next: () => {
        this.isSavingBank = false;
        this.snackBar.open('Bank details saved.', 'Dismiss', { duration: 3000 });
      },
      error: () => {
        this.isSavingBank = false;
      }
    });
  }

  upgrade(tier: 'FREE' | 'STANDARD' | 'PRO'): void {
    this.apiService.subscribePlan(tier).subscribe((url) => {
      if (url) {
        window.location.href = url;
      } else {
        this.snackBar.open(`Upgrade request for ${tier} submitted.`, 'Dismiss', { duration: 3000 });
      }
    });
  }

  manageBilling(): void {
    this.apiService.getBillingPortalUrl().subscribe((url) => {
      if (url) {
        window.open(url, '_blank', 'noopener');
      }
    });
  }

  private calculateSummary(): void {
    this.totalRevenue = this.orderRows.reduce((sum, order) => sum + order.totalAmount, 0);
    this.totalFees = this.orderRows.reduce((sum, order) => sum + order.applicationFee, 0) || this.totalRevenue * 0.02;
    this.hostNetPayout = this.orderRows.reduce((sum, order) => sum + order.hostPayoutAmount, 0) || this.totalRevenue - this.totalFees;
  }
}
