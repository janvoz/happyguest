import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin, interval, Subscription } from 'rxjs';

import { ApiService } from '../../../core/api.service';
import { ChatMessage, GuestMessage, Property } from '../../../shared/models';

@Component({
  selector: 'app-messages-tab',
  templateUrl: './messages-tab.component.html'
})
export class MessagesTabComponent implements OnInit, OnDestroy {
  readonly displayedColumns = ['guestName', 'messageType', 'content', 'rating', 'createdAt', 'read'];
  properties: Property[] = [];
  filterType = 'ALL';
  messages: GuestMessage[] = [];
  filteredMessages: GuestMessage[] = [];
  selectedMessage: GuestMessage | null = null;
  chatMessages: ChatMessage[] = [];
  readonly chatForm;
  private pollSub?: Subscription;

  constructor(
    private readonly apiService: ApiService,
    private readonly fb: FormBuilder,
    private readonly snackBar: MatSnackBar
  ) {
    this.chatForm = this.fb.group({
      messageText: ['', Validators.required]
    });
  }

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
    this.loadChat(message.bookingId);
    this.pollSub?.unsubscribe();
    this.pollSub = interval(5000).subscribe(() => this.loadChat(message.bookingId));
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

  sendHostMessage(): void {
    if (!this.selectedMessage || this.chatForm.invalid) {
      this.chatForm.markAllAsTouched();
      return;
    }
    const text = String(this.chatForm.getRawValue().messageText ?? '');
    this.apiService.sendHostChatMessage(this.selectedMessage.bookingId, text).subscribe(() => {
      this.chatForm.reset({ messageText: '' });
      this.loadChat(this.selectedMessage!.bookingId);
    });
  }

  private loadChat(bookingId: string): void {
    this.apiService.getHostChat(bookingId).subscribe((messages) => {
      this.chatMessages = messages;
    });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }
}
