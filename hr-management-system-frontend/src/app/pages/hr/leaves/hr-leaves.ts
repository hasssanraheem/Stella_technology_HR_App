import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LeaveService } from '../../../core/services/leave.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-hr-leaves',
  standalone: true,
  imports: [DatePipe, FormsModule],
  templateUrl: './hr-leaves.html',
  styleUrl: './hr-leaves.css'
})
export class HrLeavesComponent implements OnInit {
  private leaveSvc = inject(LeaveService);
  private empSvc   = inject(EmployeeService);
  private authSvc  = inject(AuthService);

  activeTab        = signal<'approvals' | 'my-leave'>('approvals');

  // Department approvals
  private allLeaves = signal<any[]>([]);
  employees         = signal<any[]>([]);
  balances          = signal<any[]>([]);
  loading           = signal(true);
  activeFilter      = signal('');
  leaveSearch       = signal('');
  balanceFilter     = signal('');
  balanceLoading    = signal(false);
  actionLoading     = signal('');

  // My Leave
  myEmployeeId    = signal('');
  myLeaves        = signal<any[]>([]);
  myBalance       = signal<any>(null);
  myLeavesLoading = signal(false);
  applyLoading    = signal(false);
  applyError      = signal('');
  applySuccess    = signal('');

  // Form
  leaveType   = signal('ANNUAL');
  startDate   = signal('');
  endDate     = signal('');
  leaveReason = signal('');

  // Only non-HR employees' leaves in approvals
  leaves = computed(() => {
    const hrIds = new Set(
      this.employees().filter((e: any) => e.userRole === 'HR').map((e: any) => e.employeeId)
    );
    const q = this.leaveSearch().trim().toLowerCase();
    const list = this.allLeaves().filter(l => !hrIds.has(l.employeeId));
    if (!q) return list;
    return list.filter(l => {
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
      next: page => {
        const emps = page.content ?? [];
        this.employees.set(emps);
        const email = this.authSvc.getEmail() ?? '';
        const me = emps.find((e: any) => e.email?.toLowerCase() === email.toLowerCase());
        if (me) {
          this.myEmployeeId.set(me.employeeId);
          this.loadMyLeaves(me.employeeId);
          this.loadMyBalance(me.employeeId);
        }
      },
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

  loadMyLeaves(empId: string) {
    this.myLeavesLoading.set(true);
    this.leaveSvc.getMyLeaves(empId).subscribe({
      next: v => { this.myLeaves.set(v); this.myLeavesLoading.set(false); },
      error: () => this.myLeavesLoading.set(false)
    });
  }

  loadMyBalance(empId: string) {
    this.leaveSvc.getLeaveBalance(empId).subscribe({
      next: v => this.myBalance.set(v),
      error: () => {}
    });
  }

  submitLeave() {
    this.applyError.set('');
    this.applySuccess.set('');
    if (!this.startDate() || !this.endDate()) {
      this.applyError.set('Start date and end date are required.');
      return;
    }
    this.applyLoading.set(true);
    this.leaveSvc.applyLeave({
      leaveType: this.leaveType(),
      startDate: this.startDate(),
      endDate: this.endDate(),
      reason: this.leaveReason()
    }).subscribe({
      next: () => {
        this.applyLoading.set(false);
        this.applySuccess.set('Leave request submitted! Your leave will be reviewed by Admin.');
        this.startDate.set('');
        this.endDate.set('');
        this.leaveReason.set('');
        this.leaveType.set('ANNUAL');
        const id = this.myEmployeeId();
        if (id) { this.loadMyLeaves(id); this.loadMyBalance(id); }
      },
      error: (err: any) => {
        this.applyLoading.set(false);
        this.applyError.set(err?.error?.message ?? 'Failed to submit leave request.');
      }
    });
  }

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
