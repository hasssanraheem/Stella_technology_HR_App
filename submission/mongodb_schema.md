# MongoDB Database Schema
## HR Management System — Stella Technology

**Database name:** `hr_management_db`  
**Total collections:** 7

---

## Collections Overview

| Collection | Purpose | Auto-seeded |
|---|---|---|
| `users` | Login credentials (email, hashed password, role) | Yes — admin on first startup |
| `employees` | Full employee profiles | No |
| `departments` | Department records with manager and HR assignment | Yes — DEPT-0000 on first startup |
| `leave_requests` | Individual leave applications | No |
| `leave_balances` | Per-employee leave quota | Auto-created on employee registration |
| `payroll` | Monthly payroll records | No |
| `employee_history` | Audit trail (promotions, designation changes, notes) | No |

---

## Collection Schemas

---

### `users`

Stores login credentials. One document per registered user. Passwords are bcrypt-hashed and never returned by any API.

```json
{
  "_id":       "ObjectId",
  "email":     "admin@hrms.com",
  "password":  "$2a$10$...",
  "role":      "ADMIN",
  "createdAt": "2026-09-03T01:50:00",
  "updatedAt": "2026-09-03T01:50:00"
}
```

| Field | Type | Required | Notes |
|---|---|---|---|
| `_id` | ObjectId | auto | MongoDB default |
| `email` | String | yes | Unique. Lookup is case-insensitive. |
| `password` | String | yes | BCrypt-hashed — never exposed via API |
| `role` | Enum | yes | `ADMIN` \| `EMPLOYEE` \| `HR` |
| `createdAt` | DateTime | auto | Set on registration |
| `updatedAt` | DateTime | auto | Updated on every change |

---

### `employees`

Stores the full profile for each employee. Linked to `users` by email and to `departments` by `departmentId`.

```json
{
  "_id":           "ObjectId",
  "employeeId":    "EMP-0001",
  "name":          "John Doe",
  "email":         "john.doe@company.com",
  "phone":         "+92-300-1234567",
  "gender":        "MALE",
  "dateOfBirth":   "1995-06-15",
  "address":       "123 Main Street, Karachi",
  "departmentId":  "DEPT-0001",
  "designation":   "Software Engineer",
  "dateOfJoining": "2026-01-01",
  "salary":        80000,
  "allowances":    10000,
  "deductions":    5000,
  "employeeType":  "EMPLOYEE",
  "userRole":      "EMPLOYEE",
  "status":        "ACTIVE",
  "createdAt":     "2026-09-03T10:00:00",
  "updatedAt":     "2026-09-03T10:00:00"
}
```

| Field | Type | Required | Notes |
|---|---|---|---|
| `_id` | ObjectId | auto | MongoDB default |
| `employeeId` | String | auto | Sequential ID: EMP-0001, EMP-0002, … |
| `name` | String | yes | Full name |
| `email` | String | yes | Must match the `users.email` of the same person |
| `phone` | String | yes | |
| `gender` | Enum | yes | `MALE` \| `FEMALE` |
| `dateOfBirth` | Date | yes | |
| `address` | String | yes | |
| `departmentId` | String | yes | References `departments.departmentId`. Defaults to `DEPT-0000` for HR employees with no dept yet |
| `designation` | String | yes | Job title |
| `dateOfJoining` | Date | yes | |
| `salary` | Number | yes | Base monthly salary |
| `allowances` | Number | no | Added to net salary |
| `deductions` | Number | no | Subtracted from net salary |
| `employeeType` | Enum | yes | `EMPLOYEE` \| `MANAGER` \| `HR` |
| `userRole` | Enum | yes | `EMPLOYEE` \| `HR` — determines which dashboard the user sees |
| `status` | Enum | auto | `ACTIVE` \| `INACTIVE`. Defaults to ACTIVE on creation |
| `createdAt` | DateTime | auto | |
| `updatedAt` | DateTime | auto | |

---

### `departments`

One document per department. Each department must have a dedicated HR person assigned.

```json
{
  "_id":          "ObjectId",
  "departmentId": "DEPT-0001",
  "name":         "Engineering",
  "description":  "Software development and engineering team",
  "managerId":    "EMP-0002",
  "hrId":         "EMP-0003",
  "isSystem":     false,
  "createdAt":    "2026-09-03T11:00:00",
  "updatedAt":    "2026-09-03T11:00:00"
}
```

| Field | Type | Required | Notes |
|---|---|---|---|
| `_id` | ObjectId | auto | |
| `departmentId` | String | auto | Sequential ID: DEPT-0001, DEPT-0002, … |
| `name` | String | yes | Unique department name |
| `description` | String | no | |
| `managerId` | String | no | References `employees.employeeId`. Manager must have `employeeType = MANAGER` |
| `hrId` | String | yes | References `employees.employeeId`. HR must have `userRole = HR`. One HR per department only. |
| `isSystem` | Boolean | auto | `true` only for the system Unassigned department (DEPT-0000). Cannot be deleted. |
| `createdAt` | DateTime | auto | |
| `updatedAt` | DateTime | auto | |

**System department (auto-seeded):**
```json
{
  "departmentId": "DEPT-0000",
  "name":         "Unassigned",
  "description":  "Holding department for employees pending reassignment",
  "isSystem":     true
}
```

---

### `leave_requests`

One document per leave application.

```json
{
  "_id":        "ObjectId",
  "leaveId":    "LVE-0001",
  "employeeId": "EMP-0001",
  "leaveType":  "ANNUAL",
  "startDate":  "2026-10-01",
  "endDate":    "2026-10-05",
  "totalDays":  5,
  "reason":     "Family vacation",
  "status":     "PENDING",
  "appliedAt":  "2026-09-15T09:00:00",
  "updatedAt":  "2026-09-15T09:00:00"
}
```

