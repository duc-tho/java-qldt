# Danh mục yêu cầu từ đề bài

## Trạng thái và cách đọc

Đây là bản phân tích nguồn, chưa phải đặc tả chức năng chi tiết đã duyệt. Các ID đã công bố trong hội thoại được giữ nguyên ý nghĩa; yêu cầu mới được nối tiếp. Mã Q tham chiếu [clarifications.md](clarifications.md).

“Đã xác định” nghĩa là đề bài đã quy định nội dung đó. Nó không có nghĩa mọi trường dữ liệu, quyền, trạng thái hay ngoại lệ đã đủ để triển khai.

## 1. Chức năng

| ID | Tên | Yêu cầu đã xác định | Chi tiết chờ làm rõ |
| --- | --- | --- | --- |
| FR-001 | Quản lý đợt đăng ký | Trưởng khoa tạo đợt gồm tên, loại môn học/NCKH/TLCN/KLTN, thời gian GV và SV đăng ký, các mốc điều kiện theo loại | Q-01–03, Q-42–44 |
| FR-002 | GV đăng ký đề tài | GV đăng ký đề tài trong thời gian cho phép; đề tài có nội dung, thuộc một bộ môn và có 1–2 GVHD | Q-06–08, Q-18 |
| FR-003 | Phê duyệt đề tài | Hệ thống hỗ trợ phê duyệt đề tài | Q-14–16: người duyệt, đối tượng và số bước phê duyệt |
| FR-004 | Công bố danh sách | Giai đoạn thứ nhất xây dựng và công bố danh sách phục vụ đăng ký của SV ở giai đoạn thứ hai | Q-04 |
| FR-005 | Quản lý nhóm SV | SV thực hiện theo nhóm, tối đa 3 người và một nhóm trưởng; mỗi SV chỉ tham gia một nhóm trong quá trình thực hiện | Q-09–12 |
| FR-006 | SV đăng ký đề tài | Mỗi nhóm đăng ký duy nhất một đề tài đã công bố trong thời gian cho phép | Q-05, Q-13, Q-17 |
| FR-007 | Chấp thuận đăng ký | Nhóm bắt đầu thực hiện sau khi đăng ký và được chấp thuận | Q-14–15, Q-17; chưa kết luận đây là quy trình duyệt độc lập với FR-003 |
| FR-008 | Phân công giảng viên | Hỗ trợ phân công GVHD, GVPB và GV chấm | Q-18–21 |
| FR-009 | Nộp báo cáo | Chỉ nhóm trưởng nộp báo cáo đề tài | Q-22–24 |
| FR-010 | Thành lập hội đồng | Quản lý hội đồng phản biện có 3–5 GV, một chủ tịch và một thư ký | Q-20, Q-25–28 |
| FR-011 | Đánh giá và chấm điểm | Nhóm tham gia phản biện theo kế hoạch; GV có thể chấm nhiều đề tài, nhưng không chấm đề tài mình hướng dẫn | Q-19–21, Q-27, Q-29–32 |
| FR-012 | Tổng hợp kết quả | Chủ tịch tổng hợp đánh giá và điểm của hội đồng, đưa ra kết quả phản biện cuối; điểm cuối là trung bình cộng điểm thành phần | Q-25, Q-29–32 |
| FR-013 | Công bố kết quả | Khoa CNTT công bố kết quả trên hệ thống sau khi phản biện và chấm hoàn tất | Q-33 |
| FR-014 | SV xem kết quả | SV xem điểm và kết quả đánh giá của đề tài mình tham gia | Q-34 |
| FR-015 | Quản lý tài khoản | Hệ thống hỗ trợ quản lý tài khoản người dùng | Q-35–37 |
| FR-016 | Đăng thông báo | Đăng thông báo chính thức của trường và khoa để cung cấp thông tin cho người sử dụng | Q-38–39 |

Khi hoàn thiện, mỗi FR sẽ được mở rộng đủ mục: mục tiêu, actor, tiền điều kiện, input, validation, business rules, luồng chính/thay thế, lỗi, quyền, tác động dữ liệu, tiêu chí chấp nhận và tình huống biên. Không tự đặt các nội dung đang chờ trả lời để điền đủ mẫu.

## 2. Quy tắc nghiệp vụ

