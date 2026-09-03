import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class PayrollService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api';

  // Employee
  getMyPayroll(employeeId: string) { return this.http.get<any[]>(`${this.base}/employees/${employeeId}/payroll`); }

  // Admin / HR
  generate(employeeId: string, month: number, year: number) {
    return this.http.post<any>(`${this.base}/payroll/generate`, { employeeId, month, year });
  }
  getAll(month?: number, year?: number, status?: string) {
    const params: any = {};
    if (month)  params['month']  = month;
    if (year)   params['year']   = year;
    if (status) params['status'] = status;
    return this.http.get<any[]>(`${this.base}/payroll`, { params });
  }
  updatePaymentStatus(payrollId: string, status: string) {
    return this.http.patch<any>(`${this.base}/payroll/${payrollId}/status?status=${status}`, {});
  }
}
