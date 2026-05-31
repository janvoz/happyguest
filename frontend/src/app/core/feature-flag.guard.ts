import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { map, Observable, of } from 'rxjs';

import { SubscriptionFeatureService } from './subscription-feature.service';

@Injectable({
  providedIn: 'root'
})
export class FeatureFlagGuard implements CanActivate {
  constructor(
    private readonly featureService: SubscriptionFeatureService,
    private readonly router: Router
  ) {}

  canActivate(route: import('@angular/router').ActivatedRouteSnapshot): Observable<boolean> {
    const featureId = route.data['requiredFeature'] as string | undefined;
    if (!featureId) {
      return of(true);
    }
    return this.featureService.hasFeature(featureId).pipe(
      map((enabled) => {
        if (!enabled) {
          void this.router.navigate(['/dashboard']);
        }
        return enabled;
      })
    );
  }
}