| ID | Quy tắc đã xác định | Giới hạn diễn giải |
| --- | --- | --- |
| BR-001 | Một đề tài thuộc một bộ môn, có nội dung rõ ràng, được hướng dẫn bởi ít nhất 1 và tối đa 2 GV | Chưa có GVHD chính/phụ hoặc hạn mức tổng đề tài/GV |
| BR-002 | Một nhóm tối đa 3 SV và có một nhóm trưởng trong nhóm | Không cần hỏi lại giới hạn tối đa; số tối thiểu cho nhóm đăng ký chờ Q-10 |
| BR-003 | Mỗi SV tham gia duy nhất một nhóm trong suốt quá trình thực hiện đề tài | Không tự chuyển thành duy nhất theo đợt, năm học hoặc trọn đời tài khoản |
| BR-004 | Mỗi nhóm đăng ký duy nhất một đề tài từ danh sách đã công bố | Không suy ra chiều ngược lại “một đề tài chỉ có một nhóm” |
| BR-005 | Hội đồng gồm từ 3 đến 5 GV, bao gồm một chủ tịch và một thư ký | Các mốc 3 và 5 được bao gồm; hai chức trách có phải khác người chờ Q-27 |
| BR-006 | Một GV có thể chấm nhiều đề tài nhưng không được chấm đề tài mình hướng dẫn | Không tự cấm GVHD tham gia hội đồng hoặc nhận chức trách không chấm |
| BR-007 | Điểm cuối cùng của đề tài là trung bình cộng các điểm thành phần | Chưa xác định thành phần; không đổi thành trung bình có trọng số |
| BR-008 | Nhóm tiến hành thực hiện sau khi đăng ký được chấp thuận | Chưa định nghĩa chuyển trạng thái, rút đơn hoặc thu hồi duyệt |
| BR-009 | Nhóm phải tham gia phản biện theo kế hoạch sau khi hoàn thành đề tài | Cách tổ chức theo bốn loại còn chờ Q-25 |
| BR-010 | Chủ tịch tổng hợp đánh giá và điểm hội đồng để đưa ra kết quả phản biện cuối | Không tự trao quyền ghi đè điểm trung bình |
| BR-011 | Kết quả được công bố sau khi phản biện và chấm hoàn tất | Điều kiện đủ điểm và người xác nhận hoàn tất chờ Q-31–33 |

## 3. Tác nhân và phân quyền

| ID | Nội dung |
| --- | --- |
| ACT-001 | Trưởng khoa tạo đợt đăng ký |
| ACT-002 | GV đề xuất đề tài và thực hiện nhiệm vụ được phân công |
| ACT-003 | SV thực hiện đề tài theo nhóm và xem kết quả liên quan |
| ACT-004 | Nhóm trưởng thực hiện việc nộp báo cáo |
| ACT-005 | Chủ tịch hội đồng tổng hợp đánh giá và điểm |
| ACT-006 | Hội đồng có thư ký; trách nhiệm chi tiết chưa được đề bài nêu |
| ACT-007 | Khoa CNTT là đơn vị công bố kết quả; vai trò thao tác cụ thể chờ xác nhận |
| PER-001 | Trưởng khoa có quyền tạo đợt đăng ký |
| PER-002 | Chỉ nhóm trưởng được nộp báo cáo đề tài |
| PER-003 | SV có quyền xem điểm và đánh giá của đề tài mình tham gia sau công bố |
| PER-004 | Không cấp quyền chấm một đề tài cho GV đang hướng dẫn đề tài đó |

Đề bài không xác định đầy đủ quyền duyệt, phân công, quản lý tài khoản, đăng thông báo hoặc công bố. Không mặc định Admin có mọi quyền nghiệp vụ.

## 4. Dữ liệu, thời gian và validation

