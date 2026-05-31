export interface User { id: string; email: string; subscriptionTier: 'FREE'|'STANDARD'|'PRO'; subscriptionStatus: string; }
export interface Property { id: string; hostId: string; name: string; address: string; wifiName: string; wifiPassword: string; airbnbReviewUrl: string; bookingReviewUrl: string; icalUrls: string[]; checkoutChecklist: string[]; customDomain?: string; }
export interface Booking { id: string; propertyId: string; guestName: string; guestEmail: string; checkIn: string; checkOut: string; doorCode: string; isRegistrationCompleted: boolean; preArrivalSent: boolean; postDepartureSent: boolean; }
export interface GuestRegistration { id: string; bookingId: string; propertyId: string; fullName: string; dateOfBirth: string; citizenship: string; documentNumber: string; address: string; createdAt: string; }
export interface GuideItem { id: string; propertyId: string; title: string; slug: string; contentMarkdown: string; videoUrl: string; qrCodeUrl: string; photoUrls: string[]; displayOrder: number; }
export interface MinibarItem { id: string; propertyId: string; name: string; price: number; stockCount: number; imageUrl: string; }
export interface MinibarOrder { id: string; bookingId: string; propertyId: string; totalAmount: number; applicationFee: number; hostPayoutAmount: number; status: string; createdAt: string; }
export interface GuestMessage { id: string; propertyId: string; bookingId: string; guestName: string; messageType: string; content: string; rating: number; createdAt: string; read: boolean; }
