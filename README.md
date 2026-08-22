## Build & run

Yêu cầu: **JDK 21+**, **Maven 3.9+**, **Node.js 18+** và **Docker Desktop**.

### 1. Cài và khởi động Oracle bằng Docker

Project sử dụng **Oracle Database Free** chạy trong Docker.

Kiểm tra Docker đã cài:

```bash
docker --version
docker compose version
```

Nếu chưa có Oracle container, tạo container bằng:

```bash
docker run -d --name oracle-db \
  -p 1521:1521 \
  -e ORACLE_PASSWORD=oracle \
  -e APP_USER=shop \
  -e APP_USER_PASSWORD=shop \
  -v oracle-data:/opt/oracle/oradata \
  gvenzl/oracle-free:latest
```

Các thông tin kết nối mặc định:

```text
Host:     localhost
Port:     1521
Service:  FREEPDB1
Schema:   shop
Username: shop
Password: shop
```

Kiểm tra container:

```bash
docker ps
```

Có thể xem log để chờ Oracle khởi động hoàn tất:

```bash
docker logs -f oracle-db
```

Khi thấy Oracle đã sẵn sàng nhận kết nối thì có thể chạy backend.

> **Lưu ý:** lần đầu Oracle khởi động có thể mất một vài phút. Chỉ cần tạo container một lần. Dữ liệu được lưu trong Docker volume `oracle-data`, nên việc restart container không làm mất dữ liệu.

Nếu muốn dừng/chạy lại Oracle:

```bash
docker stop oracle-db
docker start oracle-db
```

Nếu muốn xóa hoàn toàn database và tạo lại từ đầu:

```bash
docker rm -f oracle-db
docker volume rm oracle-data
```

Sau đó chạy lại lệnh `docker run` ở trên.

### 2. Cấu hình database

Backend mặc định kết nối tới:

```properties
spring.datasource.url=${DB_URL:jdbc:oracle:thin:@//localhost:1521/FREEPDB1}
spring.datasource.username=${DB_USERNAME:shop}
spring.datasource.password=${DB_PASSWORD:shop}
```

Có thể thay đổi thông tin kết nối bằng biến môi trường:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Schema `shop` được tạo tự động khi Oracle container được khởi tạo thông qua:

```text
APP_USER=shop
APP_USER_PASSWORD=shop
```

Vì vậy **không cần đăng nhập `SYSTEM` để tạo bảng cho ứng dụng**. Các bảng của project được tạo trong schema `SHOP`.

### 3. Chạy backend

Mở terminal:

```bash
cd backend
mvn spring-boot:run
```

Backend chạy tại:

```text
http://localhost:8080
```

Backend sẽ tự động tạo bảng và nạp dữ liệu mẫu trong lần chạy đầu tiên.

### 4. Chạy frontend

Mở terminal thứ hai:

```bash
cd frontend
npm install
npm run dev
```

Frontend chạy tại:

```text
http://localhost:5173
```

### 5. Sử dụng ứng dụng

Mở trình duyệt:

```text
http://localhost:5173
```

Sau khi frontend và backend đều chạy, ứng dụng có thể được sử dụng.

### Kiến trúc kết nối

```text
Frontend
   │
   │ HTTP
   ▼
Spring Boot Backend
   │
   │ JDBC
   ▼
Oracle Database
  
