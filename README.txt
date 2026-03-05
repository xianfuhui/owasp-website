README.txt

Website OWASP:
https://github.com/xianfuhui/owasp-website

Website OWASP SAFE:
https://github.com/xianfuhui/owasp-safe-website

Mô tả:
Dự án Website OWASP SAFE dùng để triển khai và trình bày nội dung liên quan đến OWASP
(Open Web Application Security Project), phục vụ mục đích học tập, nghiên cứu và thực hành
bảo mật ứng dụng web.

Cơ sở dữ liệu MongoDB:
Upload các file dữ liệu sau vào MongoDB:
- hr.accounts.json  → collection: accounts
- hr.employees.json → collection: employees

Ví dụ sử dụng mongoimport:
mongoimport --jsonArray --db hrdb --collection accounts --file hr.accounts.json
mongoimport --jsonArray --db hrdb --collection employees --file hr.employees.json

Tài khoản quản trị:
Username: admin
Password: #Vungoimora00@