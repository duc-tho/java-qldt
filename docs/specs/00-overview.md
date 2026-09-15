# Hệ thống quản lý đề tài sinh viên — Tổng quan

## Trạng thái tài liệu

- Trạng thái: bản phân tích yêu cầu, chờ chốt các quyết định nghiệp vụ.
- Nguồn chuẩn: đề bài đầy đủ do người dùng cung cấp trong cuộc trao đổi ngày 15/09/2026, có cả loại môn học.
- Hướng dẫn tổ chức spec: tài liệu đính kèm của người dùng, với hai điều chỉnh: lưu tại `docs/specs/` và bỏ phần testing.
- Các đề xuất trước đây trong hội thoại chưa được người dùng xác nhận không phải yêu cầu đã chốt.
- Tài liệu hiện tại chưa phải spec đủ điều kiện triển khai. Những chỗ thiếu thông tin được truy vết đến câu hỏi cụ thể; không tự điền thành quy tắc.

## 1. Mục tiêu và phạm vi

Tự động hóa quy trình quản lý đề tài sinh viên trong khoa CNTT cho bốn loại:

| Loại | Thuật ngữ dùng trong tài liệu |
| --- | --- |
| Môn học | Môn học |
| Nghiên cứu khoa học | NCKH |
| Tiểu luận chuyên ngành | TLCN |
| Khóa luận tốt nghiệp | KLTN |

Phạm vi gồm tạo đợt đăng ký, GV đề xuất đề tài, xây dựng và công bố danh sách, nhóm SV đăng ký thực hiện, phê duyệt, phân công GVHD/GVPB/GV chấm, nộp báo cáo, phản biện, chấm và tổng hợp điểm, công bố kết quả, quản lý tài khoản, thông báo và bảo mật.

Đề bài không yêu cầu theo dõi tiến độ hàng tuần, giao việc, chat, email tự động, SSO, xuất Excel/PDF hoặc quản lý đào tạo đầy đủ. Các chức năng này không nằm trong phạm vi mặc định. Việc có loại môn học không tự động kéo theo quản lý lớp học phần, tín chỉ hoặc bảng điểm học phần.

## 2. Tác nhân và thuật ngữ

| Thuật ngữ | Ý nghĩa theo đề bài | Điều cần làm rõ |
| --- | --- | --- |
| Trưởng khoa | Người tạo đợt đăng ký | Quyền duyệt, phân công và công bố cụ thể |
| Khoa CNTT | Đơn vị tổ chức quản lý và công bố kết quả | Tài khoản/vai trò nào đại diện đơn vị thao tác |
| Giảng viên (GV) | Đề xuất, hướng dẫn, phản biện hoặc chấm theo phân công | Phạm vi dữ liệu được xem/sửa |
| Sinh viên (SV) | Thực hiện đề tài theo nhóm, xem kết quả của đề tài mình tham gia | Quyền thành lập và thay đổi nhóm |
| Nhóm trưởng | Thành viên chịu trách nhiệm nộp báo cáo | Các quyền khác trong nhóm |
| GVHD / Supervisor | Giảng viên hướng dẫn đề tài | Người phân công, cách thay đổi phân công |
| GVPB / Reviewer | Giảng viên phản biện | Số lượng và cách tham gia điểm cuối theo loại |
| GV chấm / Grader | Giảng viên được giao chấm đề tài | Quan hệ với GVPB và thành viên hội đồng |
| Chủ tịch hội đồng | Tổng hợp đánh giá và điểm, đưa ra kết quả phản biện cuối | Quyền xác nhận hoặc chỉnh kết quả |
| Thư ký hội đồng | Chức trách trong hội đồng | Quyền thao tác cụ thể |
| Bộ môn / Academic Department | Đơn vị quản lý danh sách đề tài | Có cần tài khoản phụ trách bộ môn hay không |

Phân biệt khoa và bộ môn: không dịch Trưởng khoa thành người đứng đầu bộ môn. Admin là vai trò kỹ thuật được đề xuất để quản lý tài khoản, chưa được đề bài định nghĩa. Nhóm trưởng, GVHD, GVPB, chủ tịch và thư ký là chức trách theo ngữ cảnh; chưa mặc định là vai trò toàn hệ thống.

## 3. Luồng tổng thể

1. Trưởng khoa tạo đợt, chọn một trong bốn loại và thiết lập các mốc thời gian phù hợp.
2. Trong giai đoạn thứ nhất, GV đăng ký đề tài; danh sách được xây dựng và công bố.
3. Trong giai đoạn thứ hai, nhóm SV đăng ký một đề tài trong danh sách đã công bố.
4. Đăng ký được xem xét; nhóm được chấp thuận tiến hành thực hiện dưới sự hướng dẫn của GV được phân công.
5. Nhóm trưởng nộp báo cáo.
6. Nhóm tham gia phản biện theo kế hoạch; GV được phân công chấm, không chấm đề tài mình hướng dẫn.
7. Với quy trình có hội đồng, chủ tịch tổng hợp đánh giá và điểm để đưa ra kết quả phản biện cuối.
8. Điểm cuối được tính bằng trung bình cộng các điểm thành phần.
9. Khoa CNTT công bố kết quả sau khi phản biện và chấm hoàn tất.
10. SV xem điểm và đánh giá của đề tài mình tham gia.

