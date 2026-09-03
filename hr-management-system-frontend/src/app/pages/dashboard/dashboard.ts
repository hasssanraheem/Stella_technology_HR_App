import { Component, inject, OnInit, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { EmployeeService } from '../../core/services/employee.service';
import { LeaveService } from '../../core/services/leave.service';
import { PayrollService } from '../../core/services/payroll.service';
import { DatePipe, DecimalPipe } from '@angular/common';

function endAfterStart(ctrl: AbstractControl): ValidationErrors | null {
  const start = ctrl.get('startDate')?.value;
  const end   = ctrl.get('endDate')?.value;
  if (start && end && end < start) return { endBeforeStart: true };
  return null;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, DecimalPipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit {
  private auth     = inject(AuthService);
  private empSvc   = inject(EmployeeService);
  private leaveSvc = inject(LeaveService);
  private payroll$ = inject(PayrollService);
  private fb       = inject(FormBuilder);

  activeTab     = signal<'leave' | 'pending' | 'payroll' | 'profile'>('leave');
  employee      = signal<any>(null);
  leaves        = signal<any[]>([]);
  payroll       = signal<any[]>([]);
  leaveBalance  = signal<any>(null);
  showHistory   = signal(false);
  submitSuccess = signal(false);
  submitError   = signal('');
  pageLoading   = signal(true);

  readonly months = ['', 'January', 'February', 'March', 'April', 'May', 'June',
                     'July', 'August', 'September', 'October', 'November', 'December'];

  readonly leaveTypes = [
    { value: 'ANNUAL',  label: 'Annual Leave' },
    { value: 'CASUAL',  label: 'Casual Leave' },
    { value: 'SICK',    label: 'Sick Leave' },
    { value: 'UNPAID',  label: 'Unpaid Leave' }
  ];

  leaveForm = this.fb.group({
    leaveType: ['', Validators.required],
    startDate: ['', Validators.required],
    endDate:   ['', Validators.required],
    reason:    ['', [Validators.required, Validators.minLength(10)]]
  }, { validators: endAfterStart });

  get lf() { return this.leaveForm.controls; }

  ngOnInit() {
    this.empSvc.getMe().subscribe({
      next: emp => {
        this.employee.set(emp);
        this.loadLeaves(emp.employeeId);
        this.loadPayroll(emp.employeeId);
        this.loadBalance(emp.employeeId);
        this.pageLoading.set(false);
      },
      error: () => this.pageLoading.set(false)
    });
  }

  private loadLeaves(id: string) {
    this.leaveSvc.getMyLeaves(id).subscribe({ next: v => this.leaves.set(v), error: () => {} });
  }

  private loadPayroll(id: string) {
    this.payroll$.getMyPayroll(id).subscribe({ next: v => this.payroll.set(v), error: () => {} });
  }

  private loadBalance(id: string) {
    this.leaveSvc.getLeaveBalance(id).subscribe({ next: v => this.leaveBalance.set(v), error: () => {} });
  }

  switchTab(tab: 'leave' | 'pending' | 'payroll' | 'profile') {
    this.activeTab.set(tab);
    this.submitSuccess.set(false);
    this.submitError.set('');
  }

  submitLeave() {
    if (this.leaveForm.invalid) { this.leaveForm.markAllAsTouched(); return; }
    const emp = this.employee();
    if (!emp) return;

    const payload = { ...this.leaveForm.value, employeeId: emp.employeeId };
    this.leaveSvc.applyLeave(payload as any).subscribe({
      next: () => {
        this.submitSuccess.set(true);
        this.submitError.set('');
        this.leaveForm.reset();
        this.loadLeaves(emp.employeeId);
        setTimeout(() => this.submitSuccess.set(false), 5000);
      },
      error: err => {
        this.submitError.set(err.error?.message || 'Failed to submit leave request.');
      }
    });
  }

  get latestPayroll() {
    const list = this.payroll();
    return list.length ? list[list.length - 1] : null;
  }

  get payrollHistory() {
    const list = this.payroll();
    return list.length > 1 ? list.slice(0, list.length - 1).reverse() : [];
  }

  monthName(n: number) { return this.months[n] ?? ''; }

  statusClass(status: string) {
    const s = status?.toUpperCase();
    if (s === 'APPROVED' || s === 'PAID')   return 'badge-success';
    if (s === 'REJECTED' || s === 'UNPAID') return 'badge-danger';
    return 'badge-warning';
  }

  logout() { this.auth.logout(); }
}
