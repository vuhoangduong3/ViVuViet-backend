# Thiết kế database — nền tảng đặt xe liên tỉnh

**Phiên bản:** 1.0  
**Ngày:** 06/11/2026  
**Trạng thái:** Thiết kế đề xuất, chưa phải migration chạy trực tiếp.

## 1. Mục tiêu và phạm vi

Thiết kế này phục vụ nền tảng đặt xe liên tỉnh có ba vai trò chính: khách hàng, admin của hãng xe và tài xế. Một khách hàng có thể đặt vé ở nhiều hãng; dữ liệu vận hành của mỗi hãng được cô lập theo tenant. Phần hiện tại bao gồm quản lý tuyến, điểm dừng, xe, tài xế, chuyến, sơ đồ ghế, đặt vé, điều phối, sự cố và báo cáo vận hành. Thanh toán trực tuyến, thu tiền mặt và GPS được để dành cho giai đoạn mở rộng.

**Giả định nghiệp vụ:** mỗi đơn đặt thuộc một chuyến và có thể chứa nhiều vé; mỗi vé ứng với một hành khách, một ghế và một chặng. Vé khứ hồi được tạo thành hai đơn riêng trong giai đoạn đầu. Tài khoản đăng nhập dùng chung trên toàn nền tảng; quyền admin và tài xế được gán theo từng hãng.

## 2. Lựa chọn lưu trữ và quy ước dữ liệu

### Database chính

Dùng một cụm PostgreSQL làm nguồn dữ liệu chính. Các hãng dùng chung database và schema; mỗi bản ghi nghiệp vụ thuộc hãng có cột company_id. Chưa cần database riêng cho mỗi hãng hoặc microservice riêng cho từng vai trò. Transaction của PostgreSQL sẽ bảo vệ thao tác nhiều bảng như giữ ghế, xác nhận đơn, hủy vé và đổi phân công.

Redis có thể được bổ sung để cache kết quả tìm kiếm, giới hạn tốc độ request hoặc lưu trạng thái online có thời hạn. Redis không phải nguồn quyết định cuối cùng về ghế còn trống. Ảnh và tài liệu có thể lưu trong object storage tương thích S3; PostgreSQL chỉ lưu đường dẫn, loại tài liệu và metadata.

### Quy ước chung

Tên bảng và cột dùng tiếng Anh theo snake_case; tên bảng dùng số nhiều khi phù hợp để thống nhất với cách đặt tên phổ biến trong code.

- ID thực thể dùng UUID; bảng liên kết có thể dùng khóa chính ghép.
- Bảng hãng sở hữu có company_id. Ngoại lệ chính là users và areas, là dữ liệu toàn nền tảng.
- Quan hệ giữa các bảng thuộc hãng dùng khóa ngoại ghép bao gồm company_id, để ngăn bản ghi của hãng A tham chiếu dữ liệu của hãng B.
- Thời điểm dùng TIMESTAMPTZ, ngày hết hạn giấy tờ dùng DATE. Múi giờ hiển thị lấy từ cấu hình hãng; không lưu giờ địa phương thiếu múi giờ cho thời điểm đã xảy ra.
- Tiền dùng BIGINT theo đơn vị đồng VND, không dùng FLOAT. Lưu currency để có thể hỗ trợ tiền tệ khác về sau.
- Bảng nghiệp vụ thường có created_at và updated_at. Bảng sự kiện, audit và outbox là append-only, không sửa lịch sử cũ.
- Trạng thái là các giá trị có giới hạn ở tầng database hoặc tầng ứng dụng; không nhận tùy ý từ client.
- Không xóa vật lý tuyến, xe, người dùng hoặc vé đã có giao dịch. Chuyển trạng thái sang inactive, suspended, retired hoặc cancelled để bảo toàn lịch sử.

## 3. Danh sách bảng cốt lõi

