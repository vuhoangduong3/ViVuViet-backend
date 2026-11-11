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
| 1.8 | Danh sách khu vực, quản lý tuyến đường và chuyến xe, mở bán và tìm kiếm chuyến |
| Đặt vé | Cấu hình và xem ghế chuyến, giữ ghế 5 phút, xác nhận/hủy đặt vé; mỗi vé lưu địa chỉ đón/trả do khách nhập |
| Khách hàng | Xem/cập nhật hồ sơ, lọc và phân trang lịch sử đặt vé, xem thông tin vé điện tử |

Hai nhánh `feature/1.7-auth` và `feature/1.8-routes-trips` lưu phần công việc tương ứng. Để chạy bản đầy đủ bằng Docker Compose, dùng `main` theo hướng dẫn ở trên.

Khi khởi động ở môi trường mặc định hoặc bằng Docker Compose, ứng dụng tự tạo dữ liệu demo nếu chưa có hãng xe `vi-vu-viet-demo`. Seed chạy sau khi Hibernate tạo/cập nhật schema, nằm trong một transaction và không chạy ở profile `test` hay các profile môi trường khác. Bật/tắt bằng `APP_SEED_DEMO_DATA=true|false` trong `.env`.

Seed gồm một hãng xe, tài khoản admin/tài xế/khách, tuyến Hà Nội - Đà Nẵng, xe, lịch sử chuyến (đã hoàn tất/đang chạy/sắp khởi hành), ghế, đặt chỗ và vé. Đăng nhập demo trên Swagger bằng các tài khoản sau:

| Vai trò | Email | Mật khẩu |
| --- | --- | --- |
| Admin hãng | `admin@vivuviet.local` | `Admin123!` |
| Tài xế | `driver@vivuviet.local` | `Driver123!` |
| Khách | `customer@vivuviet.local` | `Customer123!` |

Admin có thể đăng nhập rồi gọi `GET /api/v1/me/companies` để lấy `companyId` cho các API hãng xe. Tài xế dùng tài khoản demo để thử API `/api/v1/me/driver/**`. Đây là thông tin đăng nhập phát triển cục bộ; không dùng cho môi trường chia sẻ hoặc triển khai.

Docker Compose giữ database trong volume `vivuviet_db-data`. Mỗi lần app khởi động, seeder kiểm tra slug hãng demo trước; nếu đã có thì bỏ qua, nếu chưa có thì tạo dữ liệu. Vì vậy, `docker compose up -d --build` sẽ tự seed cả volume mới lẫn volume hiện có chưa có hãng demo. `docker compose down` giữ nguyên database; `docker compose down -v` xóa toàn bộ volume và dữ liệu.

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

Tài khoản đăng ký mới là khách hàng. Để thử API quản lý hãng xe, dùng tài khoản admin demo ở phần trên; tài khoản này đã được gán vai trò `COMPANY_ADMIN` cho hãng demo.

Với mục 1.8, admin tạo khu vực và tuyến, tạo chuyến nháp, cấu hình ghế rồi mở bán chuyến. API tìm kiếm lọc theo khu vực đầu tuyến, khu vực cuối tuyến và ngày khởi hành. Khi đặt vé, khách chọn ghế và tự nhập địa chỉ đón/trả cho từng hành khách; giá vé được tính từ giá mặc định của tuyến.

API đặt vé yêu cầu đăng nhập. Ghế được giữ trong 5 phút; khách cần xác nhận trước khi hết hạn. Thanh toán chưa được tích hợp.

### Luồng API đặt vé

Admin hãng cấu hình ghế cho chuyến nháp bằng `PUT /api/v1/bus-companies/{company_id}/trips/{trip_id}/seats`, sau đó mở bán chuyến. Khách xem ghế tại `GET /api/v1/trips/{trip_id}/seats`, tạo đặt vé bằng `POST /api/v1/me/bookings`, rồi xác nhận tại `POST /api/v1/me/bookings/{booking_id}/confirm`. Có thể xem danh sách/chi tiết tại `GET /api/v1/me/bookings` và `GET /api/v1/me/bookings/{booking_id}`, hoặc hủy bằng `POST /api/v1/me/bookings/{booking_id}/cancel`.

Ví dụ cấu hình ghế cho chuyến nháp:

```json
{
  "wsRequest": {
    "seats": [
      { "seatCode": "A01", "seatClass": "STANDARD" },
      { "seatCode": "A02", "seatClass": "STANDARD" }
    ]
  }
}
```

Request tạo đặt vé dùng wrapper `wsRequest`; mỗi hành khách chọn một ghế và tự nhập địa chỉ:

```json
{
  "wsRequest": {
    "tripId": "b2a5417d-88d6-4ca0-8c94-886d4a79a601",
    "contactName": "Nguyen Van A",
    "contactPhone": "0901234567",
    "contactEmail": "user@example.com",
    "passengers": [
      {
        "passengerName": "Nguyen Van A",
        "passengerPhone": "0901234567",
        "pickupAddress": "12 Nguyen Trai, Ha Noi",
        "dropoffAddress": "Ben xe Da Nang",
        "seatId": "73b779aa-b7a6-4428-8de6-724abf10d08c"
      }
    ]
  }
}
```

Server tính giá từ tuyến và trả `holdExpiresAt`; client không gửi số tiền hoặc trạng thái ghế.

### Hồ sơ khách hàng

Các API dưới đây yêu cầu `Authorization: Bearer <accessToken>` và luôn thao tác trên tài khoản đang đăng nhập.

