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
2. [Repository Structure](#repository-structure)
3. [Prerequisites](#prerequisites)
4. [Setup & Running Locally](#setup--running-locally)
5. [Default Admin Account](#default-admin-account)
6. [Roles & Permissions](#roles--permissions)
7. [Features](#features)
8. [System Architecture](#system-architecture)
9. [Environment Variables](#environment-variables)
10. [API Reference](#api-reference)
11. [Database Collections](#database-collections)
12. [MongoDB Schema PDF](#mongodb-schema-pdf)
13. [API Testing with Postman](#api-testing-with-postman)
14. [Running Tests](#running-tests)
15. [Project Structure](#project-structure)
16. [Backend File Structure — Explained](#backend-file-structure--explained)
17. [Submission Files](#submission-files)

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

## Repository Structure

This is what you see when you open the repo on GitHub:

```
Stella_technology_HR_App/
│
├── backend/                         Spring Boot REST API (Java 21, Maven)
├── hr-management-system-frontend/   Angular 22 frontend
├── submission/                       Project documentation (OpenAPI, Postman, DBML schema)
├── HR_PROJECT_DB_SCHEMA.pdf          MongoDB collections schema — visual field-by-field reference
├── openapi.yaml                      OpenAPI 3.0.3 spec — import into Swagger Editor or Postman
├── postman_collection.json           Role-based Postman collection (Auth / Admin / HR / Employee)
├── docker-compose.yml               One command to run everything — backend, frontend, MongoDB
└── README.md                        You are here
```

> **Note:** A `.env` file is required locally to run the app (see below). It is not in the repo because it contains your secret key — see [.gitignore](.gitignore).

---

## Prerequisites

Before you run anything, make sure these tools are installed:

| Tool | Minimum Version | Check |
|---|---|---|
| Docker & Docker Compose | Latest | `docker -v` |
| Java (JDK) | 21 | `java -version` (manual setup only) |
| Maven | 3.9+ | `mvn -v` (manual setup only) |
| Node.js | 18+ | `node -v` (manual setup only) |
| Angular CLI | 18+ | `ng version` (manual setup only) |
| MongoDB | 6+ | `mongod --version` (manual setup only) |

If you are using **Docker**, you only need Docker itself — it handles Java, Node, and MongoDB for you.

---

## Setup & Running Locally

### Option A — Docker (Recommended)

The fastest way. One command starts the backend, frontend, and MongoDB together.

**Step 1 — Clone the repository**

```bash
git clone https://github.com/hasssanraheem/Stella_technology_HR_App.git
cd Stella_technology_HR_App
```

**Step 2 — Create your `.env` file**

The backend needs a secret key to sign login tokens. Create a file called `.env` in the root of the project (same folder as `docker-compose.yml`):

```
Stella_technology_HR_App/
├── .env                ← create this file
├── docker-compose.yml
├── backend/
...
```

Add this content to it:

```env
JWT_SECRET=replace_this_with_any_long_random_string_at_least_32_characters
```

To generate a secure secret automatically, run:

```bash
openssl rand -hex 32
```

Copy the output and paste it as the value of `JWT_SECRET`. Example:

```env
JWT_SECRET=a3f9c2e1b4d8f7a0c5e2d1b9f3a6c8e0d4b2f1a9e7c3d5b8f0a2e4c6d8b1f3
```

**Step 3 — Start everything**

```bash
docker compose up -d
```

All three servers (MongoDB, backend, frontend) start together. Open **http://localhost:4200** and log in with `admin@hrms.com` / `Admin@1234`.

---

### Option B — Manual Setup (without Docker)

Use this if you prefer to run each server yourself.

**Step 1 — Clone the repository**

```bash
git clone https://github.com/hasssanraheem/Stella_technology_HR_App.git
cd Stella_technology_HR_App
```

**Step 2 — Start MongoDB**

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

**Step 3 — Configure the backend**

Create a `.env` file inside the `backend/` folder:

```env
MONGO_URI=mongodb://localhost:27017/hr_management_db
JWT_SECRET=your_long_random_secret_at_least_32_hex_characters
JWT_EXPIRATION=86400000
```

Generate a secure secret:
```bash
openssl rand -hex 32
```

**Step 4 — Run the backend**

```bash
cd backend

# macOS / Linux
set -a && source .env && set +a
./mvnw spring-boot:run

# Windows (PowerShell)
$env:JWT_SECRET="your_secret"; $env:MONGO_URI="mongodb://localhost:27017/hr_management_db"
.\mvnw.cmd spring-boot:run
```

Backend starts on **http://localhost:8080**

**Step 5 — Run the frontend**

Open a **new terminal**:

```bash
cd hr-management-system-frontend
npm install       # first time only
ng serve
```

Frontend starts on **http://localhost:4200**

---

## Default Admin Account

Seeded automatically on first startup if no admin exists:

| Field | Value |
|---|---|
| Email | `admin@hrms.com` |
| Password | `Admin@1234` |
| Role | `ADMIN` |

### Recommended first-run order

1. Log in as **Admin** → register one or more HR employees (role: HR, no department needed)
2. **Departments** → create a department, assign one of the HR users
3. **Employees** → register regular employees, assign to the department
4. Log out → log in as **HR** to manage leaves and payroll
5. Log out → log in as **Employee** to apply for leave and view payroll

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
| View own leave history & balance | ❌ | ✅ | ✅ |
| View all leaves (org-wide) | ✅ | ❌ | ❌ |
| View department leaves | ✅ (via dept filter) | ✅ | ❌ |
| Approve / reject employee leaves | ❌ | ✅ (dept only) | ❌ |
| Approve / reject HR employees' leaves | ✅ | ❌ | ❌ |
| Generate payroll | ❌ | ✅ (dept only) | ❌ |
| Mark payroll paid / unpaid | ❌ | ✅ (dept only) | ❌ |
| View all payroll (org-wide) | ✅ | ❌ | ❌ |
| View department payroll | ✅ (via dept filter) | ✅ | ❌ |
| View own payroll | ❌ | ✅ | ✅ |
| Leave balance lookup | ✅ (all) | ✅ (dept only) | ✅ (own only) |
| Add performance note | ✅ | ❌ | ❌ |
| Change employee type | ✅ | ❌ | ❌ |
| View employee history | ✅ | ✅ | ✅ (own only) |

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

## MongoDB Schema PDF

`HR_PROJECT_DB_SCHEMA.pdf` (root of the repo) is a visual reference for all seven MongoDB collections used in this project.

It covers every collection — `users`, `employees`, `departments`, `leave_requests`, `leave_balances`, `payroll`, and `employee_history` — showing each field name, its data type, and whether it is required or optional. It also shows which fields reference documents in other collections (the equivalent of foreign keys in a relational database).

Open it directly from the repo root. No tools required — it is a standard PDF.

---

## API Testing with Postman

Two files at the root of the repo make it easy to explore and test the API:

### `openapi.yaml`

An OpenAPI 3.0.3 specification that describes every endpoint, its parameters, request body schema, and response codes.

**How to use:**

Option A — Swagger Editor (no install):
1. Go to [editor.swagger.io](https://editor.swagger.io)
2. Click **File → Import file** and choose `openapi.yaml`
3. The editor renders interactive docs on the right — try requests directly from there

Option B — Import into Postman:
1. Open Postman → **Import** → drag in `openapi.yaml`
2. Postman generates a collection from the spec automatically

### `postman_collection.json`

A ready-to-use Postman collection organised into four role-based folders:

| Folder | What it contains |
|---|---|
| **Auth** | Login, register, get current user |
| **Admin** | Employee CRUD, department management, leave approvals, payroll view |
| **HR** | Leave approvals, payroll generation, leave balance lookup |
| **Employee** | Apply for leave, view history, view payroll |

**How to import and use:**

1. Open Postman
2. Click **Import** (top-left) → drag in `postman_collection.json`
3. Log in first using the **Auth → Login** request — copy the `token` from the response
4. Set the token as a Postman variable (`{{token}}`) or paste it directly into the Authorization header of each request
5. Run requests in order: Login → then Admin, HR, or Employee requests depending on the role

**Default credentials to start with:**

| Role | Email | Password |
|---|---|---|
| Admin | `admin@hrms.com` | `Admin@1234` |

---

## Running Tests

The backend has **23 unit tests** covering the three core service layers: Employee, Leave, and Payroll. All tests use JUnit 5 and Mockito — no database or running server required.

### What the tests cover

Each test file focuses on one service and tests both the happy path (everything works) and the failure cases (wrong input, missing data, business rule violations):

**EmployeeServiceTest (8 tests)**
- Adding an employee with a new email → employee saved, leave balance record created automatically
- Adding an employee with a duplicate email → `DuplicateEmailException` thrown, nothing saved
- Getting an employee by ID → correct response returned
- Getting an employee with a non-existent ID → `EmployeeNotFoundException` thrown
- Updating an employee's designation → saved and a history record created automatically
- Updating a non-existent employee → `EmployeeNotFoundException` thrown
- Deleting an employee → `repository.delete()` called
- Deleting a non-existent employee → `EmployeeNotFoundException` thrown

**LeaveServiceTest (8 tests)**
- Applying for leave (valid dates, existing employee) → leave saved successfully
- Applying for leave for a non-existent employee → `EmployeeNotFoundException` thrown
- Applying with end date before start date → `ValidationException` thrown
- Approving a pending leave → leave balance decremented by the correct number of days (e.g. 3 days of Annual leave: 20 → 17)
- Actioning a leave that is already approved → `ValidationException` thrown (only PENDING leaves can be actioned)
- Actioning a non-existent leave → `LeaveRequestNotFoundException` thrown
- Getting leave balance for an existing employee → balance returned
- Getting leave balance for a non-existent employee → `EmployeeNotFoundException` thrown

**PayrollServiceTest (7 tests)**
- Generating payroll for a valid employee → payroll record saved
- Generating payroll for a non-existent employee → `EmployeeNotFoundException` thrown
- Generating payroll that already exists for that employee + month + year → `ValidationException` thrown ("already generated")
- Getting a payroll record by ID → record returned
- Getting a non-existent payroll record → `PayrollNotFoundException` thrown
- Updating payment status (UNPAID → PAID) → status updated and saved
- Updating status on a non-existent record → `PayrollNotFoundException` thrown

Every test name follows the pattern `methodName_scenario_expectedOutcome` so you can tell what it checks without reading the code.

### Run the tests

```bash
cd backend
mvn test
```

Expected output:

```
Tests run: 8,  Failures: 0, Errors: 0  -- EmployeeServiceTest
Tests run: 8,  Failures: 0, Errors: 0  -- LeaveServiceTest
Tests run: 7,  Failures: 0, Errors: 0  -- PayrollServiceTest

Tests run: 23, Failures: 0, Errors: 0
BUILD SUCCESS
```

### View results as an HTML report

```bash
mvn surefire-report:report
open target/reports/surefire.html   # macOS
# Windows: start target\reports\surefire.html
```

The HTML report lists every test method, its pass/fail status, and how long it took. The `target/` folder is in `.gitignore` so anyone who clones the repo generates their own fresh report.

### Where the test files live

```
backend/src/test/java/com/HR_Managnet_System/demo/service/
├── EmployeeServiceTest.java    ← 8 tests: add, get, update, delete employee
├── LeaveServiceTest.java       ← 8 tests: apply leave, approve, balance checks
└── PayrollServiceTest.java     ← 7 tests: generate payroll, update payment status
```

### How to add a new test

1. Create a new file in `backend/src/test/java/com/HR_Managnet_System/demo/service/` (or a `controller/` sub-folder)
2. Annotate the class with `@ExtendWith(MockitoExtension.class)`
3. Mock dependencies with `@Mock`, inject with `@InjectMocks`:

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {

    @Mock
    MyRepository myRepository;

    @InjectMocks
    MyService myService;

    @Test
    void myMethod_happyPath() {
        when(myRepository.findById("ID-001")).thenReturn(Optional.of(new MyEntity()));
        MyResponse result = myService.myMethod("ID-001");
        assertNotNull(result);
    }
}
```

4. Run `mvn test` — the new test is picked up automatically. Regenerate the HTML report with `mvn surefire-report:report`.

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
│   ├── src/main/resources/
│   │   └── application.yml        ← App config (reads secrets from env vars)
│   └── src/test/java/com/HR_Managnet_System/demo/service/
│       ├── EmployeeServiceTest.java   ← 8 unit tests
│       ├── LeaveServiceTest.java      ← 8 unit tests
│       └── PayrollServiceTest.java    ← 7 unit tests
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
├── HR_PROJECT_DB_SCHEMA.pdf       ← Visual MongoDB schema — all 7 collections with field types
├── openapi.yaml                   ← OpenAPI 3.0.3 spec (same as submission/, root copy for easy access)
├── postman_collection.json        ← Postman collection (same as submission/, root copy for easy access)
├── .gitignore
└── README.md
```

---

## Backend File Structure — Explained

This section walks through every folder inside `backend/src/main/java/com/HR_Managnet_System/demo/` and explains what it does and why it exists. If you have cloned this project and want to understand how it is wired together, start here.

---

### Entry Point

**`DemoApplication.java`** — the `main()` method. This is what Spring Boot runs. Nothing else to know about it.

---

### `config/` — Application Configuration

These three files run at startup and wire everything together. You will rarely touch them after initial setup, but they control security and database connection for the entire application.

**`SecurityConfig.java`** — the most important config file. It defines the security rules for every HTTP endpoint: which routes require a JWT token, which require a specific role (ADMIN, HR, EMPLOYEE), and which are public. It also attaches the `JwtAuthFilter` (see `security/` below) so that every incoming request is checked for a valid token before reaching the controller. It enables `@PreAuthorize` annotations so individual controller methods can declare their own role checks. It sets session policy to **stateless** — the server never creates an HTTP session, so every request must carry its own token.

**`DataInitializer.java`** — runs once on startup. It checks whether the default admin account (`admin@hrms.com`) and the "Unassigned" system department (`DEPT-0000`) already exist in the database. If they don't, it creates them. This is what seeds the first account so you can log in immediately after `docker compose up`.

**`MongoConnectionConfig.java`** — reads `MONGO_URI` from the environment and tells Spring Data MongoDB how to connect to the database.

---

### `controller/` — HTTP Entry Points

Each controller handles one area of the API. They receive an HTTP request, call the service layer to do the work, and return a `ResponseEntity` with the result. Controllers do not contain business logic themselves — that lives in `service/`.

| Controller | Base URL | Responsibility |
|---|---|---|
| `AuthController` | `/api/auth` | Login, register user, get current user (`/me`) |
| `EmployeeController` | `/api/employees` | Register, list, update, delete, change type, performance notes, history, leaves per employee, payroll per employee |
| `DepartmentController` | `/api/departments` | CRUD, HR assignment, manager assignment, employee reassignment |
| `LeaveController` | `/api/leaves` | Apply for leave, approve/reject, list leaves, balance lookup |
| `PayrollController` | `/api/payroll` | Generate payroll, list records, mark paid/unpaid |

---

### `dto/` — Data Transfer Objects

These are the shapes of data going **in** (Request) and **out** (Response) of the API. The controller never exposes raw database entities directly — it always uses DTOs. This keeps the database layer and the API layer decoupled: if you add an internal field to an entity, it won't automatically show up in API responses.

| File | Direction | Purpose |
|---|---|---|
| `LoginRequest` | In | Email + password for login |
| `RegisterRequest` | In | Create a standalone user account |
| `AuthResponse` | Out | JWT token returned after login |
| `EmployeeRequest` | In | Register a new employee |
| `EmployeeUpdateRequest` | In | Partial update (all fields optional) |
| `EmployeeResponse` | Out | Employee data returned to the client |
| `DepartmentRequest` | In | Create a department |
| `DepartmentUpdateRequest` | In | Update a department |
| `DepartmentResponse` | Out | Department data returned to the client |
| `LeaveRequestDto` | In | Apply for leave |
| `LeaveStatusUpdateDto` | In | Approve or reject a leave |
| `LeaveResponse` | Out | Leave request data |
| `LeaveBalanceResponse` | Out | Remaining leave days per type |
| `PayrollRequest` | In | Generate payroll (employeeId, month, year) |
| `PayrollResponse` | Out | Payroll record with net salary |
| `PerformanceNoteRequest` | In | Free-text note from admin |
| `EmployeeHistoryResponse` | Out | One history record |
| `ApiError` | Out | Standard error response shape |

---

### `entity/` — Database Documents

These classes map directly to MongoDB collections. Each one is annotated with `@Document`, which tells Spring Data MongoDB which collection to read from and write to.

| File | MongoDB Collection | What it stores |
|---|---|---|
| `User.java` | `users` | Email, bcrypt password hash, role |
| `Employee.java` | `employees` | Name, salary, departmentId, employeeType, status |
| `Department.java` | `departments` | Name, hrId, managerId |
| `LeaveRequest.java` | `leave_requests` | Start/end dates, type, status, employeeId |
| `LeaveBalance.java` | `leave_balances` | Annual/casual/sick/unpaid quotas per employee |
| `Payroll.java` | `payroll` | Net salary, month, year, payment status |
| `EmployeeHistory.java` | `employee_history` | Type (PROMOTION etc.), timestamp, note |

---

### `enums/` — Fixed Value Sets

Simple Java enums used across entities and DTOs so you never use raw strings for things like roles or statuses.

| Enum | Values |
|---|---|
| `Role` | `ADMIN`, `HR`, `EMPLOYEE` |
| `EmployeeType` | `EMPLOYEE`, `MANAGER`, `HR` |
| `EmployeeStatus` | `ACTIVE`, `INACTIVE` |
| `LeaveType` | `ANNUAL`, `CASUAL`, `SICK`, `UNPAID` |
| `LeaveStatus` | `PENDING`, `APPROVED`, `REJECTED` |
| `PaymentStatus` | `PAID`, `UNPAID` |
| `HistoryType` | `PROMOTION`, `DESIGNATION_CHANGE`, `PERFORMANCE_NOTE` |

---

### `exception/` — Error Handling

Two things live here: typed exception classes and the global handler that catches them all.

**Typed exceptions** — one class per error scenario: `EmployeeNotFoundException`, `DuplicateEmailException`, `DepartmentNotFoundException`, and so on. Throwing one of these anywhere in the codebase automatically produces the correct HTTP status code (404 for not-found, 409 for duplicate, 403 for forbidden, etc.).

**`GlobalExceptionHandler.java`** — catches every thrown exception app-wide using Spring's `@ControllerAdvice`. It converts exceptions into a consistent JSON error response (`ApiError`) so every error the API returns looks the same. You never need to write `try/catch` in a controller.

---

### `mapper/` — Entity ↔ DTO Conversion

Each mapper converts between a database entity and its corresponding DTO. The service layer works with DTOs at its boundaries — it receives a request DTO, calls the mapper to get an entity, saves it, then calls the mapper again to get a response DTO. This keeps the service focused on logic rather than data-shape translation.

| Mapper | Converts |
|---|---|
| `EmployeeMapper` | `Employee` ↔ `EmployeeRequest` / `EmployeeResponse` |
| `DepartmentMapper` | `Department` ↔ `DepartmentRequest` / `DepartmentResponse` |
| `LeaveMapper` | `LeaveRequest` ↔ `LeaveRequestDto` / `LeaveResponse` |
| `PayrollMapper` | `Payroll` ↔ `PayrollRequest` / `PayrollResponse` |

---

### `repository/` — Database Access

Each repository is an interface extending Spring Data's `MongoRepository`. Spring generates all the database queries automatically from method names — you declare `findByEmail(String email)` and Spring writes the query for you. For more complex lookups a `@Query` annotation is used.

One repository per collection: `UserRepository`, `EmployeeRepository`, `DepartmentRepository`, `LeaveRequestRepository`, `LeaveBalanceRepository`, `PayrollRepository`, `EmployeeHistoryRepository`.

---

### `security/` — JWT Filter

**`JwtAuthFilter.java`** — runs on every incoming request, before the controller is reached. It reads the `Authorization: Bearer <token>` header, validates the JWT signature and expiry, and loads the user into the Spring Security context if the token is valid. If the token is missing or invalid the request is rejected with a `401 Unauthorized` response before it ever reaches your controller.

**`CustomUserDetailsService.java`** — called by Spring Security during authentication. Given an email address, it loads the corresponding `User` from the database. Spring Security uses this to verify credentials at login time.

---

### `service/` — Business Logic

This is where the actual work happens. Services are called by controllers and call repositories. Every rule, every validation, every side effect (like decrementing a leave balance when a request is approved) lives here — not in the controller, and not in the repository.

| Service | Responsibility |
|---|---|
| `AuthService` | Login (validate password, issue JWT), register user |
| `EmployeeService` | CRUD for employees, type change, paginated filtering |
| `DepartmentService` | CRUD for departments, HR/manager assignment validation, employee reassignment |
| `LeaveService` | Apply leave, approve/reject (decrements leave balance), balance queries |
| `PayrollService` | Generate payroll (duplicate check), update payment status, filtered queries |
| `EmployeeHistoryService` | Create and fetch history records |
| `CustomUserDetailsService` | Load user from database by email (used by Spring Security) |

---

### `util/` — Utilities

**`JwtUtil.java`** — three jobs: generate a JWT token on login, validate a token on every request, and extract the email from a token. It uses the `JWT_SECRET` environment variable to sign and verify tokens.

---

### How a request flows end-to-end

Here is what happens from the moment an HTTP request arrives to the moment a response goes back:

```
HTTP Request
    ↓
JwtAuthFilter      →  validates token, loads user into security context
    ↓
Controller         →  receives request, checks @PreAuthorize role, calls service
    ↓
Service            →  runs business logic, calls repository + mapper
    ↓
Repository         →  reads from / writes to MongoDB
    ↓
Mapper             →  converts entity → response DTO
    ↓
Controller         →  returns ResponseEntity with DTO
    ↓
HTTP Response
```

If anything goes wrong at any step — a document not found, a duplicate email, a role that is not allowed — a typed exception is thrown. `GlobalExceptionHandler` catches it and returns a clean, consistent JSON error response. The controller never sees the exception.

---

## Submission Files

| File | Description |
|---|---|
| `submission/openapi.yaml` | OpenAPI 3.0.3 spec — import into [Swagger Editor](https://editor.swagger.io) or Postman |
| `submission/postman_collection.json` | Role-based Postman collection (Auth, Admin, HR, Employee folders) |
| `submission/schema.dbml` | DBML database schema — import at [dbdiagram.io → Import → DBML](https://dbdiagram.io) |
| `submission/mongodb_schema.md` | MongoDB collection field reference |

---

*Built by Hassan Raheem — Stella Technology Internship Project, 2026*
