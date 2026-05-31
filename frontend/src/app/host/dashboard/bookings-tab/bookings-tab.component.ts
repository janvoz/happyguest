import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';

import { ApiService, BookingDto } from '../../../core/api.service';
import { CurrentPropertyService } from '../../../core/current-property.service';
import { Booking, Property } from '../../../shared/models';
import { BookingDialogComponent } from '../booking-dialog/booking-dialog.component';

@Component({
  selector: 'app-bookings-tab',
  templateUrl: './bookings-tab.component.html'
})
export class BookingsTabComponent implements OnInit {
  readonly displayedColumns = ['guestName', 'guestEmail', 'checkIn', 'checkOut', 'doorCode', 'status', 'actions'];
  readonly dataSource = new MatTableDataSource<Booking>([]);
  properties: Property[] = [];
  selectedPropertyId = '';
  isLoading = false;
  readonly inviteInProgress = new Set<string>();

  constructor(
    private readonly apiService: ApiService,
    private readonly currentPropertyService: CurrentPropertyService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.currentPropertyService.properties$.subscribe((properties) => {
      this.properties = properties;
    });
    this.currentPropertyService.selectedPropertyId$.subscribe((propertyId) => {
      this.selectedPropertyId = propertyId;
      this.loadBookings();
    });
  }

  loadBookings(): void {
    if (!this.selectedPropertyId) {
      this.dataSource.data = [];
      return;
    }

    this.isLoading = true;
    this.apiService.getBookings(this.selectedPropertyId).subscribe({
      next: (bookings) => {
        this.dataSource.data = bookings;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  openDialog(booking?: Booking): void {
    const dialogRef = this.dialog.open(BookingDialogComponent, {
      width: '620px',
      data: { properties: this.properties, propertyId: this.selectedPropertyId, booking }
    });

    dialogRef.afterClosed().subscribe((result: BookingDto | undefined) => {
      if (!result) {
        return;
      }

      const request = booking
        ? this.apiService.updateBooking(booking.id, result)
        : this.apiService.createBooking(result);

      request.subscribe(() => {
        this.snackBar.open(booking ? 'Booking updated.' : 'Booking created.', 'Dismiss', { duration: 3000 });
        this.selectedPropertyId = result.propertyId ?? this.selectedPropertyId;
        this.currentPropertyService.setSelectedProperty(this.selectedPropertyId);
        this.loadBookings();
      });
    });
  }

  deleteBooking(booking: Booking): void {
    if (!confirm(`Delete booking for ${booking.guestName}?`)) {
      return;
    }
    this.apiService.deleteBooking(booking.id).subscribe(() => {
      this.snackBar.open('Booking deleted.', 'Dismiss', { duration: 3000 });
      this.loadBookings();
    });
  }

  sendRegistrationInvite(booking: Booking): void {
    if (!booking.id || this.inviteInProgress.has(booking.id) || booking.isRegistrationCompleted) {
      return;
    }

    if (!booking.guestEmail) {
      this.snackBar.open('Booking has no guest email.', 'Dismiss', { duration: 3000 });
      return;
    }

    this.inviteInProgress.add(booking.id);
    this.apiService.sendRegistrationInvite(booking.id).subscribe({
      next: (response) => {
        this.snackBar.open(response.message, 'Dismiss', { duration: 3500 });
        this.inviteInProgress.delete(booking.id);
      },
      error: () => {
        this.snackBar.open('Failed to send registration invite.', 'Dismiss', { duration: 3500 });
        this.inviteInProgress.delete(booking.id);
      }
    });
  }

  canSendInvite(booking: Booking): boolean {
    return !!booking.id && !!booking.guestEmail && !booking.isRegistrationCompleted && !this.inviteInProgress.has(booking.id);
  }
}
