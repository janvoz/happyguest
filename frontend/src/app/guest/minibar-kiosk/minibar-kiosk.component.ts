import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService } from '../../core/api.service';
import { MinibarItem } from '../../shared/models';

@Component({
  selector: 'app-minibar-kiosk',
  templateUrl: './minibar-kiosk.component.html'
})
export class MinibarKioskComponent implements OnChanges {
  @Input({ required: true }) propertyId = '';
  @Input({ required: true }) bookingId = '';

  items: MinibarItem[] = [];
  quantities: Record<string, number> = {};
  showCheckout = false;
  paymentMessage = '';
  isProcessing = false;
  paymentMethod: 'STRIPE' | 'QR_BANK' = 'STRIPE';
  spaydPayload = '';

  constructor(
    private readonly apiService: ApiService,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['propertyId']?.currentValue) {
      this.apiService.getPublicMinibarItems(this.propertyId).subscribe((items) => {
        this.items = items;
      });
    }
  }

  changeQuantity(itemId: string, delta: number): void {
    const nextValue = Math.max((this.quantities[itemId] ?? 0) + delta, 0);
    this.quantities[itemId] = nextValue;
  }

  get cartItems(): Array<{ item: MinibarItem; quantity: number }> {
    return this.items
      .map((item) => ({ item, quantity: this.quantities[item.id] ?? 0 }))
      .filter(({ quantity }) => quantity > 0);
  }

  get cartCount(): number {
    return this.cartItems.reduce((sum, entry) => sum + entry.quantity, 0);
  }

  get totalAmount(): number {
    return this.cartItems.reduce((sum, entry) => sum + entry.item.price * entry.quantity, 0);
  }

  checkout(): void {
    this.isProcessing = true;
    this.apiService.createOrder({
      bookingId: this.bookingId,
      propertyId: this.propertyId,
      items: this.cartItems.map(({ item, quantity }) => ({ minibarItemId: item.id, quantity })),
      paymentMethod: this.paymentMethod
    }).subscribe({
      next: (response) => {
        this.isProcessing = false;
        this.spaydPayload = response.spaydPayload ?? '';
        if (this.paymentMethod === 'QR_BANK') {
          this.paymentMessage = 'Bank QR generated. Scan and finish payment in your banking app.';
          this.snackBar.open('SPAYD QR prepared', 'Dismiss', { duration: 4000 });
        } else {
          this.paymentMessage = `Payment of ${this.totalAmount.toLocaleString('en-US', { style: 'currency', currency: 'USD' })} confirmed.`;
          this.quantities = {};
          this.showCheckout = false;
          this.snackBar.open('Payment Successful', 'Dismiss', { duration: 4000 });
        }
      },
      error: () => {
        this.isProcessing = false;
      }
    });
  }

  get spaydQrUrl(): string {
    return this.spaydPayload
      ? `https://api.qrserver.com/v1/create-qr-code/?size=280x280&data=${encodeURIComponent(this.spaydPayload)}`
      : '';
  }
}
