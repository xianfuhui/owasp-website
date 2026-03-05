# OWASP SAFE HR Management System

## Introduction
This project is a Human Resources (HR) management web application developed based on **OWASP Top 10 2021 security practices**.  
The system is designed to manage employee information and HR-related workflows while focusing on implementing secure web application practices.

The application demonstrates how common web vulnerabilities can be mitigated using secure coding techniques such as authentication control, input validation, and role-based access control.  
It is intended for **learning, research, and security practice related to web application security**.

Project repositories:

Website OWASP:  
https://github.com/xianfuhui/owasp-website

Website OWASP SAFE:  
https://github.com/xianfuhui/owasp-safe-website

---

## Technologies Used

**Backend**
- Spring Boot

**Frontend**
- Thymeleaf
- JavaScript
- HTML / CSS

**Database**
- MongoDB

**Other Tools**
- MongoDB Compass
- mongoimport
- Git / GitHub

---

## Features
- Employee information management
- Account management system
- Role-based access control for administrators
- Secure authentication and input validation based on OWASP recommendations

---

## Database Setup (MongoDB)

Upload the following data files into MongoDB:

- `hr.accounts.json` → collection: **accounts**
- `hr.employees.json` → collection: **employees**

Example using **mongoimport**:

```bash
mongoimport --jsonArray --db hrdb --collection accounts --file hr.accounts.json
mongoimport --jsonArray --db hrdb --collection employees --file hr.employees.json
