import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { LeaveService } from '../../../core/services/leave.service';
import { EmployeeService } from '../../../core/services/employee.service';

@Component({
  selector: 'app-hr-leaves',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './hr-leaves.html',
  styleUrl: './hr-leaves.css'
})
export class HrLeavesComponent implements OnInit {
  private leaveSvc = inject(LeaveService);
  private empSvc   = inject(EmployeeService);

  private allLeaves = signal<any[]>([]);
  employees         = signal<any[]>([]);
  balances          = signal<any[]>([]);
  loading           = signal(true);
  activeFilter      = signal('');
  leaveSearch       = signal('');
  balanceFilter     = signal('');
  balanceLoading    = signal(false);
  actionLoading     = signal('');

  leaves = computed(() => {
    const q = this.leaveSearch().trim().toLowerCase();
    if (!q) return this.allLeaves();
    return this.allLeaves().filter(l => {
      const emp = this.employees().find((e: any) => e.employeeId === l.employeeId);
      const name = emp ? emp.name.toLowerCase() : '';
      return name.includes(q) || l.employeeId.toLowerCase().includes(q);
    });
  });

  filteredBalances = computed(() => {
    const q = this.balanceFilter().trim().toLowerCase();
    if (!q) return this.balances();
    return this.balances().filter(b => {
      const emp = this.employees().find((e: any) => e.employeeId === b.employeeId);
      const name = emp ? emp.name.toLowerCase() : '';
      return name.includes(q) || b.employeeId.toLowerCase().includes(q);
    });
  });

  ngOnInit() {
    this.loadLeaves();
    this.empSvc.getAll(0, 200).subscribe({
      next: page => this.employees.set(page.content ?? []),
      error: () => {}
    });
  }

  loadLeaves() {
    this.loading.set(true);
    this.leaveSvc.getAllLeaves(this.activeFilter() || undefined).subscribe({
      next: v => { this.allLeaves.set(v); this.loading.set(false); },
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

  loadAllBalances() {
    this.balanceLoading.set(true);
    this.balanceFilter.set('');
    this.leaveSvc.getAllBalances().subscribe({
      next: v => { this.balances.set(v); this.balanceLoading.set(false); },
      error: () => this.balanceLoading.set(false)
    });
  }

  clearBalance() { this.balanceFilter.set(''); this.balances.set([]); }

  empName(id: string): string {
    const emp = this.employees().find((e: any) => e.employeeId === id);
    return emp ? emp.name : id;
  }

  statusClass(s: string) {
    if (s === 'APPROVED') return 'badge-success';
    if (s === 'REJECTED') return 'badge-danger';
    return 'badge-warning';
  }
}
