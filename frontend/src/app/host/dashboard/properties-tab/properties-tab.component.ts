import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';

import { ApiService, PropertyDto } from '../../../core/api.service';
import { Property } from '../../../shared/models';
import { PropertyDialogComponent } from '../property-dialog/property-dialog.component';

@Component({
  selector: 'app-properties-tab',
  templateUrl: './properties-tab.component.html'
})
export class PropertiesTabComponent implements OnInit {
  readonly displayedColumns = ['name', 'address', 'icalUrlsCount', 'actions'];
  readonly dataSource = new MatTableDataSource<Property>([]);
  isLoading = false;

  constructor(
    private readonly apiService: ApiService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadProperties();
  }

  loadProperties(): void {
    this.isLoading = true;
    this.apiService.getProperties().subscribe({
      next: (properties) => {
        this.dataSource.data = properties;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  openDialog(property?: Property): void {
    const dialogRef = this.dialog.open(PropertyDialogComponent, {
      width: '760px',
      data: property ?? null,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe((result: PropertyDto | undefined) => {
      if (!result) {
        return;
      }

      const request = property
        ? this.apiService.updateProperty(property.id, result)
        : this.apiService.createProperty(result);

      request.subscribe(() => {
        this.snackBar.open(property ? 'Property updated.' : 'Property created.', 'Dismiss', { duration: 3000 });
        this.loadProperties();
      });
    });
  }

  deleteProperty(property: Property): void {
    if (!confirm(`Delete ${property.name}?`)) {
      return;
    }

    this.apiService.deleteProperty(property.id).subscribe(() => {
      this.snackBar.open('Property deleted.', 'Dismiss', { duration: 3000 });
      this.loadProperties();
    });
  }
}
