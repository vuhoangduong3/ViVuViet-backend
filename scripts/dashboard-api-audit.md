# Đối chiếu API màn Tổng quan Admin

Ngày rà soát: 12/11/2026. Phạm vi: yêu cầu Tổng quan đính kèm, frontend trong workspace, controller → service → repository → DTO/entity của backend và test tích hợp H2.

## 1. Xác định đúng phiên bản đang kiểm tra

| Nguồn | Đường dẫn | Phiên bản lúc kiểm tra |
| --- | --- | --- |
| Frontend Next.js | `E:/NMCNPM/Design/nmcnpm` | nhánh `main`, commit `4687d5b` |
| Backend Spring Boot | `E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet` | nhánh `main`, commit `d84fd77` |

`Design/nmcnpm` không chứa backend Spring Boot. Frontend ở phiên bản này cũng **chưa có** route `tong-quan`, component `dash-head.tsx`, domain API dashboard/doanh thu hoặc `src/api/tai-xe/api/types.ts` được dẫn trong tài liệu đính kèm. Vì vậy, các nhận định “FE đã nối 3 endpoint”, “FE hardcode revenue = 0”, ngày header và nút xuất báo cáo là thông tin từ tài liệu, **chưa thể xác nhận từ frontend local**. Không suy diễn những phần này là đã kiểm tra được trên code.

Các kết luận về backend bên dưới đã được đối chiếu trên source hiện có. Không đổi logic ứng dụng trong lần rà soát này.

## 2. Kết quả đối chiếu từng nhu cầu

Mọi đường dẫn dưới đây có tiền tố `/api/v1/bus-companies/{companyId}`, trừ API tài xế được ghi riêng.

| Nhu cầu | API sẵn có | Đáp ứng thực tế | Việc cần làm |
| --- | --- | --- | --- |
| KPI | `GET /dashboard/summary` | Có tổng chuyến, 4 bộ đếm trạng thái, số vé CONFIRMED và giá trị vé | Thống nhất khoảng ngày, cách tính DRAFT/TERMINATED; xu hướng là bổ sung tùy chọn |
| Số chuyến/ngày | `GET /dashboard/trips` | Có `date`, `totalTrips`, `byStatus` | Thiếu field doanh thu trực tiếp, nhưng có thể ghép API doanh thu sẵn có; xử lý ngày trống |
| Giá trị vé/ngày | `GET /reports/revenue?from=&to=&status=CONFIRMED` | **Đã có** `periods: [{date,tickets,ticketValue}]` theo ngày khởi hành | Có thể dùng ngay cho đường doanh thu, không bắt buộc thêm endpoint/field mới |
| Chuyến hôm nay | `GET /trips?date=yyyy-MM-dd` và `GET /trips/{tripId}/assignments` | Lọc ngày được; có tuyến, giờ, ID chuyến, tài xế qua request riêng; thiếu trạng thái lệnh và biển số trong DTO phân công | Bổ sung dữ liệu bulk vào API chuyến, hoặc thêm `GET /dashboard/today-trips` |
| Top tài xế | `GET /dashboard/top-drivers` | Có số chuyến phân công và 4 bộ đếm trạng thái; tối đa 10 | Chưa có doanh thu/rating, chưa nhận tham số limit |
| Top tuyến | `GET /routes` chỉ cung cấp danh mục tuyến | **Chưa có API tổng hợp** số chuyến, hành khách, giá trị vé, lấp ghế theo tuyến | Thêm `GET /dashboard/top-routes` hoặc API báo cáo tương đương |
| Sự cố đang mở | Chỉ có API tài xế tạo sự cố | **Chưa có API admin đọc danh sách** | Thêm `GET /dashboard/open-incidents` hoặc API admin sự cố có bộ lọc |

Nguồn khai báo 4 API thống kê: [DashboardController.java](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/src/main/java/com/example/Vi_vu_Viet/Controller/Trip/DashboardController.java:38). API chuyến/ngày và phân công: [TripManagementController.java](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/src/main/java/com/example/Vi_vu_Viet/Controller/Trip/TripManagementController.java:59).

**Kết luận số endpoint:** hiện thiếu cả 3 URL `today-trips`, `top-routes`, `open-incidents` được đề xuất trong tài liệu. Tuy nhiên, chỉ **2 nhu cầu hoàn toàn chưa có API đọc/tổng hợp tương đương** là Top tuyến và Sự cố. Chuyến hôm nay đã có nền tảng để mở rộng. Biểu đồ doanh thu đã có nguồn theo ngày.

