# E-HMS (e-Hospital Management System) - Setup Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Database Setup](#database-setup)
3. [Backend Setup](#backend-setup)
4. [Frontend Setup](#frontend-setup)
5. [System Modules](#system-modules)
6. [Workflow & Architecture](#workflow--architecture)
7. [API Endpoints](#api-endpoints)
8. [Running the Application](#running-the-application)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software
- **Java Development Kit (JDK)**: Version 21 or higher
- **Node.js & npm**: Version 18 or higher
- **MySQL**: Version 8.0 or higher
- **Git**: For version control
- **Maven**: Version 3.9 or higher (for Java build)

### System Requirements
- Minimum 4GB RAM
- Minimum 500MB free disk space
- Internet connection for downloading dependencies

---

## Database Setup

### 1. Create MySQL Database

```sql
-- Open MySQL terminal/workbench and run:
CREATE DATABASE ehealth;
USE ehealth;
```

### 2. Import Database Schema

```bash
# Navigate to the project directory
cd path/to/hms

# Import the SQL file
mysql -u root -p ehealth < ehospital_fixed.sql
```

**Database Configuration:**
- **Host**: 127.0.0.1
- **Port**: 3306
- **Database**: ehealth
- **Username**: root
- **Password**: (empty by default, change in production)

---

## Backend Setup

### 1. Navigate to Backend Directory

```bash
cd backend
```

### 2. Install Dependencies

```bash
# Using Maven
mvn clean install

# Or on Windows
mvnw.cmd clean install
```

### 3. Configuration (Optional)

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/ehealth?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=

# JWT Secret (change in production)
jwt.secret=your-secret-key-here
jwt.expirationMs=86400000  # 24 hours

# Server Port
server.port=8086
```

### 4. Build the Application

```bash
mvn clean package -DskipTests
```

### 5. Run the Backend

```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Using Java directly
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

**Backend will run on**: `http://localhost:8086`

---

## Frontend Setup

### 1. Navigate to Frontend Directory

```bash
cd frontend
```

### 2. Install Dependencies

```bash
npm install
```

### 3. Environment Configuration (Optional)

Create or edit `.env` file:

```env
VITE_API_URL=http://localhost:8086/api
```

### 4. Run the Development Server

```bash
npm run dev
```

**Frontend will run on**: `http://localhost:5173`

### 5. Build for Production

```bash
npm run build
```

---

## System Modules

### User Management
- **Admin Dashboard** - System administration and monitoring
- **Doctor Dashboard** - Doctor-specific operations
- **Patient Dashboard** - Patient self-service portal
- **User Authentication** - Login/Register with JWT

### Clinical Modules

#### Appointments
- Schedule appointments
- View appointment history
- Manage appointment status (confirmed, cancelled, completed)
- Appointment reminders

#### Patients
- Patient registration and profiles
- Medical history tracking
- Patient demographics
- Emergency contacts

#### Doctors
- Doctor directory
- Specialty management
- Weekly availability scheduling
- Doctor performance metrics

#### Prescriptions
- Create and manage prescriptions
- Prescription history
- Digital prescription tracking
- Prescription fulfillment status

#### Medical History
- Complete patient medical records
- Diagnosis history
- Treatment tracking
- Lab test history

#### Lab Results
- Lab test ordering
- Result reporting
- Test status tracking
- Result notifications

#### Departments
- Department management
- Department staff
- Department services
- Operational hours

#### Discharge Summaries
- Create discharge records
- Follow-up recommendations
- Medication summary
- Post-discharge care instructions

### Administrative Modules

#### Billing
- Invoice generation
- Payment tracking
- Billing reports
- Insurance claims

#### Pharmacy
- Medication inventory
- Prescription fulfillment
- Medicine stock management
- Drug interactions checking

#### Reports & Analytics
- Admin reports dashboard
- System analytics
- Performance metrics
- Usage statistics

---

## Workflow & Architecture

### Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│              Frontend (React + Vite)                │
│  Components, Pages, Store (Zustand), API Calls     │
└──────────────────┬──────────────────────────────────┘
                   │ (HTTP REST API)
                   ↓
┌─────────────────────────────────────────────────────┐
│         Backend (Spring Boot + Java 21)             │
│  Controllers, Services, Repositories, Security      │
└──────────────────┬──────────────────────────────────┘
                   │ (JDBC)
                   ↓
┌─────────────────────────────────────────────────────┐
│              MySQL Database (InnoDB)                │
│  Tables, Relationships, Constraints, Indexes        │
└─────────────────────────────────────────────────────┘
```

### User Authentication Flow

1. **User Registration**
   - User submits registration form
   - Backend validates input
   - Password is hashed using bcrypt
   - User record created in database

2. **User Login**
   - User submits credentials
   - Backend validates credentials
   - JWT token generated
   - Token sent to frontend and stored

3. **API Requests**
   - Frontend includes JWT in Authorization header
   - Backend validates token
   - Request processed if token is valid
   - Response sent back to frontend

### Appointment Workflow

1. **Patient Books Appointment**
   - Patient selects doctor and time slot
   - Appointment created with "PENDING" status
   - Notification sent to doctor

2. **Doctor Approves/Rejects**
   - Doctor reviews appointment
   - Updates status to "CONFIRMED" or "CANCELLED"
   - Patient receives notification

3. **Appointment Execution**
   - Doctor completes appointment
   - Status updated to "COMPLETED"
   - Medical records updated

4. **Follow-up**
   - Discharge summary created if needed
   - Prescriptions issued
   - Patient receives care instructions

### Prescription Workflow

1. **Doctor Issues Prescription**
   - During or after appointment
   - Includes medications and dosage
   - Marked as "ACTIVE"

2. **Patient Views Prescription**
   - Patient dashboard shows active prescriptions
   - Can view medication details
   - Can request refill

3. **Pharmacy Fulfillment**
   - Prescription sent to pharmacy
   - Status updated to "FULFILLED"
   - Patient notified for collection

---

## API Endpoints

### Authentication
```
POST   /api/auth/register          - Register new user
POST   /api/auth/login             - Login user
POST   /api/auth/logout            - Logout user
POST   /api/auth/refresh-token     - Refresh JWT token
```

### Patients
```
GET    /api/patients               - Get all patients
POST   /api/patients               - Create new patient
GET    /api/patients/{id}          - Get patient details
PUT    /api/patients/{id}          - Update patient
DELETE /api/patients/{id}          - Delete patient
```

### Doctors
```
GET    /api/doctors                - Get all doctors
POST   /api/doctors                - Create new doctor
GET    /api/doctors/{id}           - Get doctor details
PUT    /api/doctors/{id}           - Update doctor
PUT    /api/doctors/{id}/availability - Set availability
```

### Appointments
```
GET    /api/appointments           - Get appointments
POST   /api/appointments           - Book appointment
GET    /api/appointments/{id}      - Get appointment details
PUT    /api/appointments/{id}      - Update appointment status
DELETE /api/appointments/{id}      - Cancel appointment
```

### Prescriptions
```
GET    /api/prescriptions          - Get prescriptions
POST   /api/prescriptions          - Create prescription
GET    /api/prescriptions/{id}     - Get prescription details
PUT    /api/prescriptions/{id}     - Update prescription
```

### Medical History
```
GET    /api/medical-history        - Get patient history
POST   /api/medical-history        - Add history record
GET    /api/medical-history/{id}   - Get specific record
```

### Lab Results
```
GET    /api/lab-results            - Get lab results
POST   /api/lab-results            - Create lab result
GET    /api/lab-results/{id}       - Get result details
```

### Departments
```
GET    /api/departments            - Get all departments
POST   /api/departments            - Create department
GET    /api/departments/{id}       - Get department details
```

### Billing
```
GET    /api/billing/invoices       - Get invoices
POST   /api/billing/invoices       - Create invoice
GET    /api/billing/invoices/{id}  - Get invoice details
```

---

## Running the Application

### Quick Start (All Components)

#### Terminal 1 - Database (MySQL)
```bash
# Make sure MySQL service is running
mysql -u root -p
```

#### Terminal 2 - Backend Server
```bash
cd backend
mvn spring-boot:run
# Or: java -jar target/demo-0.0.1-SNAPSHOT.jar
```

**Wait for**: `Started DemoApplication in X seconds`

#### Terminal 3 - Frontend Development Server
```bash
cd frontend
npm run dev
```

**Wait for**: `Local: http://localhost:5173/`

### Access the Application

- **Frontend**: [http://localhost:5173](http://localhost:5173)
- **Backend API**: [http://localhost:8086/api](http://localhost:8086/api)
- **API Documentation** (if available): [http://localhost:8086/swagger-ui.html](http://localhost:8086/swagger-ui.html)

### Default Login Credentials

Check the SQL file or documentation for default test accounts.

---

## Troubleshooting

### Database Connection Issues

**Error**: `Access denied for user 'root'@'localhost'`

**Solution**:
```bash
# Check MySQL is running
# Verify credentials in application.properties
# Reset MySQL password if needed
mysql -u root -p < ehospital_fixed.sql
```

### Backend Won't Start

**Error**: `Port 8086 is already in use`

**Solution**:
```bash
# Change port in application.properties
server.port=8087

# Or kill process using port 8086
# Windows: netstat -ano | findstr :8086
# Linux/Mac: lsof -i :8086
```

**Error**: `Java version error`

**Solution**:
```bash
# Check Java version
java -version

# Should be 21 or higher
# Update JAVA_HOME if needed
```

### Frontend Not Loading

**Error**: `Cannot find module` or blank page

**Solution**:
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### API Connection Failed

**Error**: `CORS error` or `Cannot reach backend`

**Solution**:
1. Verify backend is running on `http://localhost:8086`
2. Check VITE_API_URL in frontend `.env`
3. Check backend CORS configuration
4. Verify network connectivity

### Build Failures

**Backend build fails**:
```bash
mvn clean install -X  # Debug mode
mvn dependency:resolve  # Check dependencies
```

**Frontend build fails**:
```bash
npm cache clean --force
npm install
npm run build
```

### Common Solutions

| Issue | Solution |
|-------|----------|
| Slow performance | Increase heap size: `java -Xmx2g -Xms1g -jar app.jar` |
| Memory error | Allocate more RAM to JVM or MySQL |
| Database locked | Restart MySQL service |
| Port conflicts | Change ports in configuration files |
| CORS errors | Update CORS settings in backend |

---

## Production Deployment

### Before Deployment

1. Change JWT secret in `application.properties`
2. Update database credentials
3. Set environment variables securely
4. Build frontend: `npm run build`
5. Build backend: `mvn clean package`

### Docker Deployment (Optional)

See project's Docker files for containerization.

---

## Support & Resources

- **Backend**: Spring Boot 3.5, Spring Data JPA, MySQL
- **Frontend**: React 18, Vite, Tailwind CSS, Zustand
- **Security**: JWT Authentication, bcrypt hashing
- **API**: RESTful APIs

---

**Last Updated**: May 2, 2026
**Version**: 1.0
