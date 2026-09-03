import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private base = 'http://localhost:8080/api/auth';

  login(email: string, password: string) {
    return this.http.post<{ token: string; email: string; role: string; message: string }>(
      `${this.base}/login`, { email, password }
    ).pipe(
      tap(res => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('role', res.role);
        localStorage.setItem('email', res.email);
      })
    );
  }

  register(email: string, password: string, role: string) {
    return this.http.post<any>(`${this.base}/register`, { email, password, role });
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  getToken() { return localStorage.getItem('token'); }
  getRole()  { return localStorage.getItem('role'); }
  getEmail() { return localStorage.getItem('email'); }
  isLoggedIn() { return !!this.getToken(); }
}
