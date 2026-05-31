import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';

import { ApiService } from '../../../core/api.service';
import { GuestMessage, Property } from '../../../shared/models';

@Component({
  selector: 'app-messages-tab',
  templateUrl: './messages-tab.component.html'
})
export class MessagesTabComponent implements OnInit {
  readonly displayedColumns = ['guestName', 'messageType', 'content', 'rating', 'createdAt', 'read'];
  properties: Property[] = [];
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
      this.loadMessages();
    });
  }

  loadMessages(): void {
    if (!this.properties.length) {
      this.messages = [];
      this.filteredMessages = [];
      return;
    }

    forkJoin(this.properties.map((property) => this.apiService.getMessages(property.id))).subscribe((groupedMessages) => {
      this.messages = groupedMessages
        .flat()
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
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
