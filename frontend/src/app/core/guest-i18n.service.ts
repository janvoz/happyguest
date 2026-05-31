import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

import { ApiService } from './api.service';

export type GuestLang = 'en' | 'cs' | 'de' | 'pl';

const BASE_TEXTS: Record<string, string> = {
  gateTitle: 'Enter your booking reference',
  gateSubtitle: 'Use your booking reference and last name to unlock access.',
  bookingRef: 'Booking reference',
  lastName: 'Last name',
  continue: 'Continue',
  guestPortal: 'Guest Portal',
  portalHeadlineFallback: 'Your stay with HappyGuest',
  portalIntro: 'Access your door code, Wi-Fi, appliance guides, minibar ordering, and checkout checklist all in one place.',
  actionRequired: 'Action Required',
  registrationPrompt: 'Complete the guest registration form to reveal your Door Code and Wi-Fi password.',
  registerNow: 'Register Now',
  welcome: 'Welcome',
  stayReady: 'Hi {{name}}, your stay is ready.',
  stayDetails: 'Everything you need—from access details to how-to guides—is available below.',
  checkoutLabel: 'Check-out',
  emergencyTitle: 'Emergency & Rapid Contacts',
  emergencySubtitle: 'Tap to call immediately in case of emergency.',
  faqTitle: 'Frequently asked questions',
  faqSearchLabel: 'Search FAQ',
  faqSearchPlaceholder: 'Find answer quickly',
  faqNoMatch: 'No FAQ matches your search.',
  mapTitle: 'Interactive Local Tips Map',
  homeGuidesTitle: 'Home guides',
  homeGuidesSubtitle: 'Tap any guide for appliance instructions, videos, and quick troubleshooting tips.',
  guideSupport: 'Step-by-step instructions and support resources.',
  backToPortal: 'Back to portal',
  guideFollow: 'Follow each step carefully to get the most from your stay.',
  language: 'Language',
  bookingNotFound: 'Booking reference not found. Please try again.',
  registrationDone: 'Registration completed. Your stay details are now unlocked.'
};

const FALLBACK_TEXTS: Partial<Record<GuestLang, Partial<Record<string, string>>>> = {
  cs: {
    gateTitle: 'Zadejte referenci rezervace',
    gateSubtitle: 'Pro odemčení přístupu použijte číslo rezervace a příjmení.',
    bookingRef: 'Reference rezervace',
    lastName: 'Příjmení',
    continue: 'Pokračovat'
  },
  de: {
    gateTitle: 'Buchungsreferenz eingeben',
    gateSubtitle: 'Nutzen Sie Referenz und Nachnamen für den Zugang.',
    bookingRef: 'Buchungsreferenz',
    lastName: 'Nachname',
    continue: 'Weiter'
  },
  pl: {
    gateTitle: 'Wpisz numer rezerwacji',
    gateSubtitle: 'Użyj numeru rezerwacji i nazwiska, aby uzyskać dostęp.',
    bookingRef: 'Numer rezerwacji',
    lastName: 'Nazwisko',
    continue: 'Dalej'
  }
};

@Injectable({ providedIn: 'root' })
export class GuestI18nService {
  private readonly _language$ = new BehaviorSubject<GuestLang>((localStorage.getItem('guest-lang') as GuestLang) || 'en');
  readonly language$ = this._language$.asObservable();
  private readonly cache = new Map<GuestLang, Record<string, string>>([['en', BASE_TEXTS]]);
  private labels: Record<string, string> = BASE_TEXTS;

  constructor(private readonly api: ApiService) {}

  get currentLanguage(): GuestLang {
    return this._language$.value;
  }

  t(key: string): string {
    return this.labels[key] ?? BASE_TEXTS[key] ?? key;
  }

  initializeFromBrowserLanguage(): GuestLang {
    const persisted = localStorage.getItem('guest-lang');
    if (persisted && ['en', 'cs', 'de', 'pl'].includes(persisted)) {
      const chosen = persisted as GuestLang;
      this.setLanguage(chosen);
      return chosen;
    }
    const browserLang = (navigator.language || 'en').slice(0, 2).toLowerCase();
    const selected = (['cs', 'de', 'pl'].includes(browserLang) ? browserLang : 'en') as GuestLang;
    this.setLanguage(selected);
    return selected;
  }

  setLanguage(lang: GuestLang): void {
    this._language$.next(lang);
    localStorage.setItem('guest-lang', lang);
    if (lang === 'en') {
      this.labels = BASE_TEXTS;
      return;
    }
    const fromCache = this.cache.get(lang);
    if (fromCache) {
      this.labels = fromCache;
      return;
    }

    const keys = Object.keys(BASE_TEXTS);
    const values = keys.map((key) => BASE_TEXTS[key]);
    this.api.translateBatch(values, lang).subscribe({
      next: (translated) => {
        const mapped: Record<string, string> = {};
        keys.forEach((key, index) => {
          mapped[key] = translated[index] ?? BASE_TEXTS[key];
        });
        const fallback = FALLBACK_TEXTS[lang] ?? {};
        const complete: Record<string, string> = { ...BASE_TEXTS };
        Object.entries(fallback).forEach(([key, value]) => {
          if (value) {
            complete[key] = value;
          }
        });
        Object.entries(mapped).forEach(([key, value]) => {
          complete[key] = value;
        });
        this.cache.set(lang, complete);
        this.labels = complete;
      },
      error: () => {
        const fallback = FALLBACK_TEXTS[lang] ?? {};
        const complete: Record<string, string> = { ...BASE_TEXTS };
        Object.entries(fallback).forEach(([key, value]) => {
          if (value) {
            complete[key] = value;
          }
        });
        this.labels = complete;
      }
    });
  }
}
