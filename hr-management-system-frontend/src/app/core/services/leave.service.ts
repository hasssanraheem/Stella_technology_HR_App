import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class LeaveService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api';

  // Employee / HR
  applyLeave(payload: any)          { return this.http.post<any>(`${this.base}/leaves`, payload); }
  getMyLeaves(employeeId: string)   { return this.http.get<any[]>(`${this.base}/employees/${employeeId}/leaves`); }
  getLeaveBalance(employeeId: string) { return this.http.get<any>(`${this.base}/leaves/balance/${employeeId}`); }
  getMyLeaveHistory()               { return this.http.get<any[]>(`${this.base}/leaves`); }

  // Admin / HR
  getAllLeaves(status?: string) {
    const q = status ? `?status=${status}` : '';
    return this.http.get<any[]>(`${this.base}/leaves${q}`);
  }
  updateLeaveStatus(leaveId: string, status: string) {
    return this.http.put<any>(`${this.base}/leaves/${leaveId}/status`, { status });
  }
  getBalanceByName(name: string) {
    return this.http.get<any[]>(`${this.base}/leaves/balance?name=${encodeURIComponent(name)}`);
  }
  getAllBalances() {
    return this.http.get<any[]>(`${this.base}/leaves/balance`);
  }
}