## 3. Doanh thu: dùng được API hiện có, nhưng phải hiểu đúng số liệu

[DashboardRepository.java](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/src/main/java/com/example/Vi_vu_Viet/Repository/Trip/DashboardRepository.java:75) nhóm theo năm/tháng/ngày của `trip.departure_at`, đếm vé và cộng `ticket.fare_amount`. [DashboardServiceImpl.java](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/src/main/java/com/example/Vi_vu_Viet/Service/Trip/Implement/DashboardServiceImpl.java:100) mặc định chọn `CONFIRMED`, trả `currency=VND`, `actualCollected=false`.

Hệ quả khi nối FE:

- Ghép `/dashboard/trips` và `/reports/revenue` bằng `date`, cùng `companyId`, `from`, `to`; dùng `ticketValue / 1_000_000` để hiển thị triệu đồng.
- Đây là **giá trị vé đã xác nhận theo ngày khởi hành**, cùng cơ sở với `confirmedTicketValue` trong summary. Không phải tiền thực thu; cũng không phải số vé bán/xác nhận trong ngày đó.
- Nếu `ticketCount` được thêm vào biểu đồ, cần mô tả là số vé CONFIRMED của các chuyến khởi hành trong ngày. Muốn “vé bán trong ngày” phải dùng mốc xác nhận bán vé và một hợp đồng thống kê khác.
- API chỉ trả những ngày có dữ liệu. FE hoặc BE phải bổ sung các ngày còn thiếu bằng 0 để trục ngày không bị đứt. `byStatus` cũng chỉ có key xuất hiện, cần mặc định key thiếu bằng 0.
- Nếu chọn bổ sung `revenue`/`ticketCount` trực tiếp vào `/dashboard/trips`, tái sử dụng truy vấn doanh thu hiện có và ghép theo ngày trong service. Không cần tạo endpoint doanh thu mới. Không join vé rồi `count(trip)` trực tiếp vì sẽ nhân số chuyến theo số vé.

Test bổ sung đã xác nhận hai ngày có `ticketValue` lần lượt 250.000 và 300.000 VND, và bộ lọc `to` bao gồm trọn ngày cuối.

## 4. Chuyến hôm nay: thiếu dữ liệu và có lỗi khi dùng API hiện có

### 4.1. Phần đã có

`GET /trips?date=...` trả `TripResponse` với tuyến, điểm đi/đến, mã chuyến, `departureAt`, `actualDepartureAt`, trạng thái chuyến. `GET /trips/{id}/assignments` trả `tripId`, `vehicleId`, `driverId`, `driverName`, `driverPhone`, `assignedAt`; chưa phân công thì `wsResponse=null`.

[TripAssignmentResponse.java](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/src/main/java/com/example/Vi_vu_Viet/dto/Response/Trip/TripAssignmentResponse.java:12) và [mapper phân công](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/src/main/java/com/example/Vi_vu_Viet/Service/Trip/Implement/TripServiceImpl.java:349) **không trả `assignmentStatus` hoặc biển số xe**. Vì vậy ghép API hiện tại vẫn chưa đủ bảng theo yêu cầu. API xe có thể cấp biển số qua request riêng, nhưng sẽ làm tăng request theo từng dòng.

### 4.2. Lỗi bộ lọc phân công cần sửa

[TripServiceImpl.java](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/src/main/java/com/example/Vi_vu_Viet/Service/Trip/Implement/TripServiceImpl.java:85) xác định ASSIGNED chỉ bằng việc tồn tại bản ghi `trip_assignments`. Bản ghi đã `REJECTED` hoặc `REPLACED` vẫn được tính là đã phân công. Điều này còn lệch với Top tài xế, nơi hai trạng thái này bị loại.

Test đã tái hiện: đặt assignment thành REJECTED → `/trips?assignmentStatus=ASSIGNED` vẫn trả chuyến; `/dashboard/top-drivers` không còn đếm phân công đó.

Cần phân biệt bộ lọc **có phân công hợp lệ hay chưa** với enum **tiến trình nhận lệnh**. Không dùng `ASSIGNED/UNASSIGNED` hiện tại để suy ra ACCEPTED/PREPARING/ARRIVED. Sửa predicate theo trạng thái phân công hợp lệ và thống nhất cách hiển thị tài xế vừa từ chối.

