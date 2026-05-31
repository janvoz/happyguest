import { Injectable } from '@angular/core';
import { map, Observable, shareReplay } from 'rxjs';

import { ApiService, SubscriptionFeaturesResponse } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionFeatureService {
  private cachedFeatures?: Observable<SubscriptionFeaturesResponse>;

  constructor(private readonly apiService: ApiService) {}

  preload(): Observable<SubscriptionFeaturesResponse> {
    if (!this.cachedFeatures) {
      this.cachedFeatures = this.apiService.getSubscriptionFeatures().pipe(shareReplay(1));
    }
    return this.cachedFeatures;
  }

  hasFeature(featureId: string): Observable<boolean> {
    return this.preload().pipe(map((response) => response.features.includes(featureId)));
  }

  invalidate(): void {
    this.cachedFeatures = undefined;
  }
}
