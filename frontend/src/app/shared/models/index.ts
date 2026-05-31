export interface User { id: string; email: string; subscriptionTier: 'FREE'|'STANDARD'|'PRO'; subscriptionStatus: string; iban?: string; swift?: string; }
export interface PropertyFaqItem { question: string; answer: string; }
export interface PropertyMapMarker { title: string; category: string; description: string; latitude: number; longitude: number; }
export interface Property { id: string; hostId: string; name: string; address: string; wifiName: string; wifiPassword: string; airbnbReviewUrl: string; bookingReviewUrl: string; icalUrls: string[]; checkoutChecklist: string[]; faqList?: PropertyFaqItem[]; mapMarkers?: PropertyMapMarker[]; customDomain?: string; }
export interface Booking { id: string; propertyId: string; guestName: string; guestEmail: string; checkIn: string; checkOut: string; doorCode: string; isRegistrationCompleted: boolean; preArrivalSent: boolean; postDepartureSent: boolean; bookingRefNumber?: string; source?: 'AIRBNB'|'BOOKING'|'MANUAL'; }
export interface GuestRegistration { id: string; bookingId: string; propertyId: string; fullName: string; dateOfBirth: string; citizenship: string; documentNumber: string; address: string; createdAt: string; ubyportStatus?: 'PENDING'|'SUBMITTED'; }
export interface GuideItem { id: string; propertyId: string; title: string; slug: string; contentMarkdown: string; videoUrl: string; qrCodeUrl: string; photoUrls: string[]; displayOrder: number; }
export interface MinibarItem { id: string; propertyId: string; name: string; price: number; stockCount: number; imageUrl: string; }
export interface MinibarOrder { id: string; bookingId: string; propertyId: string; totalAmount: number; applicationFee: number; hostPayoutAmount: number; status: string; createdAt: string; variableSymbol?: string; paymentMethod?: 'STRIPE'|'QR_BANK'; }
export interface GuestMessage { id: string; propertyId: string; bookingId: string; guestName: string; messageType: string; content: string; rating: number; createdAt: string; read: boolean; }
export interface EmailTemplate { id: string; hostId: string; propertyId: string; triggerType: 'PRE_ARRIVAL'|'POST_DEPARTURE'; subject: string; htmlBody: string; }
export interface ChatMessage { id: string; bookingId: string; sender: 'GUEST'|'HOST'; messageText: string; createdAt: string; }
