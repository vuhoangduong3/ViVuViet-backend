# ViVuViet Backend

Backend cho web đặt vé xe liên tỉnh của nhóm. Project dùng Java 17, Spring Boot và PostgreSQL.

## Chạy project

Máy cần có Git và Docker. Nếu dùng Windows, mở Docker Desktop và chọn Linux containers trước khi chạy.

```bash
git clone --branch main https://github.com/vuhoangduong3/ViVuViet-backend.git
cd ViVuViet-backend
docker compose up -d --build
```

Lần đầu sẽ mất vài phút để tải các thành phần cần thiết và build source. Java, backend và PostgreSQL đều chạy trong Docker, nên không cần cài thêm Java, Maven hay PostgreSQL trên máy. Compose tạo hai service riêng: `app` cho backend và `db` cho PostgreSQL.

Để chỉnh cấu hình, sao chép `.env.example` thành `.env` rồi sửa các giá trị. Có thể bỏ qua bước này nếu dùng cấu hình mặc định. File `.env` được Git bỏ qua.

Để xem ứng dụng đã lên chưa:

```bash
docker compose ps
docker compose logs -f app
```

Khi trạng thái chuyển sang `healthy`, mở [Swagger](http://localhost:8080/swagger-ui.html) để thử API. Nhấn `Ctrl+C` để thoát màn hình log; container vẫn tiếp tục chạy.

Nếu đã có source trên máy, cập nhật rồi build lại:

```bash
git switch main
git pull --ff-only
docker compose up -d --build
```

## Dừng và chạy lại

```bash
# Dừng project, giữ lại dữ liệu
docker compose down

# Chạy lại
docker compose up -d
```

Database được lưu trong Docker volume `vivuviet_db-data`, nên dữ liệu vẫn còn khi tạo lại container. Lệnh `docker compose down -v` sẽ xóa cả dữ liệu này, chỉ dùng khi muốn làm lại từ đầu.

PostgreSQL có thể được chỉnh sửa từ máy host bằng `psql`, pgAdmin, DBeaver hoặc công cụ tương tự. Kết nối mặc định là `localhost:5433`, database `project`, username `vivuviet`, password `vivuviet_dev_password`. Đây là thông tin dùng cho phát triển cục bộ.

```bash
psql -h localhost -p 5433 -U vivuviet -d project
```

Có thể đổi `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `DB_PORT`, `APP_PORT` và `AUTH_JWT_SECRET` trong `.env`. PostgreSQL chỉ dùng `DB_NAME`, `DB_USERNAME` và `DB_PASSWORD` khi khởi tạo volume lần đầu. Nếu volume đã có dữ liệu, cần đổi tài khoản trực tiếp trong PostgreSQL hoặc xóa volume bằng `docker compose down -v` (lệnh này xóa database).

Nếu cổng 8080 đang bận, đổi sang 8081 trong PowerShell:

```powershell
$env:APP_PORT = "8081"
docker compose up -d
```

Sau đó mở [Swagger ở cổng 8081](http://localhost:8081/swagger-ui.html).

## Các chức năng hiện có

`main` đã có code của cả hai phần Sprint 1:

| Phần | Chức năng |
| --- | --- |
| 1.7 | Đăng ký, đăng nhập, làm mới token, đăng xuất và phân quyền theo hãng xe |
| 1.8 | Danh sách địa điểm, quản lý điểm đón/trả, tuyến đường, chuyến xe, mở bán và tìm kiếm chuyến |

Hai nhánh `feature/1.7-auth` và `feature/1.8-routes-trips` lưu phần công việc tương ứng. Để chạy bản đầy đủ bằng Docker Compose, dùng `main` theo hướng dẫn ở trên.

Database tạo mới chưa có hãng xe, địa điểm, tuyến, chuyến hay tài khoản admin. Vì vậy, API danh sách trả về rỗng là bình thường.

## Thử đăng nhập trên Swagger

Mở `POST /api/v1/auth/register`, chọn **Try it out** và nhập:

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

Sau khi đăng ký thành công, gọi `POST /api/v1/auth/login`:

```json
{
  "wsRequest": {
    "identifier": "user@example.com",
    "password": "DemoPassword123!"
  }
}
```

Chép `wsResponse.accessToken` trong kết quả trả về, bấm **Authorize** trên Swagger rồi dán token vào ô `bearerAuth`. Có thể gọi `GET /api/v1/me` để kiểm tra tài khoản vừa đăng nhập.

Tài khoản đăng ký mới là khách hàng. Muốn thử các API quản lý tuyến/chuyến của mục 1.8, cần có dữ liệu hãng xe và gán tài khoản làm thành viên với vai trò `COMPANY_ADMIN` tại hãng đó. Hiện chưa có API tạo admin hãng, nên phần dữ liệu này cần được chuẩn bị trong database.

Với mục 1.8, admin tạo điểm dừng, tuyến và lịch trình trước, sau đó tạo chuyến và mở bán. Khách có thể dùng API tìm kiếm để xem các chuyến đã mở bán phù hợp với nơi đi, nơi đến và ngày đi.

## Khi gặp lỗi

- Docker không kết nối được: kiểm tra Docker Desktop đã mở và Linux engine đang chạy.
- Build bị lỗi tải image hoặc dependency: kiểm tra kết nối mạng, xem log rồi chạy lại `docker compose up -d --build`.
- Container chưa `healthy`: xem `docker compose logs --tail 100 app` để biết lỗi cụ thể.
- API trả `401`: đăng nhập lại và cập nhật access token trong Swagger.
- API quản trị trả `403`: kiểm tra quyền `COMPANY_ADMIN` của tài khoản trong đúng hãng xe.

## Các file liên quan

- `compose.yaml`: cấu hình chạy project và volume dữ liệu.
- `Dockerfile`: build image cho backend.
- `DATABASE_DESIGN.md`: tài liệu thiết kế database.

Nếu cần chạy trực tiếp bằng IntelliJ hoặc Maven để debug, vẫn phải tự chuẩn bị JDK 17, PostgreSQL và các biến `DB_USERNAME`, `DB_PASSWORD`, `AUTH_JWT_SECRET`. Cấu hình kết nối mặc định nằm trong `src/main/resources/application.properties`.
