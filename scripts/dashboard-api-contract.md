# Hợp đồng API Tổng quan Admin

Base: `/api/v1/bus-companies/{companyId}`. Tất cả endpoint yêu cầu token có quyền COMPANY_ADMIN tại hãng trong URL. Response giữ envelope `{ code, message, wsResponse }`.

## Khoảng ngày và so sánh

- Ngày: `yyyy-MM-dd`, bao gồm cả `from` và `to`.
- Summary, trips, top-drivers và reports/revenue mặc định 14 ngày gồm hôm nay theo timezone hãng.
- Chỉ gửi `to`: lấy 14 ngày kết thúc tại `to`. Chỉ gửi `from`: lấy 14 ngày bắt đầu tại `from`.
- Top tuyến là ngoại lệ: không gửi ngày nghĩa là all-time; chỉ gửi một cận thì phía còn lại không giới hạn.
- Sự cố mở không bị cắt theo ngày. Chuyến hôm nay lấy ngày hiện tại theo timezone hãng.
- Biểu đồ trips và periods doanh thu trả đủ ngày, kể cả ngày bằng 0; tối đa 3660 ngày/request.
- Kỳ so sánh là khoảng ngay trước đó với cùng số ngày. `9.20` nghĩa là tăng 9,2%, không phải tỷ lệ 0.092. Nếu cả hai kỳ bằng 0 thì phần trăm bằng 0; kỳ trước bằng 0 nhưng kỳ này khác 0 thì phần trăm là `null`, FE hiển thị “Chưa có cơ sở so sánh”.
- Khoảng ngày đảo, ngoài năm 0001..9999, limit ngoài 1..100 hoặc khoảng biểu đồ quá dài trả HTTP 400 với envelope lỗi. Khoảng yêu cầu so sánh phải có kỳ trước nằm trong năm 0001..9999.

## GET /dashboard/trips?from=&to=

`wsResponse` vẫn là mảng. Giữ `date`, `totalTrips`, `byStatus`; thêm:

```json
{
  "date": "2026-10-16",
  "totalTrips": 96,
  "revenue": 32400000,
  "ticketCount": 240,
  "byStatus": {
    "DRAFT": 0,
    "SCHEDULED": 10,
    "IN_PROGRESS": 5,
    "COMPLETED": 78,
    "CANCELLED": 3,
    "TERMINATED": 0
  }
}
```

`revenue` là tổng giá trị vé CONFIRMED theo **ngày khởi hành**, đơn vị VND; `ticketCount` là số vé tương ứng. Không phải tiền thực thu hay số vé bán trong ngày. FE chia 1.000.000 khi hiển thị triệu đồng. Số chuyến vẫn gồm mọi trạng thái, có đủ key trạng thái bằng 0.

## GET /dashboard/summary?from=&to=

Giữ các field cũ; thêm `draftTrips`, `terminatedTrips` để các nhóm trạng thái cộng đủ `totalTrips`.

Thêm `previousFrom`, `previousTo`, `previousTotalTrips`, `previousScheduledTrips`, `previousInProgressTrips`, `previousCompletedTrips`, `previousCancelledTrips`, `previousConfirmedTickets`, `previousRevenue`.

Các field phần trăm: `totalTripsChangePct`, `scheduledTripsChangePct`, `inProgressTripsChangePct`, `completedTripsChangePct`, `cancelledTripsChangePct`, `confirmedTicketsChangePct`, `revenueChangePct`.

Badge doanh thu của biểu đồ có thể dùng `previousRevenue`/`revenueChangePct` ở summary nếu cùng khoảng ngày. Giá trị kỳ hiện tại là `confirmedTicketValue`.

## GET /reports/revenue?from=&to=&status=CONFIRMED

Giữ `tickets`, `ticketValue`, `currency`, `actualCollected`, `periods`. `periods` giờ có đủ ngày bằng 0.

Thêm `previousFrom`, `previousTo`, `previousTickets`, `previousRevenue`, `ticketsChangePct`, `revenueChangePct`. Hai kỳ dùng cùng bộ lọc trạng thái vé. FE có thể dùng số so sánh ở đây cho badge biểu đồ. Mặc định `status=CONFIRMED`; `actualCollected=false`.

## GET /dashboard/today-trips?limit=10