Thiết kế có 29 bảng nghiệp vụ cốt lõi. Cột company_id được ghi rõ ở các bảng thuộc hãng; bảng liên kết có thể thể hiện company_id trong khóa chính hoặc khóa ngoại ghép.

### 3.1. Tài khoản, vai trò và hãng xe — 7 bảng

| Bảng | Phạm vi | Cột quan trọng | Ghi chú |
|---|---|---|---|
| bus_companies | Toàn nền tảng | id, name, slug, hotline, timezone, status, created_at | Mỗi hãng là một tenant; slug duy nhất. |
| users | Toàn nền tảng | id, full_name, phone_normalized, email_normalized, password_hash, account_status, phone_verified_at, created_at | Không lưu mật khẩu dạng rõ; phone và email chuẩn hóa trước khi kiểm tra duy nhất. |
| auth_sessions | Toàn nền tảng | id, user_id, refresh_token_hash, expires_at, revoked_at, device_info, created_at | Chỉ cần nếu hệ thống tự quản lý phiên đăng nhập; chỉ lưu token đã băm. |
| company_members | Theo hãng | id, company_id, user_id, status, joined_at, created_at | Quan hệ một người với một hãng; UNIQUE(company_id, user_id). |
| company_member_roles | Theo thành viên | company_id, membership_id, role, created_at | role gồm company_admin hoặc driver; một membership có thể có nhiều role. |
| driver_profiles | Theo hãng | id, company_id, membership_id, employee_code, license_number, license_class, license_expires_on, status | Hồ sơ nghề nghiệp tách khỏi danh tính đăng nhập; hạn GPLX phục vụ cảnh báo. |
| company_customers | Theo hãng | id, company_id, user_id, booking_status, blocked_reason, blocked_at, blocked_by, internal_note | Chặn khách tại một hãng không khóa tài khoản toàn nền tảng; tạo khi cần gắn khách với hãng. |

Số điện thoại của tài xế có thể là thông tin tài khoản trong users; hồ sơ GPLX thuộc driver_profiles. Đặt lại mật khẩu là quy trình xác minh danh tính, không lưu mật khẩu tạm để admin xem lại.

Admin hãng chỉ xem thông tin khách cần thiết cho hoạt động đặt vé tại hãng mình. Trạng thái khóa toàn cục của users và trạng thái chặn đặt vé tại company_customers phải là hai việc riêng.

### 3.2. Địa điểm, tuyến và đội xe — 7 bảng

| Bảng | Phạm vi | Cột quan trọng | Ghi chú |
|---|---|---|---|
| areas | Toàn nền tảng | id, parent_id, code, name, type, status | Danh mục tỉnh/thành, quận/huyện hoặc khu vực; hỗ trợ cây phân cấp. |
| stops | Theo hãng | id, company_id, area_id, name, address, pickup_instructions, status | Bến, văn phòng hoặc điểm đón/trả. |
| routes | Theo hãng | id, company_id, code, name, origin_area_id, destination_area_id, distance_km, duration_minutes, base_fare_amount, status | Chiều đi là một tuyến riêng; chiều về là tuyến riêng. |
| route_stops | Theo tuyến | id, company_id, route_id, stop_id, sequence_no, arrival_offset_minutes, departure_offset_minutes, pickup_allowed, dropoff_allowed | sequence_no quyết định thứ tự; một stop có thể xuất hiện ở nhiều tuyến. |
| seat_layouts | Theo hãng | id, company_id, name, version, vehicle_type, status | Phiên bản sơ đồ ghế; phiên bản đã dùng không chỉnh sửa tại chỗ. |
| seat_layout_seats | Theo sơ đồ | id, company_id, seat_layout_id, seat_code, deck, row_no, column_no, seat_class | Mỗi sơ đồ không trùng seat_code. Ghế tài xế không nằm trong inventory bán vé. |
| vehicles | Theo hãng | id, company_id, plate_normalized, vehicle_model, passenger_capacity, seat_layout_id, default_driver_id, inspection_expires_on, insurance_expires_on, status | Biển số chuẩn hóa; status gồm active, maintenance hoặc retired. |

