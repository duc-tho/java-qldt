# Danh mục yêu cầu từ đề bài

## Trạng thái và cách đọc

Đây là danh mục yêu cầu đã hợp nhất từ đề bài và `Answer.txt`, chưa phải bộ đặc tả cuối đã được duyệt. Các ID đã công bố được giữ nguyên ý nghĩa. Mã Q tham chiếu [clarifications.md](clarifications.md).

“Đã xác định” nghĩa là đề bài đã quy định nội dung đó. Nó không có nghĩa mọi trường dữ liệu, quyền, trạng thái hay ngoại lệ đã đủ để triển khai.

## 1. Chức năng

| ID     | Tên                   | Yêu cầu đã xác định                                                                                                                                                                                         | Truy vết quyết định          |
| ------ | --------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------- |
| FR-001 | Quản lý đợt đăng ký   | Trưởng khoa tạo, sửa, gia hạn/mở lại đợt; hỗ trợ nhiều đợt đồng thời, mốc theo loại và lưu lịch sử                                                                                                          | Q-01–03, Q-42–44             |
| FR-002 | GV đăng ký đề tài     | GV đề xuất đề tài trong cửa sổ cho phép; đề tài có nội dung, bộ môn, `maxGroups` và 1–2 GVHD                                                                                                                | Q-05–08, Q-18                |
| FR-003 | Phê duyệt đề tài      | Trưởng khoa duyệt/từ chối đề tài và có thể điều chỉnh GVHD. Sửa đề tài đã duyệt/công bố trước khi có nhóm được nhận buộc duyệt lại; sau khi có nhóm được nhận thì khóa tên, nội dung, bộ môn và `maxGroups` | Q-02, Q-14–16, Q-48          |
| FR-004 | Công bố danh sách     | Hệ thống tự công bố các đề tài đã duyệt vào thời điểm được Trưởng khoa cấu hình, trước lúc SV đăng ký                                                                                                       | Q-01, Q-04                   |
| FR-005 | Quản lý nhóm SV       | SV tạo nhóm, mời/yêu cầu tham gia, thay đổi nhóm trong cửa sổ; tối đa 3 người, một trưởng nhóm, mỗi SV tối đa một nhóm/đợt                                                                                  | Q-08–12                      |
| FR-006 | SV đăng ký đề tài     | Nhóm đăng ký một đề tài đã công bố, được rút/đổi/đăng ký lại trong cửa sổ; hệ thống kiểm tra `maxGroups` lúc gửi                                                                                            | Q-05, Q-13, Q-17             |
| FR-007 | Chấp thuận đăng ký    | GVHD chấp thuận/từ chối đăng ký nhóm; từ chối có lý do; khi chấp thuận thì khóa cơ cấu nhóm                                                                                                                 | Q-09, Q-14–15                |
| FR-008 | Phân công giảng viên  | Trưởng khoa phân công/điều chỉnh GVHD, GVPB và hội đồng; thay đổi sau khi có điểm phải vô hiệu đánh giá cũ và giữ audit                                                                                     | Q-18–21, Q-45                |
| FR-009 | Nộp báo cáo           | Chỉ nhóm trưởng nộp, tối đa 10 tệp/lần; PDF/DOCX tối đa 20 MB/tệp, ZIP/RAR tối đa 50 MB/tệp; được thay thế trước hạn; nộp sau hạn mang trạng thái `LATE`                                                    | Q-22–24, Q-47                |
| FR-010 | Thành lập hội đồng    | NCKH, TLCN và KLTN dùng hội đồng 3–5 GV, đúng một Chủ tịch và một Thư ký khác nhau; một hội đồng có thể phụ trách nhiều đề tài và không chứa GVHD của các đề tài đó                                         | Q-19–20, Q-25–28, Q-45       |
| FR-011 | Đánh giá và chấm điểm | Môn học do GV phụ trách đánh giá; NCKH/TLCN/KLTN do mọi thành viên hợp lệ của hội đồng đánh giá; thang 0–10; đánh giá đã gửi chỉ sửa sau khi Trưởng khoa mở lại                                             | Q-19–21, Q-25, Q-29–32, Q-45 |
| FR-012 | Tổng hợp kết quả      | Hệ thống tính trung bình cộng mọi đánh giá bắt buộc hợp lệ, làm tròn hai chữ số; GV phụ trách hoặc Chủ tịch hội đồng xác nhận theo loại                                                                     | Q-25, Q-29–32, Q-45–46       |
| FR-013 | Công bố kết quả       | Trưởng khoa công bố theo toàn đợt khi mọi kết quả đã chốt; hỗ trợ thu hồi có lý do, audit và công bố lại                                                                                                    | Q-33                         |
| FR-014 | SV xem kết quả        | SV xem điểm cuối và nhận xét tổng hợp đã công bố của nhóm mình; hệ thống không quản lý trạng thái Đạt/Không đạt                                                                                             | Q-34, Q-46                   |
| FR-015 | Quản lý tài khoản     | Admin cấp/quản lý tài khoản; người dùng đổi mật khẩu và thông tin liên hệ, không tự đổi mã hoặc quyền                                                                                                       | Q-35–37                      |
| FR-016 | Đăng thông báo        | Trưởng khoa đăng, sửa/thu hồi thông báo nội bộ cấp Khoa và chọn audience theo role                                                                                                                          | Q-38–39                      |

