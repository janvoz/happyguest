import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatNativeDateModule } from '@angular/material/core';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { ErrorInterceptor } from './core/error.interceptor';
import { JwtInterceptor } from './core/jwt.interceptor';
import { AccessCardComponent } from './guest/access-card/access-card.component';
import { CheckoutChecklistComponent } from './guest/checkout-checklist/checkout-checklist.component';
import { GuideDetailComponent } from './guest/guide-detail/guide-detail.component';
import { GuideGridComponent } from './guest/guide-grid/guide-grid.component';
import { MessageBoxComponent } from './guest/message-box/message-box.component';
import { MinibarKioskComponent } from './guest/minibar-kiosk/minibar-kiosk.component';
import { GuestPortalComponent } from './guest/portal/guest-portal.component';
import { RegistrationComponent } from './guest/registration/registration.component';
import { ReviewDialogComponent } from './guest/review-dialog/review-dialog.component';
import { ReviewComponent } from './guest/review/review.component';
import { BookingDialogComponent } from './host/dashboard/booking-dialog/booking-dialog.component';
import { BookingsTabComponent } from './host/dashboard/bookings-tab/bookings-tab.component';
import { DashboardComponent } from './host/dashboard/dashboard.component';
import { FinancesTabComponent } from './host/dashboard/finances-tab/finances-tab.component';
import { GuideDialogComponent } from './host/dashboard/guide-dialog/guide-dialog.component';
import { GuidesTabComponent } from './host/dashboard/guides-tab/guides-tab.component';
import { LogbookTabComponent } from './host/dashboard/logbook-tab/logbook-tab.component';
import { MessagesTabComponent } from './host/dashboard/messages-tab/messages-tab.component';
import { MinibarItemDialogComponent } from './host/dashboard/minibar-item-dialog/minibar-item-dialog.component';
import { MinibarTabComponent } from './host/dashboard/minibar-tab/minibar-tab.component';
import { PropertiesTabComponent } from './host/dashboard/properties-tab/properties-tab.component';
import { PropertyDialogComponent } from './host/dashboard/property-dialog/property-dialog.component';
import { TemplateDialogComponent } from './host/dashboard/template-dialog/template-dialog.component';
import { TemplatesTabComponent } from './host/dashboard/templates-tab/templates-tab.component';
import { TruncatePipe } from './shared/pipes/truncate.pipe';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    DashboardComponent,
    PropertiesTabComponent,
    PropertyDialogComponent,
    BookingsTabComponent,
    BookingDialogComponent,
    LogbookTabComponent,
    GuidesTabComponent,
    GuideDialogComponent,
    MinibarTabComponent,
    MinibarItemDialogComponent,
    FinancesTabComponent,
    MessagesTabComponent,
    GuestPortalComponent,
    RegistrationComponent,
    AccessCardComponent,
    GuideGridComponent,
    GuideDetailComponent,
    MinibarKioskComponent,
    CheckoutChecklistComponent,
    ReviewDialogComponent,
    ReviewComponent,
    MessageBoxComponent,
    TruncatePipe,
    TemplatesTabComponent,
    TemplateDialogComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    AppRoutingModule,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCheckboxModule,
    MatDialogModule,
    MatSnackBarModule,
    MatStepperModule,
    MatProgressBarModule,
    MatChipsModule,
    MatMenuModule,
    MatBadgeModule,
    MatTooltipModule,
    MatAutocompleteModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
    { provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: { appearance: 'outline', subscriptSizing: 'dynamic' } }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