routes là tuyến mẫu; route_stops là thứ tự các điểm dừng mẫu. Khi tạo chuyến, hệ thống sao chép điểm dừng sang trip_stops. Chỉnh tuyến về sau không thay đổi lịch trình chuyến đã bán vé.

Giá trên routes chỉ là giá mặc định khi tạo chuyến. Giá của chuyến đang bán vé được lưu tại trip_fares và giá cuối cùng của giao dịch được chụp tại từng vé.

### 3.3. Chuyến, điểm dừng, ghế, giá và phân công — 6 bảng

| Bảng | Phạm vi | Cột quan trọng | Ghi chú |
|---|---|---|---|
| trips | Theo hãng | id, company_id, route_id, trip_code, departure_at, expected_arrival_at, actual_departure_at, actual_arrival_at, status, sales_status, booking_cutoff_at, created_by | Chuyến cụ thể vào ngày/giờ cụ thể; xe và tài xế lưu trong bảng phân công. |
| trip_stops | Theo chuyến | id, company_id, trip_id, stop_id, area_id, sequence_no, name_snapshot, address_snapshot, planned_arrival_at, planned_departure_at, pickup_allowed, dropoff_allowed | Bản chụp điểm dừng và giờ dự kiến của chuyến. UNIQUE(company_id, trip_id, sequence_no). |
| trip_seats | Theo chuyến | id, company_id, trip_id, seat_code, deck, row_no, column_no, seat_class | Inventory ghế của chuyến, được tạo từ layout lúc cấu hình xe. UNIQUE(company_id, trip_id, seat_code). |
| trip_fares | Theo chuyến | id, company_id, trip_id, from_seq, to_seq, seat_class, amount, currency | Giá theo cặp điểm lên/xuống và hạng ghế; chỉ bán khi có giá hợp lệ. |
| trip_vehicle_assignments | Theo chuyến | id, company_id, trip_id, vehicle_id, occupied_from, occupied_to, status, assigned_by, replaced_by, reason | Lịch sử phân xe; khoảng chiếm dụng tính cả thời gian chuẩn bị/quay đầu cần thiết. |
| trip_driver_assignments | Theo chuyến | id, company_id, trip_id, driver_profile_id, driver_user_id, duty_role, occupied_from, occupied_to, assignment_status, accepted_at, agreed_compensation_amount | Hỗ trợ tài xế chính/phụ; lưu mức công đã thỏa thuận của chuyến. |

status của trips và trạng thái mở bán phải tách riêng. Gợi ý trạng thái chuyến: draft, scheduled, in_progress, completed, cancelled, terminated. Trạng thái bán: closed, open, paused. Gợi ý trạng thái phân công tài xế: assigned, accepted, preparing, arrived, completed, rejected, replaced.

Một chuyến chỉ được mở bán khi có các điểm dừng hợp lệ, inventory ghế, giá cho chặng được phép bán và thông tin khởi hành đầy đủ. Chuyến có thể tồn tại ở trạng thái scheduled trong khi đang chờ phân công tài xế hoặc xe, tùy chính sách vận hành.

### 3.4. Đơn đặt, vé và phần giữ ghế — 3 bảng

| Bảng | Phạm vi | Cột quan trọng | Ghi chú |
|---|---|---|---|
| bookings | Theo hãng và chuyến | id, company_id, trip_id, customer_user_id, booking_code, contact_name_snapshot, contact_phone_snapshot, contact_email_snapshot, status, hold_expires_at, quoted_total_amount, currency, channel, created_by_user_id, confirmed_at, cancelled_at, cancellation_reason | Một đơn có thể gồm nhiều vé; snapshot liên hệ giữ thông tin tại lúc đặt. |
| tickets | Theo hãng, chuyến và đơn | id, company_id, trip_id, booking_id, ticket_code, passenger_name, passenger_phone, boarding_seq, alighting_seq, seat_label_snapshot, fare_amount, status, boarded_at, completed_at, cancellation_reason | Một hàng là một hành khách/ghế/chặng; người đặt có thể không phải hành khách. |
| seat_allocations | Theo hãng, chuyến và ghế | id, company_id, trip_id, trip_seat_id, ticket_id, from_seq, to_seq, state, reason, created_at, released_at | Là nguồn xác định ghế đang giữ, đã bán hoặc bị chặn. |

