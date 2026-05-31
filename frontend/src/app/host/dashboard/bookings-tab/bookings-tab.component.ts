import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';

import { ApiService, BookingDto } from '../../../core/api.service';
import { Booking, Property } from '../../../shared/models';
import { BookingDialogComponent } from '../booking-dialog/booking-dialog.component';

@Component({
  selector: 'app-bookings-tab',
  templateUrl: './bookings-tab.component.html'
})
export class BookingsTabComponent implements OnInit {
  readonly displayedColumns = ['guestName', 'guestEmail', 'checkIn', 'checkOut', 'doorCode', 'status'];
  readonly dataSource = new MatTableDataSource<Booking>([]);
  properties: Property[] = [];
  selectedPropertyId = '';
  isLoading = false;

  constructor(
    private readonly apiService: ApiService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.apiService.getProperties().subscribe((properties) => {
      this.properties = properties;
      this.selectedPropertyId = properties[0]?.id ?? '';
      if (this.selectedPropertyId) {
        this.loadBookings();
      }
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

  openDialog(): void {
    const dialogRef = this.dialog.open(BookingDialogComponent, {
      width: '620px',
      data: { properties: this.properties, propertyId: this.selectedPropertyId }
    });

    dialogRef.afterClosed().subscribe((result: BookingDto | undefined) => {
      if (!result) {
        return;
      }

      this.apiService.createBooking(result).subscribe(() => {
        this.snackBar.open('Booking created.', 'Dismiss', { duration: 3000 });
        this.selectedPropertyId = result.propertyId ?? this.selectedPropertyId;
        this.loadBookings();
      });
    });
  }
}
