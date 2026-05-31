import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { environment } from '../../environments/environment';
import { User } from '../shared/models';

interface AuthResponse {
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly tokenKey = 'guesthost.token';
  private readonly authUrl = `${environment.apiUrl}/auth`;

  constructor(private readonly http: HttpClient) {}

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authUrl}/login`, { email, password }).pipe(
      tap((response) => localStorage.setItem(this.tokenKey, response.token))
    );
  }

  register(email: string, password: string, name: string): Observable<unknown> {
    return this.http.post(`${this.authUrl}/register`, { email, password, name });
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): User | null {
    const token = this.getToken();
    if (!token) {
      return null;
    }

    try {
      const payload = token.split('.')[1];
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
      const decoded = JSON.parse(atob(normalized));
      return {
        id: decoded.sub ?? decoded.id ?? '',
        email: decoded.email ?? '',
        subscriptionTier: decoded.subscriptionTier ?? 'FREE',
        subscriptionStatus: decoded.subscriptionStatus ?? 'inactive'
      } as User;
    } catch {
      return null;
    }
  }
}