`wsResponse` là mảng, sắp xếp `departureAt` tăng dần, sau đó ID; limit từ 1 đến 100. Bao gồm chuyến chưa phân công, bỏ DRAFT, giữ các trạng thái chuyến còn lại.

Các field: `tripId`, `tripCode`, `routeName`, `originAreaName`, `destinationAreaName`, `departureAt`, `actualDepartureAt`, `tripStatus`, `vehicleId`, `vehiclePlate`, `driverId`, `driverName`, `assignmentStatus`, `lateMinutes`.

- Thời điểm có offset ISO-8601 theo timezone hãng, ví dụ `2026-11-12T09:30:00+07:00`.
- Khi không có phân công hợp lệ, các field xe/tài xế/trạng thái lệnh là null. Phân công REJECTED/REPLACED không được dùng làm phân công hiện hành.
- `assignmentStatus` là trạng thái lệnh, tách biệt với `tripStatus`.
- `lateMinutes` đo trễ **khởi hành**: dùng giờ khởi hành thực tế khi đã có; chuyến SCHEDULED chưa khởi hành dùng giờ hiện tại. Không âm, chuyến CANCELLED bằng 0. Chuyến đã kết thúc/đang chạy nhưng thiếu mốc khởi hành thực tế trả 0, không tăng trễ mãi theo hiện tại.
- Endpoint chỉ đọc trạng thái lệnh đã lưu; không bổ sung thao tác chuyển PREPARING/ARRIVED cho tài xế.

## GET /dashboard/top-routes?from=&to=&limit=5

`wsResponse` là mảng: `routeId`, `routeCode`, `routeName`, `tripCount`, `passengerCount`, `revenue`, `seatCount`, `occupancyRate`.

- Xếp theo revenue giảm dần, số chuyến giảm dần, sau đó routeId; giới hạn tại database.
- Nhóm theo routeId; không gộp hai tuyến ngược chiều theo tên.
- Bỏ DRAFT; các trạng thái chuyến còn lại được tính vào tripCount và tổng ghế. Tuyến chưa có vé vẫn có thể xuất hiện với doanh thu 0.
- passengerCount là số vé CONFIRMED, không phải số booking hoặc khách distinct.
- revenue là tổng giá trị các vé đó, VND.
- seatCount là tổng ghế snapshot của cùng tập chuyến. occupancyRate = passengerCount / seatCount, làm tròn 4 chữ số thập phân, FE tự đổi thành %. Nếu chưa có ghế snapshot thì trả null.

## GET /dashboard/open-incidents?limit=10

`wsResponse` là mảng: `incidentId`, `incidentCode`, `tripId`, `tripCode`, `type`, `typeLabel`, `description`, `driverId`, `driverName`, `reportedAt`, `ageMinutes`, `status`.

- Chỉ OPEN/IN_PROGRESS; bỏ cả RESOLVED và CLOSED; mới nhất trước, sau đó ID.
- driverId/driverName lấy từ người báo sự cố, không lấy tài xế vừa được phân công lại.
- incidentCode là `CN-` + UUID đầy đủ bỏ dấu gạch, viết hoa; là mã hiển thị ổn định, không phải số thứ tự.
- reportedAt ánh xạ opened_at, có offset timezone hãng; ageMinutes không âm.
- Type hợp lệ giữ nguyên: VEHICLE_BREAKDOWN, ACCIDENT, TRAFFIC, PASSENGER, HEALTH, OTHER. Có typeLabel tiếng Việt. Không bổ sung DRIVER_LATE hoặc thao tác xử lý/đóng sự cố trong phạm vi này.

## GET /dashboard/top-drivers?from=&to=

Giữ số chuyến và xếp hạng theo số chuyến được phân công; giới hạn 10 tại database. Bỏ phân công REJECTED/REPLACED và chuyến DRAFT.

Thêm `terminatedTrips`, `revenue` và `rating`. Revenue là giá trị vé CONFIRMED quy thuộc tài xế đang được lưu trong phân công của chuyến. Hệ thống chưa có lịch sử phân công để chia doanh thu cho các tài xế đã bị thay thế. `rating=null` vì chưa có nguồn đánh giá thật; FE cần hiển thị dấu gạch hoặc ẩn sao, không mặc định 5 sao.

## Phạm vi chỉnh sửa

Chỉ thêm/sửa controller, service, repository, DTO và tài liệu; không sửa frontend/schema, không triển khai các khối chỉ có trong design. Không chạy build hoặc test theo yêu cầu.
