import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';

import { ApiService, MinibarItemDto } from '../../../core/api.service';
import { MinibarItem, Property } from '../../../shared/models';
import { MinibarItemDialogComponent } from '../minibar-item-dialog/minibar-item-dialog.component';

@Component({
  selector: 'app-minibar-tab',
  templateUrl: './minibar-tab.component.html'
})
export class MinibarTabComponent implements OnInit {
  readonly displayedColumns = ['name', 'price', 'stockCount', 'imageUrl', 'actions'];
  readonly dataSource = new MatTableDataSource<MinibarItem>([]);
  properties: Property[] = [];
  selectedPropertyId = '';

  constructor(
    private readonly apiService: ApiService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.apiService.getProperties().subscribe((properties) => {
      this.properties = properties;
      this.selectedPropertyId = properties[0]?.id ?? '';
      this.loadItems();
    });
  }

  loadItems(): void {
    if (!this.selectedPropertyId) {
      this.dataSource.data = [];
      return;
    }
    this.apiService.getMinibarItems(this.selectedPropertyId).subscribe((items) => {
      this.dataSource.data = items;
    });
  }

  openDialog(item?: MinibarItem): void {
    const dialogRef = this.dialog.open(MinibarItemDialogComponent, {
      width: '520px',
      data: { item, propertyId: this.selectedPropertyId }
    });

    dialogRef.afterClosed().subscribe((result: MinibarItemDto | undefined) => {
      if (!result) {
        return;
      }
      const request = item ? this.apiService.updateMinibarItem(item.id, result) : this.apiService.createMinibarItem(result);
      request.subscribe(() => {
        this.snackBar.open(item ? 'Item updated.' : 'Item created.', 'Dismiss', { duration: 3000 });
        this.loadItems();
      });
    });
  }

  deleteItem(item: MinibarItem): void {
    if (!confirm(`Delete ${item.name}?`)) {
      return;
    }
    this.apiService.deleteMinibarItem(item.id).subscribe(() => {
      this.snackBar.open('Item deleted.', 'Dismiss', { duration: 3000 });
      this.loadItems();
    });
  }
}
