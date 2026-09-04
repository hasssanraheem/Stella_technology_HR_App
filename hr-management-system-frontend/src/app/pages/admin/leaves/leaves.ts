import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { LeaveService } from '../../../core/services/leave.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { DepartmentService } from '../../../core/services/department.service';

@Component({
  selector: 'app-leaves',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './leaves.html',
  styleUrl: './leaves.css'
})
export class LeavesComponent implements OnInit {
  private leaveSvc = inject(LeaveService);
  private empSvc   = inject(EmployeeService);
  private deptSvc  = inject(DepartmentService);

  private allLeaves  = signal<any[]>([]);
  employees          = signal<any[]>([]);
  departments        = signal<any[]>([]);
  balances           = signal<any[]>([]);
  loading            = signal(true);
  activeFilter       = signal<string>('');
  leaveSearch        = signal('');
  filterDept         = signal('');
  balanceFilter      = signal('');
  balanceLoading     = signal(false);
  actionLoading      = signal<string>('');

  leaves = computed(() => {
    const q = this.leaveSearch().trim().toLowerCase();
    if (!q) return this.allLeaves();
    const emps = this.employees();
    return this.allLeaves().filter(l => {
      const emp = emps.find((e: any) => e.employeeId === l.employeeId);
      const name = emp ? emp.name.toLowerCase() : '';
      return name.includes(q) || l.employeeId.toLowerCase().includes(q);
    });
  });

  displayedLeaves = computed(() => {
    const dept = this.filterDept();
    const base = this.leaves();
    if (!dept) return base;
    const empIds = new Set(
      this.employees().filter((e: any) => e.departmentId === dept).map((e: any) => e.employeeId)
    );
    return base.filter(l => empIds.has(l.employeeId));
  });

  filteredBalances = computed(() => {
    const q = this.balanceFilter().trim().toLowerCase();
    if (!q) return this.balances();
    const emps = this.employees();
    return this.balances().filter(b => {
      const emp = emps.find((e: any) => e.employeeId === b.employeeId);
      const name = emp ? emp.name.toLowerCase() : '';
      return name.includes(q) || b.employeeId.toLowerCase().includes(q);
    });
  });

  readonly months = ['','January','February','March','April','May',
                     'June','July','August','September','October','November','December'];

  ngOnInit() {
    this.loadLeaves();
    this.empSvc.getAll(0, 200, '').subscribe({
      next: page => this.employees.set(page.content ?? []),
      error: () => {}
    });
    this.deptSvc.getAll().subscribe({
      next: v => this.departments.set(v),
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

  deptNameForLeave(empId: string): string {
    const emp = this.employees().find((e: any) => e.employeeId === empId);
    if (!emp) return '—';
    const dept = this.departments().find((d: any) => d.departmentId === emp.departmentId);
    return dept ? dept.name : '—';
  }

  hrNameForLeave(empId: string): string {
    const emp = this.employees().find((e: any) => e.employeeId === empId);
    if (!emp) return '—';
    const dept = this.departments().find((d: any) => d.departmentId === emp.departmentId);
    if (!dept?.hrId) return '—';
    const hr = this.employees().find((e: any) => e.employeeId === dept.hrId);
    return hr ? hr.name : '—';
  }

  statusClass(s: string) {
    if (s === 'APPROVED') return 'badge-success';
    if (s === 'REJECTED') return 'badge-danger';
    return 'badge-warning';
  }
}
