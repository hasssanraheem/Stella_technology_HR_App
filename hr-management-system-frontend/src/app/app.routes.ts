import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then(m => m.LoginComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./pages/dashboard/dashboard').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'admin',
    loadComponent: () => import('./pages/admin/layout/admin-layout').then(m => m.AdminLayoutComponent),
    canActivate: [adminGuard],
    children: [
      { path: '', redirectTo: 'employees', pathMatch: 'full' },
      { path: 'employees',   loadComponent: () => import('./pages/admin/employees/employees').then(m => m.EmployeesComponent) },
      { path: 'departments', loadComponent: () => import('./pages/admin/departments/departments').then(m => m.DepartmentsComponent) },
      { path: 'leaves',      loadComponent: () => import('./pages/admin/leaves/leaves').then(m => m.LeavesComponent) },
      { path: 'payroll',     loadComponent: () => import('./pages/admin/payroll/payroll').then(m => m.PayrollComponent) }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
