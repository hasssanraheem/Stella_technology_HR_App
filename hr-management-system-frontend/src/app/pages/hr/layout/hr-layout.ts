import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-hr-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './hr-layout.html',
  styleUrl: './hr-layout.css'
})
export class HrLayoutComponent {
  private auth = inject(AuthService);
  get email() { return this.auth.getEmail(); }
  logout() { this.auth.logout(); }
}