Phân công GVHD/GVPB/GV chấm và thành lập hội đồng hỗ trợ luồng trên. Thời điểm phân công, người duyệt và nhánh phản biện theo từng loại còn chờ làm rõ; thứ tự liệt kê không tự đặt ra thời hạn chưa có trong đề bài.

## 4. Khác biệt đã được xác định theo loại

| Thuộc tính | Môn học | NCKH | TLCN | KLTN |
| --- | --- | --- | --- | --- |
| Thời gian GV đăng ký | Có | Có | Có | Có |
| Thời gian SV đăng ký | Có | Có | Có | Có |
| Hạn GVPB nộp điểm về Khoa | Không thiết lập trường này | Không thiết lập trường này | Được thiết lập; tính bắt buộc chờ Q-43 | Được thiết lập; tính bắt buộc chờ Q-43 |
| Ngày báo cáo hội đồng của đợt | Không thiết lập trường này | Không thiết lập trường này | Không thiết lập trường này | Được thiết lập; tính bắt buộc chờ Q-43 |
| Có GVPB, số người chấm, cơ chế phản biện | Chờ Q-19/Q-25 | Chờ Q-19/Q-25 | Chờ Q-19/Q-25 | Có ngày hội đồng; chi tiết chờ Q-19/Q-25 |

Không suy ra “không có GVPB” từ việc không có trường hạn GVPB. Không suy ra “không có hội đồng” từ việc không có trường ngày báo cáo hội đồng của đợt. Đây là điểm cần xác nhận từ đề bài.

## 5. Mô hình miền ứng viên

| Thực thể | Mục đích | Quan hệ đã biết / quyết định còn thiếu |
| --- | --- | --- |
| User, Role, Permission | Tài khoản và phân quyền | Danh sách vai trò chờ Q-35 |
| Department | Bộ môn | Một bộ môn quản lý nhiều đề tài; một đề tài thuộc một bộ môn |
| RegistrationPeriod | Đợt đăng ký | Có loại và các mốc thời gian; quan hệ đề tài–đợt chờ Q-06 |
| Topic | Nội dung đề tài | Thuộc một bộ môn, có 1–2 GVHD |
| TopicSupervisor | Liên kết đề tài–GVHD | Số lượng 1–2; phân chính/phụ chờ Q-07 |
| StudentGroup, GroupMember | Nhóm và thành viên | Tối đa 3 SV, một nhóm trưởng; phạm vi độc quyền chờ Q-09 |
| TopicRegistration | Đăng ký của nhóm vào đề tài | Mỗi nhóm một đề tài; số nhóm của một đề tài chờ Q-05 |
| Assignment | Phân công phản biện/chấm | Có cần thực thể chung hay quan hệ riêng chờ Q-19 |
| Report | Báo cáo và người nộp | Chỉ nhóm trưởng nộp; phiên bản và giới hạn chờ Q-22–24 |
| Council, CouncilMember | Hội đồng và chức trách | 3–5 GV, một chủ tịch, một thư ký; quan hệ với đề tài chờ Q-26 |
| Evaluation | Điểm và đánh giá | Thành phần điểm và đối tượng nhận điểm chờ Q-29–30 |
| FinalResult | Kết quả tổng hợp và công bố | Trung bình cộng; thẩm quyền chốt/công bố chờ Q-32–33 |
| Announcement | Thông báo chính thức | Người đăng và đối tượng xem chờ Q-38 |

Chưa cố định khóa ngoại, cardinality chưa rõ hoặc bộ trạng thái. Đặc biệt, Q-05 quyết định báo cáo/đánh giá/kết quả gắn với đề tài hay từng đăng ký thực hiện.

## 6. Định hướng kỹ thuật đề xuất

Tiếp tục ứng dụng Spring Boot WAR, Java 25, Thymeleaf, JPA, MariaDB, Flyway, Vite/Tailwind/daisyUI hiện có. Tổ chức theo feature trong một ứng dụng. Đây là đề xuất kế thừa repository, chờ xác nhận ở Q-40; không yêu cầu đổi stack hoặc bổ sung hạ tầng.

Spec hợp đồng HTTP phải phù hợp cách giao tiếp được chốt: trang HTML, form, fragment và/hoặc JSON. Không mặc định biến toàn bộ ứng dụng thành REST API.

## 7. Bộ tài liệu

Hiện có:

- [01-requirements.md](01-requirements.md): yêu cầu đã được đề bài xác định, giữ ID ổn định.
- [clarifications.md](clarifications.md): câu hỏi đã chuẩn hóa, đề xuất và các quyết định còn chờ.

Sau khi chốt nghiệp vụ, hoàn thiện bộ spec với các tên đã thống nhất: `00-overview.md`, `01-requirements.md`, `02-business-rules.md`, `03-roles-permissions.md`, `04-user-flows.md`, `05-domain-model.md`, `06-data-model.md`, `07-api.md`, `08-state-machines.md`, `09-security.md`, `10-non-functional.md`, `12-acceptance-criteria.md`, `13-phase-plan.md`.

Không tạo `11-testing.md`, không đưa chiến lược kiểm thử, test case hoặc mục Tests vào kế hoạch giai đoạn. Giữ tiêu chí chấp nhận nghiệp vụ Given/When/Then và rà soát tính nhất quán tài liệu. Việc bỏ testing trong spec không tự sửa quy định phát triển hiện có của repository.

Chỉ bổ sung hướng dẫn cho coding agent vào `AGENTS.md` sau khi toàn bộ spec được duyệt, theo hướng dẫn nguồn; bảo toàn các quy tắc repository đang có.
