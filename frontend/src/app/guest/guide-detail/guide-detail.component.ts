import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';

import { ApiService } from '../../core/api.service';
import { GuestI18nService, GuestLang } from '../../core/guest-i18n.service';
import { GuideItem } from '../../shared/models';

@Component({
  selector: 'app-guide-detail',
  templateUrl: './guide-detail.component.html'
})
export class GuideDetailComponent implements OnInit {
  readonly languages = [
    { label: 'English', value: 'en' },
    { label: 'German', value: 'de' },
    { label: 'Čeština', value: 'cs' },
    { label: 'Polski', value: 'pl' }
  ];

  propertyId = '';
  slug = '';
  guide: GuideItem | null = null;
  selectedLanguage: GuestLang = 'en';
  renderedContent = '';
  embedUrl: SafeResourceUrl | null = null;
  videoKind: 'youtube' | 'vimeo' | 'mp4' | 'unknown' = 'unknown';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly apiService: ApiService,
    private readonly sanitizer: DomSanitizer,
    readonly i18n: GuestI18nService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.propertyId = params.get('propertyId') ?? '';
      this.slug = params.get('slug') ?? '';
      const requested = this.route.snapshot.queryParamMap.get('lang') ?? this.i18n.initializeFromBrowserLanguage();
      this.selectedLanguage = (['en', 'cs', 'de', 'pl'].includes(requested) ? requested : 'en') as GuestLang;
      this.i18n.setLanguage(this.selectedLanguage);
      this.loadGuide();
    });
  }

  translate(): void {
    this.i18n.setLanguage(this.selectedLanguage);
    this.loadGuide();
  }

  private loadGuide(): void {
    this.apiService.getPublicGuide(this.propertyId, this.slug, this.selectedLanguage).subscribe((guide) => {
      this.guide = guide;
      this.videoKind = this.detectVideoKind(guide.videoUrl);
      this.embedUrl = this.buildEmbedUrl(guide.videoUrl);
      this.renderedContent = this.renderMarkdown(guide.contentMarkdown);
    });
  }

  private detectVideoKind(url: string): 'youtube' | 'vimeo' | 'mp4' | 'unknown' {
    if (!url) {
      return 'unknown';
    }
    try {
      const parsed = new URL(url);
      const host = parsed.hostname.toLowerCase();
      if (host === 'www.youtube.com' || host === 'youtube.com' || host === 'youtu.be') {
        return 'youtube';
      }
      if (host === 'vimeo.com' || host === 'www.vimeo.com') {
        return 'vimeo';
      }
    } catch {
      // not a valid absolute URL — fall through
    }
    if (/\.mp4($|\?)/.test(url)) {
      return 'mp4';
    }
    return 'unknown';
  }

  private buildEmbedUrl(url: string): SafeResourceUrl | null {
    const kind = this.detectVideoKind(url);
    if (kind === 'youtube') {
      const videoId = url.includes('youtu.be/') ? url.split('youtu.be/')[1]?.split(/[?&]/)[0] : url.split('v=')[1]?.split('&')[0];
      return videoId ? this.sanitizer.bypassSecurityTrustResourceUrl(`https://www.youtube.com/embed/${videoId}`) : null;
    }
    if (kind === 'vimeo') {
      const videoId = url.split('/').filter(Boolean).pop();
      return videoId ? this.sanitizer.bypassSecurityTrustResourceUrl(`https://player.vimeo.com/video/${videoId}`) : null;
    }
    return null;
  }

  private renderMarkdown(markdown: string): string {
    return markdown
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/\n/g, '<br>');
  }
}
