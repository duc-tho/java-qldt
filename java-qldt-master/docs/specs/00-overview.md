# Tổng quan hệ thống quản lý đề tài sinh viên

## 1. Trạng thái tài liệu

Tài liệu tổng hợp đề bài gốc và các quyết định trong `Answer.txt`. Đây là bản đặc tả đang hoàn thiện; điểm chưa đủ dữ liệu được truy vết tại [clarifications.md](clarifications.md), không được tự suy diễn khi thiết kế hoặc triển khai.

Đặc tả đặt tại `docs/specs`. Theo yêu cầu của người dùng, bộ tài liệu không có chiến lược kiểm thử hoặc test case; tiêu chí chấp nhận nghiệp vụ vẫn được giữ để xác định hành vi mong đợi.

## 2. Mục tiêu và phạm vi

Hệ thống tự động hóa quản lý đề tài của Khoa CNTT cho bốn loại:

| Mã        | Loại                   |
| --------- | ---------------------- |
| `MON_HOC` | Đề tài môn học         |
| `NCKH`    | Nghiên cứu khoa học    |
| `TLCN`    | Tiểu luận chuyên ngành |
| `KLTN`    | Khóa luận tốt nghiệp   |

Phạm vi gồm quản lý đợt đăng ký; đề xuất, phê duyệt và công bố đề tài; lập nhóm và đăng ký đề tài; phân công giảng viên; nộp báo cáo; đánh giá/phản biện; hội đồng NCKH/TLCN/KLTN; tổng hợp và công bố kết quả; tài khoản; thông báo cấp Khoa; phân quyền và bảo vệ dữ liệu.

Không thuộc phạm vi: quản lý lớp học phần/học kỳ/tín chỉ, thông báo cấp Trường, email, thông báo cá nhân, push thời gian thực, hẹn giờ thông báo, SSO, cloud/object storage, kiến trúc phân tán và quản lý tiến độ hằng tuần.

## 3. Tác nhân

| Tác nhân / chức trách | Trách nhiệm chính                                                                                                                               |
| --------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| `ADMIN`               | Cấp và quản lý tài khoản; không mặc định có quyền nghiệp vụ                                                                                     |
| `HEAD_OF_FACULTY`     | Quản lý đợt, duyệt/công bố đề tài, phân công GV, quản lý hội đồng, mở lại đánh giá, công bố kết quả và đăng thông báo cấp Khoa                  |
| `LECTURER`            | Đề xuất đề tài; làm GV phụ trách, GVHD, GVPB hoặc thành viên hội đồng theo phân công; không được thuộc hội đồng phụ trách đề tài mình hướng dẫn |
| `STUDENT`             | Tạo/tham gia nhóm, đăng ký và thực hiện đề tài, xem kết quả đã công bố                                                                          |
| Nhóm trưởng           | Mời thành viên, quản lý đăng ký và nộp báo cáo                                                                                                  |
| Chủ tịch hội đồng     | Xác nhận kết quả NCKH/TLCN/KLTN do hệ thống tổng hợp                                                                                            |
| Thư ký hội đồng       | Quản lý hồ sơ/phiên, nhập biên bản; không sửa điểm của thành viên khác                                                                          |

Nhóm trưởng, GVHD, GVPB, Chủ tịch và Thư ký là chức trách theo ngữ cảnh, không phải vai trò tài khoản độc lập. Khoa và bộ môn là hai cấp khác nhau; không có vai trò Trưởng bộ môn trong phạm vi đã chốt.

## 4. Quy trình tổng thể

1. Trưởng khoa tạo đợt và các mốc theo loại.
2. GV đề xuất đề tài trong cửa sổ đăng ký GV; người đề xuất là GVHD ban đầu.
3. Sau khi cửa sổ đóng, Trưởng khoa duyệt đề tài và có thể điều chỉnh GVHD.
4. Hệ thống tự động công bố danh sách đã duyệt tại thời điểm cấu hình.
5. Trong cửa sổ đăng ký SV, sinh viên lập nhóm và nhóm đăng ký một đề tài đã công bố.
6. GVHD chấp thuận hoặc từ chối đăng ký; từ chối phải có lý do.
7. Khi được chấp thuận, nhóm bắt đầu thực hiện; thành viên và nhóm trưởng bị khóa.
8. Nhóm trưởng nộp báo cáo. Báo cáo nộp sau hạn được đánh dấu `LATE`.
9. Chấm theo loại: môn học do GV phụ trách; NCKH, TLCN và KLTN do mọi thành viên hợp lệ của hội đồng phản biện đánh giá. GVHD không được thuộc hội đồng phụ trách đề tài mình hướng dẫn.
10. Hệ thống tính trung bình cộng khi đủ đánh giá bắt buộc và người có thẩm quyền chốt kết quả.
11. Trưởng khoa công bố toàn bộ kết quả của đợt; SV chỉ xem kết quả đã công bố của nhóm mình.

