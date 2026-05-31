import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';

import { ApiService, GuideDto } from '../../../core/api.service';
import { CurrentPropertyService } from '../../../core/current-property.service';
import { GuideItem, Property } from '../../../shared/models';
import { GuideDialogComponent } from '../guide-dialog/guide-dialog.component';

@Component({
  selector: 'app-guides-tab',
  templateUrl: './guides-tab.component.html'
})
export class GuidesTabComponent implements OnInit {
  readonly displayedColumns = ['title', 'slug', 'videoUrl', 'qrCodeUrl', 'displayOrder', 'actions'];
  readonly dataSource = new MatTableDataSource<GuideItem>([]);
  properties: Property[] = [];
  selectedPropertyId = '';

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
      this.loadGuides();
    });
  }

  loadGuides(): void {
    if (!this.selectedPropertyId) {
      this.dataSource.data = [];
      return;
    }

    this.apiService.getGuides(this.selectedPropertyId).subscribe((guides) => {
      this.dataSource.data = guides;
    });
  }

  openDialog(guide?: GuideItem): void {
    const dialogRef = this.dialog.open(GuideDialogComponent, {
      width: '720px',
      data: { guide, propertyId: this.selectedPropertyId }
    });

    dialogRef.afterClosed().subscribe((result: GuideDto | undefined) => {
      if (!result) {
        return;
      }

      const request = guide ? this.apiService.updateGuide(guide.id, result) : this.apiService.createGuide(result);
      request.subscribe(() => {
        this.snackBar.open(guide ? 'Guide updated.' : 'Guide created.', 'Dismiss', { duration: 3000 });
        this.loadGuides();
      });
    });
  }

  deleteGuide(guide: GuideItem): void {
    if (!confirm(`Delete guide \"${guide.title}\"?`)) {
      return;
    }
    this.apiService.deleteGuide(guide.id).subscribe(() => {
      this.snackBar.open('Guide deleted.', 'Dismiss', { duration: 3000 });
      this.loadGuides();
    });
  }
}
