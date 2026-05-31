import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import {
  Booking,
  ChatMessage,
  EmailTemplate,
  GuestMessage,
  GuestRegistration,
  GuideItem,
  MinibarItem,
  MinibarOrder,
  Property
} from '../shared/models';

export interface PropertyDto extends Partial<Property> {}
export interface BookingDto extends Partial<Booking> {}
export interface GuideDto extends Partial<GuideItem> {}
export interface MinibarItemDto extends Partial<MinibarItem> {}
export interface GuestRegistrationDto {
  bookingId: string;
  propertyId: string;
  guests: Array<Pick<GuestRegistration, 'fullName' | 'dateOfBirth' | 'citizenship' | 'documentNumber' | 'address'>>;
}
export interface ReviewDto {
  bookingId: string;
  rating: number;
  feedback?: string;
}
export interface GuestMessageDto {
  propertyId: string;
  bookingId: string;
  guestName: string;
  messageType: string;
  content: string;
}
export interface OrderDto {
  bookingId: string;
  propertyId: string;
  items: Array<{ itemId: string; quantity: number }>;
  paymentMethod?: 'STRIPE' | 'QR_BANK';
}
export interface EmailTemplateDto {
  propertyId: string;
  triggerType: 'PRE_ARRIVAL' | 'POST_DEPARTURE';
  subject: string;
  htmlBody: string;
}
export interface PaymentIntentResponse {
  clientSecret: string;
  spaydPayload?: string;
  spaydQrDataUrl?: string;
}

export interface RegistrationInviteResponse {
  bookingId: string;
  recipient: string;
  sent: boolean;
  status: string;
  message: string;
}

export interface SubscriptionFeaturesResponse {
  tier: 'FREE' | 'STANDARD' | 'PRO';
  features: string[];
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly baseUrl = environment.apiUrl;
  private readonly hostUrl = `${this.baseUrl}/host`;
  private readonly guestUrl = `${this.baseUrl}/guest`;
  private readonly publicUrl = `${this.baseUrl}/public`;
  private readonly billingUrl = `${this.baseUrl}/host/billing`;

  constructor(private readonly http: HttpClient) {}

  getProperties(): Observable<Property[]> {
    return this.http.get<Property[]>(`${this.hostUrl}/properties`);
  }

  getProperty(id: string): Observable<Property> {
    return this.http.get<Property>(`${this.hostUrl}/properties/${id}`);
  }

  getPublicProperty(propertyId: string): Observable<Property> {
    return this.http.get<Property>(`${this.publicUrl}/properties/${propertyId}`);
  }

  createProperty(dto: PropertyDto): Observable<Property> {
    return this.http.post<Property>(`${this.hostUrl}/properties`, dto);
  }

  updateProperty(id: string, dto: PropertyDto): Observable<Property> {
    return this.http.put<Property>(`${this.hostUrl}/properties/${id}`, dto);
  }

  deleteProperty(id: string): Observable<void> {
    return this.http.delete<void>(`${this.hostUrl}/properties/${id}`);
  }

