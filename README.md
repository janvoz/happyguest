# GuestHost 🏠

A production-ready SaaS platform for Airbnb / Booking.com hosts.

## Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.3 · Java 21 · MongoDB |
| Frontend | Angular 17 · Angular Material · Tailwind CSS |
| Auth | JWT (HMAC-SHA256) |
| Payments | Stripe Billing + PaymentIntents (FastSpring stub) |
| Email | JavaMailSender (SMTP) |
| Calendar Sync | ical4j (`@Scheduled` every 15 min) |
| Translation | Google Translate REST API v2 |
| QR Codes | ZXing |
| Export | OpenCSV |
| Container | Docker Compose (MongoDB + backend + nginx-served frontend) |

---

## Feature Overview

| # | Feature |
|---|---------|
| 1 | **Pre-Arrival Guest Registration** — multi-step form, legal accommodation logbook, CSV export |
| 2 | **Subscription Tiers** — FREE / STANDARD $9 / PRO $29 via Stripe Billing |
| 3 | **Digital Minibar** — mobile shopping cart, Stripe PaymentIntents, 2% application fee, earnings dashboard |
| 4 | **Multi-Source iCal Sync** — Airbnb, Booking.com, VRBO, overlap resolution |
| 5 | **Automated Messaging** — pre-arrival email (–3 days), post-departure email (+2 h) |
| 6 | **Door Code Engine** — 6-digit SecureRandom code, revealed only after registration |
| 7 | **Contextual QR Codes & Guides** — per-appliance deep links, Markdown + video + photos |
| 8 | **Translation Engine** — EN / DE / CS / PL via Google Translate |
| 9 | **Checkout Countdown & Checklist** — live HH:MM:SS timer, host-configurable tasks |
| 10 | **Smart Pre-Review Filter** — 4–5★ → redirect to Airbnb/Booking; 1–3★ → internal form |
| 11 | **Live Message Box** — floating chat widget for instant host notification |
| 12 | **Dual Billing Engine** — Stripe (full) + FastSpring (stub), webhook handling |

---

## Quick Start (Docker Compose)

```bash
# 1. Clone
git clone https://github.com/janvoz/happyguest.git
cd happyguest

# 2. Configure environment
cp .env.example .env
# Edit .env and fill in real values

# 3. Launch everything
docker compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8080 |
| MongoDB | localhost:27017 |

---

## Local Development

### Backend

Requirements: Java 21, Maven 3.9+, running MongoDB

```bash
cd backend
# Set environment variables (see .env.example) or export them manually
export MONGODB_URI=mongodb://localhost:27017/guesthost
export JWT_SECRET=dev-secret-key-at-least-32-characters-long
mvn spring-boot:run
```

API base: `http://localhost:8080`

### Frontend

Requirements: Node 20+

```bash
cd frontend
npm install
npm start          # ng serve → http://localhost:4200
```

The Angular dev server proxies `/api/**` to `http://localhost:8080` via the nginx config in production.
For local dev, configure `src/proxy.conf.json` if you don't want CORS issues:

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  }
}
```

and add `"proxyConfig": "src/proxy.conf.json"` to `angular.json` serve options.

---

## Project Structure

```
happyguest/
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/guesthost/
│       ├── GuestHostApplication.java
│       ├── config/          SecurityConfig, AppConfig
│       ├── security/        JwtTokenProvider, JwtAuthenticationFilter, UserDetailsServiceImpl
│       ├── model/           User, Property, Booking, GuestRegistration, GuideItem,
│       │                    MinibarItem, MinibarOrder, OrderLine, GuestMessage
│       ├── repository/      (Spring Data MongoDB)
│       ├── dto/             Request/Response DTOs
│       ├── service/         AuthService, PropertyService, BookingService,
│       │                    RegistrationService, GuideService, MinibarService,
│       │                    OrderProcessingService, ICalSyncService,
│       │                    NotificationService, BillingService (Stripe + FastSpring),
│       │                    TranslationService, StripeClient
│       ├── controller/      Auth, Property, Booking, Registration, Guide, Minibar,
│       │                    Order, Message, Review, Translation, Billing, Webhook
│       └── exception/       GlobalExceptionHandler, ResourceNotFoundException
│
├── frontend/
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── tailwind.config.js
│   └── src/app/
│       ├── core/            AuthService, ApiService, AuthGuard, JwtInterceptor, ErrorInterceptor
│       ├── auth/            Login, Register
│       ├── host/dashboard/  DashboardComponent + tabs:
│       │                      PropertiesTab, BookingsTab, LogbookTab, GuidesTab,
│       │                      MinibarTab, FinancesTab, MessagesTab
│       │                    + dialogs: PropertyDialog, BookingDialog, GuideDialog,
│       │                      MinibarItemDialog
│       ├── guest/           GuestPortal, Registration (MatStepper), AccessCard,
│       │                    GuideGrid, GuideDetail, MinibarKiosk, CheckoutChecklist,
│       │                    ReviewDialog, Review, MessageBox
│       └── shared/          TypeScript models, TruncatePipe
│
├── docker-compose.yml
├── .env.example
└── .gitignore
```

---

## API Security

- **Public** (no auth): `/api/auth/**`, `/api/public/guest/**`, `/api/webhooks/**`, `/actuator/health`
- **Host-protected** (JWT Bearer): `/api/host/**`

---

## Subscription Tiers

| Tier | Price | Properties | Features |
|------|-------|-----------|---------|
| FREE | $0 | 1 | Text guides, Wi-Fi info |
| STANDARD | $9/mo | 3 | + Video guides, QR codes, iCal sync, Door codes, Translation |
| PRO | $29/mo | Unlimited | + Minibar, Accommodation Logbook, Pre-Review Filter, Custom Domain |

Tier limits are enforced server-side in `PropertyService`.

---

## Environment Variables Reference

| Variable | Description |
|----------|-------------|
| `MONGODB_URI` | MongoDB connection string |
| `JWT_SECRET` | HMAC-SHA256 signing key (≥ 32 chars) |
| `MAIL_HOST` / `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP credentials |
| `STRIPE_SECRET_KEY` | Stripe secret key (`sk_live_...` or `sk_test_...`) |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret (`whsec_...`) |
| `GOOGLE_TRANSLATE_API_KEY` | Google Cloud Translation API v2 key |
| `FASTSPRING_WEBHOOK_SECRET` | FastSpring webhook HMAC secret |
| `FRONTEND_URL` | Base URL used in email links |

---

## Stripe Setup

1. Create a Stripe account at https://stripe.com
2. Create Products & Prices for STANDARD and PRO tiers
3. Configure webhook endpoint → `https://yourdomain.com/api/webhooks/stripe`
4. Subscribe to events: `customer.subscription.updated`, `payment_intent.succeeded`

---

## License

MIT
