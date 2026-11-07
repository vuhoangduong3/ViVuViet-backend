# ViVuViet Backend

Backend đặt xe liên tỉnh, sử dụng Java 17, Spring Boot, Spring Security và PostgreSQL.

## Chạy nhanh bằng Docker Compose

Cài Docker Desktop và bật Linux containers (hoặc Docker Engine cùng Compose trên Linux). Clone nhánh `main`, sau đó chạy Compose:

```powershell
git clone --branch main https://github.com/vuhoangduong3/ViVuViet-backend.git
cd ViVuViet-backend
docker compose up -d --build
```

Lần đầu cần Internet để tải base image và dependency. Compose build từ source hiện tại; chưa cần image trên Docker Hub/GHCR. Backend và PostgreSQL chạy chung trong **một container**. Không cần cài Java, Maven, PostgreSQL riêng hoặc tạo `.env`. Docker tự tạo database và sinh mật khẩu/JWT secret, lưu cùng dữ liệu trong volume `app-data`. Nhánh `main` đã tích hợp code xác thực, tuyến/chuyến và cấu hình Docker.

Xem tiến trình khởi động và trạng thái:

```powershell
docker compose logs -f app
docker compose ps
```

Khi container báo `healthy`, mở Swagger tại <http://localhost:8080/swagger-ui.html>. Database mới chưa có dữ liệu hãng xe, địa điểm, tuyến/chuyến hay tài khoản admin; dùng Swagger để đăng ký khách hàng. Các hướng dẫn chuẩn bị dữ liệu ở phần dưới vẫn áp dụng.

Các lệnh quản lý:

```powershell
# Dừng và gỡ container, giữ dữ liệu trong volume
docker compose down

# Chạy lại với dữ liệu cũ
docker compose up -d

# Build lại sau khi sửa source
docker compose up -d --build
```

`docker compose down -v` sẽ xóa cả volume, database và secret đã sinh; chỉ dùng khi muốn xóa toàn bộ dữ liệu Docker của project. Database trên máy ở cổng 5433 không bị sử dụng hay thay đổi. Chỉ cổng HTTP 8080 được publish, PostgreSQL bên trong không mở ra máy host.

Nếu cổng 8080 đã có ứng dụng dùng, đổi cổng host trong PowerShell:

```powershell
$env:APP_PORT = "8081"
docker compose up -d
```

Sau đó mở Swagger bằng cổng 8081. Các phần tiếp theo hướng dẫn chạy trực tiếp trên máy khi không dùng Docker.

## 1. Chọn nhánh

| Nhánh | Nội dung |
| --- | --- |
| `main` | Bản tích hợp: codebase, xác thực, tuyến/chuyến và Docker Compose |
| `feature/1.7-auth` | Codebase và API đăng ký, đăng nhập, JWT, phân quyền theo hãng xe |
| `feature/1.8-routes-trips` | Kế thừa nhánh 1.7, bổ sung API địa điểm, điểm dừng, tuyến, chuyến và tìm kiếm |

Để chạy bản tích hợp hiện có, clone nhánh `main`:

```powershell
git clone --branch main https://github.com/vuhoangduong3/ViVuViet-backend.git
cd ViVuViet-backend
```

Nếu đã clone repository, chuyển nhánh trong thư mục project:

```powershell
git switch main
git pull --ff-only
```

## 2. Chuẩn bị môi trường

- Cài JDK 17 và thiết lập `JAVA_HOME` trỏ tới thư mục JDK.
- Cài Git.
- Có PostgreSQL đang chạy và tài khoản được phép tạo/cập nhật bảng trong database của project.
- Có Internet ở lần chạy đầu để Maven Wrapper tải Maven và các dependency.

Kiểm tra trong PowerShell:

```powershell
java -version
git --version
.\mvnw.cmd -v
```

Project có Maven Wrapper nên không cần cài Maven riêng. Phiên bản Spring Boot và dependency được khai báo trong `pom.xml`; phiên bản Maven được cấu hình trong `.mvn/wrapper/maven-wrapper.properties`.

## 3. Chuẩn bị PostgreSQL

Cấu hình mặc định trong `src/main/resources/application.properties` kết nối tới:

```text
Host: localhost
Port: 5433
Database: project
```

Nếu database `project` chưa tồn tại, mở pgAdmin hoặc psql, kết nối bằng tài khoản có quyền tạo database và chạy:

```sql
CREATE DATABASE project;
```

Tài khoản ứng dụng phải có quyền kết nối và tạo bảng trong database này. Nếu database đã có sẵn thì dùng database đó, không cần tạo lại.

## 4. Tạo file .env

Tạo file tên `.env` ngay cạnh `pom.xml`, với nội dung sau và thay các giá trị mẫu bằng cấu hình trên máy:

```properties
DB_USERNAME=your_database_user
DB_PASSWORD=your_database_password
AUTH_JWT_SECRET=replace-with-your-own-random-secret-at-least-32-characters
```

`AUTH_JWT_SECRET` phải có ít nhất 32 ký tự. Có thể tạo một chuỗi ngẫu nhiên bằng PowerShell rồi chép kết quả vào file `.env`:

