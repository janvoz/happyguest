import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';

import { ApiService, EmailTemplateDto } from '../../../core/api.service';
import { CurrentPropertyService } from '../../../core/current-property.service';
import { EmailTemplate } from '../../../shared/models';
import { TemplateDialogComponent } from '../template-dialog/template-dialog.component';

@Component({
  selector: 'app-templates-tab',
  templateUrl: './templates-tab.component.html'
})
export class TemplatesTabComponent implements OnInit {
  readonly displayedColumns = ['triggerType', 'subject', 'actions'];
  readonly dataSource = new MatTableDataSource<EmailTemplate>([]);
  selectedPropertyId = '';

  constructor(
    private readonly apiService: ApiService,
    private readonly currentPropertyService: CurrentPropertyService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.currentPropertyService.selectedPropertyId$.subscribe((propertyId) => {
      this.selectedPropertyId = propertyId;
      this.loadTemplates();
    });
  }

  loadTemplates(): void {
    if (!this.selectedPropertyId) {
      this.dataSource.data = [];
      return;
    }

    this.apiService.getTemplates(this.selectedPropertyId).subscribe((templates) => {
      this.dataSource.data = templates;
    });
  }

  openDialog(template?: EmailTemplate): void {
    const dialogRef = this.dialog.open(TemplateDialogComponent, {
      width: '800px',
      data: { template, propertyId: this.selectedPropertyId }
    });

    dialogRef.afterClosed().subscribe((result: EmailTemplateDto | undefined) => {
      if (!result) {
        return;
      }

      const request = template
        ? this.apiService.updateTemplate(template.id, result)
        : this.apiService.createTemplate(result);
      request.subscribe(() => {
        this.snackBar.open(template ? 'Template updated.' : 'Template created.', 'Dismiss', { duration: 3000 });
        this.loadTemplates();
      });
    });
  }

  deleteTemplate(template: EmailTemplate): void {
    if (!confirm(`Delete template "${template.subject}"?`)) {
      return;
    }
    this.apiService.deleteTemplate(template.id).subscribe(() => {
      this.snackBar.open('Template deleted.', 'Dismiss', { duration: 3000 });
      this.loadTemplates();
    });
  }

  triggerLabel(triggerType: string): string {
    return triggerType === 'PRE_ARRIVAL' ? 'Pre-arrival' : 'Post-departure';
  }
}