## 5. Khác biệt theo loại

| Nội dung                  | Môn học      | NCKH                               | TLCN                               | KLTN                               |
| ------------------------- | ------------ | ---------------------------------- | ---------------------------------- | ---------------------------------- |
| Người hướng dẫn/phụ trách | GV phụ trách | GVHD                               | GVHD                               | GVHD                               |
| Người đánh giá bắt buộc   | GV phụ trách | Mọi thành viên hợp lệ của hội đồng | Mọi thành viên hợp lệ của hội đồng | Mọi thành viên hợp lệ của hội đồng |
| Hội đồng                  | Không        | Có                                 | Có                                 | Có                                 |
| Hạn GVPB                  | Không        | Không                              | Bắt buộc                           | Bắt buộc                           |
| Ngày hội đồng             | Không        | Không                              | Không                              | Bắt buộc                           |
| Người chốt                | GV phụ trách | Chủ tịch hội đồng                  | Chủ tịch hội đồng                  | Chủ tịch hội đồng                  |

Mỗi hội đồng có 3–5 GV, đúng một Chủ tịch và một Thư ký. Mọi thành viên hợp lệ đều phải gửi đánh giá; chỉ được chốt khi đủ các đánh giá bắt buộc. Người chốt chỉ xác nhận kết quả do hệ thống tính, không ghi đè công thức hoặc điểm của người chấm.

## 6. Ranh giới kiến trúc

Tiếp tục kiến trúc nguyên khối hiện có: Spring Boot, Java 25, Thymeleaf, Spring Security, JPA, MariaDB và Flyway. Giao diện chính dùng tiếng Việt. Hợp đồng HTTP mô tả đúng trang HTML, form submit, fragment hoặc JSON thực sự cần dùng. Không dùng HTMX trong phần chức năng mới.

Hệ thống chạy trên máy phát triển/lab hoặc một máy chủ Linux, với 50–100 người dùng đồng thời là mức tham chiếu. Tệp báo cáo nằm trên filesystem máy chủ; cơ sở dữ liệu chỉ lưu metadata và định danh/đường dẫn lưu trữ.

## 7. Mô hình miền cấp cao

- Danh tính và tổ chức: `User`, `Role`, `Permission`, `Department`.
- Vận hành đợt: `RegistrationPeriod`, lịch sử thay đổi, `Topic`, GVHD, lịch sử phê duyệt/công bố.
- Nhóm và đăng ký: `StudentGroup`, `GroupMember`, lời mời/yêu cầu tham gia, `TopicRegistration`.
- Thực hiện và đánh giá: `ReportSubmission`, `ReportFile`, phân công, `Evaluation`, lịch sử mở lại/vô hiệu.
- Hội đồng phản biện NCKH/TLCN/KLTN: `Council`, `CouncilMember`, đề tài thuộc hội đồng, biên bản.
- Kết quả và truyền thông: `FinalResult`, lịch sử công bố/thu hồi, `Announcement`.

Đề tài có thể tái sử dụng qua nhiều đợt. `maxGroups` áp dụng cho đề tài trong từng đợt, mặc định 1; vì vậy báo cáo, đánh giá và kết quả gắn với lần thực hiện của nhóm trong đợt, không chỉ gắn với nội dung đề tài dùng chung.

## 8. Bộ tài liệu

- [01-requirements.md](01-requirements.md): danh mục yêu cầu và truy vết quyết định.
- [clarifications.md](clarifications.md): sổ quyết định nghiệp vụ đã chốt.
- `02-business-rules.md`
- `03-roles-permissions.md`
- `04-user-flows.md`
- `05-domain-model.md`
- `06-data-model.md`
- `07-api.md`
- `08-state-machines.md`
- `09-security.md`
- `10-non-functional.md`
- `12-acceptance-criteria.md`
- `13-phase-plan.md`

Các quyết định nghiệp vụ cốt lõi đã được chốt; có thể hoàn thiện các tài liệu từ `02` trở đi. `AGENTS.md` chỉ được cập nhật sau khi người dùng duyệt toàn bộ đặc tả.
