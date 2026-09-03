import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { DepartmentService } from '../../../core/services/department.service';
import { EmployeeService } from '../../../core/services/employee.service';

@Component({
  selector: 'app-departments',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './departments.html',
  styleUrl: './departments.css'
})
export class DepartmentsComponent implements OnInit {
  private deptSvc = inject(DepartmentService);
  private empSvc  = inject(EmployeeService);
  private fb      = inject(FormBuilder);

  departments  = signal<any[]>([]);
  managers     = signal<any[]>([]);
  loading      = signal(true);
  searchQuery  = signal('');

  showAddModal  = signal(false);
  showEditModal = signal(false);
  showDeleteModal = signal(false);
  selected      = signal<any>(null);
  modalError    = signal('');
  modalLoading  = signal(false);

  deptForm = this.fb.group({
    name:        ['', Validators.required],
    description: [''],
    managerId:   ['']
  });

  ngOnInit() {
    this.loadDepartments();
    this.empSvc.getAll(0, 100, '', '').subscribe({
      next: page => {
        const mgrs = (page.content ?? []).filter((e: any) => e.employeeType === 'MANAGER');
        this.managers.set(mgrs);
      },
      error: () => {}
    });
  }

  loadDepartments() {
    this.loading.set(true);
    const obs = this.searchQuery()
      ? this.deptSvc.search(this.searchQuery())
      : this.deptSvc.getAll();
    obs.subscribe({ next: v => { this.departments.set(v); this.loading.set(false); }, error: () => this.loading.set(false) });
  }

  search() { this.loadDepartments(); }
  clearSearch() { this.searchQuery.set(''); this.loadDepartments(); }

  openAdd() { this.deptForm.reset(); this.modalError.set(''); this.showAddModal.set(true); }
  closeAdd() { this.showAddModal.set(false); }

  openEdit(dept: any) {
    this.selected.set(dept);
    this.deptForm.patchValue({ name: dept.name, description: dept.description, managerId: dept.managerId });
    this.modalError.set('');
    this.showEditModal.set(true);
  }
  closeEdit() { this.showEditModal.set(false); }

  openDelete(dept: any) { this.selected.set(dept); this.showDeleteModal.set(true); }
  closeDelete() { this.showDeleteModal.set(false); }

  submitAdd() {
    if (this.deptForm.invalid) { this.deptForm.markAllAsTouched(); return; }
    this.modalLoading.set(true);
    this.deptSvc.create(this.deptForm.value).subscribe({
      next: () => { this.modalLoading.set(false); this.closeAdd(); this.loadDepartments(); },
      error: err => { this.modalLoading.set(false); this.modalError.set(err.error?.message || 'Failed to create department.'); }
    });
  }

  submitEdit() {
    if (this.deptForm.invalid) { this.deptForm.markAllAsTouched(); return; }
    this.modalLoading.set(true);
    this.deptSvc.update(this.selected().departmentId, this.deptForm.value).subscribe({
      next: () => { this.modalLoading.set(false); this.closeEdit(); this.loadDepartments(); },
      error: err => { this.modalLoading.set(false); this.modalError.set(err.error?.message || 'Update failed.'); }
    });
  }

  confirmDelete() {
    this.deptSvc.delete(this.selected().departmentId).subscribe({
      next: () => { this.closeDelete(); this.loadDepartments(); },
      error: () => this.closeDelete()
    });
  }
}
