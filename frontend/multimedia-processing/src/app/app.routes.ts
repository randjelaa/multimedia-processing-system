import { Routes } from '@angular/router';

import { LoginComponent } from './features/login/login.component';

import { DashboardComponent } from './features/dashboard/dashboard.component';

import { UploadComponent } from './features/upload/upload.component';

import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
  },

  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard],
  },

  {
    path: 'upload',
    component: UploadComponent,
    canActivate: [authGuard],
  },

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
];