Trạng thái gợi ý của bookings: holding, confirmed, partially_cancelled, cancelled, expired. Trạng thái vé: holding, confirmed, boarded, completed, cancelled, no_show, expired. Trạng thái seat_allocations: held, reserved, blocked, released.

Không lưu các ghế đã đặt trong một chuỗi kiểu A1,A2,A3. Mỗi vé và mỗi allocation phải có bản ghi riêng để hủy/đổi từng vé và kiểm tra xung đột chính xác.

Giá của vé phải được tính phía server và lưu tại thời điểm tạo đơn. Thay đổi giá tuyến/chuyến về sau không được thay đổi giá trên vé đã xác nhận. Nếu có phí hoặc ưu đãi sau này, mở rộng các cột chi tiết giá trên tickets hoặc thêm bảng dòng giá; luôn giữ tổng tiền cuối đã bán làm snapshot.

## 4. Chống bán trùng ghế theo chặng

Một ghế có thể bán cho hai hành khách trên các đoạn không giao nhau của cùng chuyến. Ví dụ A01 đi từ A đến B và một khách khác đi từ B đến C thì đều hợp lệ. A đến C cùng với B đến C bị xung đột.

Biểu diễn chặng theo thứ tự điểm dừng bằng khoảng nửa mở [from_seq, to_seq). Hai khoảng giao nhau khi khách phải dùng cùng ghế trong ít nhất một đoạn hành trình chung.

Các điều kiện dữ liệu:

- from_seq và to_seq bắt buộc, from_seq < to_seq.
- Hai đầu mút phải tham chiếu trip_stops của đúng trip_id và cùng company_id.
- trip_seat_id phải thuộc đúng chuyến.
- state held, reserved và blocked đều chiếm ghế; released không chiếm ghế.
- Ghế bị chặn bán cũng nằm trong seat_allocations, tránh có bảng chặn ghế riêng không được so với vé.

Ràng buộc PostgreSQL quan trọng:

~~~sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE seat_allocations
ADD CONSTRAINT seat_allocations_no_overlap
EXCLUDE USING gist (
    company_id WITH =,
    trip_id WITH =,
    trip_seat_id WITH =,
    int4range(from_seq, to_seq, '[)') WITH &&
)
WHERE (state IN ('held', 'reserved', 'blocked'));
~~~

Cần có thêm CHECK(from_seq < to_seq), NOT NULL trên các cột tham gia và khóa ngoại ghép để inventory, chuyến và hãng khớp nhau. Khi PostgreSQL từ chối allocation do xung đột, API trả lỗi nghiệp vụ SEAT_UNAVAILABLE; không trả SQL hoặc tên constraint cho client. PostgreSQL hỗ trợ exclusion constraint với range và GiST để ngăn khoảng bị chồng lấn.

## 5. Transaction giữ chỗ và xác nhận

