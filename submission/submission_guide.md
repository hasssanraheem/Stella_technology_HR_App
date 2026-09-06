# Submission Guide
## HR Management System — Stella Technology Final Project

---

## What's in this folder

| File | Description |
|---|---|
| `postman_collection.json` | Import into Postman to test every API endpoint |
| `openapi.yaml` | Full OpenAPI 3.0 / Swagger specification — open in Swagger Editor to browse docs |
| `mongodb_schema.md` | Complete MongoDB database schema with all 7 collections, field types, and examples |
| `submission_guide.md` | This file |

The **README.md**, **source code**, and **GitHub repository** are in the root of the project.

---

## How to use each file

### Postman Collection (`postman_collection.json`)

1. Open Postman
2. Click **Import** → drag in `postman_collection.json`
3. Open the imported collection → go to the **Variables** tab
4. The `base_url` is already set to `http://localhost:8080`
5. Call **1. Authentication → Login** first
6. Copy the `token` value from the response
7. Paste it into the `jwt_token` variable → Save
8. All other requests will now authenticate automatically

The collection is organised into five folders matching the API modules:
- Authentication
- Employees
- Departments
- Leave Management
- Payroll

---

### Swagger / OpenAPI Docs (`openapi.yaml`)

**Option A — Swagger Editor (no install needed):**
1. Go to [https://editor.swagger.io](https://editor.swagger.io)
2. Click **File → Import File** → select `openapi.yaml`
3. Browse the interactive documentation on the right panel

**Option B — Import into Postman:**
1. Open Postman → **Import** → select `openapi.yaml`
2. Postman will generate a full collection from the spec automatically

The YAML documents:
- Every endpoint with method, path, and description
- All request body schemas with required fields and data types
- All response schemas (200, 201, 400, 401, 403, 404)
- JWT Bearer authentication setup

---

### MongoDB Schema (`mongodb_schema.md`)

Open in any Markdown viewer (VS Code, GitHub, Notion, etc.). Contains:
- Overview table of all 7 collections
- Full field-by-field schema for each collection with types, constraints, and example documents
- Entity relationship diagram
- Recommended indexes for production use

---

## Project submission checklist

| Item | Location | Status |
|---|---|---|
| Complete source code | Root of project folder | ✅ |
| GitHub repository | https://github.com/hasssanraheem/Stella_technology_HR_App | ✅ |
| MongoDB schema | `submission/mongodb_schema.md` | ✅ |
| Postman collection | `submission/postman_collection.json` | ✅ |
| Swagger / OpenAPI docs | `submission/openapi.yaml` | ✅ |
| README | `README.md` in root | ✅ |
