import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

import { Booking, Property } from '../../shared/models';

@Component({
  selector: 'app-access-card',
  templateUrl: './access-card.component.html'
})
export class AccessCardComponent implements OnInit, OnDestroy {
  @Input({ required: true }) booking!: Booking;
  @Input({ required: true }) property!: Property;

  countdown = '00:00:00';
  isUrgent = false;
  private intervalId?: ReturnType<typeof setInterval>;

  constructor(private readonly snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.updateCountdown();
    this.intervalId = setInterval(() => this.updateCountdown(), 1000);
  }

  ngOnDestroy(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }

  copy(value: string, label: string): void {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(value).then(() => {
        this.snackBar.open(`${label} copied.`, 'Dismiss', { duration: 2500 });
      });
      return;
    }

    this.snackBar.open(`Copy ${label}: ${value}`, 'Dismiss', { duration: 4000 });
  }

  private updateCountdown(): void {
    const checkOut = new Date(this.booking.checkOut).getTime();
    const diff = Math.max(checkOut - Date.now(), 0);
    const hours = Math.floor(diff / 3_600_000).toString().padStart(2, '0');
    const minutes = Math.floor((diff % 3_600_000) / 60_000).toString().padStart(2, '0');
    const seconds = Math.floor((diff % 60_000) / 1000).toString().padStart(2, '0');
    this.countdown = `${hours}:${minutes}:${seconds}`;
    this.isUrgent = diff < 3_600_000;
  }
}
