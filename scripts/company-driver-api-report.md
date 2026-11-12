# Kiểm thử API quản lý tài xế A3

Thời điểm: 2026-11-12T17:47:24.277431+07:00.

Server: `http://localhost:8080`; database PostgreSQL thật. Kết quả: **163/166 PASS**, **3 FAIL**.

Company ID: `a3000000-0000-4000-8000-000000000001`. Admin: `qa-admin@vivuviet.local` / `DriverQa123!`.

Seed giữ lại 2 hãng QA, 10 tài khoản, 10 membership, 9 hồ sơ tài xế, 2 tuyến, 2 xe, 11 chuyến, 16 ghế mẫu và 160 ghế chuyến. Hãng chính có 8 tài xế (5 ACTIVE, 3 LOCKED); hãng thứ hai dùng kiểm tra phạm vi quyền.

Script dọn tài khoản/chuyến tạm do chính lượt test tạo. Token và mật khẩu sinh từ API được che trong JSON kết quả.

| Nhóm kiểm thử | PASS | FAIL |
| --- | ---: | ---: |
| authentication | 8 | 0 |
| authorization | 40 | 0 |
| list | 14 | 0 |
| validation | 38 | 1 |
| detail | 4 | 0 |
| trips | 4 | 0 |
| create | 4 | 0 |
| conflicts | 6 | 0 |
| update | 4 | 0 |
| assignment | 5 | 0 |
| status | 7 | 0 |
| password | 3 | 0 |
| database | 2 | 1 |
| account-scope | 8 | 0 |
| legacy | 1 | 0 |
| regression | 14 | 1 |
| cleanup | 1 | 0 |