  getBookings(propertyId: string): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.hostUrl}/properties/${propertyId}/bookings`);
  }

  getPublicBooking(bookingId: string): Observable<Booking> {
    return this.http.get<Booking>(`${this.publicUrl}/bookings/${bookingId}`);
  }

  getBookingByReference(propertyId: string, reference: string): Observable<Booking> {
    return this.http.get<Booking>(`${this.publicUrl}/properties/${propertyId}/bookings/${reference}`);
  }

  getBookingByGate(propertyId: string, reference: string, lastName: string): Observable<Booking> {
    return this.http.get<Booking>(`${this.publicUrl}/properties/${propertyId}/bookings/access`, {
      params: {
        reference,
        lastName
      }
    });
  }

  createBooking(dto: BookingDto): Observable<Booking> {
    return this.http.post<Booking>(`${this.hostUrl}/bookings`, dto);
  }

  sendRegistrationInvite(bookingId: string): Observable<RegistrationInviteResponse> {
    return this.http.post<RegistrationInviteResponse>(`${this.hostUrl}/bookings/${bookingId}/registration-invite`, {});
  }

  updateBooking(id: string, dto: BookingDto): Observable<Booking> {
    return this.http.put<Booking>(`${this.hostUrl}/bookings/${id}`, dto);
  }

  deleteBooking(id: string): Observable<void> {
    return this.http.delete<void>(`${this.hostUrl}/bookings/${id}`);
  }

  getLogbook(propertyId: string, from?: string, to?: string): Observable<GuestRegistration[]> {
    return this.http.get<GuestRegistration[]>(`${this.hostUrl}/properties/${propertyId}/logbook`, {
      params: this.buildDateParams(from, to)
    });
  }

  exportLogbookCsv(propertyId: string, from?: string, to?: string): Observable<Blob> {
    return this.http.get(`${this.hostUrl}/properties/${propertyId}/logbook/export`, {
      params: this.buildDateParams(from, to),
      responseType: 'blob'
    });
  }

  exportUbyportXml(propertyId: string, from?: string, to?: string): Observable<Blob> {
    return this.http.get(`${this.hostUrl}/properties/${propertyId}/logbook/export/ubyport`, {
      params: this.buildDateParams(from, to),
      responseType: 'blob'
    });
  }

  getGuides(propertyId: string): Observable<GuideItem[]> {
    return this.http.get<GuideItem[]>(`${this.hostUrl}/properties/${propertyId}/guides`);
  }

  createGuide(dto: GuideDto): Observable<GuideItem> {
    return this.http.post<GuideItem>(`${this.hostUrl}/guides`, dto);
  }

  updateGuide(id: string, dto: GuideDto): Observable<GuideItem> {
    return this.http.put<GuideItem>(`${this.hostUrl}/guides/${id}`, dto);
  }

  deleteGuide(id: string): Observable<void> {
    return this.http.delete<void>(`${this.hostUrl}/guides/${id}`);
  }

  getPublicGuides(propertyId: string): Observable<GuideItem[]> {
    return this.http.get<GuideItem[]>(`${this.publicUrl}/properties/${propertyId}/guides`);
  }

  getPublicGuide(propertyId: string, slug: string): Observable<GuideItem> {
    return this.http.get<GuideItem>(`${this.publicUrl}/properties/${propertyId}/guides/${slug}`);
  }

  getMinibarItems(propertyId: string): Observable<MinibarItem[]> {
    return this.http.get<MinibarItem[]>(`${this.hostUrl}/properties/${propertyId}/minibar-items`);
  }

  createMinibarItem(dto: MinibarItemDto): Observable<MinibarItem> {
    return this.http.post<MinibarItem>(`${this.hostUrl}/minibar-items`, dto);
  }

  updateMinibarItem(id: string, dto: MinibarItemDto): Observable<MinibarItem> {
    return this.http.put<MinibarItem>(`${this.hostUrl}/minibar-items/${id}`, dto);
  }

  deleteMinibarItem(id: string): Observable<void> {
    return this.http.delete<void>(`${this.hostUrl}/minibar-items/${id}`);
  }

  getPublicMinibarItems(propertyId: string): Observable<MinibarItem[]> {
    return this.http.get<MinibarItem[]>(`${this.publicUrl}/properties/${propertyId}/minibar-items`);
  }

  getOrders(propertyId: string): Observable<MinibarOrder[]> {
    return this.http.get<MinibarOrder[]>(`${this.hostUrl}/properties/${propertyId}/orders`);
  }

  createOrder(dto: OrderDto): Observable<PaymentIntentResponse> {
    return this.http.post<PaymentIntentResponse>(`${this.guestUrl}/orders`, dto);
  }

  getPublicChat(bookingId: string): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.publicUrl}/bookings/${bookingId}/chat`);
  }

  getHostChat(bookingId: string): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.hostUrl}/bookings/${bookingId}/chat`);
  }

  sendGuestChatMessage(bookingId: string, messageText: string): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`${this.publicUrl}/bookings/${bookingId}/chat`, { bookingId, messageText });
  }

  sendHostChatMessage(bookingId: string, messageText: string): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`${this.hostUrl}/bookings/${bookingId}/chat`, { bookingId, messageText });
  }

  getMessages(propertyId: string): Observable<GuestMessage[]> {
    return this.http.get<GuestMessage[]>(`${this.hostUrl}/properties/${propertyId}/messages`);
  }

  markMessageRead(id: string): Observable<GuestMessage> {
    return this.http.patch<GuestMessage>(`${this.hostUrl}/messages/${id}/read`, {});
  }

  submitGuestRegistration(dto: GuestRegistrationDto): Observable<unknown> {
    return this.http.post(`${this.guestUrl}/registrations`, dto);
  }

  submitReview(dto: ReviewDto): Observable<unknown> {
    return this.http.post(`${this.guestUrl}/reviews`, dto);
  }

  submitGuestMessage(dto: GuestMessageDto): Observable<GuestMessage> {
    return this.http.post<GuestMessage>(`${this.guestUrl}/messages`, dto);
  }

  translate(text: string, lang: string): Observable<string> {
    return this.http.post<{ translatedText?: string } | string>(`${this.guestUrl}/translate`, { text, lang }).pipe(
      map((response) => typeof response === 'string' ? response : response.translatedText ?? text)
    );
  }

  subscribePlan(tier: 'FREE' | 'STANDARD' | 'PRO'): Observable<string> {
    return this.http.post<{ checkoutUrl?: string; url?: string }>(`${this.billingUrl}/subscribe`, { tier }).pipe(
      map((response) => response.checkoutUrl ?? response.url ?? '')
    );
  }

  getBillingPortalUrl(): Observable<string> {
    return this.http.get<{ url?: string }>(`${this.billingUrl}/portal`).pipe(
      map((response) => response.url ?? '')
    );
  }

  getSubscriptionFeatures(): Observable<SubscriptionFeaturesResponse> {
    return this.http.get<SubscriptionFeaturesResponse>(`${this.hostUrl}/subscription/features`);
  }

  getTemplates(propertyId: string): Observable<EmailTemplate[]> {
    return this.http.get<EmailTemplate[]>(`${this.hostUrl}/properties/${propertyId}/templates`);
  }

  createTemplate(dto: EmailTemplateDto): Observable<EmailTemplate> {
    return this.http.post<EmailTemplate>(`${this.hostUrl}/templates`, dto);
  }

  updateTemplate(id: string, dto: EmailTemplateDto): Observable<EmailTemplate> {
    return this.http.put<EmailTemplate>(`${this.hostUrl}/templates/${id}`, dto);
  }

  deleteTemplate(id: string): Observable<void> {
    return this.http.delete<void>(`${this.hostUrl}/templates/${id}`);
  }

  private buildDateParams(from?: string, to?: string): Record<string, string> {
    const params: Record<string, string> = {};
    if (from) {
      params['from'] = from;
    }
    if (to) {
      params['to'] = to;
    }
    return params;
  }
}
