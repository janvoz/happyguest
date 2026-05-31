import { Injectable } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable, tap } from 'rxjs';

import { Property } from '../shared/models';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class CurrentPropertyService {
  private readonly propertiesSubject = new BehaviorSubject<Property[]>([]);
  private readonly selectedPropertyIdSubject = new BehaviorSubject<string>('');

  readonly properties$ = this.propertiesSubject.asObservable();
  readonly selectedPropertyId$ = this.selectedPropertyIdSubject.asObservable();
  readonly selectedProperty$ = combineLatest([this.properties$, this.selectedPropertyId$]).pipe(
    map(([properties, selectedPropertyId]) => properties.find((property) => property.id === selectedPropertyId) ?? null)
  );

  constructor(private readonly apiService: ApiService) {}

  refreshProperties(): Observable<Property[]> {
    return this.apiService.getProperties().pipe(
      tap((properties) => this.setProperties(properties))
    );
  }

  setSelectedProperty(propertyId: string): void {
    this.selectedPropertyIdSubject.next(propertyId);
  }

  setProperties(properties: Property[]): void {
    this.propertiesSubject.next(properties);
    const selected = this.selectedPropertyIdSubject.value;
    const hasSelected = properties.some((property) => property.id === selected);
    this.selectedPropertyIdSubject.next(hasSelected ? selected : properties[0]?.id ?? '');
  }
}