| Field | Type | Required | Notes |
|---|---|---|---|
| `_id` | ObjectId | auto | |
| `leaveId` | String | auto | Sequential ID: LVE-0001, LVE-0002, … |
| `employeeId` | String | yes | References `employees.employeeId` |
| `leaveType` | Enum | yes | `ANNUAL` \| `CASUAL` \| `SICK` \| `UNPAID` |
| `startDate` | Date | yes | Must not be after `endDate` |
| `endDate` | Date | yes | |
| `totalDays` | Number | auto | Calculated as `endDate − startDate + 1` |
| `reason` | String | yes | |
| `status` | Enum | auto | `PENDING` → `APPROVED` or `REJECTED`. Only PENDING requests can be actioned. |
| `appliedAt` | DateTime | auto | |
| `updatedAt` | DateTime | auto | |

---

### `leave_balances`

Tracks remaining leave quota per employee. One document per employee, auto-created with default values on registration.

```json
{
  "_id":          "ObjectId",
  "employeeId":   "EMP-0001",
  "annualLeaves": 18,
  "casualLeaves": 10,
  "sickLeaves":   8,
  "unpaidLeaves": 5,
  "createdAt":    "2026-09-03T10:00:00",
  "updatedAt":    "2026-09-03T10:00:00"
}
```

| Field | Type | Notes |
|---|---|---|
| `employeeId` | String | References `employees.employeeId`. Unique. |
| `annualLeaves` | Number | Decremented when an ANNUAL leave request is approved |
| `casualLeaves` | Number | Decremented when a CASUAL leave request is approved |
| `sickLeaves` | Number | Decremented when a SICK leave request is approved |
| `unpaidLeaves` | Number | Decremented when an UNPAID leave request is approved |

---

### `payroll`

One document per employee per month/year combination.

```json
{
  "_id":           "ObjectId",
  "payrollId":     "PAY-0001",
  "employeeId":    "EMP-0001",
  "month":         9,
  "year":          2026,
  "basicSalary":   80000,
  "allowances":    10000,
  "deductions":    5000,
  "netSalary":     85000,
  "paymentStatus": "UNPAID",
  "generatedAt":   "2026-09-30T18:00:00",
  "updatedAt":     "2026-09-30T18:00:00"
}
```

| Field | Type | Required | Notes |
|---|---|---|---|
| `_id` | ObjectId | auto | |
| `payrollId` | String | auto | Sequential ID: PAY-0001, PAY-0002, … |
| `employeeId` | String | yes | References `employees.employeeId` |
| `month` | Number | yes | 1–12 |
| `year` | Number | yes | Full year e.g. 2026 |
| `basicSalary` | Number | auto | Copied from `employees.salary` at generation time |
| `allowances` | Number | auto | Copied from `employees.allowances` |
| `deductions` | Number | auto | Copied from `employees.deductions` |
| `netSalary` | Number | auto | `basicSalary + allowances − deductions` |
| `paymentStatus` | Enum | auto | `UNPAID` (default) → `PAID` |
| `generatedAt` | DateTime | auto | |
| `updatedAt` | DateTime | auto | |

---

### `employee_history`

Immutable audit trail. Each record captures a single event in an employee's career. Records are never deleted.

```json
{
  "_id":            "ObjectId",
  "employeeId":     "EMP-0001",
  "type":           "PROMOTION",
  "previousValue":  "EMPLOYEE",
  "newValue":       "MANAGER",
  "note":           "Promoted to Manager role",
  "recordedAt":     "2026-09-10T14:30:00"
}
```

| Field | Type | Notes |
|---|---|---|
| `employeeId` | String | References `employees.employeeId` |
| `type` | Enum | `PROMOTION` \| `DESIGNATION_CHANGE` \| `PERFORMANCE_NOTE` |
| `previousValue` | String | Old value before the change (e.g. old employee type, old designation) |
| `newValue` | String | New value after the change |
| `note` | String | Free-text note (used for `PERFORMANCE_NOTE` type) |
| `recordedAt` | DateTime | Timestamp of when the event occurred |

**How records are created:**

| Trigger | Record type | Previous / New value |
|---|---|---|
| Admin changes employee type | `PROMOTION` | Old type → New type |
| Admin changes designation via profile update | `DESIGNATION_CHANGE` | Old designation → New designation |
| Admin adds a performance note | `PERFORMANCE_NOTE` | — | Note text |

---

## Entity Relationships

```
users ──────────────────────── employees
(email)                        (email, employeeId)
                                    │
                                    ├── departments (departmentId → managerId, hrId)
                                    ├── leave_requests (employeeId)
                                    ├── leave_balances (employeeId)
                                    ├── payroll (employeeId)
                                    └── employee_history (employeeId)
```

MongoDB does not enforce foreign key constraints. Referential integrity is maintained at the application (service) layer.

---

## Indexes (Recommended for Production)

| Collection | Field | Type | Reason |
|---|---|---|---|
| `users` | `email` | Unique | Login lookup |
| `employees` | `employeeId` | Unique | Profile lookup |
| `employees` | `email` | Unique | Prevents duplicate profiles |
| `employees` | `departmentId` | Regular | Department scoping queries |
| `departments` | `departmentId` | Unique | |
| `departments` | `hrId` | Unique | Enforce one HR per department |
| `leave_requests` | `employeeId` | Regular | Fetch employee's leave history |
| `leave_requests` | `status` | Regular | Filter by status |
| `leave_balances` | `employeeId` | Unique | |
| `payroll` | `employeeId` | Regular | |
| `payroll` | `(employeeId, month, year)` | Compound Unique | Prevent duplicate payroll |
| `employee_history` | `employeeId` | Regular | |