| Ca kiểm thử | Kết quả | HTTP thực tế / mong đợi |
| --- | --- | --- |
| QA admin login | PASS | 200 / 200 |
| QA driver login | PASS | 200 / 200 |
| QA customer login | PASS | 200 / 200 |
| Other company driver login | PASS | 200 / 200 |
| GET / rejects anonymous | PASS | 401 / 401 |
| GET / rejects customer | PASS | 403 / 403 |
| GET / rejects driver | PASS | 403 / 403 |
| GET / rejects other-company-driver | PASS | 403 / 403 |
| GET / rejects admin at another company | PASS | 403 / 403 |
| POST / rejects anonymous | PASS | 401 / 401 |
| POST / rejects customer | PASS | 403 / 403 |
| POST / rejects driver | PASS | 403 / 403 |
| POST / rejects other-company-driver | PASS | 403 / 403 |
| POST / rejects admin at another company | PASS | 403 / 403 |
| GET /91c65187-6ddb-1fe8-583c-9d94abbeb735 rejects anonymous | PASS | 401 / 401 |
| GET /91c65187-6ddb-1fe8-583c-9d94abbeb735 rejects customer | PASS | 403 / 403 |
| GET /91c65187-6ddb-1fe8-583c-9d94abbeb735 rejects driver | PASS | 403 / 403 |
| GET /91c65187-6ddb-1fe8-583c-9d94abbeb735 rejects other-company-driver | PASS | 403 / 403 |
| GET /91c65187-6ddb-1fe8-583c-9d94abbeb735 rejects admin at another company | PASS | 403 / 403 |
| PATCH /91c65187-6ddb-1fe8-583c-9d94abbeb735 rejects anonymous | PASS | 401 / 401 |
| PATCH /91c65187-6ddb-1fe8-583c-9d94abbeb735 rejects customer | PASS | 403 / 403 |
| PATCH /91c65187-6ddb-1fe8-583c-9d94abbeb735 rejects driver | PASS | 403 / 403 |
| PATCH /91c65187-6ddb-1fe8-583c-9d94abbeb735 rejects other-company-driver | PASS | 403 / 403 |
| PATCH /91c65187-6ddb-1fe8-583c-9d94abbeb735 rejects admin at another company | PASS | 403 / 403 |
| PATCH /91c65187-6ddb-1fe8-583c-9d94abbeb735/status rejects anonymous | PASS | 401 / 401 |
| PATCH /91c65187-6ddb-1fe8-583c-9d94abbeb735/status rejects customer | PASS | 403 / 403 |
| PATCH /91c65187-6ddb-1fe8-583c-9d94abbeb735/status rejects driver | PASS | 403 / 403 |
| PATCH /91c65187-6ddb-1fe8-583c-9d94abbeb735/status rejects other-company-driver | PASS | 403 / 403 |
| PATCH /91c65187-6ddb-1fe8-583c-9d94abbeb735/status rejects admin at another company | PASS | 403 / 403 |
| POST /91c65187-6ddb-1fe8-583c-9d94abbeb735/reset-password rejects anonymous | PASS | 401 / 401 |
| POST /91c65187-6ddb-1fe8-583c-9d94abbeb735/reset-password rejects customer | PASS | 403 / 403 |
| POST /91c65187-6ddb-1fe8-583c-9d94abbeb735/reset-password rejects driver | PASS | 403 / 403 |
| POST /91c65187-6ddb-1fe8-583c-9d94abbeb735/reset-password rejects other-company-driver | PASS | 403 / 403 |
| POST /91c65187-6ddb-1fe8-583c-9d94abbeb735/reset-password rejects admin at another company | PASS | 403 / 403 |
| GET /91c65187-6ddb-1fe8-583c-9d94abbeb735/trips rejects anonymous | PASS | 401 / 401 |
| GET /91c65187-6ddb-1fe8-583c-9d94abbeb735/trips rejects customer | PASS | 403 / 403 |
| GET /91c65187-6ddb-1fe8-583c-9d94abbeb735/trips rejects driver | PASS | 403 / 403 |
| GET /91c65187-6ddb-1fe8-583c-9d94abbeb735/trips rejects other-company-driver | PASS | 403 / 403 |
| GET /91c65187-6ddb-1fe8-583c-9d94abbeb735/trips rejects admin at another company | PASS | 403 / 403 |
| List includes all eight fixture drivers | PASS | 200 / 200 |
| ACTIVE filter includes legacy driver without a profile | PASS | 200 / 200 |
| LOCKED filter includes account and membership suspension | PASS | 200 / 200 |
| Search by Unicode name | PASS | 200 / 200 |
| Search by formatted phone | PASS | 200 / 200 |
| Search by case-insensitive email | PASS | 200 / 200 |
| Literal search '%' | PASS | 200 / 200 |
| Literal search '_' | PASS | 200 / 200 |
| Literal search '!' | PASS | 200 / 200 |
| Literal search 'no-such-driver' | PASS | 200 / 200 |
| Whitespace search behaves as no filter | PASS | 200 / 200 |
| First page metadata | PASS | 200 / 200 |
| Second page has different drivers | PASS | 200 / 200 |
| Out-of-range page keeps total count | PASS | 200 / 200 |
| Reject invalid list query page=-1 | PASS | 400 / 400 |
| Reject invalid list query size=0 | PASS | 400 / 400 |
| Reject invalid list query size=101 | PASS | 400 / 400 |
| Reject invalid list query page=abc | PASS | 400 / 400 |
| Reject invalid list query status=INVALID | PASS | 400 / 400 |
| Reject invalid list query search=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa | PASS | 400 / 400 |
| Reject malformed driver UUID | PASS | 400 / 400 |
| GET outsider is hidden with 404 | PASS | 404 / 404 |
| PATCH outsider is hidden with 404 | PASS | 404 / 404 |
| PATCH outsider/status is hidden with 404 | PASS | 404 / 404 |
| POST outsider/reset-password is hidden with 404 | PASS | 404 / 404 |
| GET outsider/trips is hidden with 404 | PASS | 404 / 404 |
| Driver details and 30-day count | PASS | 200 / 200 |
| Shared driver's count is scoped to current company | PASS | 200 / 200 |
| Legacy driver has null profile and license | PASS | 200 / 200 |
| Unknown driver returns 404 | PASS | 404 / 404 |
| Trip history excludes rejected and replaced assignments | PASS | 200 / 200 |
| Trip history is scoped to company | PASS | 200 / 200 |
| Driver with no assignments has empty history | PASS | 200 / 200 |
| Trip history pagination | PASS | 200 / 200 |
| Reject invalid trip history page=-1 | PASS | 400 / 400 |
| Reject invalid trip history size=0 | PASS | 400 / 400 |
| Reject invalid trip history size=101 | PASS | 400 / 400 |
| Create rejects invalid fullName ' ' | PASS | 400 / 400 |
| Create rejects invalid fullName 'Ab' | PASS | 400 / 400 |
| Create rejects invalid fullName 'aaaaaaaaaaaaaaaaaaaaaaaa | PASS | 400 / 400 |
| Create rejects invalid phone '1234567890' | PASS | 400 / 400 |
| Create rejects invalid phone '012345678' | PASS | 400 / 400 |
| Create rejects invalid email 'invalid-email' | PASS | 400 / 400 |
| Create rejects invalid licenseNumber '12345678901' | PASS | 400 / 400 |
| Create rejects invalid licenseClass 'D' | PASS | 400 / 400 |
| Create rejects invalid licenseExpiresOn '2026-11-12' | PASS | 400 / 400 |
| Create rejects invalid licenseExpiresOn '2020-01-01' | PASS | 400 / 400 |
| Create rejects missing wrapper | PASS | 400 / 400 |
| Create rejects null wrapper | PASS | 400 / 400 |
| Create rejects missing required fields | PASS | 400 / 400 |
| Create driver and return one-time credentials with no-store | PASS | 200 / 200 |
| New driver's generated password works | PASS | 200 / 200 |
| Created driver has normalized email and complete profile | PASS | 200 / 200 |
| Create rejects duplicate phone | PASS | 409 / 409 |
| Create rejects duplicate email | PASS | 409 / 409 |
| Create rejects duplicate license | PASS | 409 / 409 |
| Create driver without optional email | PASS | 200 / 200 |
| Optional email stays null | PASS | 200 / 200 |
| Update rejects invalid fullName ' ' | PASS | 400 / 400 |
| Update rejects invalid fullName 'Ab' | PASS | 400 / 400 |
| Update rejects invalid fullName 'aaaaaaaaaaaaaaaaaaaaaaaa | PASS | 400 / 400 |
| Update rejects invalid phone '1234567890' | PASS | 400 / 400 |
| Update rejects invalid phone '012345678' | PASS | 400 / 400 |
| Update rejects invalid email 'invalid-email' | PASS | 400 / 400 |
| Update rejects invalid licenseNumber '12345678901' | PASS | 400 / 400 |
| Update rejects invalid licenseClass 'D' | PASS | 400 / 400 |
| Update rejects invalid licenseExpiresOn '2026-11-12' | PASS | 400 / 400 |
| Update rejects invalid licenseExpiresOn '2020-01-01' | PASS | 400 / 400 |
| Update rejects empty body | PASS | 400 / 400 |
| Update rejects all null fields | PASS | 400 / 400 |
| Update rejects duplicate phone | PASS | 409 / 409 |
| Update rejects duplicate email | PASS | 409 / 409 |
| Update rejects duplicate licenseNumber | PASS | 409 / 409 |
| Update identity and license with normalization | PASS | 200 / 200 |
| Clear optional email using empty string | PASS | 200 / 200 |
| Unchanged phone and license do not conflict | PASS | 200 / 200 |
| Explicit null leaves other fields unchanged | PASS | 200 / 200 |
| Create disposable draft for driver assignment | PASS | 200 / 200 |
| driverId from create response works for assignment | PASS | 200 / 200 |
| Lock driver and trim status note | PASS | 200 / 200 |
| Lock preserves existing assignment | PASS | 200 / 200 |
| Lock blocks new assignment | PASS | 409 / 409 |
| Locked driver can still read already assigned trip | PASS | 200 / 200 |
| Self-service profile update preserves admin lock | PASS | 200 / 200 |
| Admin sees lock after self-service update | PASS | 200 / 200 |
| Unlock driver | PASS | 200 / 200 |
| Assignment succeeds again after unlock | PASS | 200 / 200 |
| Status rejects unknown status | FAIL | 500 / 400 |
| Status rejects missing status | PASS | 400 / 400 |
| Status rejects oversized note | PASS | 400 / 400 |
| Cannot unlock account-suspended | PASS | 409 / 409 |
| Cannot unlock member-suspended | PASS | 409 / 409 |
| Reset rejects oversized note | PASS | 400 / 400 |
| Reset returns a fresh password with no-store | PASS | 200 / 200 |
| Reset revokes previous access token | PASS | 401 / 401 |
| Reset revokes previous refresh token | PASS | 401 / 401 |
| Old password no longer works | PASS | 401 / 401 |
| New password works with phone | PASS | 200 / 200 |
| New password works with normalized email | PASS | 200 / 200 |
| Password is BCrypt; reset note and timestamp are persisted | FAIL | — / — |
| All pre-reset sessions were revoked | PASS | — / — |
| Prevent identity change for shared account | PASS | 403 / 403 |
| Prevent password reset for shared account | PASS | 403 / 403 |
| Allow company-specific license update for shared | PASS | 200 / 200 |
| Prevent identity change for admin account | PASS | 403 / 403 |
| Prevent password reset for admin account | PASS | 403 / 403 |
| Allow company-specific license update for admin | PASS | 200 / 200 |
| Shared account can be locked within current company | PASS | 200 / 200 |
| Company-specific lock does not change other company's profile | PASS | — / — |
| Restore shared account's profile to ACTIVE | PASS | 200 / 200 |
| Update creates missing legacy profile | PASS | 200 / 200 |
| Driver profile | PASS | 200 / 200 |
| Driver trip list | FAIL | 500 / 200 |
| Driver assigned vehicle | PASS | 200 / 200 |
| Driver trip detail | PASS | 200 / 200 |
| Company memberships | PASS | 200 / 200 |
| Dashboard summary | PASS | 200 / 200 |
| Dashboard trip chart | PASS | 200 / 200 |
| Dashboard top drivers | PASS | 200 / 200 |
| Revenue report | PASS | 200 / 200 |
| Company customer list | PASS | 200 / 200 |
| Company trip bookings | PASS | 200 / 200 |
| Customer profile | PASS | 200 / 200 |
| Customer booking history | PASS | 200 / 200 |
| Public trip detail | PASS | 200 / 200 |
| Public trip seats | PASS | 200 / 200 |
| Remove only users, trips and legacy profile created by this run | PASS | — / — |

Chi tiết lỗi ở `target/company-driver-api-results.json`.

Chạy lại: `python scripts/test-company-driver-api.py --seed`.