1. Khách chọn chuyến, điểm lên/xuống và ghế. API đọc trạng thái ghế để hiển thị, nhưng kết quả đọc này chỉ là tham khảo; nó chưa đảm bảo ghế được giữ.
2. Client gọi tạo đơn kèm Idempotency-Key. Server xác thực khách, xác nhận chuyến đang mở bán, kiểm tra chặng và tính giá từ database.
3. Trong một transaction ngắn, server tạo booking trạng thái holding, tickets trạng thái holding và seat_allocations trạng thái held cho toàn bộ ghế trong đơn.
4. Nếu bất kỳ ghế nào xung đột, transaction rollback toàn bộ; không tạo đơn chỉ giữ được một phần ghế.
5. API trả hạn giữ, thời gian server và tổng tiền đã tính. Client có thể hoàn tất thông tin và gọi confirm trong thời hạn.
6. Confirm khóa booking, kiểm tra lại status và hold_expires_at, rồi chuyển booking, tickets và allocations sang trạng thái xác nhận trong cùng transaction.
7. Worker tìm booking holding đã hết hạn, khóa và cập nhật chúng sang expired, đồng thời chuyển allocation sang released. Confirm và worker phải dùng cùng quy tắc khóa và kiểm tra lại trạng thái sau khi lấy khóa.

Không giữ transaction/database row lock trong toàn bộ thời gian khách suy nghĩ. Hạn giữ có thể cấu hình, ví dụ 5 phút. Nếu API confirm gửi lại do timeout sau khi xác nhận thành công, trả kết quả đã xác nhận trước đó thay vì tạo trạng thái mới.

Idempotency-Key cần được lưu bền trong idempotency_requests cùng actor, operation, scope, request hash và kết quả. Gửi lại cùng key/cùng payload trả lại kết quả cũ. Cùng key nhưng payload khác phải bị từ chối.

## 6. Phân vùng dữ liệu và phân quyền

Mỗi API đọc hoặc sửa đối tượng đều phải kiểm tra quyền trên chính đối tượng đó:

- Khách chỉ xem booking/ticket của mình; chuyến công khai chỉ trả dữ liệu cần cho bán vé.
- Admin chỉ xem và sửa các hàng thuộc operator đã xác minh trong membership và role.
- Tài xế chỉ xem các chuyến đang được phân công cho chính họ và thông tin khách cần cho đón/trả.
- Khóa khách tại company_customers chỉ chặn đặt mới tại hãng đó; các vé cũ vẫn giữ lịch sử.
- Không tin company_id, customer_user_id, amount, status, seat state hoặc fare do client gửi. Server xác định hoặc xác thực các giá trị này.

Mọi bảng nghiệp vụ thuộc hãng nên dùng khóa ngoại ghép. Ví dụ:

~~~text
trips: UNIQUE(company_id, id)

bookings: FOREIGN KEY (company_id, trip_id)
          REFERENCES trips(company_id, id)

tickets: FOREIGN KEY (company_id, trip_id, booking_id)
         REFERENCES bookings(company_id, trip_id, id)
~~~

Áp dụng nguyên tắc này cho tuyến, xe, điểm dừng, sơ đồ ghế, chuyến, tài xế, phân công, vé và allocation. Chỉ có cột company_id nhưng vẫn FK bằng id đơn lẻ thì chưa đủ để ngăn liên kết chéo hãng.

Có thể bật Row-Level Security trên bảng tenant làm lớp bảo vệ bổ sung. Backend phải đặt context operator đã xác thực trong từng transaction, đặc biệt khi dùng connection pool. Tài khoản database của ứng dụng không được có quyền BYPASSRLS; chủ sở hữu bảng cũng cần được xử lý theo cấu hình RLS của PostgreSQL.

Nếu tài xế được phép làm cho nhiều hãng, chống trùng lịch phải áp dụng theo driver_user_id toàn nền tảng, không chỉ theo company_id và driver_profile_id. Phân công xe được chống trùng theo vehicle_id và khoảng occupied_from/occupied_to. Nếu chuyển xe giữa các hãng, cần quy trình chuyển quyền sở hữu và giữ lịch sử.

## 7. Index khởi điểm và báo cáo

