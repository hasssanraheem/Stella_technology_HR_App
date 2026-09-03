import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api';

  // Shared
  getMe()           { return this.http.get<any>(`${this.base}/employees/me`); }
  getById(id: string) { return this.http.get<any>(`${this.base}/employees/${id}`); }
  getLeaves(id: string)  { return this.http.get<any[]>(`${this.base}/employees/${id}/leaves`); }
  getPayroll(id: string) { return this.http.get<any[]>(`${this.base}/employees/${id}/payroll`); }
  getHistory(id: string, type?: string) {
    const q = type ? `?type=${type}` : '';
    return this.http.get<any[]>(`${this.base}/employees/${id}/history${q}`);
  }

  // Admin
  getAll(page = 0, size = 20, name = '', status = '') {
    const params: any = { page, size };
    if (name)   params['name']   = name;
    if (status) params['status'] = status;
    return this.http.get<any>(`${this.base}/employees`, { params });
  }
  create(data: any)             { return this.http.post<any>(`${this.base}/employees`, data); }
  update(id: string, data: any) { return this.http.put<any>(`${this.base}/employees/${id}`, data); }
  delete(id: string)            { return this.http.delete<void>(`${this.base}/employees/${id}`); }
  updateType(id: string, type: string) {
    return this.http.patch<any>(`${this.base}/employees/${id}/type?employeeType=${type}`, {});
  }
  addNote(id: string, note: string) {
    return this.http.post<any>(`${this.base}/employees/${id}/performance-notes`, { note });
  }
}
