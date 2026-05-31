import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable } from 'rxjs';

import { AuthService } from './auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private readonly authService: AuthService) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authService.getToken();
    const isHostApi = /\/(?:api\/)?host\//.test(req.url);

    if (token && isHostApi) {
      req = req.clone({
        setHeaders: {
          Authorization: 'Bearer ' + token
        }
      });
    }

    return next.handle(req);
  }
}