```powershell
$jwtBytes = New-Object byte[] 32
$jwtRng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$jwtRng.GetBytes($jwtBytes)
$jwtRng.Dispose()
[Convert]::ToBase64String($jwtBytes)
```

File này được Spring đọc theo định dạng Java properties: dùng `KEY=value`, không thêm `export` và không bọc giá trị trong dấu nháy như file shell. Không commit file `.env` chứa thông tin thật.

Nếu PostgreSQL chạy ở cổng khác, ví dụ `5432`, hoặc dùng database khác, thêm dòng ghi đè vào `.env`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
```

Project hiện dùng `spring.jpa.hibernate.ddl-auto=update`: Hibernate tạo/cập nhật bảng theo entity khi khởi động. Database phải tồn tại trước; cấu hình này không tự tạo database. Nội dung `DATABASE_DESIGN.md` là tài liệu thiết kế, không phải script khởi tạo SQL.

## 5. Chạy backend

Chạy tại thư mục chứa `pom.xml` để ứng dụng đọc đúng file `.env`:

```powershell
.\mvnw.cmd spring-boot:run
```

Trên Linux/macOS:

```bash
sh ./mvnw spring-boot:run
```

Khi log xuất hiện thông báo `Started ViVuVietApplication`, truy cập:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- API lấy địa điểm: <http://localhost:8080/api/v1/areas> (nhánh 1.8).

Nhấn `Ctrl+C` tại terminal để dừng backend. Project hiện chưa có dependency Actuator nên chưa cung cấp `/actuator/health`.

## 6. Thử API trên Swagger

Các bước dưới đây áp dụng cho nhánh 1.7 và 1.8. Request xác thực được bọc trong trường `wsRequest`.

1. Mở Swagger, chọn `POST /api/v1/auth/register`, bấm **Try it out**, nhập:

```json
{
  "wsRequest": {
    "fullName": "Nguyen Van A",
    "email": "user@example.com",
    "phone": "0901234567",
    "password": "DemoPassword123!"
  }
}
```

2. Gọi `POST /api/v1/auth/login`:

```json
{
  "wsRequest": {
    "identifier": "user@example.com",
    "password": "DemoPassword123!"
  }
}
```

3. Lấy access token từ dữ liệu trả về, bấm **Authorize** trên Swagger và dán token vào ô `bearerAuth`.
4. Gọi `GET /api/v1/me` để xem tài khoản hiện tại.

Đăng ký chỉ tạo tài khoản khách hàng. API quản trị tuyến/chuyến yêu cầu tài khoản có membership và vai trò `COMPANY_ADMIN` của đúng hãng xe. Project chưa cung cấp API tạo admin hãng; cần chuẩn bị dữ liệu hãng, membership và vai trò trước khi thử các API quản trị.

Database mới chưa có dữ liệu địa điểm, tuyến hoặc chuyến nên API danh sách có thể trả kết quả rỗng. Khách chỉ tìm thấy chuyến đáp ứng điều kiện tìm kiếm và đã được mở bán.

## 7. Build và chạy kiểm thử

Build JAR, đồng thời chạy test:

```powershell
.\mvnw.cmd clean package
java -jar .\target\Vi-vu-Viet-0.0.1-SNAPSHOT.jar
```

Chỉ chạy test:

```powershell
.\mvnw.cmd test
```

Test hiện tại là `@SpringBootTest`, khởi động Spring context nên cần cấu hình `.env` và PostgreSQL sẵn sàng. Khi chạy JAR cũng cần đứng tại thư mục project để đọc `.env`.

## 8. Lỗi thường gặp

| Hiện tượng | Cách kiểm tra |
| --- | --- |
| Không tìm thấy Java hoặc `JAVA_HOME` không hợp lệ | Kiểm tra JDK 17, `JAVA_HOME` và kết quả `.\mvnw.cmd -v` |
| Không tải được Maven/dependency | Kiểm tra Internet, proxy và URL tải trong cấu hình Wrapper hoặc `pom.xml` |
| `Connection refused` | Kiểm tra PostgreSQL đang chạy, host và cổng `5433`/`5432` |
| `database ... does not exist` | Tạo database hoặc sửa `spring.datasource.url` trong `.env` |
| `password authentication failed` | Kiểm tra `DB_USERNAME` và `DB_PASSWORD` |
| `permission denied for schema` | Cấp quyền dùng schema và tạo bảng cho tài khoản ứng dụng |
| Không resolve được biến môi trường | Kiểm tra tên file `.env`, tên biến và thư mục đang chạy lệnh |
| JWT secret quá ngắn | Đặt `AUTH_JWT_SECRET` có ít nhất 32 ký tự |
| Cổng 8080 đang được sử dụng | Dừng ứng dụng cũ hoặc thêm `server.port=8081` vào `.env`, rồi dùng URL với cổng mới |
| API trả `401` | Đăng nhập lại và gửi access token hợp lệ |
| API quản trị trả `403` | Kiểm tra membership và quyền `COMPANY_ADMIN` trong đúng hãng xe |

## 9. File không đưa lên Git

`.env`, `target/`, `.idea/` và `.maven-home/` là file/thư mục cục bộ, đã được loại trừ trong `.gitignore`.
