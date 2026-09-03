import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class DepartmentService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/departments';

  getAll()                        { return this.http.get<any[]>(this.base); }
  getById(id: string)             { return this.http.get<any>(`${this.base}/${id}`); }
  create(data: any)               { return this.http.post<any>(this.base, data); }
  update(id: string, data: any)   { return this.http.put<any>(`${this.base}/${id}`, data); }
  delete(id: string)              { return this.http.delete<void>(`${this.base}/${id}`); }
  search(name: string)            { return this.http.get<any[]>(`${this.base}/search?name=${encodeURIComponent(name)}`); }
}
