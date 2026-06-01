import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

export type HostLang = 'en' | 'cs';

@Injectable({ providedIn: 'root' })
export class HostI18nService {
  private readonly _lang$ = new BehaviorSubject<HostLang>((localStorage.getItem('host-lang') as HostLang) || 'en');
  readonly lang$ = this._lang$.asObservable();

  constructor(private readonly translate: TranslateService) {
    this.translate.addLangs(['en', 'cs']);
    this.translate.setDefaultLang('en');
    this.translate.use(`host-${this._lang$.value}`);
  }

  get currentLang(): HostLang {
    return this._lang$.getValue();
  }

  setLang(lang: HostLang): void {
    this._lang$.next(lang);
    localStorage.setItem('host-lang', lang);
    this.translate.use(`host-${lang}`);
  }

  t(key: string): string {
    return this.translate.instant(key) || key;
  }
}