| Method | Endpoint | Chức năng |
| --- | --- | --- |
| GET | `/api/v1/me/profile` | Lấy hồ sơ hiện tại |
| PATCH | `/api/v1/me/profile` | Cập nhật một phần hồ sơ |

`wsResponse` của cả hai API gồm `id`, `fullName`, `email`, `phone`, `accountStatus`, `phoneVerifiedAt`, `createdAt`, `updatedAt`.

Ví dụ cập nhật:

```json
{
  "wsRequest": {
    "fullName": "Nguyen Van B",
    "email": "new-email@example.com",
    "phone": "0901234567"
  }
}
```

Chỉ cần gửi các trường muốn đổi: `fullName` (tối đa 160 ký tự), `email` (đúng định dạng, tối đa 254 ký tự), `phone` (số nội địa bắt đầu bằng `0` hoặc dạng quốc tế `+...`). Trường không gửi hoặc `null` được giữ nguyên; chuỗi rỗng và request không có trường cập nhật trả `400`. Họ tên/email được bỏ khoảng trắng đầu/cuối, email chuyển thành chữ thường, số điện thoại được bỏ khoảng trắng, dấu ngoặc và dấu gạch nối. Email hoặc số điện thoại đã thuộc tài khoản khác trả `409`; gửi lại thông tin của chính mình vẫn hợp lệ. Khi đổi số điện thoại, `phoneVerifiedAt` được đặt lại thành `null`.

### Lọc và phân trang lịch sử đặt vé

`GET /api/v1/me/bookings` nhận các query parameter tùy chọn:

| Tham số | Mặc định | Quy tắc |
| --- | --- | --- |
| `status` | Tất cả | `HOLDING`, `CONFIRMED`, `CANCELLED`, `EXPIRED` |
| `routeId` | Tất cả | UUID của tuyến |
| `q` | Không lọc | Tối đa 200 ký tự; tìm mã đặt vé, mã vé, mã chuyến, mã/tên tuyến, tên hãng hoặc tên hành khách |
| `page` | `0` | Chỉ số trang bắt đầu từ 0 |
| `size` | `20` | Số đơn mỗi trang, từ 1 đến 100 |

Các bộ lọc được kết hợp với nhau. Tìm kiếm `q` không phân biệt hoa/thường, bỏ khoảng trắng đầu/cuối; `%`, `_`, `!` được tìm như ký tự thông thường. Kết quả sắp theo thời gian tạo mới nhất, sau đó theo ID giảm dần. Đơn `HOLDING` đã quá `holdExpiresAt` được tính là `EXPIRED` ngay trong bộ lọc và response, kể cả khi trạng thái lưu trong database chưa được cập nhật.

Ví dụ: `GET /api/v1/me/bookings?status=CONFIRMED&q=Ha%20Noi&page=0&size=10`.

**Thay đổi response:** `wsResponse` hiện là đối tượng phân trang, thay cho mảng trực tiếp. Frontend lấy danh sách tại `wsResponse.content`:

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

Mỗi phần tử trong `content` có cùng cấu trúc với chi tiết đặt vé bên dưới. Trang vượt quá số kết quả trả `content: []` và vẫn giữ tổng số đơn phù hợp bộ lọc. Tham số sai định dạng hoặc ngoài giới hạn trả `400`.

### Dữ liệu cho màn “Xem vé”

`GET /api/v1/me/bookings/{booking_id}` trả thông tin đặt vé trong `wsResponse`. ID không tồn tại hoặc thuộc khách hàng khác đều trả `404`.

| Nhóm | Trường |
| --- | --- |
| Đặt vé | `id`, `bookingCode`, `tripId`, `status`, `createdAt`, `updatedAt`, `holdExpiresAt`, `confirmedAt`, `cancelledAt` |
| Liên hệ | `contactName`, `contactPhone`, `contactEmail` |
| Giá đã đặt | `quotedTotalAmount`, `currency` |
| `trip` | `id`, `tripCode`, `status`, `departureAt`, `expectedArrivalAt`, `timezone` |
| Hãng trong `trip` | `companyId`, `companyName`, `companyHotline` |
| Tuyến trong `trip` | `routeId`, `routeCode`, `routeName`, `originAreaId`, `originAreaName`, `destinationAreaId`, `destinationAreaName` |
| Mỗi phần tử `tickets` | `id`, `ticketCode`, `status`, `createdAt`, `passengerName`, `passengerPhone`, `pickupAddress`, `dropoffAddress`, `seatId`, `seatCode`, `seatClass`, `fareAmount` |

`quotedTotalAmount`, `fareAmount` và `seatCode` được giữ theo dữ liệu lúc đặt. Thông tin hãng/tuyến/lịch chuyến phản ánh dữ liệu hiện tại. Các mốc thời gian được trả ở dạng ISO local datetime; `trip.timezone` là múi giờ của hãng. `pickupAddress` và `dropoffAddress` là địa chỉ khách nhập cho từng vé.

Frontend dùng `bookingCode`/`ticketCode` để hiển thị mã đặt vé/vé; `CONFIRMED` là trạng thái đã xác nhận. Chức năng QR check-in, thông tin xe/tài xế và thanh toán chưa có trong mô hình hiện tại.

## Kiểm thử

Chạy `./mvnw test` (Windows: `./mvnw.cmd test`). Các bài kiểm thử dùng profile `test` với database H2 trong bộ nhớ, không cần PostgreSQL đang chạy. Bộ kiểm thử khách hàng kiểm tra xác thực, quyền sở hữu vé, cập nhật hồ sơ, dữ liệu vé điện tử, các bộ lọc và phân trang, bao gồm đơn giữ ghế đã hết hạn.

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
