# Đối chiếu `chuc_nang.md` với hệ thống hiện tại

Tài liệu này ghi nhận chênh lệch giữa đặc tả [chuc_nang.md](chuc_nang.md) và mã nguồn hiện tại. Nó là tài liệu khảo sát, không thay thế đặc tả chốt.

## 1. Tổng quan

Hệ thống hiện tại đã có nền tảng cho quản lý tài khoản, thông báo, đợt đăng ký, đề xuất/duyệt đề tài, nhóm sinh viên, đăng ký đề tài và hội đồng. Tuy nhiên, route vẫn dùng tiếng Anh, chưa tách theo vai trò, và nhiều nghiệp vụ mới trong `chuc_nang.md` chưa được triển khai.

## 2. Hạng mục đã có nền tảng

| Hạng mục đặc tả | Hiện trạng | Chênh lệch cần xử lý |
| --- | --- | --- |
| Quản trị tài khoản | Có CRUD tại `/users`, chỉ Admin | Đổi sang `/quan-tri/tai-khoan`; áp dụng một tài khoản một vai trò |
| Hồ sơ và đổi mật khẩu | Có tại `/account` và `/account/password` | Đổi sang `/ho-so`, `/doi-mat-khau` |
| Thông báo nội bộ | Có CRUD tại `/announcements`; Admin và Trưởng khoa quản lý | Đổi route sang `/thong-bao` và kiểm tra lại hiển thị theo đối tượng role |
| Đợt đăng ký | Có CRUD tại `/registration-periods` | Đổi sang nhóm route `/truong-khoa/dot-dang-ky`; cập nhật field và validation mới |
| Giảng viên đề xuất đề tài | Có tại `/topic-proposals` | Đổi sang `/giang-vien/de-tai` |
| Trưởng khoa duyệt đề tài | Có approve/reject đề tài trong route của đợt | Đổi tổ chức route/màn hình theo đặc tả |
| Nhóm sinh viên | Có tạo nhóm, thêm/xóa thành viên, chuyển trưởng nhóm | Đổi sang `/sinh-vien/nhom`; cho Admin thao tác toàn quyền |
| Chọn và yêu cầu duyệt đề tài | Có chọn đề tài và gửi yêu cầu duyệt | Đổi route và điều chỉnh điều kiện UI theo đặc tả |
| Hội đồng | Có tạo hội đồng 3–5 người, Chủ tịch/Thư ký và giao đề tài | Chỉ áp dụng KLTN, thêm entry UI từ trang đợt, đổi route |
| Công bố kết quả | Có publish/revoke bằng endpoint | Cần có màn hình quản lý kết quả và màn hình Sinh viên xem kết quả |

## 3. Hạng mục chưa có

| Hạng mục | Nội dung cần có theo đặc tả |
| --- | --- |
| Trang chủ công khai | `/` là trang giới thiệu, cho khách truy cập không cần đăng nhập |
| Dashboard theo vai trò | Dashboard riêng cho Admin, Trưởng khoa, Giảng viên và Sinh viên |
| Nộp bài/báo cáo | Nhóm trưởng nộp/thay thế bài trước `reportDeadline`; thành viên xem lịch sử/tệp nộp |
| Phân công GVPB TLCN | Trưởng khoa chọn một GVPB sau khi kết thúc đăng ký sinh viên |
| Chấm Môn học/NCKH | Tất cả GVHD/GV phụ trách chấm; hệ thống lấy trung bình cộng |
| Chấm TLCN | Chỉ GVPB được phân công chấm trước deadline |
| Chấm KLTN hoàn chỉnh | Thành viên hội đồng chấm, Chủ tịch chốt; không có GVPB riêng và GVHD không được chấm |
| Kết quả Sinh viên | `/sinh-vien/ket-qua`, chỉ hiện sau khi Trưởng khoa công bố |

## 4. Chênh lệch route và điều hướng

| Đặc tả | Hiện tại | Việc cần làm |
| --- | --- | --- |
| `/` công khai | `/` trả view `index` nhưng Security yêu cầu đăng nhập | Cho phép công khai `/` và xây trang giới thiệu |
| `/dang-nhap`, `/dang-xuat` | `/login`, `/logout` | Đổi route xác thực và URL chuyển hướng |
| `/quan-tri/...` | `/users` | Di chuyển route quản trị tài khoản |
| `/truong-khoa/...` | `/registration-periods`, route hội đồng lồng dưới đợt | Tổ chức lại route Trưởng khoa |
| `/giang-vien/...` | `/topic-proposals`, `/topic-registrations` | Tổ chức lại route Giảng viên |
| `/sinh-vien/...` | `/student-groups`, `/registration-periods/{id}/student-group/...` | Tổ chức lại route Sinh viên |
| `/thong-bao`, `/ho-so`, `/doi-mat-khau` | `/announcements`, `/account`, `/account/password` | Đổi route dùng chung |

