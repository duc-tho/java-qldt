# Sổ quyết định nghiệp vụ

## 1. Nguồn và quy ước

Đề bài gốc và `Answer.txt` là nguồn nghiệp vụ. Q-01–Q-44 giữ nguyên chủ đề để truy vết. Câu trả lời chưa rõ hoặc mâu thuẫn được tách thành Q-45 trở đi; không tự lấp khoảng trống.

## 2. Quyết định đã chốt

### Đợt đăng ký và thời gian

| ID | Quyết định chuẩn hóa |
| --- | --- |
| Q-01 | Có khoảng duyệt/công bố riêng; `lecturerRegistrationEnd < studentRegistrationStart`. |
| Q-02 | Cho phép nhiều đợt đồng thời, kể cả cùng loại. Đơn được xem và sửa trong cửa sổ đăng ký; duyệt sau khi cửa sổ đóng. |
| Q-03 | Trưởng khoa được sửa thời gian, gia hạn hoặc mở lại. Không sửa trường làm dữ liệu phát sinh mất hợp lệ, đặc biệt không đổi loại sau khi có đăng ký. Gia hạn/mở lại phải lưu lịch sử. |
| Q-04 | Trưởng khoa duyệt đề tài; hệ thống tự công bố danh sách đã duyệt vào thời điểm cấu hình. |
| Q-42 | Dùng ngày giờ và múi giờ `Asia/Ho_Chi_Minh`; thời điểm kết thúc vẫn hợp lệ. |
| Q-43 | TLCN/KLTN bắt buộc hạn GVPB; KLTN bắt buộc ngày hội đồng ngay khi tạo. `reviewerDeadline < councilReportDate`; ngày hội đồng sau lúc đóng đăng ký SV. |
| Q-44 | Đợt môn học chỉ cần tên, loại `MON_HOC` và mốc chung; không quản lý môn/lớp/học kỳ. |

### Đề tài, nhóm và đăng ký

| ID | Quyết định chuẩn hóa |
| --- | --- |
| Q-05 | Đề tài trong đợt có `maxGroups`, mặc định 1; từ chối đăng ký khi đã đủ chỗ. |
| Q-06 | Nội dung đề tài có thể tái sử dụng qua nhiều đợt; tên không duy nhất toàn hệ thống. |
| Q-07 | 1–2 GVHD ngang quyền; không phân chính/phụ, không đặt hạn mức hướng dẫn. |
| Q-08 | Nhóm tối đa 3 SV và đúng một nhóm trưởng. |
| Q-09 | Trong một đợt, mỗi SV thuộc tối đa một nhóm. Khi bắt đầu thực hiện, thành viên và trưởng nhóm bị khóa. |
| Q-10 | Nhóm 1–3 SV được đăng ký; không giới hạn cùng lớp, khóa hoặc bộ môn. |
| Q-11 | Bất kỳ SV nào cũng có thể tạo nhóm và mặc định là trưởng nhóm. Trưởng nhóm mời SV; SV có thể yêu cầu tham gia. |
| Q-12 | Trong cửa sổ đăng ký và trước lúc thực hiện, nhóm được đổi trưởng, thêm, xóa hoặc rời thành viên nếu vẫn hợp lệ. |
| Q-13 | Trong cửa sổ đăng ký, nhóm được rút/đổi đề tài khi đang chờ; sau từ chối được đăng ký lại. SV được chuyển nhóm nếu chưa bị khóa. |
| Q-14 | Trưởng khoa duyệt đề tài. GVHD duyệt đăng ký nhóm. Người đề xuất là GVHD ban đầu; Trưởng khoa được điều chỉnh GVHD chính thức. |
| Q-15 | Từ chối phải có lý do. Không thu hồi chấp thuận sau khi nhóm bắt đầu thực hiện. |
| Q-16 | Trước khi có nhóm được chấp thuận, đề tài được sửa; nếu đã duyệt hoặc công bố thì phải đưa về chờ duyệt và phê duyệt lại. Sau khi có ít nhất một nhóm được chấp thuận, khóa tên, nội dung, bộ môn và `maxGroups`. |
| Q-17 | Kiểm tra `maxGroups` lúc gửi; khi đủ chỗ thì từ chối và không tạo đăng ký. |
| Q-48 | Không cho mở sửa tên, nội dung, bộ môn hoặc `maxGroups` sau khi đã có nhóm được chấp thuận. Thay đổi GVHD là nghiệp vụ phân công riêng của Trưởng khoa, phải lưu lịch sử và không được coi là sửa nội dung đề tài. |

### Phân công, báo cáo và đánh giá

