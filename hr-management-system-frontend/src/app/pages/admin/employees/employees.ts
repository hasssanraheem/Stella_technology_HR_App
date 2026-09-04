import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { DepartmentService } from '../../../core/services/department.service';

@Component({
  selector: 'app-employees',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './employees.html',
  styleUrl: './employees.css'
})
export class EmployeesComponent implements OnInit {
  private auth    = inject(AuthService);
  private empSvc  = inject(EmployeeService);
  private deptSvc = inject(DepartmentService);
  private fb      = inject(FormBuilder);

  employees   = signal<any[]>([]);
  departments = signal<any[]>([]);

  filterType = signal<string>('');

  unassignedCount = computed(() =>
    this.employees().filter(e => e.departmentId === 'DEPT-0000').length
  );

  assignableDepts = computed(() =>
    this.departments().filter(d => d.departmentId !== 'DEPT-0000')
  );

  // Client-side filter for EMPLOYEE/MANAGER types; HR is server-side via userRole
  displayedEmployees = computed(() => {
    const t = this.filterType();
    if (!t || t === 'HR') return this.employees();
    return this.employees().filter(e => e.employeeType === t);
  });
  loading     = signal(true);
  searchName  = signal('');

  showAddModal    = signal(false);
  showEditModal   = signal(false);
  showTypeModal   = signal(false);
  showNoteModal   = signal(false);
  showHistoryModal = signal(false);
  showDeleteModal = signal(false);

  selected    = signal<any>(null);
  history     = signal<any[]>([]);
  modalError  = signal('');
  modalLoading = signal(false);

  readonly genders       = ['MALE', 'FEMALE'];
  readonly roles         = ['EMPLOYEE', 'HR'];
  readonly employeeTypes = ['EMPLOYEE', 'MANAGER', 'HR'];
  readonly months = ['','January','February','March','April','May','June',
                     'July','August','September','October','November','December'];