Khi hoàn thiện, mỗi FR sẽ được mở rộng đủ mục: mục tiêu, actor, tiền điều kiện, input, validation, business rules, luồng chính/thay thế, lỗi, quyền, tác động dữ liệu, tiêu chí chấp nhận và tình huống biên. Không tự đặt các nội dung đang chờ trả lời để điền đủ mẫu.

## 2. Quy tắc nghiệp vụ

| ID     | Quy tắc đã xác định                                                                               | Giới hạn diễn giải                                                                                                                           |
| ------ | ------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| BR-001 | Đề tài thuộc một bộ môn, có nội dung, `maxGroups` mặc định 1 và 1–2 GVHD ngang quyền              | Không đặt tên duy nhất hoặc hạn mức GVHD                                                                                                     |
| BR-002 | Nhóm có 1–3 SV và đúng một nhóm trưởng                                                            | Cơ cấu bị khóa khi bắt đầu thực hiện                                                                                                         |
| BR-003 | Mỗi SV thuộc tối đa một nhóm trong một đợt                                                        | Không cấm tham gia nhóm ở đợt khác                                                                                                           |
| BR-004 | Mỗi nhóm đăng ký một đề tài đã công bố; một đề tài nhận không quá `maxGroups` nhóm trong đợt      | Kiểm tra sức chứa tại thời điểm gửi                                                                                                          |
| BR-005 | Hội đồng phản biện của NCKH/TLCN/KLTN có 3–5 GV, đúng một Chủ tịch và một Thư ký khác nhau        | Mọi thành viên hợp lệ chấm mọi đề tài được giao cho hội đồng                                                                                 |
| BR-006 | GVHD không được tham gia hội đồng phụ trách đề tài mình hướng dẫn                                 | Kiểm tra xung đột trước khi giao đề tài cho hội đồng hoặc thêm thành viên                                                                    |
| BR-007 | Điểm cuối là trung bình cộng đánh giá bắt buộc hợp lệ, thang 0–10, làm tròn hai chữ số            | Môn học có một điểm của GV phụ trách; NCKH/TLCN/KLTN có điểm của mọi thành viên hợp lệ trong hội đồng; không suy ra trạng thái Đạt/Không đạt |
| BR-008 | Nhóm bắt đầu thực hiện sau khi GVHD chấp thuận đăng ký                                            | Không thu hồi chấp thuận sau thời điểm này                                                                                                   |
| BR-009 | Môn học không có hội đồng và chỉ do GV phụ trách đánh giá; NCKH/TLCN/KLTN dùng hội đồng phản biện | Mọi thành viên hợp lệ của hội đồng phải gửi đánh giá                                                                                         |
| BR-010 | Người chốt theo loại xác nhận kết quả do hệ thống tính                                            | Không được sửa điểm người khác hoặc ghi đè điểm cuối                                                                                         |
| BR-011 | Chỉ công bố toàn đợt khi mọi kết quả bắt buộc đã chốt                                             | Thu hồi công bố phải có lý do và audit                                                                                                       |
| BR-012 | Nội dung nghiệp vụ của đề tài bị khóa khi có nhóm đầu tiên được chấp thuận                        | Thay đổi GVHD là nghiệp vụ phân công riêng, do Trưởng khoa thực hiện và phải lưu lịch sử                                                     |

## 3. Tác nhân và phân quyền