### 4.3. Cách triển khai đề xuất

Ưu tiên `GET /dashboard/today-trips?limit=10` nếu muốn bảng có hợp đồng riêng. Phương án ít URL hơn: mở rộng `/trips` với dữ liệu phân công được lấy bulk và hỗ trợ giới hạn/sắp xếp phù hợp. Không ghép N request phân công trong FE.

Dữ liệu cần trả: các field trong yêu cầu, thêm `tripStatus` và `actualDepartureAt` để phân biệt tiến trình chuyến với tiến trình lệnh. `assignmentStatus` phải nullable khi chưa có phân công hợp lệ; không gán ASSIGNED cho chuyến chưa có tài xế. Giữ nguyên `REJECTED` trong hợp đồng hoặc định nghĩa rõ khi nào chuyển thành trạng thái cần phân công lại. Enum backend có thêm REJECTED so với danh sách đính kèm.

Query cần LEFT JOIN để giữ chuyến chưa gán tài xế/xe; lọc ngày tại database theo `[đầu ngày, đầu ngày kế tiếp)`, sort `departureAt ASC, tripId ASC`, giới hạn tại DB. Hiện [TripRepository.java](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/src/main/java/com/example/Vi_vu_Viet/Repository/Trip/TripRepository.java:22) lấy toàn bộ chuyến hãng theo giờ giảm dần; service mới lọc ngày trong bộ nhớ. Chưa có limit/pagination. Phải thống nhất có loại DRAFT, CANCELLED, TERMINATED khỏi bảng vận hành hay không.

### 4.4. Enum có nhưng quy trình cập nhật chưa đủ

Backend khai báo `PREPARING`, `ARRIVED`, nhưng các service hiện chỉ ghi ASSIGNED khi phân công, ACCEPTED khi nhận, REJECTED khi từ chối và COMPLETED khi kết thúc. Không tìm thấy API nghiệp vụ chuyển sang PREPARING/ARRIVED; code seed có thể tạo dữ liệu mẫu với trạng thái truyền vào.

API tài xế `PATCH /api/v1/me/driver/trips/{id}/status` nhận **TripStatus**, không phải DriverAssignmentStatus: [UpdateTripProgressRequest.java](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/src/main/java/com/example/Vi_vu_Viet/dto/Request/Trip/UpdateTripProgressRequest.java:12). Muốn hai nhãn này cập nhật thật, cần thêm thao tác tiến trình lệnh cho tài xế hoặc mở rộng hợp đồng hiện có với transition rõ ràng. Chỉ thêm field vào GET dashboard sẽ chưa giải quyết được.

### 4.5. Quy tắc trễ phải rõ

- Trễ khởi hành khi đã chạy: `max(0, phút(actualDepartureAt - departureAt))`.
- Chưa chạy và đã quá giờ: `max(0, phút(now - departureAt))`, sau khi loại trạng thái không áp dụng.
- Không dùng `now - departureAt` cho mọi dòng; chuyến đã hoàn thành sẽ bị tăng trễ mãi.
- Nếu cần đo “tài xế đến muộn” thay vì “khởi hành muộn”, hiện chưa có timestamp `arrivedAt` để đo chính xác. Phải bổ sung mốc sự kiện; enum ARRIVED tự nó không cung cấp thời gian đến.

Các công thức trên là đề xuất hợp đồng, chưa có `lateMinutes` trong backend hiện tại.

## 5. Top tuyến: thực sự cần API tổng hợp

`GET /routes` chỉ là danh mục, không có các chỉ số yêu cầu. Đề xuất `GET /dashboard/top-routes?from=&to=&limit=5`, mặc định all-time đúng theo mục riêng trong tài liệu.

Cần chốt và ghi trong OpenAPI:

