# Hệ thống Đơn hàng & Kho hàng

## Build & run

Yêu cầu: JDK 21+, Maven 3.9+, Node.js 18+ và Docker.

Khởi động Oracle (chỉ cần làm một lần). `APP_USER` tạo sẵn schema riêng cho ứng
dụng — bảng của app không nằm lẫn trong `SYSTEM`, và app không cần quyền DBA:

```bash
docker run -d --name oracle-db -p 1521:1521 \
  -e ORACLE_PASSWORD=oracle \
  -e APP_USER=shop -e APP_USER_PASSWORD=shop \
  -v oracle-data:/opt/oracle/oradata \
  gvenzl/oracle-free:latest
```

Nếu container đã được tạo từ trước mà chưa có schema này, tạo tay một lần:

```bash
docker exec -i oracle-db sqlplus -s system/oracle@//localhost:1521/FREEPDB1 <<'SQL'
CREATE USER shop IDENTIFIED BY shop;
GRANT CONNECT, RESOURCE TO shop;
ALTER USER shop QUOTA UNLIMITED ON USERS;
exit;
SQL
```

Backend tự tạo bảng và nạp dữ liệu mẫu trong lần chạy đầu. Thông tin kết nối mặc
định nằm ở `application.properties`, đổi được qua biến môi trường `DB_URL`,
`DB_USERNAME`, `DB_PASSWORD`.

Chờ Oracle khởi động xong, rồi mở hai terminal.

```bash
# Terminal 1 — backend, http://localhost:8080
cd backend
mvn spring-boot:run
```

```bash
# Terminal 2 — frontend, http://localhost:5173
cd frontend
npm install
npm run dev
```

Mở http://localhost:5173 để sử dụng ứng dụng.