Không nên chỉ đổi URL ở template. Controller mapping, redirect, breadcrumb, sidebar, security policy và test phải đổi đồng bộ.

## 5. Chênh lệch phân quyền

### 5.1. Admin chưa có toàn quyền

`WebSecurityConfig` hiện dùng `hasRole(...)` riêng cho nhiều route, ví dụ route nhóm Sinh viên, đề xuất đề tài, yêu cầu đăng ký đề tài và hội đồng. Các rule này không bao gồm `ADMIN`, nên Admin có thể bị từ chối dù đặc tả mới quy định Admin toàn quyền.

### 5.2. Một tài khoản đang có thể có nhiều vai trò

Form tài khoản hiện dùng checkbox `roleIds`; service và cấu trúc dữ liệu cũng đang theo hướng nhiều role. Điều này trái với đặc tả mới: mỗi tài khoản có đúng một role và UI dùng radio/select một lựa chọn.

### 5.3. Điều kiện dữ liệu theo người liên quan

Một số quyền sở hữu đã tồn tại, ví dụ nhóm trưởng mới sửa nhóm và giảng viên phụ trách mới xử lý yêu cầu đăng ký. Các điều kiện này cần được giữ lại khi đổi route; server phải kiểm tra quyền độc lập với việc UI có ẩn nút hay không.

## 6. Chênh lệch nghiệp vụ đợt và chấm điểm

| Quy tắc chốt | Hiện trạng | Việc cần làm |
| --- | --- | --- |
| Mọi loại đợt có hạn nộp bài | Đã có `reportDeadline` | Bổ sung luồng nộp bài và kiểm tra deadline |
| TLCN có hạn GVPB nộp điểm | Có `reviewerDeadline` cho TLCN/KLTN | Giữ cho TLCN, đồng thời bổ sung phân công GVPB |
| KLTN có hạn chốt điểm hội đồng, không có GVPB riêng | Validation hiện vẫn bắt KLTN có `reviewerDeadline` trước `councilDate` | Đổi tên/ý nghĩa field và validation KLTN |
| Hội đồng chỉ KLTN | Service hiện chỉ cấm `MON_HOC`, nên NCKH/TLCN vẫn tạo hội đồng được | Chỉ cho `KLTN` tạo/xem/phân công hội đồng |
| Môn học/NCKH lấy trung bình điểm GVHD | Chưa có luồng chấm tương ứng | Bổ sung phân công/người chấm, điểm và tính điểm |
| TLCN chỉ GVPB chấm | Chưa có GVPB | Bổ sung mô hình và luồng GVPB |

## 7. Giao diện và menu

Sidebar hiện dùng route tiếng Anh và có menu theo role cơ bản, nhưng chưa có dashboard vai trò, menu GVPB TLCN, kết quả Sinh viên hoặc entry từ đợt đến Hội đồng KLTN. Đặc biệt, nút “Thành lập hội đồng” chỉ nằm trong trang danh sách hội đồng; trang đợt chưa dẫn người dùng tới danh sách này.

## 8. Chức năng ngoài đặc tả mới nhưng đang tồn tại

- CRUD `/departments` đang tồn tại, trong khi `chuc_nang.md` đưa CRUD bộ môn ra ngoài phạm vi.
- `/settings` đang tồn tại cho thiết lập giao diện/ngôn ngữ nhưng chưa được mô tả trong `chuc_nang.md`.

Trước khi triển khai route mới, cần quyết định giữ hai chức năng này như tiện ích Admin hay loại khỏi giao diện.

## 9. Thứ tự ưu tiên đề xuất

1. Chốt và cập nhật mô hình một role cho một tài khoản; sau đó sửa Security để Admin toàn quyền.
2. Đổi route, redirect, sidebar, breadcrumb và route trang chủ/xác thực theo `chuc_nang.md`.
3. Cập nhật đợt đăng ký: field theo loại, validation deadline và giới hạn hội đồng chỉ KLTN.
4. Bổ sung nộp bài/báo cáo và các kiểm tra deadline.
5. Bổ sung GVPB TLCN và ba luồng chấm điểm theo loại đợt.
6. Hoàn thiện màn hình hội đồng KLTN, kết quả và dashboard theo vai trò.