| ID | Quyết định chuẩn hóa |
| --- | --- |
| Q-18 | Trưởng khoa phân công; người đề xuất là GVHD ban đầu. |
| Q-19 | NCKH, TLCN và KLTN đều dùng hội đồng phản biện gồm 3–5 GV, đúng một Chủ tịch và một Thư ký. Mọi thành viên hợp lệ của hội đồng đều chấm các đề tài thuộc hội đồng. |
| Q-20 | GVHD không được tham gia hội đồng phụ trách đề tài mình hướng dẫn, kể cả với vai trò Chủ tịch hoặc Thư ký. |
| Q-21 | Sau `SUBMITTED`, chỉ Trưởng khoa được đổi người đánh giá. Đánh giá cũ bị vô hiệu nhưng giữ audit; người mới đánh giá lại. |
| Q-22 | Hạn báo cáo không gia hạn; nộp sau hạn nhận trạng thái `LATE`. Hạn GVPB chỉ Trưởng khoa được gia hạn; chỉ gửi điểm sau hạn cũ khi đã gia hạn. |
| Q-23 | Mỗi lần nộp tối đa 10 tệp. PDF/DOCX tối đa 20 MB/tệp; ZIP/RAR tối đa 50 MB/tệp. |
| Q-47 | Xác nhận áp dụng giới hạn chi tiết tại Q-23: PDF/DOCX tối đa 20 MB/tệp; ZIP/RAR tối đa 50 MB/tệp; tối đa 10 tệp cho mỗi lần nộp. Quy định chung “mọi tệp tối đa 20 MB” không áp dụng cho ZIP/RAR. |
| Q-24 | Được thay thế lần nộp trước hạn; chỉ người liên quan truy cập; không có bước duyệt báo cáo. |
| Q-25 | Môn học không dùng hội đồng và chỉ do GV phụ trách đánh giá. NCKH, TLCN và KLTN đều dùng hội đồng phản biện. Người chốt: GV phụ trách đối với môn học và Chủ tịch hội đồng đối với NCKH/TLCN/KLTN. |
| Q-26 | Một hội đồng có thể phụ trách nhiều đề tài. Không quản lý lịch chi tiết ngoài các mốc đã cấu hình cho đợt. |
| Q-27 | Chủ tịch và Thư ký khác nhau. Mọi thành viên chấm. Thư ký quản lý hồ sơ/phiên, nhập biên bản, không sửa điểm người khác. |
| Q-28 | Trưởng khoa quản lý hội đồng; hội đồng phải có 3–5 thành viên hợp lệ khi đưa vào sử dụng. Hội đồng không được chứa GVHD của bất kỳ đề tài nào được giao cho chính hội đồng đó. |
| Q-29 | Điểm cuối là trung bình cộng tự động của mọi đánh giá bắt buộc đã gửi và còn hiệu lực. |
| Q-30 | Thang 0–10; điểm cuối làm tròn hai chữ số; không rubric/xếp loại; điểm chung cho nhóm. |
| Q-31 | Chỉ chốt khi đủ đánh giá bắt buộc. Đánh giá sau hạn chỉ hợp lệ khi đã gia hạn. |
| Q-32 | Người chấm không tự sửa bản đã gửi. Trưởng khoa mở lại; chính người chấm sửa/gửi lại. Người chốt xác nhận điểm hệ thống tính, không ghi đè. |
| Q-45 | Với môn học, đánh giá bắt buộc duy nhất do GV phụ trách gửi. Với NCKH, TLCN và KLTN, mọi thành viên hợp lệ của hội đồng đều phải gửi đánh giá; GVHD không gửi điểm riêng và không được thuộc hội đồng phụ trách đề tài mình hướng dẫn. Chỉ được chốt khi đã có đủ các đánh giá bắt buộc. |

### Công bố, tài khoản, thông báo và kỹ thuật

| ID | Quyết định chuẩn hóa |
| --- | --- |
| Q-33 | Trưởng khoa công bố theo toàn đợt khi mọi kết quả đã chốt. Muốn sửa phải thu hồi với lý do/audit, điều chỉnh, chốt và công bố lại. |
| Q-34 | SV chỉ xem điểm cuối và nhận xét tổng hợp đã công bố của nhóm mình; không xem chi tiết/danh tính người chấm. Trưởng khoa xem thống kê cấp Khoa. Phần trạng thái Đạt/Không đạt trong câu trả lời ban đầu được loại bỏ theo Q-46. |
| Q-35 | `ADMIN` quản lý tài khoản; ba role nghiệp vụ hoạt động theo phân quyền. Admin không mặc định duyệt/chấm/công bố. |
| Q-36 | Admin cấp tài khoản. Người dùng đổi mật khẩu/thông tin liên hệ, không tự sửa mã hoặc quyền. |
| Q-37 | Hồ sơ có mã SV/GV và bộ môn. |
| Q-38 | Chỉ Trưởng khoa đăng thông báo cấp Khoa, chọn audience theo role, sửa/thu hồi; lưu tác giả, thời gian và trạng thái. |
| Q-39 | Chỉ hiển thị nội bộ và lưu để xem lại; không email, push, realtime, hẹn giờ hoặc gửi riêng. |
| Q-40 | Kế thừa Spring Boot + Thymeleaf; hợp đồng HTTP theo HTML/form/fragment/JSON thực tế; loại bỏ HTMX khỏi thiết kế mới. |
| Q-41 | Chạy trên máy cá nhân/lab hoặc một Linux server; 50–100 người đồng thời là tham chiếu. Tệp ở filesystem, DB lưu metadata; UI tiếng Việt; không cloud/object storage hoặc đa ngôn ngữ. |
| Q-46 | Không quản lý trạng thái Đạt/Không đạt và không đặt ngưỡng đạt. Kết quả công bố cho SV chỉ gồm điểm cuối và nhận xét tổng hợp. |

## 3. Trạng thái làm rõ

Không còn câu hỏi nghiệp vụ cốt lõi đang mở. Bộ đặc tả chi tiết có thể được hoàn thiện từ các quyết định trên mà không tự tạo thêm quy tắc.
