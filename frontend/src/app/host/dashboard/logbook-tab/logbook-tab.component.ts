import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

import { ApiService } from '../../../core/api.service';
import { CurrentPropertyService } from '../../../core/current-property.service';
import { GuestRegistration, Property } from '../../../shared/models';

@Component({
  selector: 'app-logbook-tab',
  templateUrl: './logbook-tab.component.html'
})
export class LogbookTabComponent implements OnInit, AfterViewInit {
  @ViewChild(MatPaginator) paginator?: MatPaginator;
  @ViewChild(MatSort) sort?: MatSort;

  readonly displayedColumns = ['fullName', 'dateOfBirth', 'citizenship', 'documentNumber', 'address', 'createdAt'];
  readonly dataSource = new MatTableDataSource<GuestRegistration>([]);
  properties: Property[] = [];
  selectedPropertyId = '';
  fromDate: Date | null = null;
  toDate: Date | null = null;
  searchTerm = '';

  constructor(
    private readonly apiService: ApiService,
    private readonly currentPropertyService: CurrentPropertyService,
    private readonly snackBar: MatSnackBar
  ) {
    this.dataSource.filterPredicate = (record, filter) => {
      const normalized = filter.trim().toLowerCase();
      return record.fullName.toLowerCase().includes(normalized) || record.documentNumber.toLowerCase().includes(normalized);
    };
  }

  ngOnInit(): void {
    this.currentPropertyService.properties$.subscribe((properties) => {
      this.properties = properties;
    });
    this.currentPropertyService.selectedPropertyId$.subscribe((propertyId) => {
      this.selectedPropertyId = propertyId;
      if (this.selectedPropertyId) {
        this.loadLogbook();
      }
    });
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator ?? null;
    this.dataSource.sort = this.sort ?? null;
  }

  loadLogbook(): void {
    if (!this.selectedPropertyId) {
      return;
    }

    this.apiService.getLogbook(this.selectedPropertyId, this.isoDate(this.fromDate), this.isoDate(this.toDate)).subscribe((entries) => {
      this.dataSource.data = entries;
      this.applyFilter();
    });
  }

  applyFilter(): void {
    this.dataSource.filter = this.searchTerm.toLowerCase();
    this.dataSource.paginator?.firstPage();
  }

  exportCsv(): void {
    if (!this.selectedPropertyId) {
      return;
    }

    this.apiService.exportLogbookCsv(this.selectedPropertyId, this.isoDate(this.fromDate), this.isoDate(this.toDate)).subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'guest-logbook.csv';
      link.click();
      URL.revokeObjectURL(url);
      this.snackBar.open('CSV exported.', 'Dismiss', { duration: 3000 });
    });
  }

  private isoDate(value: Date | null): string | undefined {
    return value ? value.toISOString() : undefined;
  }
}
