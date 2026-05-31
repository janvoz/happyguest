import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService } from '../../../core/api.service';
import { Booking, MinibarOrder, Property } from '../../../shared/models';

interface OrderRow extends MinibarOrder {
  guestName: string;
}

@Component({
  selector: 'app-finances-tab',
  templateUrl: './finances-tab.component.html'
})
export class FinancesTabComponent implements OnInit {
  readonly displayedColumns = ['createdAt', 'guestName', 'totalAmount', 'applicationFee', 'hostPayoutAmount', 'status'];
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

  constructor(
    private readonly apiService: ApiService,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.apiService.getProperties().subscribe((properties) => {
      this.properties = properties;
      this.selectedPropertyId = properties[0]?.id ?? '';
      this.loadOrders();
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
