import { Component, Input, OnChanges, OnDestroy, AfterViewInit, SimpleChanges, ElementRef, ViewChild } from '@angular/core';
import * as L from 'leaflet';
import { PropertyMapMarker } from '../models';

// Fix default marker icon paths broken by webpack
const iconDefault = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

const CATEGORY_COLORS: Record<string, string> = {
  Restaurant: '#ef4444',
  Trip: '#3b82f6',
  Hike: '#22c55e',
  Groceries: '#f59e0b',
  Other: '#8b5cf6',
};

@Component({
  selector: 'app-map',
  template: `<div #mapContainer class="w-full rounded overflow-hidden" style="height:400px"></div>`,
})
export class MapComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() markers: PropertyMapMarker[] = [];
  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef<HTMLDivElement>;

  private map?: L.Map;
  private leafletMarkers: L.Marker[] = [];

  ngAfterViewInit(): void {
    this.initMap();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['markers'] && this.map) {
      this.refreshMarkers();
    }
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  private initMap(): void {
    const center = this.getCenter();
    this.map = L.map(this.mapContainer.nativeElement).setView(center, 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      maxZoom: 19,
    }).addTo(this.map);
    this.refreshMarkers();
  }

  private getCenter(): [number, number] {
    if (this.markers?.length > 0) {
      return [this.markers[0].latitude, this.markers[0].longitude];
    }
    return [50.0755, 14.4378]; // Prague default
  }

  private refreshMarkers(): void {
    this.leafletMarkers.forEach(m => m.remove());
    this.leafletMarkers = [];
    if (!this.markers?.length || !this.map) return;

    const latLngs = this.markers.map(m => L.latLng(m.latitude, m.longitude));

    this.markers.forEach(m => {
      const color = CATEGORY_COLORS[m.category] ?? CATEGORY_COLORS['Other'];
      const icon = L.divIcon({
        className: '',
        html: `<div style="background:${color};width:12px;height:12px;border-radius:50%;border:2px solid #fff;box-shadow:0 1px 3px rgba(0,0,0,.4)"></div>`,
        iconSize: [12, 12],
        iconAnchor: [6, 6],
      });
      const marker = L.marker([m.latitude, m.longitude], { icon })
        .addTo(this.map!)
        .bindPopup(`<strong>${m.title}</strong><br><em>${m.category}</em>${m.description ? '<br>' + m.description : ''}`);
      this.leafletMarkers.push(marker);
    });

    const bounds = L.latLngBounds(latLngs);
    this.map.fitBounds(bounds, { padding: [40, 40] });
  }
}
