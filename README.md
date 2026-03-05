OWASP Website

------------------------------------------------------------------------

Introduction

This project is a Human Resources (HR) management web application
designed with a strong focus on security. It is developed following
OWASP Top 10 (2021) security practices. The system manages employee
information and HR workflows while implementing protections against
common web vulnerabilities.

The application emphasizes secure authentication, input validation, and
role‑based access control to protect sensitive HR data. It is intended
for HR administrators and internal company management.

------------------------------------------------------------------------

Technologies Used

Backend - Spring Boot

Frontend - Thymeleaf - JavaScript

Database - MongoDB

Other Tools - OWASP security practices - Git / GitHub

------------------------------------------------------------------------

Features

-   Employee management
-   Secure authentication system
-   Role-based access control (RBAC)
-   Protection against common OWASP Top 10 vulnerabilities

------------------------------------------------------------------------

Screenshots

Home Page images/home.png

Feature Page images/feature.png

------------------------------------------------------------------------

Database Setup (MongoDB)

Upload the following dataset files into MongoDB:

-   hr.accounts.json → collection: accounts
-   hr.employees.json → collection: employees

Example using mongoimport:

mongoimport –jsonArray –db hrdb –collection accounts –file
hr.accounts.json mongoimport –jsonArray –db hrdb –collection employees
–file hr.employees.json

------------------------------------------------------------------------

Admin Account

Username: admin
Password: #Vungoimora00@

------------------------------------------------------------------------

Project Repositories

Website OWASP: https://github.com/xianfuhui/owasp-website

Website OWASP SAFE: https://github.com/xianfuhui/owasp-safe-website

------------------------------------------------------------------------

Author

Name: Tien Phu Huy
Email: tphuyvvk@gmail.com
GitHub: xianfuhui
