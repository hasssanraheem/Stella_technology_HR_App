# Stella Technology — HR Management System

A full-stack Human Resources Management System built as an internship project at Stella Technology. The system covers employee lifecycle management, department administration, leave tracking, payroll generation, and employee history.

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
├── demo/                          ← Spring Boot backend
│   ├── src/main/java/com/HR_Managment_System/demo/
│   │   ├── config/                ← SecurityConfig, DataInitializer
│   │   ├── controller/            ← REST controllers (Auth, Employee, Department, Leave, Payroll)
│   │   ├── dto/                   ← Request/Response data transfer objects
│   │   ├── entity/                ← MongoDB document entities
│   │   ├── enums/                 ← Role, LeaveType, EmployeeType, etc.
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
│       │   ├── guards/            ← authGuard, adminGuard
│       │   ├── interceptors/      ← JWT interceptor (auto-attaches token)
│       │   └── services/          ← AuthService, EmployeeService, LeaveService, etc.
│       └── pages/
│           ├── login/             ← Login page (shared by all roles)
│           ├── dashboard/         ← Employee dashboard (4 tabs)
│           └── admin/
│               ├── layout/        ← Sidebar layout shell
│               ├── employees/     ← CRUD + promote + performance notes + history
│               ├── departments/   ← CRUD + manager assignment
│               ├── leaves/        ← Approve/reject + balance lookup
│               └── payroll/       ← Generate payroll + mark paid/unpaid
│
├── .gitignore
├── .env.example                   ← Copy this to demo/.env before running
└── README.md
```

---

## Features

### Module 1 — Authentication
- JWT-based stateless authentication
- Roles: `ADMIN`, `HR`, `EMPLOYEE`
- Role-based route protection on both frontend (guards) and backend (`@PreAuthorize`)
- Default admin auto-seeded on first startup

### Module 2 — Employee Management (Admin)
- Create employee: registers login credentials + creates profile in one flow
- Update employee details (name, phone, designation, salary, etc.)
- Change employee type (EMPLOYEE → MANAGER, triggers promotion history record)
- Delete employee
- Search employees by name (paginated)

### Module 3 — Department Management (Admin)
- Create / update / delete departments
- Assign a manager from existing employees
- Search departments by name

### Module 4 — Leave Management
- **Employee**: Apply for leave (Annual / Casual / Sick / Unpaid), view own leave history
- **Admin**: View all leave requests, filter by status (PENDING / APPROVED / REJECTED), approve or reject
- Leave balances tracked per employee; auto-decremented on approval
- Admin can search an employee's leave balance by name

### Module 5 — Payroll Management
- **Admin**: Generate payroll for any employee for a specific month/year
- Auto-calculates net salary: `Basic Salary + Allowances − Deductions`
- Mark payroll as PAID or UNPAID
- Filter payroll records by month, year, and payment status

### Module 6 — Employee History & Records
- Automatic promotion records when an employee's type changes
- Automatic designation change records when designation is updated
- Admin can add performance notes to any employee
- Full history timeline viewable per employee

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
    │
    └── MongoDB  (port 27017)
          ├── users
          ├── employees
          ├── departments
          ├── leave_requests
          ├── leave_balances
          ├── payrolls
          └── employee_history
```

**How JWT works in this project:**
1. User logs in with email + password → backend returns a JWT token
2. Angular stores the token in `localStorage`
3. A global HTTP interceptor automatically attaches `Authorization: Bearer <token>` to every outgoing request
4. The `JwtAuthFilter` on the backend validates the token and sets the Spring Security context
5. `@PreAuthorize` annotations on controller methods enforce role-based access

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
cd demo
cp ../.env.example .env
```

Open `demo/.env` and fill in your values:

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
# From inside the demo/ directory
cd demo

# macOS / Linux
set -a && source .env && set +a
./mvnw spring-boot:run

# Windows (PowerShell)
$env:JWT_SECRET="your_secret"; $env:MONGO_URI="mongodb://localhost:27017/hr_management_db"
.\mvnw.cmd spring-boot:run
```

The backend starts on **http://localhost:8080**

On first startup, the system automatically creates a default admin account:
- Email: `admin@hrms.com`
- Password: `Admin@1234`

