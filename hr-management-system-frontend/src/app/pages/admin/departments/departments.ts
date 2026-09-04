import { Component, computed, inject, OnInit, signal } from '@angular/core';
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
  hrEmployees  = signal<any[]>([]);
  loading      = signal(true);
  searchQuery  = signal('');

  showAddModal    = signal(false);
  showEditModal   = signal(false);
  showDeleteModal = signal(false);

  selected        = signal<any>(null);
  modalError      = signal('');
  modalLoading    = signal(false);

  // Reassign-all modal (for Unassigned dept)
  showReassignModal    = signal(false);
  reassignTargetId     = signal('');
  reassignLoading      = signal(false);
  reassignError        = signal('');
  reassignEmpCount     = signal(0);
  reassignCountLoading = signal(false);

  nonSystemDepts = computed(() =>
    this.departments().filter(d => !d.isSystem)
  );

  // Delete flow state
  deleteStep      = signal<'confirm' | 'reassign'>('confirm');
  deleteEmpCount  = signal(0);
  deleteCountLoading = signal(false);
  moveToUnassigned   = signal(true);
  targetDeptId       = signal('');

  // Departments available as reassignment targets (excludes the one being deleted + system dept shown separately)
  reassignTargets = computed(() =>
    this.departments().filter(d =>
      d.departmentId !== this.selected()?.departmentId && !d.isSystem
    )
  );

  deptForm = this.fb.group({
    name:        ['', Validators.required],
    description: [''],
    managerId:   [''],
    hrId:        ['', Validators.required]
  });

  ngOnInit() {
    this.loadDepartments();
    this.empSvc.getAll(0, 200, '', '').subscribe({
      next: page => {
        const all = page.content ?? [];
        this.managers.set(all.filter((e: any) => e.employeeType === 'MANAGER'));
        this.hrEmployees.set(all.filter((e: any) => e.userRole === 'HR'));
      },
      error: () => {}
    });
  }

  loadDepartments() {
    this.loading.set(true);
    const obs = this.searchQuery()
      ? this.deptSvc.search(this.searchQuery())
      : this.deptSvc.getAll();
    obs.subscribe({
      next: v => { this.departments.set(v); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  search() { this.loadDepartments(); }
  clearSearch() { this.searchQuery.set(''); this.loadDepartments(); }

  openReassign() {
    this.reassignTargetId.set('');
    this.reassignError.set('');
    this.reassignEmpCount.set(0);
    this.reassignCountLoading.set(true);
    this.showReassignModal.set(true);
    this.deptSvc.getEmployeeCount('DEPT-0000').subscribe({
      next: count => { this.reassignEmpCount.set(count); this.reassignCountLoading.set(false); },
      error: () => this.reassignCountLoading.set(false)
    });
  }
  closeReassign() { this.showReassignModal.set(false); }

  managerName(id: string): string {
    if (!id) return '—';
    const m = this.managers().find(m => m.employeeId === id);
    return m ? m.name : id;
  }

  hrName(id: string): string {
    if (!id) return '—';
    const h = this.hrEmployees().find(h => h.employeeId === id);
    return h ? h.name : '—';
  }

  submitReassign() {
    if (!this.reassignTargetId()) { this.reassignError.set('Please select a target department.'); return; }
    this.reassignLoading.set(true);
    this.deptSvc.reassignAll('DEPT-0000', this.reassignTargetId()).subscribe({
      next: () => { this.reassignLoading.set(false); this.closeReassign(); this.loadDepartments(); },
      error: err => { this.reassignLoading.set(false); this.reassignError.set(err.error?.message || 'Reassignment failed.'); }
    });
  }

  openAdd() { this.deptForm.reset({ hrId: '' }); this.modalError.set(''); this.showAddModal.set(true); }
  closeAdd() { this.showAddModal.set(false); }

  openEdit(dept: any) {
    this.selected.set(dept);
    this.deptForm.patchValue({ name: dept.name, description: dept.description, managerId: dept.managerId, hrId: dept.hrId });
    this.modalError.set('');
    this.showEditModal.set(true);
  }
  closeEdit() { this.showEditModal.set(false); }

  openDelete(dept: any) {
    this.selected.set(dept);
    this.modalError.set('');
    this.deleteStep.set('confirm');
    this.deleteEmpCount.set(0);
    this.moveToUnassigned.set(true);
    this.targetDeptId.set('');
    this.deleteCountLoading.set(true);
    this.showDeleteModal.set(true);

    this.deptSvc.getEmployeeCount(dept.departmentId).subscribe({
      next: count => {
        this.deleteEmpCount.set(count);
        this.deleteCountLoading.set(false);
        if (count > 0) this.deleteStep.set('reassign');
      },
      error: () => this.deleteCountLoading.set(false)
    });
  }
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
    const count = this.deleteEmpCount();
    let target: string | undefined;

    if (count > 0) {
      target = this.moveToUnassigned() ? 'DEPT-0000' : this.targetDeptId();
      if (!this.moveToUnassigned() && !target) {
        this.modalError.set('Please select a department to move the employees to.');
        return;
      }
    }

    this.modalLoading.set(true);
    this.deptSvc.delete(this.selected().departmentId, target).subscribe({
      next: () => { this.modalLoading.set(false); this.closeDelete(); this.loadDepartments(); },
      error: err => {
        this.modalLoading.set(false);
        this.modalError.set(err.error?.message || 'Delete failed.');
      }
    });
  }
}
