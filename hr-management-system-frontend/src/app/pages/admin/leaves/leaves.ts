import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { LeaveService } from '../../../core/services/leave.service';

@Component({
  selector: 'app-leaves',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './leaves.html',
  styleUrl: './leaves.css'
})
export class LeavesComponent implements OnInit {
  private leaveSvc = inject(LeaveService);

  leaves       = signal<any[]>([]);
  balances     = signal<any[]>([]);
  loading      = signal(true);
  activeFilter = signal<string>('');
  balanceName  = signal('');
  balanceLoading = signal(false);
  actionLoading  = signal<string>('');

  readonly months = ['','January','February','March','April','May',
                     'June','July','August','September','October','November','December'];

  ngOnInit() { this.loadLeaves(); }

  loadLeaves() {
    this.loading.set(true);
    this.leaveSvc.getAllLeaves(this.activeFilter() || undefined).subscribe({
      next: v => { this.leaves.set(v); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  setFilter(f: string) { this.activeFilter.set(f); this.loadLeaves(); }

  approve(leave: any) {
    this.actionLoading.set(leave.leaveId);
    this.leaveSvc.updateLeaveStatus(leave.leaveId, 'APPROVED').subscribe({
      next: () => { this.actionLoading.set(''); this.loadLeaves(); },
      error: () => this.actionLoading.set('')
    });
  }

  reject(leave: any) {
    this.actionLoading.set(leave.leaveId + '_r');
    this.leaveSvc.updateLeaveStatus(leave.leaveId, 'REJECTED').subscribe({
      next: () => { this.actionLoading.set(''); this.loadLeaves(); },
      error: () => this.actionLoading.set('')
    });
  }

  searchBalance() {
    if (!this.balanceName().trim()) { this.balances.set([]); return; }
    this.balanceLoading.set(true);
    this.leaveSvc.getBalanceByName(this.balanceName()).subscribe({
      next: v => { this.balances.set(v); this.balanceLoading.set(false); },
      error: () => this.balanceLoading.set(false)
    });
  }

  clearBalance() { this.balanceName.set(''); this.balances.set([]); }

  statusClass(s: string) {
    if (s === 'APPROVED') return 'badge-success';
    if (s === 'REJECTED') return 'badge-danger';
    return 'badge-warning';
  }
}
