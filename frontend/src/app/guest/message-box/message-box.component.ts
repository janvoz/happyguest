import { Component, Input, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { interval, Subscription } from 'rxjs';

import { ApiService } from '../../core/api.service';
import { ChatMessage } from '../../shared/models';

@Component({
  selector: 'app-message-box',
  templateUrl: './message-box.component.html'
})
export class MessageBoxComponent implements OnDestroy {
  @Input({ required: true }) propertyId = '';
  @Input({ required: true }) bookingId = '';
  @Input() guestName = 'Guest';

  readonly form: FormGroup;
  expanded = false;
  isSubmitting = false;
  messages: ChatMessage[] = [];
  private pollSub?: Subscription;

  constructor(
    private readonly fb: FormBuilder,
    private readonly apiService: ApiService,
    private readonly snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      content: ['', [Validators.required, Validators.minLength(1)]]
    });
  }

  toggle(): void {
    this.expanded = !this.expanded;
    if (this.expanded) {
      this.loadMessages();
      this.pollSub?.unsubscribe();
      this.pollSub = interval(5000).subscribe(() => this.loadMessages());
    } else {
      this.pollSub?.unsubscribe();
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const message = String(this.form.getRawValue().content ?? '');
    this.apiService.sendGuestChatMessage(this.bookingId, message).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.form.reset({ content: '' });
        this.loadMessages();
        this.snackBar.open('Message sent.', 'Dismiss', { duration: 3000 });
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }

  private loadMessages(): void {
    this.apiService.getPublicChat(this.bookingId).subscribe({
      next: (messages) => {
        this.messages = messages;
      }
    });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }
}