| Nhu cầu truy vấn | Index gợi ý |
|---|---|
| Chuyến theo hãng và thời gian | trips(company_id, departure_at) |
| Chuyến theo trạng thái mở bán và thời gian | trips(company_id, sales_status, departure_at) |
| Điểm dừng để tìm chuyến theo điểm lên | trip_stops(area_id, planned_departure_at, trip_id) |
| Đơn của một khách | bookings(customer_user_id, created_at DESC) |
| Đơn của hãng | bookings(company_id, created_at DESC) |
| Vé trong một chuyến | tickets(company_id, trip_id, status) |
| Dọn hold hết hạn | Partial index theo hold_expires_at cho status=holding |
| Mã chuyến/đơn/vé | UNIQUE(company_id, trip_code/booking_code/ticket_code) |
| Chống allocation chồng lấn | GiST exclusion constraint trên hãng, chuyến, ghế và khoảng chặng |

Tìm kiếm phải ghép điểm lên và điểm xuống trên cùng chuyến và bảo đảm boarding sequence nhỏ hơn alighting sequence. Ngày khách đi được xét tại điểm lên đã chọn; khách lên ở điểm giữa có thể đi ngày hôm sau so với giờ xe rời điểm đầu.

Dashboard nên tổng hợp từ dữ liệu chuẩn bằng query/view trước. Khi tải tăng, có thể thêm materialized view, read replica hoặc bảng tổng hợp cập nhật bất đồng bộ qua outbox. Kết quả cache/report không được dùng làm căn cứ cuối cùng để giữ ghế.

Không partition seat_allocations một cách máy móc theo thời gian tạo: mọi allocation có thể xung đột trên cùng ghế/chặng phải được bảo vệ bởi cùng cơ chế ràng buộc. Chỉ partition khi đã đo tải và xác minh ràng buộc chống trùng vẫn bao phủ đúng dữ liệu.

## 8. Sự cố, lịch sử, audit và xử lý nền

| Bảng | Cột quan trọng | Mục đích |
|---|---|---|
| trip_incidents | id, company_id, trip_id, reported_by, incident_type, description, status, assigned_to, action_plan, resolution, opened_at, resolved_at | Báo, giao xử lý và đóng sự cố xe/chuyến. |
| trip_events | id, company_id, trip_id, event_type, actor_user_id, occurred_at, details JSONB | Timeline tạo chuyến, đổi lịch, phân công, mở bán và trạng thái chạy. |
| booking_events | id, company_id, booking_id, ticket_id, event_type, actor_user_id, occurred_at, reason, details JSONB | Timeline giữ, xác nhận, hủy đơn/vé và thao tác vận hành. |
| audit_logs | id, company_id, actor_user_id, action, entity_type, entity_id, request_id, before_data, after_data, created_at | Nhật ký quản trị và hỗ trợ tra soát. |
| outbox_events | id, event_type, aggregate_type, aggregate_id, payload JSONB, created_at, published_at, attempt_count, next_attempt_at | Gửi thông báo và chạy tác vụ sau khi transaction đã commit; hỗ trợ retry. |
| idempotency_requests | id, scope_key, actor_user_id, operation, idempotency_key, request_hash, response_status, resource_id, expires_at, created_at | Chống tạo đơn hoặc áp dụng thao tác trùng khi client gửi lại request. |

Worker hết hạn giữ chỗ, gửi thông báo và tổng hợp báo cáo là tiến trình nền, không phải API công khai. Consumer của outbox phải chịu được việc nhận cùng một event nhiều lần. Không đưa dữ liệu cá nhân không cần thiết vào payload event.

## 9. Doanh thu trong giai đoạn chưa có thanh toán

Ở giai đoạn này có thể báo cáo số vé xác nhận, số vé hủy, giá trị vé dự kiến và tiền công tài xế đã thỏa thuận. Chưa thể báo cáo chính xác thực thu, vé đã thu hoặc chênh lệch tiền mặt nếu chưa có bản ghi nghiệp vụ thu tiền.

Không dùng trạng thái booking=paid khi chưa có thanh toán. Giá vé xác nhận và mức công của từng chuyến vẫn lưu snapshot để thay đổi cấu hình về sau không làm sai số liệu lịch sử.

