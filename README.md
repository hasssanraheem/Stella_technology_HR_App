# Stella Technology — HR Management System

A full-stack Human Resources Management System built as an internship project at Stella Technology. The system covers employee lifecycle management, department administration, leave tracking, payroll generation, and employee history — across three distinct user roles.

---

## What is this application?

**Admin** manages the entire organisation — registers employees, creates departments (each must have a dedicated HR assigned), oversees all leave requests (including a dedicated **HR Leave Approvals** tab to approve or reject leave submitted by HR employees), and monitors all payroll history across every department.

**HR** manages day-to-day people operations for their assigned department only. HR can approve or reject leave requests for their department's employees (regular employees only — not other HR staff). HR can also apply for their own leave through the **My Leave** tab; those requests go to Admin for approval. HR also generates monthly payroll and marks salaries as paid.

**Employee** has a personal dashboard to apply for leave (Annual, Casual, Sick, or Unpaid), track request status, check leave balance, and view payroll history.

### Leave approval chain

| Who submits | Approved by |
|---|---|
| Employee | HR (their department's HR) |
| HR employee | Admin |

### Key workflow

1. Admin registers HR employees first (no department needed at this stage).
2. Admin creates departments — each must have an HR person assigned before it can be saved.
3. Admin registers regular employees and assigns them to departments.
4. HR logs in and manages leaves and payroll for their department.
5. Employees log in to apply for leave and view their records.

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Project Structure](#project-structure)
3. [Features](#features)
4. [System Architecture](#system-architecture)
5. [Prerequisites](#prerequisites)
6. [Setup & Running Locally](#setup--running-locally)
7. [Environment Variables](#environment-variables)
8. [API Reference](#api-reference)
9. [Roles & Permissions](#roles--permissions)
10. [Database Collections](#database-collections)
11. [Submission Files](#submission-files)
12. [Default Admin Account](#default-admin-account)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Angular 22 (standalone components, signals, reactive forms) |
| Backend | Spring Boot 4.1.1 |
| Database | MongoDB (via Spring Data MongoDB) |
| Authentication | JWT (jjwt 0.12.6) + Spring Security 7 |
| Build Tools | Maven (backend), Angular CLI / Vite (frontend) |

---

## Project Structure

```
Stella_Technology_Final_Project/
│
├── backend/                       ← Spring Boot backend
│   ├── src/main/java/com/HR_Managnet_System/demo/
│   │   ├── config/                ← SecurityConfig, DataInitializer
│   │   ├── controller/            ← Auth, Employee, Department, Leave, Payroll
│   │   ├── dto/                   ← Request / Response DTOs
│   │   ├── entity/                ← MongoDB @Document entities
│   │   ├── enums/                 ← Role, LeaveType, LeaveStatus, EmployeeType, PaymentStatus, HistoryType
│   │   ├── exception/             ← Typed exceptions + GlobalExceptionHandler
│   │   ├── mapper/                ← Entity ↔ DTO converters
│   │   ├── repository/            ← Spring Data MongoDB repositories
│   │   ├── security/              ← JwtAuthFilter, CustomUserDetailsService
│   │   ├── service/               ← Business logic layer
│   │   └── util/                  ← JwtUtil
│   └── src/main/resources/
│       └── application.yml        ← App config (reads secrets from env vars)
│
├── hr-management-system-frontend/ ← Angular 22 frontend
│   └── src/app/
│       ├── core/
│       │   ├── guards/            ← authGuard, adminGuard, hrGuard
│       │   ├── interceptors/      ← JWT interceptor (auto-attaches Bearer token)
│       │   └── services/          ← AuthService, EmployeeService, DepartmentService,
│       │                             LeaveService, PayrollService
│       └── pages/
│           ├── login/             ← Shared login page
│           ├── dashboard/         ← Employee self-service dashboard
│           ├── admin/
│           │   ├── layout/        ← Admin sidebar shell
│           │   ├── employees/     ← CRUD, type change, performance notes, history
│           │   ├── departments/   ← CRUD, HR assignment, manager, reassign employees
│           │   ├── leaves/        ← Employee Leaves tab (read-only) + HR Leave Approvals tab
│           │   └── payroll/       ← Org-wide payroll view
│           └── hr/
│               ├── layout/        ← HR sidebar shell
│               ├── leaves/        ← Department Approvals tab + My Leave tab (apply & history)
│               └── payroll/       ← Generate + mark paid/unpaid for own department
│
├── submission/                    ← Project documentation & API specs
│   ├── openapi.yaml               ← OpenAPI 3.0.3 specification (import into Swagger UI or Postman)
│   ├── postman_collection.json    ← Role-based Postman collection (Auth, Admin, HR, Employee)
│   ├── schema.dbml                ← DBML schema for dbdiagram.io
│   ├── mongodb_schema.md          ← MongoDB collections reference
│   └── pdf_html/                  ← Source HTML for documentation PDFs
│
├── .env.example                   ← Copy to backend/.env before running
├── .gitignore
└── README.md
```

---

## Features

### Module 1 — Authentication
- JWT-based stateless authentication (24-hour token expiry)
- Three roles: `ADMIN`, `HR`, `EMPLOYEE`
- Case-insensitive email login (`ADMIN@HRMS.COM` = `admin@hrms.com`)
- `GET /api/auth/me` returns the logged-in user's email and role
- Role-based route guards on the frontend (`authGuard`, `adminGuard`, `hrGuard`)
- `@PreAuthorize` annotations enforce role rules on every backend endpoint
- Default admin account auto-seeded on first startup

### Module 2 — Employee Management (Admin)
- Register employee: creates login credentials and employee profile in one step
- Employee types: `EMPLOYEE`, `MANAGER`, `HR`
- HR employees can be registered without a department — held in the Unassigned bucket (DEPT-0000) until a department is created for them
- Partial updates: all fields optional on `PUT /api/employees/{id}` — send only what changes
- Change employee type (PATCH) — triggers automatic history record
- Delete employee — also removes user account, leave balance, and leave history
- Paginated employee list with filters: name, status, userRole, departmentId
- Filter pills: All / Employee / Manager / HR
- Unassigned employees banner alerts admin when staff have no department

### Module 3 — Department Management (Admin)
- Create / update / delete departments
- Every department requires a mandatory HR assignment — cannot be saved without one
- Validation prevents assigning the same HR to more than one department
- Optional manager assignment (MANAGER-type employees only)
- Departments table shows Manager name and HR name columns
- Delete with employee reassignment — move staff to another department or Unassigned before deletion
- Reassign all employees endpoint: `PUT /api/departments/{id}/reassign-employees`

### Module 4 — Leave Management

**Admin view**
- **Employee Leaves tab**: view all employee (non-HR) leave requests org-wide — filter by status and department
- **HR Leave Approvals tab**: view leave requests submitted by HR employees with Approve / Reject buttons and a red badge showing the pending count
- Leave Balance Lookup: check remaining balances for all employees

**HR view**
- **Department Approvals tab**: approve or reject pending leave requests for their department's employees (non-HR only — backend returns 403 if HR tries to action another HR's leave)
- **My Leave tab**: apply for own leave (Annual / Casual / Sick / Unpaid); own leave history and balance displayed; HR leaves are sent to Admin for approval
- Leave Balance Lookup: check balances for employees in their department

**Employee view**
- Apply for leave with start/end dates and optional reason
- View own leave history and current balance

Leave balances are auto-decremented when a request is approved. Only PENDING requests can be actioned.

### Module 5 — Payroll Management

**HR** generates monthly payroll for department employees; marks records as PAID / UNPAID.
Net salary = `Basic Salary + Allowances − Deductions`.
Data is auto-scoped to the HR's own department server-side.

**Admin** views all payroll records org-wide (read-only), filterable by month, year, status, and department.

**Employee** views own payroll history with net salary breakdown.

Duplicate prevention: 400 error if payroll already exists for that employee / month / year.

### Module 6 — Employee History & Records
- Automatic record when employee type changes (PROMOTION)
- Automatic record when designation is updated (DESIGNATION_CHANGE)
- Admin adds free-text performance notes (PERFORMANCE_NOTE)
- Full history timeline per employee, filterable by record type

---

## System Architecture

```
Browser (Angular 22)
    │
    │  HTTP + JWT Bearer token
    ▼
Spring Boot REST API  (port 8080)
    │
    ├── Spring Security 7
    │     └── JwtAuthFilter validates every request
    │
    ├── Controllers → Services → Repositories
    │     └── HR role auto-scoped to own department
    │          (backend reads authentication.getName() → looks up departmentId)
    │
    └── MongoDB  (port 27017)
          ├── users              ├── leave_requests
          ├── employees          ├── leave_balances
          ├── departments        ├── payroll
                                 └── employee_history
```

**JWT flow:**
1. User logs in → backend returns a signed JWT (24 h expiry)
2. Angular stores the token in `localStorage`
3. Global HTTP interceptor attaches `Authorization: Bearer <token>` to every request
4. `JwtAuthFilter` validates the token and sets the Spring Security context
5. `@PreAuthorize` annotations enforce role-based access per endpoint
6. For HR-scoped endpoints the backend reads the HR's email from the JWT, looks up their `departmentId`, and filters results accordingly

---

## Prerequisites

| Tool | Minimum Version | Check |
|---|---|---|
| Java (JDK) | 21 | `java -version` |
| Maven | 3.9+ | `mvn -v` (or use `./mvnw`) |
| Node.js | 18+ | `node -v` |
| Angular CLI | 18+ | `ng version` |
| MongoDB | 6+ | `mongod --version` |

---

## Setup & Running Locally

### Step 1 — Clone the repository

```bash
git clone https://github.com/hasssanraheem/Stella_technology_HR_App.git
cd Stella_technology_HR_App
```

### Step 2 — Start MongoDB

```bash
# macOS (Homebrew)
brew services start mongodb-community

# Linux
sudo systemctl start mongod
```

Verify:
```bash
mongosh --eval "db.runCommand({ ping: 1 })"
# Expected: { ok: 1 }
```

### Step 3 — Configure the backend

```bash
cd backend
cp ../.env.example .env
```

Edit `backend/.env`:

```env
MONGO_URI=mongodb://localhost:27017/hr_management_db
JWT_SECRET=your_long_random_secret_at_least_32_hex_characters
JWT_EXPIRATION=86400000
```

Generate a secure secret:
```bash
openssl rand -hex 32
```

### Step 4 — Run the backend

```bash
# From inside the backend/ directory

# macOS / Linux
set -a && source .env && set +a
./mvnw spring-boot:run

# Windows (PowerShell)
$env:JWT_SECRET="your_secret"; $env:MONGO_URI="mongodb://localhost:27017/hr_management_db"
.\mvnw.cmd spring-boot:run
```

Backend starts on **http://localhost:8080**

On first startup the system seeds:
- Default admin account (`admin@hrms.com` / `Admin@1234`)
- System "Unassigned" department (DEPT-0000)

### Step 5 — Run the frontend

Open a **new terminal**:

```bash
cd hr-management-system-frontend
npm install       # first time only
ng serve
```

Frontend starts on **http://localhost:4200**

### Step 6 — Log in

Open **http://localhost:4200** and log in with:

```
Email:    admin@hrms.com
Password: Admin@1234
```

### Recommended first-run order

1. Log in as **Admin** → register one or more HR employees (role: HR, no department needed)
2. **Departments** → create a department, assign one of the HR users
3. **Employees** → register regular employees, assign to the department
4. Log out → log in as **HR** to manage leaves and payroll
5. Log out → log in as **Employee** to apply for leave and view payroll

---

## Environment Variables

| Variable | Description | Default |
|---|---|---|
| `MONGO_URI` | MongoDB connection string | `mongodb://localhost:27017/hr_management_db` |
| `JWT_SECRET` | Secret key for signing/verifying JWT tokens | **required — no default** |
| `JWT_EXPIRATION` | Token lifetime in milliseconds | `86400000` (24 h) |

> **Security:** Never commit `backend/.env`. The file is in `.gitignore`. `application.yml` references secrets as `${JWT_SECRET}` — no hardcoded values.

---

## API Reference

All protected endpoints require:
```
Authorization: Bearer <jwt_token>
```

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | None | Login — returns JWT token. Email is case-insensitive. |
| `POST` | `/api/auth/register` | ADMIN | Create a standalone user account |
| `GET` | `/api/auth/me` | Any | Returns `{email, role}` of the logged-in user |

### Employees

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/employees/me` | Any | Own profile from JWT |
| `POST` | `/api/employees` | ADMIN | Register employee (creates profile + login account) |
| `GET` | `/api/employees` | ADMIN, HR | Paginated list — HR auto-scoped to their dept. Query: `page`, `size`, `name`, `departmentId`, `status`, `userRole` |
| `GET` | `/api/employees/{id}` | Any | Get employee by ID |
| `PUT` | `/api/employees/{id}` | ADMIN | Partial update — all fields optional |
| `PATCH` | `/api/employees/{id}/type` | ADMIN | Change type (`?employeeType=MANAGER`) — auto-creates history record |
| `POST` | `/api/employees/{id}/performance-notes` | ADMIN | Add performance note |
| `GET` | `/api/employees/{id}/history` | Any | History records. Query: `type` (PROMOTION \| DESIGNATION_CHANGE \| PERFORMANCE_NOTE) |
| `GET` | `/api/employees/{id}/leaves` | Any | Employee's own leave history |
| `GET` | `/api/employees/{id}/payroll` | Any | Employee's payroll records |
| `DELETE` | `/api/employees/{id}` | ADMIN | Delete employee + user account + leave data |

### Departments

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/departments` | ADMIN | Create department (requires `hrId`) |
| `GET` | `/api/departments` | ADMIN, HR | List all. Query: `name` (search) |
| `GET` | `/api/departments/{id}` | ADMIN, HR | Get by ID |
| `PUT` | `/api/departments/{id}` | ADMIN | Partial update |
| `PUT` | `/api/departments/{id}/reassign-employees` | ADMIN | Move all employees to `?targetDepartmentId=` |
| `GET` | `/api/departments/{id}/employee-count` | ADMIN | Count employees in dept |
| `DELETE` | `/api/departments/{id}` | ADMIN | Delete dept. Query: `targetDepartmentId` (optional) |

### Leave Requests

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/leaves` | Any | Apply for leave. `employeeId` derived from JWT automatically. |
| `GET` | `/api/leaves` | ADMIN, HR | All leaves (ADMIN) or dept leaves (HR auto-scoped). Query: `status` |
| `PUT` | `/api/leaves/{id}/status` | ADMIN, HR | Approve/reject leave. **ADMIN**: any leave including HR employees'. **HR**: employee leaves only — returns 403 for HR employees' leaves. |
| `GET` | `/api/leaves/balance` | ADMIN, HR | Leave balances. ADMIN: all employees (query: `name`). HR: dept-scoped. |
| `GET` | `/api/leaves/balance/{employeeId}` | Any | Balance for one employee |

### Payroll

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/payroll/generate` | HR | Generate payroll for a dept employee. Body: `{employeeId, month, year}` |
| `GET` | `/api/payroll` | ADMIN, HR | All records (ADMIN) or dept records (HR auto-scoped). Query: `month`, `year`, `status` |
| `PATCH` | `/api/payroll/{id}/status` | HR | Mark PAID or UNPAID. Query: `?status=PAID` |

---

## Roles & Permissions

| Feature | ADMIN | HR | EMPLOYEE |
|---|:---:|:---:|:---:|
| Register / Edit / Delete employee | ✅ | ❌ | ❌ |
| Create / Edit / Delete department | ✅ | ❌ | ❌ |
| Assign HR and manager to department | ✅ | ❌ | ❌ |
| View all employees (org-wide) | ✅ | ✅ (dept only) | ❌ |
| View own profile | ✅ | ✅ | ✅ |
| Apply for leave | ❌ | ✅ | ✅ |
| View own leave history & balance | ✅ | ✅ | ✅ |
| View all leaves (org-wide) | ✅ | ❌ | ❌ |
| View department leaves | ❌ | ✅ | ❌ |
| Approve / reject employee leaves | ❌ | ✅ (dept only) | ❌ |
| Approve / reject HR employees' leaves | ✅ | ❌ | ❌ |
| Generate payroll | ❌ | ✅ (dept only) | ❌ |
| Mark payroll paid / unpaid | ❌ | ✅ (dept only) | ❌ |
| View all payroll (org-wide) | ✅ | ❌ | ❌ |
| View department payroll | ❌ | ✅ | ❌ |
| View own payroll | ✅ | ✅ | ✅ |
| Leave balance lookup | ✅ (all) | ✅ (dept only) | ❌ |
| Add performance note | ✅ | ❌ | ❌ |
| Change employee type | ✅ | ❌ | ❌ |
| View employee history | ✅ | ✅ | ✅ (own only) |

---

## Database Collections

| Collection | Description |
|---|---|
| `users` | Login credentials — email, bcrypt password, role |
| `employees` | Employee profiles — name, designation, salary, departmentId, userRole, employeeType |
| `departments` | Departments with mandatory `hrId` and optional `managerId` |
| `leave_requests` | Leave applications — type, dates, status (PENDING / APPROVED / REJECTED) |
| `leave_balances` | Per-employee leave quota — annual (20), casual (10), sick (15), unpaid (30) days |
| `payroll` | Monthly payroll records — net salary, payment status (PAID / UNPAID) |
| `employee_history` | Audit trail — PROMOTION, DESIGNATION_CHANGE, PERFORMANCE_NOTE records |

See `submission/schema.dbml` for the full relational schema (import at [dbdiagram.io](https://dbdiagram.io)).

---

## Submission Files

| File | Description |
|---|---|
| `submission/openapi.yaml` | OpenAPI 3.0.3 spec — import into [Swagger Editor](https://editor.swagger.io) or Postman |
| `submission/postman_collection.json` | Role-based Postman collection (Auth, Admin, HR, Employee folders) |
| `submission/schema.dbml` | DBML database schema — import at [dbdiagram.io → Import → DBML](https://dbdiagram.io) |
| `submission/mongodb_schema.md` | MongoDB collection field reference |

---

## Default Admin Account

Seeded automatically on first startup if no admin exists:

| Field | Value |
|---|---|
| Email | `admin@hrms.com` |
| Password | `Admin@1234` |
| Role | `ADMIN` |

---

*Built by Hassan Raheem — Stella Technology Internship Project, 2026*
