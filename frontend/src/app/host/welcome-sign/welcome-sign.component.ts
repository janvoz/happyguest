import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { Property, PortalQrResponse } from '../../shared/models';

@Component({
  selector: 'app-welcome-sign',
  template: `
<div class="min-h-screen bg-gray-100 flex flex-col items-center justify-center p-6 print:bg-white print:p-0">
  <!-- Action bar (hidden on print) -->
  <div class="no-print flex gap-3 mb-6">
    <button mat-raised-button color="primary" (click)="print()">
      <mat-icon>print</mat-icon> Print
    </button>
    <a mat-stroked-button routerLink="/host/dashboard">Back to Dashboard</a>
  </div>

  <!-- A4 sheet -->
  <div class="bg-white shadow-xl rounded-lg p-10 welcome-sheet w-full max-w-[794px] flex flex-col items-center gap-6 print:shadow-none print:rounded-none print:max-w-full">
    <h1 class="text-4xl font-bold text-center tracking-tight">{{ property?.name }}</h1>
    <p class="text-lg text-gray-500 text-center">{{ property?.address }}</p>

    <mat-divider class="w-full"></mat-divider>

    <div class="w-full flex flex-col sm:flex-row gap-6 items-start justify-center">
      <div class="flex-1">
        <h2 class="text-xl font-semibold mb-2">Wi-Fi</h2>
        <p><span class="font-medium">Network:</span> {{ property?.wifiName }}</p>
        <p><span class="font-medium">Password:</span> {{ property?.wifiPassword }}</p>
      </div>

      <div class="flex flex-col items-center gap-2">
        <img *ngIf="qr?.qrDataUrl" [src]="qr.qrDataUrl" alt="Guest Portal QR" class="w-48 h-48" />
        <p class="text-sm text-gray-400 text-center">Scan for Guest Portal</p>
      </div>
    </div>

    <mat-divider class="w-full"></mat-divider>

    <p *ngIf="qr?.portalUrl" class="text-xs text-gray-400 break-all text-center">{{ qr.portalUrl }}</p>
    <p class="text-xs text-gray-300 mt-auto">Powered by HappyGuest</p>
  </div>
</div>
  `,
  styles: [`
    @media print {
      .no-print { display: none !important; }
      .welcome-sheet { border: none; box-shadow: none; }
    }
  `],
})
export class WelcomeSignComponent implements OnInit {
  property?: Property;
  qr?: PortalQrResponse;
  loading = true;
  error = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly api: ApiService,
  ) {}

  ngOnInit(): void {
    const propertyId = this.route.snapshot.paramMap.get('propertyId') ?? '';
    if (!propertyId) { this.error = 'No property selected.'; this.loading = false; return; }
    this.api.getProperty(propertyId).subscribe({
      next: p => this.property = p,
      error: () => { this.error = 'Could not load property.'; this.loading = false; },
    });
    this.api.getPortalQr(propertyId).subscribe({
      next: q => { this.qr = q; this.loading = false; },
      error: () => this.loading = false,
    });
  }

  print(): void {
    window.print();
  }
}
