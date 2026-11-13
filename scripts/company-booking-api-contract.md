# API quản lý đơn đặt

Tiền tố: `/api/v1/bus-companies/{company_id}/bookings`.
Các endpoint yêu cầu Bearer token và quyền `COMPANY_ADMIN` của hãng trong URL.
Response giữ cấu trúc `BaseResponse`, dữ liệu nằm trong `wsResponse`.

## 1. GET /summary

Không nhận bộ lọc. Trả thống kê toàn hãng:

```json
{
  "date": "2026-11-13",
  "timezone": "Asia/Ho_Chi_Minh",
  "todayBookings": 9,
  "bookedTickets": 20,
  "totalTicketAmount": 3030000,
  "currency": "VND"
}
```

- `todayBookings`: số đơn có chuyến khởi hành trong hôm nay theo múi giờ hãng,
  tính mọi trạng thái đơn, từ đầu ngày đến trước đầu ngày kế tiếp.
- `bookedTickets`: số vé `CONFIRMED` thuộc đơn `CONFIRMED`, trên toàn bộ thời gian.
  Một đơn hai ghế đóng góp hai vé.
- `totalTicketAmount`: tổng `fare_amount` của chính các vé trên, tính bằng VND.
  Không cộng lại giá trị đơn cho từng vé; không tính vé hủy, hết hạn hoặc giữ chỗ.
- Không có vé thì số vé và tổng giá trị bằng 0. Đây là giá trị vé, chưa phải tiền thực thu.

## 2. GET /status-counts

Nhận cùng bộ lọc với danh sách. Bỏ qua `viewStatus`, `page`, `size` khi đếm;
các bộ lọc khác vẫn áp dụng, kể cả `status` và `passengerTripStatus` nếu được gửi.

```json
{
  "all": 24,
  "booked": 5,
  "pickedUp": 7,
  "completed": 6,
  "noShow": 2,
  "cancelled": 4,
  "holding": 0,
  "expired": 0
}
```

`all = booked + pickedUp + completed + noShow + cancelled + holding + expired`.
Giao diện dùng sáu số đầu cho sáu tab. Hai số cuối giải thích những đơn chưa xác nhận
chỉ xuất hiện trong tab Tất cả. Đơn hết thời gian giữ chỗ được tính EXPIRED ngay cả
khi tác vụ cập nhật trạng thái hết hạn chưa chạy.

## 3. GET /?viewStatus=...&driverId=...

Giữ phân trang, thứ tự tạo mới nhất trước và các bộ lọc hiện có:
`q`, `status`, `tripId`, `routeId`, `customerId`, `createdFrom`, `createdTo`,
`departureFrom`, `departureTo`, `page` (mặc định 0), `size` (mặc định 20, tối đa 100).
Các cận thời gian bao gồm giá trị được gửi và dùng ISO local datetime.

Thêm các bộ lọc:

| Tham số | Ý nghĩa |
| --- | --- |
| `viewStatus` | `BOOKED`, `PICKED_UP`, `COMPLETED`, `NO_SHOW`, `CANCELLED`; bỏ trống cho Tất cả. Cũng hỗ trợ `HOLDING`, `EXPIRED` |
| `driverId` | UUID tài khoản tài xế hiện được phân công; không tính phân công REJECTED/REPLACED |
| `passengerTripStatus` | `WAITING_PICKUP`, `PICKED_UP`, `DROPPED_OFF`, `NO_SHOW`; chỉ áp dụng cho đơn CONFIRMED |

Ví dụ lấy tab Đã đón của một tài xế:

```text
GET /api/v1/bus-companies/{company_id}/bookings?viewStatus=PICKED_UP&driverId={driver_id}&page=0&size=20
```

Mỗi dòng thêm `viewStatus` và `passengerTripStatus` vào dữ liệu hiện có.
`status` vẫn là trạng thái đặt chỗ gốc để tương thích với client cũ.
`passengerTripStatus` bằng null nếu đơn chưa xác nhận, đã hủy hoặc hết hạn.

Quy tắc trạng thái chung:

- Một đơn có chung hành trình và điểm đón/trả; mỗi tab đếm đơn, không đếm ghế.
- CONFIRMED + WAITING_PICKUP → BOOKED; PICKED_UP → PICKED_UP;
  DROPPED_OFF → COMPLETED; NO_SHOW → NO_SHOW.
- Chỉ dùng vé CONFIRMED để xác định hành trình; không dùng trạng thái chuyến
  COMPLETED để tự suy ra tất cả khách đã xuống xe.
- Nếu dữ liệu vé cũ lệch trạng thái, dùng thứ tự ưu tiên WAITING_PICKUP → PICKED_UP
  → NO_SHOW → DROPPED_OFF. Đơn CONFIRMED không có vé CONFIRMED được xem là BOOKED.
  Danh sách, số đếm và chi tiết dùng cùng quy tắc này.
- Các bộ lọc kết hợp bằng AND. Để chuyển tab, thay `viewStatus` và đặt `page=0`;
  không cần gửi thêm `status` hoặc `passengerTripStatus` trừ khi muốn thu hẹp kết quả.

## 4. GET /{booking_id}

Giữ dữ liệu liên hệ, chuyến/tuyến, giá, vé/hành khách và thông tin hủy hiện có.
Bổ sung:

- `viewStatus`, `passengerTripStatus`: trạng thái chung như danh sách.
- `pickupAddress`, `dropoffAddress`: địa chỉ chung lấy từ vé đầu tiên theo thứ tự
  thời điểm tạo và ID; dữ liệu gốc từng vé vẫn nằm trong `tickets`.
- `assignment`: `driverId`, `driverName`, `driverPhone`, `vehicleId`, `plateNumber`,
  `vehicleModel`, `passengerCapacity`, `status`. Bằng null nếu chưa phân công
  hoặc phân công đã REJECTED/REPLACED. Đây là phân công hiện tại của chuyến.
- `boardedAt`, `completedAt`: thời điểm muộn nhất của các vé CONFIRMED, chỉ trả
  khi toàn bộ các vé đó đã có mốc thời gian tương ứng.
- `history`: các phần tử có `eventType`, `title`, `occurredAt`, `state`, `description`.
  `state` là DONE hoặc PENDING; các mốc chờ có thời gian null.
- `historyComplete`: false khi các vé lệch trạng thái hoặc thiếu thời điểm của
  mốc đã hoàn tất. Lịch sử dựng từ thời điểm thật đã lưu, không phải nhật ký
  đầy đủ mọi lần chỉnh sửa. NO_SHOW hiện chưa có mốc thời gian riêng nên
  trả DONE với `occurredAt=null`, `historyComplete=false`.

API hủy hiện có vẫn dùng `POST /{booking_id}/cancel`, trả cấu trúc chi tiết mở rộng.
Không bổ sung API thanh toán hay API admin cập nhật hành trình trong thay đổi này.

## Xác thực dữ liệu

UUID/enum sai, cận ngày đảo ngược, từ khóa quá 200 ký tự hoặc phân trang không hợp lệ:
400. Chưa đăng nhập: 401. Không có quyền hãng: 403. Hãng không hoạt động hoặc
đơn không thuộc hãng: 404. Múi giờ hãng trống dùng `Asia/Ho_Chi_Minh`; giá trị sai trả 400.