| Field | Cách tính đề xuất |
| --- | --- |
| `routeId`, `routeCode`, `routeName` | Từ Route; nhóm theo ID, không theo tên. Hai tuyến ngược chiều vẫn là hai ID nếu mô hình hiện tại lưu riêng |
| `tripCount` | Đếm chuyến thuộc tập trạng thái được chấp nhận, không bị nhân theo số vé |
| `passengerCount` | Số vé CONFIRMED nếu mục đích là khách đặt chỗ; không dùng số booking hoặc số tài khoản distinct để thay thế |
| `revenue` | Tổng fare_amount của chính tập vé này, VND, giá trị vé chứ chưa phải tiền thực thu |
| `occupancyRate` | Số ghế có vé hợp lệ / tổng ghế snapshot của cùng tập chuyến; trả 0..1. Mẫu số 0 cần quy định null hoặc 0 |

Nguồn ghế đã có trong [TripSeat.java](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/src/main/java/com/example/Vi_vu_Viet/Entity/Trip/TripSeat.java:28). Ưu tiên snapshot ghế chuyến thay vì sức chứa hiện tại của xe vì xe có thể thay đổi. Nếu cần “khách thực đi”, dùng dữ liệu đón/trả khách và định nghĩa NO_SHOW riêng; đó là chỉ số khác số khách đặt vé.

Đề xuất xếp theo `revenue DESC`, sau đó `tripCount DESC`, `routeId` để ổn định; validate limit, giới hạn tại DB. Lấy tổng chuyến, tổng ghế và tổng vé bằng aggregate độc lập để tránh nhân bản khi join nhiều quan hệ một-nhiều. Tuyến có chuyến nhưng chưa có vé vẫn cần được xét đúng theo quy tắc xếp hạng.

Không tạo thêm bảng thống kê chỉ để có endpoint này; các entity nguồn đã tồn tại. Cách tính và tập trạng thái ở bảng trên là đề xuất cần thống nhất trước khi triển khai.

## 6. Sự cố: entity và enum đã có, thiếu API admin đọc

Đã xác nhận [TripIncident.java](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/src/main/java/com/example/Vi_vu_Viet/Entity/Trip/TripIncident.java:27) ánh xạ bảng `trip_incidents` với: `company_id`, `trip_id`, `reported_by`, `incident_type`, `description`, `status`, `opened_at`, cùng ID và timestamps kế thừa BaseEntity.

API tạo hiện có: `POST /api/v1/me/driver/trips/{tripId}/incidents`, body envelope `wsRequest` chứa `type` và `description`. Service ghi OPEN. Repository mới chỉ kế thừa JpaRepository, chưa có truy vấn danh sách theo hãng/trạng thái.

Những điểm phải sửa trong tài liệu đính kèm:

- Type **đã chuẩn hóa**, không phải chỉ free-text: `VEHICLE_BREAKDOWN | ACCIDENT | TRAFFIC | PASSENGER | HEALTH | OTHER`.
- `DRIVER_LATE` **không phải enum hợp lệ hiện tại**. Không đưa vào FE như mã đã được BE hỗ trợ. Nếu muốn bổ sung, cần sửa hợp đồng request/response và quy tắc validation tương ứng.
- Status thực tế: `OPEN | IN_PROGRESS | RESOLVED | CLOSED`. Danh sách đang mở phải lấy **OPEN, IN_PROGRESS**; điều kiện `status != RESOLVED` sẽ lấy nhầm CLOSED.
- Chưa có `incidentCode` hoặc `typeLabel`. `typeLabel` có thể map từ enum mà không thêm cột. Nếu mã CN-1042 là bắt buộc, cần cơ chế sinh mã ổn định và schema/migration tương ứng; nếu không, thống nhất bỏ field mã khỏi hợp đồng.
- Response tài xế hiện dùng `id`, `openedAt`, không phải `incidentId`, `reportedAt`. API dashboard có thể map tên mới nhưng phải ghi rõ. Có thể bổ sung `tripCode`, tên người báo từ các quan hệ sẵn có.
- Tài xế báo sự cố phải lấy từ `reported_by`, không lấy tài xế đang được phân công lại cho chuyến.

Nguồn enum: [Enums.java](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/src/main/java/com/example/Vi_vu_Viet/Util/Enums.java:75). Nguồn tạo và mapping: [DriverServiceImpl.java](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/src/main/java/com/example/Vi_vu_Viet/Service/Trip/Implement/DriverServiceImpl.java:236).

Đề xuất `GET /dashboard/open-incidents?limit=10`, lọc company và OPEN/IN_PROGRESS tại DB, trả ID/mã chuyến, loại, mô tả, người báo, thời điểm mở, trạng thái; sort và limit phải ghi rõ. `ageMinutes` có thể tính từ openedAt hoặc FE tự tính khi thời điểm đã có múi giờ rõ ràng.

