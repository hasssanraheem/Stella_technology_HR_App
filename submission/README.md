# Stella Technology — HR Management System

A full-stack Human Resources Management System built as an internship project at Stella Technology. The system covers employee lifecycle management, department administration, leave tracking, payroll generation, and employee history.

---

## What is this application?

This is a role-based HR Management System that gives three types of users a tailored experience:

**Admin** manages the entire organisation — they register employees, create departments (each must have a dedicated HR assigned), oversee all leave requests (read-only view), and monitor all payroll history across every department. Admins control the structure; they do not process leave approvals or run payroll.

**HR** manages day-to-day people operations for their assigned department only. After logging in, HR lands on a dedicated HR Dashboard where they can approve or reject leave requests for their department's employees, generate monthly payroll, and mark salaries as paid or unpaid. All data is automatically scoped to their department — they cannot see or affect other departments.

**Employee** has a personal dashboard where they can apply for leave (Annual, Casual, Sick, or Unpaid), track the status of their requests, check their remaining leave balance, and view their payroll history.

### Key workflow

1. Admin registers HR employees first (no department needed at this stage).
2. Admin creates departments — each department must have an HR person assigned before it can be saved.
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
11. [Default Admin Account](#default-admin-account)

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
│   ├── src/main/java/com/HR_Managment_System/demo/
│   │   ├── config/                ← SecurityConfig, DataInitializer
│   │   ├── controller/            ← REST controllers (Auth, Employee, Department, Leave, Payroll)
│   │   ├── dto/                   ← Request/Response data transfer objects
│   │   ├── entity/                ← MongoDB document entities
│   │   ├── enums/                 ← Role, LeaveType, LeaveStatus, EmployeeType, PaymentStatus, etc.
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
│       │   ├── interceptors/      ← JWT interceptor (auto-attaches token)
│       │   └── services/          ← AuthService, EmployeeService, DepartmentService,
│       │                             LeaveService, PayrollService
│       └── pages/
│           ├── login/             ← Login page (shared by all roles)
│           ├── dashboard/         ← Employee self-service dashboard
│           ├── admin/
│           │   ├── layout/        ← Admin sidebar layout shell
│           │   ├── employees/     ← CRUD + type filters + promote + notes + history
│           │   ├── departments/   ← CRUD + mandatory HR + manager assignment + reassign
│           │   ├── leaves/        ← View-only leave list + department filter + balance lookup
│           │   └── payroll/       ← View-only payroll history + department filter
│           └── hr/
│               ├── layout/        ← HR sidebar layout shell
│               ├── leaves/        ← Approve/reject leaves for own department
│               └── payroll/       ← Generate + mark paid payroll for own department
│
├── .gitignore
├── .env.example                   ← Copy this to backend/.env before running
└── README.md
```

---

## Features

### Module 1 — Authentication
- JWT-based stateless authentication
- Roles: `ADMIN`, `HR`, `EMPLOYEE`
- Case-insensitive email login (e.g. `ADMIN@HRMS.COM` = `admin@hrms.com`)
- Role-based route guards on the frontend (`authGuard`, `adminGuard`, `hrGuard`)
- `@PreAuthorize` annotations enforce role rules on every backend endpoint
- Default admin account auto-seeded on first startup

### Module 2 — Employee Management (Admin)
- Register employee: creates login credentials and employee profile in one flow
- Employee types: `EMPLOYEE`, `MANAGER`, `HR`
- HR employees can be registered without a department — they are held in the system Unassigned bucket (DEPT-0000) until a department is created for them
- Update employee details (name, phone, designation, salary, allowances, deductions, etc.)
- Change employee type — triggers an automatic promotion record in the history log
- Delete employee with instant list refresh
- Search employees by name (paginated)
- Filter pills: All / Employee / Manager / HR
- Unassigned employees banner warns the admin when staff have no department

### Module 3 — Department Management (Admin)
- Create / update / delete departments
- **Every department requires a mandatory HR assignment** — cannot be saved without selecting an HR employee
- Validation prevents assigning the same HR to more than one department
- Assign a manager from existing MANAGER-type employees
- Departments table shows both Manager name and HR name columns
- Delete with employee reassignment: if a department has employees, admin must move them to another department or the Unassigned holding bucket before deletion
- Reassign button on the Unassigned system department lets admin bulk-move unassigned employees
- Search departments by name

### Module 4 — Leave Management

**Admin view**
- **Employee Leaves tab**: see all leave requests from non-HR employees org-wide — filter by status and department
- **HR Leave Approvals tab**: view and approve/reject leave requests submitted by HR employees (red badge shows pending count)
- Leave Balance Lookup: check remaining balances for all employees

**HR view (department-scoped)**
- **Department Approvals tab**: approve or reject pending leave for their department employees (non-HR only — backend returns 403 for HR employees)
- **My Leave tab**: apply for own leave (Annual / Casual / Sick / Unpaid); HR leaves go to Admin for approval
- Leave Balance Lookup: load all employees in their department
- Data is scoped server-side — HR cannot see other departments' data

**Employee view**
- Apply for leave (Annual / Casual / Sick / Unpaid)
- Choose start and end dates with an end-after-start validation
- Add a reason
- View own leave history and current leave balance

Leave balances are auto-decremented when a request is approved. Only PENDING requests can be actioned.

### Module 5 — Payroll Management

**Admin view (read-only)**
- View all payroll records across the organisation
- Filter by month, year, payment status, and department
- Cannot generate payroll or change payment status

**HR view (department-scoped)**
- Generate payroll for any employee in their department for a chosen month/year — the employee dropdown only shows department members
- Net salary auto-calculated: `Basic Salary + Allowances − Deductions`
- Mark payroll as PAID / UNPAID
- Filter by month, year, and payment status
- Data is scoped server-side — HR cannot see or generate payroll for other departments

**Employee view**
- View own payroll history with net salary and payment status

### Module 6 — Employee History & Records
- Automatic promotion record when employee type changes
- Automatic designation change record when designation is updated
- Admin can add free-text performance notes to any employee
- Full timeline viewable per employee, filterable by record type

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
    │          (detected via Authentication object, no extra token claims needed)
    │
    └── MongoDB  (port 27017)
          ├── users
          ├── employees
          ├── departments
          ├── leave_requests
          ├── leave_balances
          ├── payroll
          └── employee_history
```

**How JWT works in this project:**
1. User logs in with email + password → backend returns a JWT token
2. Angular stores the token in `localStorage`
3. A global HTTP interceptor automatically attaches `Authorization: Bearer <token>` to every outgoing request
4. The `JwtAuthFilter` on the backend validates the token and sets the Spring Security context
5. `@PreAuthorize` annotations on controller methods enforce role-based access
6. For HR-scoped endpoints, the backend reads `authentication.getName()` (the HR's email) to look up their `departmentId` and filter data accordingly

---

## Prerequisites

Make sure the following are installed on your machine before you start:

| Tool | Minimum Version | How to check |
|---|---|---|
| Java (JDK) | 21 | `java -version` |
| Maven | 3.9+ | `mvn -v` (or use `./mvnw`) |
| Node.js | 18+ | `node -v` |
| Angular CLI | 18+ | `ng version` |
| MongoDB | 6+ | `mongod --version` |

---

## Setup & Running Locally

Follow these steps in order. Open a separate terminal for each server.

### Step 1 — Clone the repository

```bash
git clone https://github.com/hasssanraheem/Stella_technology_HR_App.git
cd Stella_technology_HR_App
```

### Step 2 — Start MongoDB

MongoDB must be running before the backend starts.

```bash
# macOS (Homebrew)
brew services start mongodb-community

# Linux
sudo systemctl start mongod

# Windows — start MongoDB from Services or run:
mongod
```

Verify MongoDB is running:
```bash
mongosh --eval "db.runCommand({ ping: 1 })"
# Expected output: { ok: 1 }
```

### Step 3 — Configure the backend environment

```bash
cd backend
cp ../.env.example .env
```

Open `backend/.env` and fill in your values:

```env
MONGO_URI=mongodb://localhost:27017/hr_management_db
JWT_SECRET=your_long_random_secret_at_least_32_hex_characters
JWT_EXPIRATION=86400000
```

> **How to generate a JWT secret:**
> ```bash
> openssl rand -hex 32
> ```

### Step 4 — Run the backend

```bash
# From inside the backend/ directory
cd backend

# macOS / Linux
set -a && source .env && set +a
./mvnw spring-boot:run

# Windows (PowerShell)
$env:JWT_SECRET="your_secret"; $env:MONGO_URI="mongodb://localhost:27017/hr_management_db"
.\mvnw.cmd spring-boot:run
```

The backend starts on **http://localhost:8080**

On first startup, the system automatically creates:
- A default admin account
- A system "Unassigned" department (DEPT-0000) used as a holding bucket for employees without a department

You will see this in the console:
```
Default admin created — email: admin@hrms.com | password: Admin@1234
System department 'Unassigned' (DEPT-0000) created.
```

### Step 5 — Run the frontend

Open a **new terminal window**:

```bash
cd hr-management-system-frontend
npm install          # only needed on first run
ng serve             # or: npx ng serve
```

The frontend starts on **http://localhost:4200**

### Step 6 — Open the app

Go to **http://localhost:4200** in your browser. You will be redirected to the login page.

Use the default admin credentials:

```
Email:    admin@hrms.com
Password: Admin@1234
```

### Recommended first-run setup order

1. Log in as **Admin**
2. Go to **Employees** → register one or more HR users (role: HR, no department needed)
3. Go to **Departments** → create a department and assign one of the HR users to it
4. Go to **Employees** → register regular employees and assign them to the department
5. Log out → log in as the **HR** user to manage leaves and payroll for that department
6. Log out → log in as an **Employee** to apply for leave and view payroll

---

## Environment Variables

| Variable | Description | Default |
|---|---|---|
| `MONGO_URI` | MongoDB connection string | `mongodb://localhost:27017/hr_management_db` |
| `JWT_SECRET` | Secret key used to sign/verify JWT tokens | **required — no default** |
| `JWT_EXPIRATION` | Token lifetime in milliseconds | `86400000` (24 hours) |

> **Security note:** Never commit your `.env` file or put the real `JWT_SECRET` in `application.yml`. The `.env` file is listed in `.gitignore` and will not be pushed to GitHub.

---

## API Reference

All protected endpoints require the header:
```
Authorization: Bearer <your_jwt_token>
```

### Auth
| Method | URL | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | Public | Login, returns JWT. Email is case-insensitive. |
| `POST` | `/api/auth/register` | ADMIN | Create a login account (EMPLOYEE, HR) |

### Employees
| Method | URL | Access | Description |
|---|---|---|---|
| `POST` | `/api/employees` | ADMIN | Create employee profile |
| `GET` | `/api/employees` | ADMIN, HR | List employees (paginated, filterable by `?name=`, `?departmentId=`, `?status=`, `?userRole=`) |
| `GET` | `/api/employees/me` | All roles | Get own profile from JWT |
| `GET` | `/api/employees/{id}` | All roles | Get employee by ID |
| `PUT` | `/api/employees/{id}` | ADMIN | Update employee details |
| `DELETE` | `/api/employees/{id}` | ADMIN | Delete employee |
| `PATCH` | `/api/employees/{id}/type` | ADMIN | Change employee type (EMPLOYEE / MANAGER / HR) |
| `POST` | `/api/employees/{id}/performance-notes` | ADMIN | Add a performance note |
| `GET` | `/api/employees/{id}/history` | All roles | View history (`?type=PROMOTION\|DESIGNATION_CHANGE\|PERFORMANCE_NOTE`) |

### Departments
| Method | URL | Access | Description |
|---|---|---|---|
| `POST` | `/api/departments` | ADMIN | Create department (requires `hrId`) |
| `GET` | `/api/departments` | All roles | List all departments |
| `GET` | `/api/departments/{id}` | All roles | Get department by ID |
| `PUT` | `/api/departments/{id}` | ADMIN | Update department |
| `DELETE` | `/api/departments/{id}` | ADMIN | Delete department (with optional `?reassignTo=` param) |
| `GET` | `/api/departments/search?name=` | All roles | Search by name |
| `GET` | `/api/departments/{id}/employee-count` | ADMIN | Count employees in a department |
| `PUT` | `/api/departments/{id}/reassign-all` | ADMIN | Bulk-move all employees to another department |

### Leave Requests
| Method | URL | Access | Description |
|---|---|---|---|
| `POST` | `/api/leaves` | EMPLOYEE, HR | Apply for leave |
| `GET` | `/api/leaves/employees/{id}` | All roles | Get leave requests for a specific employee |
| `GET` | `/api/leaves` | ADMIN, HR | All leave requests; auto-scoped to department for HR (`?status=`) |
| `PUT` | `/api/leaves/{id}/status` | ADMIN, HR | Approve/reject. HR: employee leaves only (403 for HR employees). ADMIN: any leave including HR. |
| `GET` | `/api/leaves/balance/{employeeId}` | All roles | Get leave balance for an employee |
| `GET` | `/api/leaves/balance` | ADMIN, HR | All leave balances (HR: dept-scoped) |

### Payroll
| Method | URL | Access | Description |
|---|---|---|---|
| `POST` | `/api/payroll/generate` | HR only | Generate payroll for an employee |
| `GET` | `/api/payroll` | ADMIN, HR | All payroll records; auto-scoped to dept for HR (`?month=&year=&status=`) |
| `GET` | `/api/payroll/employees/{id}` | All roles | Payroll records for a specific employee |
| `PATCH` | `/api/payroll/{id}/status` | HR only | Mark payroll PAID or UNPAID |

---

## Roles & Permissions

| Feature | ADMIN | HR | EMPLOYEE |
|---|:---:|:---:|:---:|
| Register / Edit / Delete employee | ✅ | ❌ | ❌ |
| Create / Edit / Delete department | ✅ | ❌ | ❌ |
| Assign HR and manager to department | ✅ | ❌ | ❌ |
| View all employees (org-wide) | ✅ | ✅ | ❌ |
| View own profile | ✅ | ✅ | ✅ |
| Apply for leave | ❌ | ✅ | ✅ |
| View own leave history | ✅ | ✅ | ✅ |
| View all leaves (org-wide) | ✅ | ❌ | ❌ |
| View department leaves | ❌ | ✅ | ❌ |
| Approve / reject employee leaves | ❌ | ✅ (dept only) | ❌ |
| Approve / reject HR leaves | ✅ | ❌ | ❌ |
| Generate payroll | ❌ | ✅ (dept only) | ❌ |
| Mark payroll paid / unpaid | ❌ | ✅ (dept only) | ❌ |
| View all payroll (org-wide) | ✅ | ❌ | ❌ |
| View department payroll | ❌ | ✅ | ❌ |
| View own payroll | ✅ | ✅ | ✅ |
| Leave balance lookup (all employees) | ✅ | ✅ (dept only) | ❌ |
| Add performance note | ✅ | ❌ | ❌ |
| Change employee type | ✅ | ❌ | ❌ |
| View employee history | ✅ | ✅ | ✅ (own only) |

---

## Database Collections

| Collection | Description |
|---|---|
| `users` | Login credentials (email, bcrypt-hashed password, role) |
| `employees` | Employee profiles (name, designation, salary, allowances, deductions, departmentId, userRole, employeeType) |
| `departments` | Department records with optional manager (`managerId`) and mandatory HR (`hrId`) assignment |
| `leave_requests` | Individual leave applications with status (PENDING / APPROVED / REJECTED) |
| `leave_balances` | Per-employee leave quota (annual, casual, sick, unpaid days remaining) |
| `payroll` | Monthly payroll records with net salary and payment status |
| `employee_history` | Audit trail of promotions, designation changes, and performance notes |

---

## Default Admin Account

On first startup, the backend automatically seeds one admin account if none exists:

| Field | Value |
|---|---|
| Email | `admin@hrms.com` |
| Password | `Admin@1234` |
| Role | `ADMIN` |

Change this password after your first login by updating the user directly in MongoDB or by implementing the change-password endpoint.

---

*Built by Hassan Raheem — Stella Technology Internship Project, 2026*
