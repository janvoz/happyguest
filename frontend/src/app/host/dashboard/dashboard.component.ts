import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatSidenav } from '@angular/material/sidenav';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { ApiService } from '../../core/api.service';
import { CurrentPropertyService } from '../../core/current-property.service';
import { SubscriptionFeatureService } from '../../core/subscription-feature.service';
import { Property } from '../../shared/models';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  @ViewChild(MatSidenav) sidenav?: MatSidenav;

  readonly navItems = [
    { label: 'Properties', icon: 'home_work', tabIndex: 0 },
    { label: 'Bookings', icon: 'event', tabIndex: 1 },
    { label: 'Logbook', icon: 'description', tabIndex: 2 },
    { label: 'Guides', icon: 'menu_book', tabIndex: 3 },
    { label: 'Minibar', icon: 'local_bar', tabIndex: 4, requiredFeature: 'MINIBAR' },
    { label: 'Templates', icon: 'mark_email_read', tabIndex: 5 },
    { label: 'Finances & Billing', icon: 'payments', tabIndex: 6 },
    { label: 'Messages', icon: 'chat', tabIndex: 7, isGlobal: true }
  ];
  availableFeatures = new Set<string>();
  properties: Property[] = [];
  selectedPropertyId = '';
  isMobile = false;
  selectedTabIndex = 0;
  unreadMessages = 0;
  currentUserEmail = '';

  constructor(
    private readonly breakpointObserver: BreakpointObserver,
    private readonly apiService: ApiService,
    private readonly authService: AuthService,
    private readonly currentPropertyService: CurrentPropertyService,
    private readonly subscriptionFeatureService: SubscriptionFeatureService
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
    this.selectedTabIndex = Math.min(index, 7);
    if (this.isMobile) {
      void this.sidenav?.close();
    }
  }

  toggleSidenav(): void {
    void this.sidenav?.toggle();
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
