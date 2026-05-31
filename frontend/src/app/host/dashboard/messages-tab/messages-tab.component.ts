import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService } from '../../../core/api.service';
import { GuestMessage, Property } from '../../../shared/models';

@Component({
  selector: 'app-messages-tab',
  templateUrl: './messages-tab.component.html'
})
export class MessagesTabComponent implements OnInit {
  readonly displayedColumns = ['guestName', 'messageType', 'content', 'rating', 'createdAt', 'read'];
  properties: Property[] = [];
  selectedPropertyId = '';
  filterType = 'ALL';
  messages: GuestMessage[] = [];
  filteredMessages: GuestMessage[] = [];
  selectedMessage: GuestMessage | null = null;

  constructor(
    private readonly apiService: ApiService,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.apiService.getProperties().subscribe((properties) => {
      this.properties = properties;
      this.selectedPropertyId = properties[0]?.id ?? '';
      this.loadMessages();
    });
  }

  loadMessages(): void {
    if (!this.selectedPropertyId) {
      this.messages = [];
      this.filteredMessages = [];
      return;
    }

    this.apiService.getMessages(this.selectedPropertyId).subscribe((messages) => {
      this.messages = messages;
      this.applyFilter();
    });
  }

  applyFilter(): void {
    this.filteredMessages = this.filterType === 'ALL'
      ? [...this.messages]
      : this.messages.filter((message) => message.messageType === this.filterType);
  }

  selectMessage(message: GuestMessage): void {
    this.selectedMessage = message;
    if (!message.read) {
      this.apiService.markMessageRead(message.id).subscribe(() => {
        message.read = true;
        this.snackBar.open('Message marked as read.', 'Dismiss', { duration: 3000 });
      });
    }
  }

  ratingStars(rating: number): string[] {
    return Array.from({ length: rating || 0 }, () => 'star');
  }
}