  addForm = this.fb.group({
    email:       ['', [Validators.required, Validators.email]],
    password:    ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/)]],
    role:        ['EMPLOYEE', Validators.required],
    name:        ['', Validators.required],
    phone:       ['', Validators.required],
    gender:      ['MALE', Validators.required],
    dateOfBirth: ['', Validators.required],
    address:     ['', Validators.required],
    departmentId:[''],
    designation: ['', Validators.required],
    dateOfJoining:['', Validators.required],
    salary:      [0, [Validators.required, Validators.min(0)]],
    allowances:  [0],
    deductions:  [0],
    employeeType:['EMPLOYEE']
  });

  editForm = this.fb.group({
    name:        ['', Validators.required],
    email:       ['', [Validators.required, Validators.email]],
    phone:       ['', Validators.required],
    gender:      ['MALE', Validators.required],
    dateOfBirth: ['', Validators.required],
    address:     ['', Validators.required],
    departmentId:['', Validators.required],
    designation: ['', Validators.required],
    dateOfJoining:['', Validators.required],
    salary:      [0, [Validators.required, Validators.min(0)]],
    allowances:  [0],
    deductions:  [0],
    employeeType:['EMPLOYEE']
  });

  typeForm = this.fb.group({ employeeType: ['EMPLOYEE', Validators.required] });
  noteForm = this.fb.group({ note: ['', [Validators.required, Validators.minLength(10)]] });

  ngOnInit() {
    this.loadEmployees();
    this.deptSvc.getAll().subscribe({ next: v => this.departments.set(v), error: () => {} });
  }

  loadEmployees() {
    this.loading.set(true);
    const role = this.filterType() === 'HR' ? 'HR' : '';
    this.empSvc.getAll(0, 50, this.searchName(), '', role).subscribe({
      next: page => { this.employees.set(page.content ?? []); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  search() { this.loadEmployees(); }
  clearSearch() { this.searchName.set(''); this.loadEmployees(); }

  setTypeFilter(t: string) { this.filterType.set(t); this.loadEmployees(); }

  openAdd() { this.addForm.reset({ role: 'EMPLOYEE', gender: 'MALE', employeeType: 'EMPLOYEE', salary: 0, allowances: 0, deductions: 0 }); this.modalError.set(''); this.showAddModal.set(true); }
  closeAdd() { this.showAddModal.set(false); }

  openEdit(emp: any) {
    this.selected.set(emp);
    this.editForm.patchValue({
      name: emp.name, email: emp.email, phone: emp.phone, gender: emp.gender,
      dateOfBirth: emp.dateOfBirth, address: emp.address, departmentId: emp.departmentId,
      designation: emp.designation, dateOfJoining: emp.dateOfJoining,
      salary: emp.salary, allowances: emp.allowances, deductions: emp.deductions,
      employeeType: emp.employeeType
    });
    this.modalError.set('');
    this.showEditModal.set(true);
  }
  closeEdit() { this.showEditModal.set(false); }

  openType(emp: any) { this.selected.set(emp); this.typeForm.patchValue({ employeeType: emp.employeeType }); this.modalError.set(''); this.showTypeModal.set(true); }
  closeType() { this.showTypeModal.set(false); }

  openNote(emp: any) { this.selected.set(emp); this.noteForm.reset(); this.modalError.set(''); this.showNoteModal.set(true); }
  closeNote() { this.showNoteModal.set(false); }

  openHistory(emp: any) {
    this.selected.set(emp);
    this.history.set([]);
    this.showHistoryModal.set(true);
    this.empSvc.getHistory(emp.employeeId).subscribe({ next: v => this.history.set(v), error: () => {} });
  }
  closeHistory() { this.showHistoryModal.set(false); }

  openDelete(emp: any) { this.selected.set(emp); this.showDeleteModal.set(true); }
  closeDelete() { this.showDeleteModal.set(false); }

  submitAdd() {
    if (this.addForm.invalid) { this.addForm.markAllAsTouched(); return; }
    this.modalLoading.set(true);
    const v = this.addForm.value;
    this.auth.register(v.email!, v.password!, v.role!).subscribe({
      next: () => {
        const empData = { name: v.name, email: v.email, phone: v.phone, gender: v.gender,
          dateOfBirth: v.dateOfBirth, address: v.address, departmentId: v.departmentId || 'DEPT-0000',
          designation: v.designation, dateOfJoining: v.dateOfJoining,
          salary: v.salary, allowances: v.allowances, deductions: v.deductions, employeeType: v.employeeType,
          userRole: v.role };
        this.empSvc.create(empData).subscribe({
          next: () => { this.modalLoading.set(false); this.closeAdd(); this.loadEmployees(); },
          error: err => { this.modalLoading.set(false); this.modalError.set(err.error?.message || 'Failed to create employee profile.'); }
        });
      },
      error: err => { this.modalLoading.set(false); this.modalError.set(err.error?.message || 'Failed to register user account.'); }
    });
  }

  submitEdit() {
    if (this.editForm.invalid) { this.editForm.markAllAsTouched(); return; }
    this.modalLoading.set(true);
    this.empSvc.update(this.selected().employeeId, this.editForm.value).subscribe({
      next: () => { this.modalLoading.set(false); this.closeEdit(); this.loadEmployees(); },
      error: err => { this.modalLoading.set(false); this.modalError.set(err.error?.message || 'Update failed.'); }
    });
  }

  submitType() {
    this.modalLoading.set(true);
    this.empSvc.updateType(this.selected().employeeId, this.typeForm.value.employeeType!).subscribe({
      next: () => { this.modalLoading.set(false); this.closeType(); this.loadEmployees(); },
      error: err => { this.modalLoading.set(false); this.modalError.set(err.error?.message || 'Failed to update type.'); }
    });
  }

  submitNote() {
    if (this.noteForm.invalid) { this.noteForm.markAllAsTouched(); return; }
    this.modalLoading.set(true);
    this.empSvc.addNote(this.selected().employeeId, this.noteForm.value.note!).subscribe({
      next: () => { this.modalLoading.set(false); this.closeNote(); },
      error: err => { this.modalLoading.set(false); this.modalError.set(err.error?.message || 'Failed to add note.'); }
    });
  }

  confirmDelete() {
    this.empSvc.delete(this.selected().employeeId).subscribe({
      next: () => { this.closeDelete(); this.loadEmployees(); },
      error: () => this.closeDelete()
    });
  }

  deptName(id: string) {
    if (!id) return '—';
    const dept = this.departments().find(d => d.departmentId === id);
    return dept ? dept.name : id;
  }

  statusClass(s: string) {
    if (s === 'ACTIVE' || s === 'MANAGER') return 'badge-success';
    if (s === 'INACTIVE') return 'badge-danger';
    return 'badge-gray';
  }
  typeClass(t: string) { return t === 'MANAGER' ? 'badge-blue' : 'badge-gray'; }
  historyIcon(t: string) { return t === 'PROMOTION' ? '🏅' : t === 'DESIGNATION_CHANGE' ? '🔄' : '📝'; }
}