| ID      | Nội dung                                                                                          |
| ------- | ------------------------------------------------------------------------------------------------- |
| ACT-001 | Trưởng khoa tạo đợt đăng ký                                                                       |
| ACT-002 | GV đề xuất đề tài và thực hiện nhiệm vụ được phân công                                            |
| ACT-003 | SV thực hiện đề tài theo nhóm và xem kết quả liên quan                                            |
| ACT-004 | Nhóm trưởng thực hiện việc nộp báo cáo                                                            |
| ACT-005 | Chủ tịch hội đồng NCKH/TLCN/KLTN xác nhận kết quả do hệ thống tổng hợp                            |
| ACT-006 | Thư ký quản lý hồ sơ/phiên, nhập biên bản và hỗ trợ tổng hợp nhưng không sửa điểm thành viên khác |
| ACT-007 | Trưởng khoa là tài khoản đại diện Khoa để công bố kết quả                                         |
| PER-001 | Trưởng khoa có quyền tạo đợt đăng ký                                                              |
| PER-002 | Chỉ nhóm trưởng được nộp báo cáo đề tài                                                           |
| PER-003 | SV có quyền xem điểm và đánh giá của đề tài mình tham gia sau công bố                             |
| PER-004 | Không cho GVHD tham gia hội đồng phụ trách đề tài mình hướng dẫn                                  |
| PER-005 | Admin quản lý tài khoản nhưng không mặc định có quyền duyệt, chấm hoặc công bố                    |
| PER-006 | Chỉ Trưởng khoa được mở lại đánh giá đã gửi và công bố/thu hồi kết quả toàn đợt                   |

Đề bài không xác định đầy đủ quyền duyệt, phân công, quản lý tài khoản, đăng thông báo hoặc công bố. Không mặc định Admin có mọi quyền nghiệp vụ.

## 4. Dữ liệu, thời gian và validation

| ID       | Nội dung đã xác định                                                                                                                     |
| -------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| DATA-001 | Đợt có tên, một loại trong môn học/NCKH/TLCN/KLTN, thời gian bắt đầu/kết thúc đăng ký GV và SV                                           |
| DATA-002 | Hạn GVPB bắt buộc cho TLCN/KLTN; không được thiết lập cho môn học/NCKH                                                                   |
| DATA-003 | Ngày hội đồng bắt buộc cho KLTN; không được thiết lập cho loại khác                                                                      |
| DATA-004 | Đề tài có nội dung, bộ môn, `maxGroups` mặc định 1 và 1–2 GVHD                                                                           |
| DATA-005 | Cần lưu nhóm, thành viên, nhóm trưởng và đề tài đăng ký                                                                                  |
| DATA-006 | Cần lưu phân công GV, báo cáo, hội đồng NCKH/TLCN/KLTN, thành viên, điểm và đánh giá phục vụ luồng nghiệp vụ                             |
| DATA-007 | Cần lưu tài khoản và thông báo chính thức                                                                                                |
| TIME-001 | GV/SV đăng ký trong thời gian quy định; thời điểm kết thúc được tính là hợp lệ                                                           |
| TIME-002 | Mọi mốc dùng ngày giờ theo `Asia/Ho_Chi_Minh`; kết thúc đăng ký GV trước bắt đầu đăng ký SV                                              |
| TIME-003 | Với KLTN, kết thúc đăng ký SV trước hạn GVPB, và hạn GVPB trước ngày hội đồng                                                            |
| WF-001   | Hai giai đoạn riêng biệt: xây dựng/công bố danh sách, sau đó nhóm SV đăng ký từ danh sách đã công bố                                     |
| VAL-001  | Kiểm tra 1–3 thành viên/nhóm, đúng một nhóm trưởng, 1–2 GVHD, 3–5 GV/hội đồng và cấm GVHD thuộc hội đồng phụ trách đề tài mình hướng dẫn |
| VAL-002  | Chỉ nhận đăng ký vào đề tài đã công bố                                                                                                   |
| VAL-003  | Không thiết lập hạn GVPB cho môn học/NCKH; không thiết lập ngày báo cáo hội đồng của đợt cho môn học/NCKH/TLCN                           |
| VAL-004  | Mỗi lần nộp báo cáo có tối đa 10 tệp; chỉ nhận PDF/DOCX không quá 20 MB/tệp hoặc ZIP/RAR không quá 50 MB/tệp                             |
| VAL-005  | Từ chối sửa tên, nội dung, bộ môn hoặc `maxGroups` của đề tài đã có ít nhất một nhóm được chấp thuận                                     |

Kiểu dữ liệu, độ dài trường, khóa ngoại và cơ chế chống thao tác đồng thời sẽ được đặc tả trong mô hình dữ liệu. Không còn câu hỏi nghiệp vụ cốt lõi đang mở.

## 5. Kết quả, thông báo, bảo mật và phi chức năng

