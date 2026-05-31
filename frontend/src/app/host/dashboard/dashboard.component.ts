import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatSidenav } from '@angular/material/sidenav';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { ApiService } from '../../core/api.service';
import { CurrentPropertyService } from '../../core/current-property.service';
import { SubscriptionFeatureService } from '../../core/subscription-feature.service';
import { HostI18nService, HostLang } from '../../core/host-i18n.service';
import { Property } from '../../shared/models';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  @ViewChild(MatSidenav) sidenav?: MatSidenav;

  readonly navItems = [
    { labelKey: 'property', icon: 'home_work', tabIndex: 0 },
    { labelKey: 'bookings', icon: 'event', tabIndex: 1 },
    { labelKey: 'logbook', icon: 'description', tabIndex: 2, requiredFeature: 'LEGAL_LOGBOOK' },
    { labelKey: 'guides', icon: 'menu_book', tabIndex: 3 },
    { labelKey: 'faq', icon: 'quiz', tabIndex: 4 },
    { labelKey: 'minibar', icon: 'local_bar', tabIndex: 5, requiredFeature: 'MINIBAR' },
    { labelKey: 'templates', icon: 'mark_email_read', tabIndex: 6, requiredFeature: 'CUSTOM_TEMPLATES' },
    { labelKey: 'finances', icon: 'payments', tabIndex: 7 },
    { labelKey: 'messages', icon: 'chat', tabIndex: 8, isGlobal: true, requiredFeature: 'TWO_WAY_CHAT' }
  ];
  availableFeatures = new Set<string>();
  properties: Property[] = [];
  selectedPropertyId = '';
  isMobile = false;
  selectedTabIndex = 0;
  unreadMessages = 0;
  currentUserEmail = '';
  hostLangs: HostLang[] = ['en', 'cs'];

  constructor(
    private readonly breakpointObserver: BreakpointObserver,
    private readonly apiService: ApiService,
    private readonly authService: AuthService,
    private readonly currentPropertyService: CurrentPropertyService,
    private readonly subscriptionFeatureService: SubscriptionFeatureService,
    private readonly snackBar: MatSnackBar,
    readonly i18n: HostI18nService,
  ) {}

  ngOnInit(): void {
    this.currentUserEmail = this.authService.getCurrentUser()?.email ?? 'host@guesthost.app';
    this.breakpointObserver.observe('(max-width: 767px)').subscribe((state) => {
      this.isMobile = state.matches;
    });
    this.currentPropertyService.properties$.subscribe((properties) => {
      this.properties = properties;
      this.refreshUnreadCount();
    });
    this.currentPropertyService.selectedPropertyId$.subscribe((propertyId) => {
      this.selectedPropertyId = propertyId;
    });
    this.currentPropertyService.refreshProperties().subscribe();
    this.subscriptionFeatureService.preload().subscribe((response) => {
      this.availableFeatures = new Set(response.features);
    });
  }

  openTab(index: number): void {
    this.selectedTabIndex = Math.min(index, 8);
    if (this.isMobile) {
      void this.sidenav?.close();
    }
  }

  toggleSidenav(): void {
    void this.sidenav?.toggle();
  }

  setHostLang(lang: HostLang): void {
    this.i18n.setLang(lang);
  }

  generateWelcomeSheetPdf(): void {
    if (!this.selectedPropertyId) {
      return;
    }
    this.apiService.downloadWelcomeSheetPdf(this.selectedPropertyId).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = `welcome-sheet-${this.selectedPropertyId}.pdf`;
        anchor.click();
        URL.revokeObjectURL(url);
        this.snackBar.open(this.i18n.t('welcomePdfReady'), 'OK', { duration: 2500 });
      },
      error: () => this.snackBar.open(this.i18n.t('welcomePdfError'), 'OK', { duration: 3000 })
    });
  }

  refreshUnreadCount(): void {
    this.loadUnreadFromProperties(this.properties);
  }

  private loadUnreadFromProperties(properties: Property[]): void {
    if (!properties.length) {
      this.unreadMessages = 0;
      return;
    }

    forkJoin(properties.map((property) => this.apiService.getMessages(property.id))).subscribe({
      next: (messageGroups) => {
        this.unreadMessages = messageGroups
          .flat()
          .filter((message) => !message.read)
          .length;
      },
      error: () => {
        this.unreadMessages = 0;
      }
    });
  }

  isFeatureEnabled(requiredFeature?: string): boolean {
    if (!requiredFeature) {
      return true;
    }
    return this.availableFeatures.has(requiredFeature);
  }

  setCurrentProperty(propertyId: string): void {
    this.currentPropertyService.setSelectedProperty(propertyId);
  }
}
