import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatSidenav } from '@angular/material/sidenav';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { ApiService } from '../../core/api.service';
import { Property } from '../../shared/models';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  @ViewChild(MatSidenav) sidenav?: MatSidenav;

  readonly navItems = [
    { label: 'Properties', icon: 'home_work' },
    { label: 'Bookings', icon: 'event' },
    { label: 'Logbook', icon: 'description' },
    { label: 'Guides', icon: 'menu_book' },
    { label: 'Minibar', icon: 'local_bar' },
    { label: 'Finances & Billing', icon: 'payments' },
    { label: 'Messages', icon: 'chat' },
    { label: 'Settings', icon: 'settings' }
  ];
  isMobile = false;
  selectedTabIndex = 0;
  unreadMessages = 0;
  currentUserEmail = '';

  constructor(
    private readonly breakpointObserver: BreakpointObserver,
    private readonly apiService: ApiService,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUserEmail = this.authService.getCurrentUser()?.email ?? 'host@guesthost.app';
    this.breakpointObserver.observe('(max-width: 767px)').subscribe((state) => {
      this.isMobile = state.matches;
    });
    this.refreshUnreadCount();
  }

  openTab(index: number): void {
    this.selectedTabIndex = Math.min(index, 6);
    if (this.isMobile) {
      void this.sidenav?.close();
    }
  }

  toggleSidenav(): void {
    void this.sidenav?.toggle();
  }

  refreshUnreadCount(): void {
    this.apiService.getProperties().subscribe({
      next: (properties) => this.loadUnreadFromProperties(properties),
      error: () => {
        this.unreadMessages = 0;
      }
    });
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
}