| ID      | Nội dung                                                                          | Nguồn/trạng thái   |
| ------- | --------------------------------------------------------------------------------- | ------------------ |
| RES-001 | SV xem điểm và kết quả đánh giá của đề tài mình tham gia sau công bố              | Đề bài             |
| NOT-001 | Thông báo nội bộ cấp Khoa do Trưởng khoa quản lý, phân phối theo role             | Q-38–39            |
| SEC-001 | Hệ thống phân quyền theo role và chức trách; tệp chỉ cho người liên quan truy cập | Đề bài, Q-24, Q-35 |
| NFR-001 | Ứng dụng nguyên khối chạy trên máy cá nhân/lab hoặc một Linux server              | Q-40–41            |
| NFR-002 | Mức tải tham chiếu 50–100 người dùng đồng thời, không phải SLA                    | Q-41               |
| NFR-003 | Giao diện tiếng Việt; tệp lưu trên filesystem và DB lưu metadata                  | Q-41               |

Không đặt SLA thời gian phản hồi hoặc thời gian lưu trữ khi chưa có yêu cầu.

## 6. Tiêu chí chấp nhận nguồn

Các tiêu chí sau mô tả hành vi nghiệp vụ đã rõ, không phải danh mục kiểm thử. Danh mục đầy đủ sẽ được phát triển trong `12-acceptance-criteria.md` sau khi chốt nghiệp vụ; giữ nguyên các ID dưới đây.

| ID     | Given                                                      | When                                                                       | Then                                                                                                      | Truy vết              |
| ------ | ---------------------------------------------------------- | -------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------- | --------------------- |
| AC-001 | Nhóm đã có 3 SV                                            | Có yêu cầu thêm SV thứ tư                                                  | Từ chối thay đổi, nhóm giữ nguyên thành viên                                                              | BR-002, VAL-001       |
| AC-002 | SV là thành viên nhưng không phải nhóm trưởng              | SV yêu cầu nộp báo cáo của nhóm                                            | Từ chối, không tạo/thay thế báo cáo                                                                       | FR-009, PER-002       |
| AC-003 | GV đang hướng dẫn đề tài                                   | Trưởng khoa thêm GV đó vào hội đồng phụ trách đề tài                       | Từ chối, không thay đổi hội đồng                                                                          | BR-006, PER-004       |
| AC-004 | Đề tài chưa công bố                                        | Nhóm yêu cầu đăng ký đề tài đó                                             | Từ chối, không tạo đăng ký                                                                                | BR-004, VAL-002       |
| AC-005 | Thời điểm đăng ký nằm ngoài cửa sổ được quy định cho actor | Actor gửi đăng ký                                                          | Từ chối, không ghi nhận đăng ký mới                                                                       | TIME-001              |
| AC-006 | Kết quả đã công bố và SV thuộc nhóm thực hiện đề tài       | SV mở kết quả                                                              | Hiển thị điểm cuối và nhận xét tổng hợp; không hiển thị trạng thái Đạt/Không đạt hoặc chi tiết người chấm | FR-014, RES-001       |
| AC-007 | Đề tài đã đủ số nhóm theo `maxGroups`                      | Nhóm khác gửi đăng ký                                                      | Từ chối và không tạo đăng ký                                                                              | BR-004, Q-17          |
| AC-008 | Hội đồng có một GV là GVHD của đề tài sắp được giao        | Trưởng khoa giao đề tài cho hội đồng                                       | Từ chối và yêu cầu giải quyết xung đột thành viên trước khi giao                                          | BR-006, Q-20          |
| AC-009 | Đánh giá đã ở `SUBMITTED`                                  | Người chấm tự yêu cầu sửa                                                  | Từ chối cho đến khi Trưởng khoa mở lại                                                                    | Q-32                  |
| AC-010 | Một kết quả bắt buộc trong đợt chưa được chốt              | Trưởng khoa yêu cầu công bố đợt                                            | Từ chối, đợt vẫn chưa công bố                                                                             | BR-011, Q-33          |
| AC-011 | Nhóm trưởng chọn tệp báo cáo                               | Tệp sai định dạng, vượt giới hạn theo định dạng hoặc lần nộp có hơn 10 tệp | Từ chối lần nộp và không lưu tệp                                                                          | FR-009, VAL-004       |
| AC-012 | Đề tài đã có ít nhất một nhóm được chấp thuận              | Có yêu cầu sửa tên, nội dung, bộ môn hoặc `maxGroups`                      | Từ chối thay đổi và giữ nguyên dữ liệu đề tài                                                             | BR-012, VAL-005, Q-48 |