You will see this in the console:
```
Default admin created — email: admin@hrms.com | password: Admin@1234
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

Go to **http://localhost:4200** in your browser.

You will be redirected to the login page automatically. Use the default admin credentials:

```
Email:    admin@hrms.com
Password: Admin@1234
```

After login, admins land on the **Employee Management** page. From the sidebar, navigate to:
- **Employees** — manage all staff
- **Departments** — manage teams
- **Leaves** — approve or reject leave requests
- **Payroll** — generate and manage payroll

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
| `POST` | `/api/auth/login` | Public | Login, returns JWT |
| `POST` | `/api/auth/register` | ADMIN | Create a login account |

### Employees
| Method | URL | Access | Description |
|---|---|---|---|
| `POST` | `/api/employees` | ADMIN | Create employee profile |
| `GET` | `/api/employees` | ADMIN, HR | List all employees (paginated, `?name=`) |
| `GET` | `/api/employees/me` | All roles | Get own profile from JWT |
| `GET` | `/api/employees/{id}` | All roles | Get employee by ID |
| `PUT` | `/api/employees/{id}` | ADMIN | Update employee details |
| `DELETE` | `/api/employees/{id}` | ADMIN | Delete employee |
| `PATCH` | `/api/employees/{id}/type` | ADMIN | Change employee type (EMPLOYEE/MANAGER) |
| `POST` | `/api/employees/{id}/performance-notes` | ADMIN | Add a performance note |
| `GET` | `/api/employees/{id}/history` | All roles | View history (`?type=PROMOTION\|DESIGNATION_CHANGE\|PERFORMANCE_NOTE`) |

### Departments
| Method | URL | Access | Description |
|---|---|---|---|
| `POST` | `/api/departments` | ADMIN | Create department |
| `GET` | `/api/departments` | All roles | List all departments |
| `GET` | `/api/departments/{id}` | All roles | Get department by ID |
| `PUT` | `/api/departments/{id}` | ADMIN | Update department |
| `DELETE` | `/api/departments/{id}` | ADMIN | Delete department |
| `GET` | `/api/departments/search?name=` | All roles | Search by name |

### Leave Requests
| Method | URL | Access | Description |
|---|---|---|---|
| `POST` | `/api/leaves` | EMPLOYEE, HR | Apply for leave |
| `GET` | `/api/leaves/my` | EMPLOYEE, HR | My own leave requests |
| `GET` | `/api/leaves/my/balance` | EMPLOYEE, HR | My leave balance |
| `GET` | `/api/leaves/all` | ADMIN, HR | All leave requests (`?status=`) |
| `PATCH` | `/api/leaves/{id}/status` | ADMIN, HR | Approve or reject a leave |
| `GET` | `/api/leaves/balance/{name}` | ADMIN, HR | Balance lookup by employee name |
| `GET` | `/api/leaves/balances` | ADMIN | All employees' balances |

### Payroll
| Method | URL | Access | Description |
|---|---|---|---|
| `POST` | `/api/payroll` | ADMIN | Generate payroll for an employee |
| `GET` | `/api/payroll/my` | EMPLOYEE, HR | My own payroll records |
| `GET` | `/api/payroll` | ADMIN | All payroll (`?month=&year=&status=`) |
| `PATCH` | `/api/payroll/{id}/payment-status` | ADMIN | Mark PAID or UNPAID |

---

## Roles & Permissions

| Feature | ADMIN | HR | EMPLOYEE |
|---|:---:|:---:|:---:|
| Create/Edit/Delete Employee | ✅ | ❌ | ❌ |
| Create/Edit/Delete Department | ✅ | ❌ | ❌ |
| View all employees | ✅ | ✅ | ❌ |
| View own profile | ✅ | ✅ | ✅ |
| Apply for leave | ✅ | ✅ | ✅ |
| View own leave history | ✅ | ✅ | ✅ |
| Approve / reject leave | ✅ | ✅ | ❌ |
| Generate payroll | ✅ | ❌ | ❌ |
| View own payroll | ✅ | ✅ | ✅ |
| Add performance note | ✅ | ❌ | ❌ |
| Change employee type | ✅ | ❌ | ❌ |
| View employee history | ✅ | ✅ | ✅ |

---

## Database Collections

| Collection | Description |
|---|---|
| `users` | Login credentials (email, hashed password, role) |
| `employees` | Employee profiles (name, designation, salary, etc.) |
| `departments` | Department records with optional manager assignment |
| `leave_requests` | Individual leave applications with status |
| `leave_balances` | Per-employee leave quota (annual, casual, sick, unpaid) |
| `payrolls` | Monthly payroll records with net salary calculation |
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
