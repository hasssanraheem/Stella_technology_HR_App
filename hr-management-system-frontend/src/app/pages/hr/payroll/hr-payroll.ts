import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { PayrollService } from '../../../core/services/payroll.service';
import { EmployeeService } from '../../../core/services/employee.service';

@Component({
  selector: 'app-hr-payroll',
  standalone: true,
  imports: [ReactiveFormsModule, DecimalPipe],
  templateUrl: './hr-payroll.html',
  styleUrl: './hr-payroll.css'
})
export class HrPayrollComponent implements OnInit {
  private payrollSvc = inject(PayrollService);
  private empSvc     = inject(EmployeeService);
  private fb         = inject(FormBuilder);

  payrolls      = signal<any[]>([]);
  employees     = signal<any[]>([]);
  loading       = signal(true);
  genLoading    = signal(false);
  genError      = signal('');
  genSuccess    = signal('');
  actionLoading = signal('');

  filterMonth  = signal('');
  filterYear   = signal('');
  filterStatus = signal('');

  readonly months = ['','January','February','March','April','May','June',
                     'July','August','September','October','November','December'];
  readonly currentYear = new Date().getFullYear();
  readonly years = Array.from({ length: 5 }, (_, i) => this.currentYear - i);

  genForm = this.fb.group({
    employeeId: ['', Validators.required],
    month:      [null as number | null, [Validators.required, Validators.min(1), Validators.max(12)]],
    year:       [null as number | null, [Validators.required, Validators.min(2000)]]
  });

  ngOnInit() {
    this.loadPayroll();
    this.empSvc.getMe().subscribe({
      next: me => {
        this.empSvc.getAll(0, 200, '', '', '', me.departmentId).subscribe({
          next: page => this.employees.set(page.content ?? []),
          error: () => {}
        });
      },
      error: () => {}
    });
  }

  loadPayroll() {
    this.loading.set(true);
    const m = this.filterMonth() ? +this.filterMonth() : undefined;
    const y = this.filterYear()  ? +this.filterYear()  : undefined;
    const s = this.filterStatus() || undefined;
    this.payrollSvc.getAll(m, y, s).subscribe({
      next: v => { this.payrolls.set(v); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  applyFilter() { this.loadPayroll(); }
  clearFilter() { this.filterMonth.set(''); this.filterYear.set(''); this.filterStatus.set(''); this.loadPayroll(); }

  submitGenerate() {
    if (this.genForm.invalid) { this.genForm.markAllAsTouched(); return; }
    this.genLoading.set(true);
    this.genError.set('');
    this.genSuccess.set('');
    const v = this.genForm.value;
    this.payrollSvc.generate(v.employeeId!, +v.month!, +v.year!).subscribe({
      next: () => {
        this.genLoading.set(false);
        this.genSuccess.set('Payroll generated successfully!');
        this.genForm.reset();
        this.loadPayroll();
        setTimeout(() => this.genSuccess.set(''), 4000);
      },
      error: err => { this.genLoading.set(false); this.genError.set(err.error?.message || 'Failed to generate payroll.'); }
    });
  }

  markPaid(p: any) {
    const newStatus = p.paymentStatus === 'PAID' ? 'UNPAID' : 'PAID';
    this.actionLoading.set(p.payrollId);
    this.payrollSvc.updatePaymentStatus(p.payrollId, newStatus).subscribe({
      next: () => { this.actionLoading.set(''); this.loadPayroll(); },
      error: () => this.actionLoading.set('')
    });
  }

  monthName(n: number) { return this.months[n] ?? n; }
  statusClass(s: string) { return s === 'PAID' ? 'badge-success' : 'badge-danger'; }
  empName(id: string) {
    const emp = this.employees().find(e => e.employeeId === id);
    return emp ? emp.name : id;
  }
}
