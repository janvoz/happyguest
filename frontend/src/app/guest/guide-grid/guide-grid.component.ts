import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';

import { ApiService } from '../../core/api.service';
import { GuestI18nService, GuestLang } from '../../core/guest-i18n.service';
import { GuideItem } from '../../shared/models';

@Component({
  selector: 'app-guide-grid',
  templateUrl: './guide-grid.component.html'
})
export class GuideGridComponent implements OnChanges {
  @Input({ required: true }) propertyId = '';
  @Input() language: GuestLang = 'en';

  guides: GuideItem[] = [];

  constructor(
    private readonly apiService: ApiService,
    readonly i18n: GuestI18nService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['propertyId']?.currentValue || changes['language']) {
      this.apiService.getPublicGuides(this.propertyId, this.language).subscribe((guides) => {
        this.guides = guides;
      });
    }
  }
}
