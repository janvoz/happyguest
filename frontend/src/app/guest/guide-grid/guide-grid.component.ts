import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';

import { ApiService } from '../../core/api.service';
import { GuideItem } from '../../shared/models';

@Component({
  selector: 'app-guide-grid',
  templateUrl: './guide-grid.component.html'
})
export class GuideGridComponent implements OnChanges {
  @Input({ required: true }) propertyId = '';

  guides: GuideItem[] = [];

  constructor(private readonly apiService: ApiService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['propertyId']?.currentValue) {
      this.apiService.getPublicGuides(this.propertyId).subscribe((guides) => {
        this.guides = guides;
      });
    }
  }
}