| ID | Nội dung đã xác định |
| --- | --- |
| DATA-001 | Đợt có tên, một loại trong môn học/NCKH/TLCN/KLTN, thời gian bắt đầu/kết thúc đăng ký GV và SV |
| DATA-002 | Hạn GVPB nộp điểm về Khoa chỉ thiết lập cho TLCN hoặc KLTN |
| DATA-003 | Ngày báo cáo hội đồng của đợt chỉ thiết lập cho KLTN |
| DATA-004 | Đề tài có nội dung, bộ môn và danh sách GVHD |
| DATA-005 | Cần lưu nhóm, thành viên, nhóm trưởng và đề tài đăng ký |
| DATA-006 | Cần lưu phân công GV, báo cáo, hội đồng, thành viên, điểm và đánh giá phục vụ luồng nghiệp vụ |
| DATA-007 | Cần lưu tài khoản và thông báo chính thức |
| TIME-001 | GV/SV đăng ký trong thời gian quy định |
| TIME-002 | Các mốc hạn GVPB và ngày báo cáo hội đồng áp dụng theo điều kiện tại DATA-002/003 |
| WF-001 | Hai giai đoạn riêng biệt: xây dựng/công bố danh sách, sau đó nhóm SV đăng ký từ danh sách đã công bố |
| VAL-001 | Kiểm tra tối đa 3 thành viên/nhóm, một nhóm trưởng, 1–2 GVHD, 3–5 GV/hội đồng và cấm chấm đề tài đang hướng dẫn |
| VAL-002 | Chỉ nhận đăng ký vào đề tài đã công bố |
| VAL-003 | Không thiết lập hạn GVPB cho môn học/NCKH; không thiết lập ngày báo cáo hội đồng của đợt cho môn học/NCKH/TLCN |

Kiểu dữ liệu, độ dài trường, quy tắc biên thời gian, tính bắt buộc của các mốc điều kiện, khóa ngoại và cơ chế chống thao tác đồng thời sẽ được đặc tả sau khi chốt câu hỏi liên quan.

## 5. Kết quả, thông báo, bảo mật và phi chức năng

| ID | Nội dung | Nguồn/trạng thái |
| --- | --- | --- |
| RES-001 | SV xem điểm và kết quả đánh giá của đề tài mình tham gia sau công bố | Đề bài |
| NOT-001 | Hỗ trợ đăng tải thông báo chính thức của trường và khoa | Đề bài |
| SEC-001 | Hệ thống có phân quyền và giải pháp bảo mật dữ liệu | Đề bài; chi tiết xác thực, quyền sở hữu, phiên, upload chờ đặc tả |
| NFR-001 | Giải pháp đơn giản, phù hợp đồ án đại học | Hướng dẫn spec của người dùng |

Chưa có chỉ tiêu số người dùng đồng thời, thời gian phản hồi, SLA hoặc thời gian lưu trữ trong đề bài. Không tự đặt chúng thành yêu cầu đã xác nhận.

## 6. Tiêu chí chấp nhận nguồn

Các tiêu chí sau mô tả hành vi nghiệp vụ đã rõ, không phải danh mục kiểm thử. Danh mục đầy đủ sẽ được phát triển trong `12-acceptance-criteria.md` sau khi chốt nghiệp vụ; giữ nguyên các ID dưới đây.

| ID | Given | When | Then | Truy vết |
| --- | --- | --- | --- | --- |
| AC-001 | Nhóm đã có 3 SV | Có yêu cầu thêm SV thứ tư | Từ chối thay đổi, nhóm giữ nguyên thành viên | BR-002, VAL-001 |
| AC-002 | SV là thành viên nhưng không phải nhóm trưởng | SV yêu cầu nộp báo cáo của nhóm | Từ chối, không tạo/thay thế báo cáo | FR-009, PER-002 |
| AC-003 | GV đang hướng dẫn đề tài | GV yêu cầu ghi điểm chấm cho đề tài đó | Từ chối, không ghi điểm | BR-006, PER-004 |
| AC-004 | Đề tài chưa công bố | Nhóm yêu cầu đăng ký đề tài đó | Từ chối, không tạo đăng ký | BR-004, VAL-002 |
| AC-005 | Thời điểm đăng ký nằm ngoài cửa sổ được quy định cho actor | Actor gửi đăng ký | Từ chối, không ghi nhận đăng ký mới | TIME-001 |
| AC-006 | Kết quả đã công bố và SV thuộc nhóm thực hiện đề tài | SV mở kết quả của đề tài | Hiển thị điểm và đánh giá trong phạm vi được chốt tại Q-34 | FR-014, RES-001 |

AC-005 chưa quyết định cách xử lý đúng thời điểm biên; nội dung đó chờ Q-42. AC-006 chưa quyết định công khai điểm từng người chấm.