Trong code hiện tại chưa có thao tác admin chuyển OPEN → IN_PROGRESS → RESOLVED/CLOSED. Chỉ GET là đủ để dựng phần đọc của Tổng quan; vòng đời xử lý vẫn là việc phải bổ sung cho màn Chuyến theo đúng phạm vi ghi chú ban đầu. Không mô tả vòng đời đó là đã có sẵn.

## 7. Các hợp đồng chung phải chỉnh lại

### Khoảng ngày

[DashboardServiceImpl.java](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/src/main/java/com/example/Vi_vu_Viet/Service/Trip/Implement/DashboardServiceImpl.java:157) dùng cận từ năm 1 tới đầu năm 10000 khi không truyền ngày. Đây là **all-time**, gồm cả chuyến tương lai, không phải 14 ngày gần nhất. Response from/to giữ null khi request không có giá trị.

Đề xuất cho summary/trips/top-drivers: mặc định 14 ngày gồm hôm nay và 13 ngày trước theo timezone hãng; trả from/to đã áp dụng. Top tuyến giữ ngoại lệ all-time; today-trips chỉ hôm nay; open-incidents theo trạng thái, không tự cắt còn 14 ngày vì có thể bỏ mất sự cố cũ chưa xử lý. Báo cáo revenue cần cùng khoảng ngày rõ ràng khi ghép chart. Nếu muốn đổi mặc định của API báo cáo đang phục vụ màn khác, phải kiểm tra tương thích trước.

Quy định thêm khi chỉ có from hoặc chỉ có to; không tự áp một cận tùy ý mà thiếu tài liệu. `from > to` và ngày sai định dạng đã được xử lý HTTP 400 trong hợp đồng hiện có. Dữ liệu hợp lệ rỗng phải tiếp tục trả success với số 0/mảng rỗng.

### Đếm trạng thái chuyến

Query summary `count(trip)` tính cả DRAFT/TERMINATED nhưng chỉ tách bốn trạng thái SCHEDULED/IN_PROGRESS/COMPLETED/CANCELLED. Top tài xế loại DRAFT, vẫn có thể tính TERMINATED trong assignedTrips mà không có cột terminatedTrips. Vì thế các nhóm không cộng đủ tổng và phạm vi giữa các khối khác nhau.

Test đã xác nhận khi thêm 1 DRAFT và 1 TERMINATED vào 2 chuyến gốc: summary totalTrips=4 nhưng tổng bốn nhóm=2; Top tài xế assignedTrips=3. Fixture tạo trực tiếp trạng thái để kiểm tra phép tổng hợp, không phải xác nhận API cho phép tạo vé cho chuyến nháp.

Cần chọn rõ: (a) thống kê vận hành loại DRAFT và có nhóm TERMINATED; hoặc (b) thống kê mọi chuyến và trả đủ cả hai bộ đếm. Áp dụng cùng chính sách cho summary, chart và các bảng xếp hạng có liên quan. Đây là vấn đề định nghĩa số liệu, không cần endpoint mới.

### Thời điểm và timezone

`departureAt`, `actualDepartureAt`, `openedAt` hiện dùng **LocalDateTime**, không chứa offset. ISO-8601 không tự đảm bảo có `+07:00`. Không thể mặc định rằng JSON hiện tại khớp ví dụ có offset trong tài liệu.

Hãng đã có `timezone`. Khi bổ sung today/late/age, dùng timezone đó cho khái niệm hôm nay và chuẩn hóa response bằng OffsetDateTime hoặc tài liệu hóa rõ LocalDateTime + timezone. Tránh đổi hàng loạt các DTO dùng chung mà chưa đánh giá tương thích. Không lấy timezone server làm quy tắc ngầm cho toàn bộ hãng.

### Envelope, phân quyền, giới hạn

Envelope `code/message/wsResponse` đã có; controller dashboard đã kiểm tra COMPANY_ADMIN theo companyId. Test xác nhận request không đăng nhập bị 401, token admin dùng companyId khác bị 403, khoảng ngày đảo bị 400 với code lỗi business. Các endpoint mới phải giữ cùng nguyên tắc lọc company tại truy vấn.

