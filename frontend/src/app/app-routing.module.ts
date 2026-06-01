import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { AuthGuard } from './core/auth.guard';
import { FeatureFlagGuard } from './core/feature-flag.guard';
import { GuideDetailComponent } from './guest/guide-detail/guide-detail.component';
import { GuestPortalComponent } from './guest/portal/guest-portal.component';
import { ReviewComponent } from './guest/review/review.component';
import { DashboardComponent } from './host/dashboard/dashboard.component';
import { WelcomeSignComponent } from './host/welcome-sign/welcome-sign.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'dashboard/minibar', component: DashboardComponent, canActivate: [AuthGuard, FeatureFlagGuard], data: { requiredFeature: 'MINIBAR' } },
  { path: 'host/welcome-sign/:propertyId', component: WelcomeSignComponent, canActivate: [AuthGuard] },
  { path: 'guest/portal/:propertyId', component: GuestPortalComponent },
  { path: 'guest/portal/:propertyId/guide/:slug', component: GuideDetailComponent },
  { path: 'guest/review/:bookingId', component: ReviewComponent },
  { path: '**', redirectTo: 'dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