Khi bắt đầu thu tiền, bổ sung tối thiểu payments, payment_transactions và refunds. Nếu cần thu/bàn giao tiền mặt, bổ sung cash_collections và cash_handover_batches. Giao dịch thanh toán phải có mã tham chiếu nhà cung cấp, trạng thái, số tiền, currency, thời điểm và cơ chế chống webhook trùng.

## 10. Mở rộng sau này

| Nhu cầu | Thiết kế mở rộng |
|---|---|
| Thanh toán trực tuyến | payments, payment_transactions, refunds và webhook idempotency. |
| GPS trực tiếp | Lưu vehicle_id, trip_id, recorded_at, latitude, longitude và accuracy; dữ liệu vị trí tăng nhanh có thể đặt trong kho time-series riêng. |
| Chuyến lặp theo lịch | trip_schedule_templates, quy tắc ngày chạy và ngày ngoại lệ; sinh trips cụ thể để bán. |
| Hồ sơ giấy tờ nhiều loại/phiên bản | vehicle_documents, driver_documents, object storage cho file. |
| Khuyến mãi | promotions, promotion_redemptions; lưu số tiền giảm cuối cùng trên ticket. |
| Khiếu nại và đánh giá | support_tickets, trip_reviews. |
| Thông báo và hiện diện | notifications; Redis TTL cho trạng thái online ngắn hạn. |
| Tìm kiếm nâng cao | Search index đồng bộ bất đồng bộ từ PostgreSQL khi đo tải cho thấy cần thiết. |
| Nhiều đơn trong một hành trình khứ hồi | Thêm orders làm đơn tổng liên kết hai hoặc nhiều bookings. |

## 11. Ánh xạ các màn hình tham khảo

| Màn hình | Bảng chính |
|---|---|
| [Tổng quan](https://helloworld-cmyk.github.io/design/admin/tong-quan.html) | trips, tickets, trip_driver_assignments, trip_incidents; view/bảng tổng hợp báo cáo. |
| [Quản lý chuyến](https://helloworld-cmyk.github.io/design/admin/admin-chuyen.html) | trips, trip_stops, trip_seats, trip_fares, phân công, seat_allocations, trip_events. |
| [Tài xế](https://helloworld-cmyk.github.io/design/admin/tai-xe.html) | users, company_members, company_member_roles, driver_profiles. |
| [Xe](https://helloworld-cmyk.github.io/design/admin/xe.html) | xe, seat_layouts, seat_layout_seats. |
| [Tuyến đường](https://helloworld-cmyk.github.io/design/admin/tuyen-duong.html) | areas, stops, routes, route_stops. |
| [Đơn đặt](https://helloworld-cmyk.github.io/design/admin/don-dat.html) | bookings, tickets, seat_allocations, booking_events. |
| [Doanh thu](https://helloworld-cmyk.github.io/design/admin/admin-doanh-thu.html) | Fare snapshot trên tickets, agreed_compensation_amount trên trip_driver_assignments; chưa có dữ liệu thực thu. |
| [Khách hàng](https://helloworld-cmyk.github.io/design/admin/khach-hang.html) | users, company_customers, bookings và tickets thuộc hãng. |

Luồng tìm kiếm nơi đi/nơi đến/ngày đi tham khảo giao diện [Vexere](https://vexere.com/). Ràng buộc khoảng và exclusion constraint tham khảo [PostgreSQL Range Types](https://www.postgresql.org/docs/current/rangetypes.html). Cách giới hạn hàng theo hãng có thể bổ sung [PostgreSQL Row Security](https://www.postgresql.org/docs/current/ddl-rowsecurity.html); bảo vệ quyền trên từng ID API cũng cần theo hướng dẫn [OWASP API1: Broken Object Level Authorization](https://api-security.owasp.org/editions/2023/en/0xa1-broken-object-level-authorization/).