Top tài xế hiện lấy toàn bộ aggregate về rồi `.stream().limit(10)`, chưa limit tại DB. Với endpoint mới và khi tối ưu endpoint này, truyền Pageable/limit xuống repository; kiểm tra giới hạn và thứ tự ổn định. Đây là cải tiến hiệu năng, không phải lý do tự tạo API khác.

## 8. Phần tùy chọn và thứ tự triển khai

Chưa có previousRevenue/revenueChangePct hoặc xu hướng KPI. Chưa có revenue/rating trong DashboardDriverResponse. Không tìm thấy nguồn entity/API đánh giá sao trong backend đang kiểm tra; không trả rating giả hoặc mặc định 5 sao. Doanh thu theo tài xế có thể tổng hợp nhưng cần quy tắc quy thuộc khi thay tài xế: `trip_assignments` hiện chỉ có một bản ghi mỗi chuyến và lần gán mới cập nhật bản ghi đó, chưa phải lịch sử phân công đầy đủ.

Thứ tự công việc đề xuất:

1. **Chốt hợp đồng đang lệch:** phiên bản FE, khoảng ngày, nhóm trạng thái chuyến, enum sự cố, đơn vị giá trị vé và timezone.
2. **Nối lại nguồn có sẵn:** ghép `/reports/revenue` vào chart; không yêu cầu BE tạo lại báo cáo doanh thu ngày.
3. **Hoàn thiện bảng chuyến:** sửa bộ lọc phân công; thêm bulk assignment/status/plate, sort/limit/query ngày tại DB hoặc thêm today-trips; định nghĩa trễ.
4. **Thêm hai API thật sự thiếu nguồn đọc/tổng hợp:** top-routes và open-incidents, kèm test thống kê và cách ly hãng.
5. **Hoàn thiện luồng liên quan:** tiến trình PREPARING/ARRIVED cho tài xế; nhận/đóng sự cố cho admin ở màn Chuyến. Các mục này riêng với API GET Tổng quan nhưng cần thiết để dữ liệu trạng thái hoạt động đầy đủ.
6. **Tùy chọn sau:** xu hướng, doanh thu Top tài xế, rating khi có domain đánh giá, xuất file khi đã có yêu cầu định dạng/phạm vi rõ ràng.

## 9. Kiểm chứng đã thực hiện và giới hạn

Chạy Maven offline bằng JDK 21, profile `test`, H2 in-memory chế độ PostgreSQL, giao dịch test rollback. Không gửi request ghi vào database ứng dụng đang chạy.

| Nhóm | Kết quả |
| --- | --- |
| DashboardApiTests hiện có | 1 test PASS |
| Kiểm chứng thêm: periods doanh thu theo ngày và cận cuối | PASS |
| Không truyền khoảng ngày vẫn gồm chuyến cũ và tương lai | PASS |
| Chart thiếu revenue, thiếu ngày trống và key trạng thái bằng 0 | PASS |
| Summary/Top tài xế xử lý DRAFT/TERMINATED không cùng phạm vi | PASS |
| Lọc chuyến theo ngày hoạt động, DTO phân công thiếu status/plate | PASS |
| Assignment REJECTED vẫn qua bộ lọc ASSIGNED | PASS |
| Khoảng ngày đảo/không đăng nhập/company khác bị từ chối | PASS |

Tổng lượt chạy cuối: **8 tests, 0 failures, 0 errors, BUILD SUCCESS**. Các ca bổ sung là test xác nhận **hành vi hiện tại**, bao gồm hành vi sai cần sửa; PASS không có nghĩa đã đạt yêu cầu màn Tổng quan.

Log local: [dashboard-audit-contract.log](E:/NMCNPM/Vi-vu-Viet/Vi-vu-Viet/target/dashboard-audit-contract.log). Nguồn ca audit được lưu ở `target/DashboardContractAuditTests.java`; file test tạm trong `src/test` đã được dọn sau khi chạy. Script tạo lại từ fixture gốc nằm ở `target/generate-dashboard-audit.py`. Thư mục target là đầu ra build, có thể mất khi clean.

Chưa xác minh giao diện `/tong-quan` chạy thực tế, vì source FE tương ứng không có trong checkout này. Các endpoint mới chưa được triển khai. Test H2 xác nhận query và JSON trong môi trường test; chưa thay thế kiểm thử PostgreSQL thật và kiểm thử trình duyệt sau khi sửa/nối API.
