# OWASP SAFE Website

## Repositories

-   OWASP Website: https://github.com/xianfuhui/owasp-website
-   OWASP SAFE Website: https://github.com/xianfuhui/owasp-safe-website

## Description

The OWASP SAFE Website project is developed to deploy and present
content related to OWASP (Open Web Application Security Project). It is
designed for learning, research, and practicing web application
security.

## Database Setup (MongoDB)

Upload the following data files into MongoDB:

-   `hr.accounts.json` → collection: **accounts**
-   `hr.employees.json` → collection: **employees**

### Example using mongoimport

``` bash
mongoimport --jsonArray --db hrdb --collection accounts --file hr.accounts.json
mongoimport --jsonArray --db hrdb --collection employees --file hr.employees.json
```

## Admin Account

Username: **admin**\
Password: **#Vungoimora00@**
