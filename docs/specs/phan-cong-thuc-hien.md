# PHÂN CÔNG THỰC HIỆN ĐỒ ÁN

## Hệ thống quản lý đề tài sinh viên Khoa Công nghệ Thông tin

Tài liệu này xác định rõ chức năng mỗi thành viên phải tự làm, tự kiểm tra, viết báo cáo và bảo vệ. Mỗi chức năng chỉ có một người chịu trách nhiệm chính.

Tài liệu được tổng hợp từ yêu cầu đề tài, tiêu chí chấm, source hiện tại và các vấn đề còn tồn đọng.

---

## 1. Thành viên

| MSSV | Họ và tên | Vai trò |
| --- | --- | --- |
| 25810246 | Phạm Minh Trí | Thành viên |
| 25810238 | Huỳnh Thôn | Thành viên |
| 25810240 | Trần Nhựt Tiến | Thành viên |
| 25810237 | Ngô Đức Thọ | Nhóm trưởng |
| 25810189 | Dương Chấn Hưng | Thành viên |

---

## 2. Quy định nghiệp vụ đã chốt

### 2.1. Nhóm sinh viên

- Mỗi nhóm có từ **03 đến 05 sinh viên** khi gửi đăng ký đề tài.
- Có thể tạo nhóm ban đầu chỉ gồm nhóm trưởng để tiếp tục mời thành viên.
- Mỗi nhóm luôn có đúng một nhóm trưởng.
- Một sinh viên chỉ được tham gia một nhóm trong cùng một đợt.
- Chỉ nhóm trưởng được quản lý thành viên, chọn đề tài, gửi đăng ký và nộp báo cáo.

### 2.2. Giảng viên hướng dẫn không được chấm đề tài mình hướng dẫn

- Mọi loại đợt đều phải sử dụng người chấm không thuộc danh sách giảng viên hướng dẫn của đề tài.
- Kiểm tra ràng buộc khi phân công người chấm và kiểm tra lại khi gửi điểm.
- Nếu đổi giảng viên hướng dẫn gây xung đột với phân công chấm hiện tại thì phải từ chối thay đổi.
- Khi đã có điểm được gửi, không cho đổi giảng viên hướng dẫn hoặc người chấm.

### 2.3. Phân quyền

- Ẩn nút trên giao diện không thay thế cho kiểm tra quyền ở phía máy chủ.
- Trưởng khoa quản lý đợt, đề tài, người dùng, bộ môn, hội đồng, công bố điểm và thông báo.
- Giảng viên quản lý đề tài của mình, duyệt nhóm đăng ký đề tài mình hướng dẫn và chấm các đề tài được phân công.
- Sinh viên quản lý nhóm của mình, chọn đề tài, nộp báo cáo và chỉ xem kết quả của nhóm mình.

---

## 3. Phân công tổng quát

| Thành viên | Phần chức năng chịu trách nhiệm |
| --- | --- |
| Phạm Minh Trí | Đợt đăng ký, mốc thời gian, giai đoạn và khóa thao tác theo thời gian |
| Huỳnh Thôn | Nhóm sinh viên, đăng ký đề tài, nộp báo cáo và tra cứu kết quả |
| Trần Nhựt Tiến | Đề tài, giảng viên hướng dẫn, duyệt đề tài và nghiệp vụ giảng viên |
| Ngô Đức Thọ | Quản trị dữ liệu chung, giao diện dùng chung, gửi thư, xuất báo cáo, trang tổng quan và triển khai |
| Dương Chấn Hưng | Đăng nhập, phân quyền, người chấm, hội đồng, chấm điểm và công bố kết quả |

---

## 4. Phạm Minh Trí — Đợt đăng ký

| Mã | Chức năng | Cần làm | Kết quả cần đạt |
| --- | --- | --- | --- |
| TRI-01 | Quản lý đợt đăng ký | Hoàn thiện danh sách, tạo, xem, sửa và xóa; kiểm tra dữ liệu đầu vào và quyền Trưởng khoa; trang sửa chỉ hiển thị biểu mẫu trong bố cục chung có thanh bên và chân trang. | Đợt được quản lý đầy đủ, dữ liệu sai bị từ chối và người không có quyền không thể thay đổi. |
| TRI-02 | Hai giai đoạn đăng ký | Kiểm tra thứ tự các mốc: đề xuất, duyệt và công bố đề tài, sinh viên đăng ký, nộp báo cáo, chấm điểm; xác định giai đoạn hiện tại từ thời gian hệ thống. | Giai đoạn 1 dành cho đề xuất và duyệt đề tài; chỉ sang giai đoạn 2 sinh viên mới được đăng ký. |
| TRI-03 | Khóa thao tác theo thời gian | Dùng cùng một quy tắc thời gian tại phần xử lý nghiệp vụ cho đề xuất đề tài, tạo nhóm, chọn đề tài, gửi đăng ký và nộp báo cáo. | Gọi yêu cầu trực tiếp cũng không thể thực hiện thao tác ngoài thời gian cho phép. |
| TRI-04 | Công bố danh sách đề tài | Thêm thao tác công bố đề tài đã duyệt; lọc danh sách sinh viên được xem; ẩn nút thêm đề tài tại trang chi tiết đợt khi không đúng vai trò hoặc giai đoạn. | Sinh viên chỉ thấy đề tài đã duyệt sau thời điểm công bố. |
| TRI-05 | Biểu mẫu ngày giờ | Chỉ hiện các mốc phù hợp với từng loại đợt; dùng bộ chọn ngày giờ theo ngày/tháng/năm và kiểm tra lại dữ liệu ở phía máy chủ. | Ngày giờ hiển thị dễ hiểu, lưu đúng và không tạo được khoảng thời gian mâu thuẫn. |
| TRI-06 | Điều hướng và tóm tắt đợt | Hiển thị tên, loại, giai đoạn, mốc gần nhất và việc cần làm; bổ sung đường dẫn điều hướng dạng `Trang chủ > Tên đợt > Màn hình`. | Người dùng luôn biết đang ở đợt nào, giai đoạn nào và có thể thực hiện việc gì. |

Phạm Minh Trí phải tự chuẩn bị dữ liệu minh họa cho đợt ở từng giai đoạn và giải thích được cách kiểm tra thời gian tại phần xử lý nghiệp vụ.

---

## 5. Huỳnh Thôn — Nhóm sinh viên, báo cáo và kết quả

| Mã | Chức năng | Cần làm | Kết quả cần đạt |
| --- | --- | --- | --- |
| THON-01 | Tạo nhóm và giới hạn thành viên | Cho phép tạo nhóm với một nhóm trưởng; thêm hoặc xóa thành viên; kiểm tra tối thiểu 03, tối đa 05 người khi gửi đăng ký. | Không thể gửi đăng ký nếu nhóm dưới 03 hoặc trên 05 thành viên. |
| THON-02 | Không trùng thành viên | Kiểm tra một sinh viên chỉ thuộc một nhóm trong cùng đợt và tạo ràng buộc dữ liệu để chống hai yêu cầu đồng thời. | Một sinh viên không thể xuất hiện trong hai nhóm của cùng một đợt. |
| THON-03 | Quản lý nhóm trưởng | Chỉ nhóm trưởng được sửa nhóm; cho phép chuyển quyền cho thành viên hiện tại; không cho xóa nhóm trưởng trực tiếp. | Sau mọi thao tác, nhóm luôn có đúng một nhóm trưởng. |
| THON-04 | Đăng ký đề tài | Cho nhóm chọn đề tài đã công bố, gửi yêu cầu, xem trạng thái và hủy khi quy định cho phép; kiểm tra lại số thành viên khi giảng viên duyệt. | Trạng thái và các nút gửi, hủy, sửa hiển thị đúng quyền và đúng giai đoạn. |
| THON-05 | Chi tiết nhóm | Tạo màn hình hiển thị đề tài, trạng thái, trưởng nhóm, thành viên, báo cáo và kết quả; giới hạn dữ liệu theo quan hệ của sinh viên, giảng viên và Trưởng khoa. | Sinh viên thấy nhóm của mình; giảng viên và Trưởng khoa chỉ thấy nhóm đúng phạm vi quyền. |
| THON-06 | Nộp báo cáo | Chỉ nhóm trưởng của đăng ký đã duyệt được nộp đúng hạn; lưu các lần nộp, tệp, thời gian và người nộp. | Xem được lịch sử nộp và xác định rõ bản báo cáo hiện hành. |
| THON-07 | Lịch sử tham gia và tra cứu điểm | Liệt kê các đợt sinh viên đã tham gia, kể cả đợt đã đóng; chỉ hiển thị điểm đã công bố của chính nhóm đó. | Sinh viên xem được đợt và điểm của mình nhưng không thể đổi mã trên đường dẫn để xem nhóm khác. |

Huỳnh Thôn phải tự chuẩn bị nhóm có 01, 03 và 05 thành viên, trường hợp sinh viên đã có nhóm, nhiều lần nộp báo cáo và một đợt cũ đã công bố điểm.

---

## 6. Trần Nhựt Tiến — Đề tài và nghiệp vụ giảng viên

| Mã | Chức năng | Cần làm | Kết quả cần đạt |
| --- | --- | --- | --- |
| TIEN-01 | Quản lý đề tài | Hoàn thiện danh sách, tạo, xem, sửa và xóa; kiểm tra người đề xuất, thời gian và trạng thái trước khi thay đổi. | Giảng viên chỉ sửa hoặc xóa đề tài của mình khi còn được phép. |
| TIEN-02 | Duyệt và từ chối đề tài | Hoàn thiện luồng gửi duyệt, duyệt, từ chối và gửi lại; bắt buộc lý do khi từ chối. | Trạng thái chuyển đúng quy định và người đề xuất luôn thấy lý do từ chối. |
| TIEN-03 | Giảng viên hướng dẫn | Mỗi đề tài có 01–02 giảng viên hợp lệ; bổ sung ô tìm theo tên hoặc mã, chọn rõ ràng, không trùng và không vượt giới hạn. | Không thể thêm tài khoản sai vai trò hoặc thêm trùng giảng viên hướng dẫn. |
| TIEN-04 | Xử lý nhóm đăng ký | Hiển thị các nhóm đăng ký đề tài của giảng viên theo mọi trạng thái; cho xem thành viên, trưởng nhóm, báo cáo; duyệt hoặc từ chối đúng thời gian. | Giảng viên quản lý được đầy đủ các nhóm liên quan đến đề tài mình hướng dẫn. |
| TIEN-05 | Kiểm tra xung đột người chấm | Trước khi đổi giảng viên hướng dẫn, kiểm tra phân công chấm và điểm đã gửi; trả thông báo rõ nếu có xung đột. | Không thể biến người đang chấm thành giảng viên hướng dẫn của cùng đề tài. |
| TIEN-06 | Tìm kiếm và danh sách | Tìm theo tên đề tài, bộ môn, giảng viên hướng dẫn, trạng thái và đợt; tải đủ dữ liệu cần thiết mà không truy vấn lặp theo từng dòng. | Tìm kiếm đúng điều kiện, danh sách không thiếu dữ liệu và thao tác xem, sửa, duyệt, từ chối hiển thị đúng lúc. |

Trần Nhựt Tiến phải tự chuẩn bị đề tài đang chờ duyệt, bị từ chối, được gửi lại, có một hoặc hai giảng viên hướng dẫn và các nhóm đăng ký ở nhiều trạng thái.

---

## 7. Ngô Đức Thọ — Quản trị, giao diện và chức năng mở rộng

| Mã | Chức năng | Cần làm | Kết quả cần đạt |
| --- | --- | --- | --- |
| THO-01 | Quản lý giảng viên và sinh viên | Hoàn thiện danh sách, tạo, xem, sửa, khóa hoặc xóa theo quy định; kiểm tra mã, thư điện tử, bộ môn và vai trò. | Trưởng khoa quản lý được tài khoản; dữ liệu trùng hoặc sai vai trò bị từ chối. |
| THO-02 | Quản lý bộ môn và thông báo | Hoàn thiện các thao tác với bộ môn; cho tạo, sửa, xem, thu hồi và xóa thông báo theo trạng thái. | Không xóa bộ môn đang được sử dụng; thông báo có vòng đời và thao tác rõ ràng. |
| THO-03 | Sửa chữ và bản dịch | Bổ sung toàn bộ khóa bản dịch còn thiếu; đổi “không gian chấm điểm” thành “Chấm điểm”; thống nhất thuật ngữ tiếng Việt. | Không còn chuỗi lỗi dạng `??...??` và tên chức năng dễ hiểu, nhất quán. |
| THO-04 | Thanh trên và thanh bên | Căn biểu tượng đăng xuất cùng dòng với chữ; sắp xếp menu Đợt đăng ký gồm Nhóm, Chấm điểm, Đề xuất đề tài; xử lý tràn nội dung khi thu nhỏ. | Menu dùng được trên màn hình rộng và hẹp, không che hoặc tràn khỏi vùng hiển thị. |
| THO-05 | Giao diện dùng chung | Chuẩn hóa đường dẫn điều hướng, nút thao tác trong bảng, biểu mẫu, trạng thái trống và thông báo lỗi theo tài liệu thiết kế. | Các màn hình có cách trình bày và thao tác nhất quán, dễ nhận biết hành động chính và phụ. |
| THO-06 | Trang chủ theo vai trò | Hiển thị việc cần làm và số liệu thật cho Trưởng khoa, giảng viên và sinh viên. | Mỗi vai trò đăng nhập vào thấy đúng đợt, nhóm, đề tài hoặc công việc liên quan. |
| THO-07 | Gửi thư thông báo điểm | Nhận thông tin sau khi công bố thành công; gửi kết quả cho từng sinh viên; cấu hình máy chủ thư bằng biến môi trường; ghi nhận lỗi và ngăn gửi trùng. | Lỗi gửi một thư không làm mất kết quả đã công bố và xác định được người nhận gửi thất bại. |
| THO-08 | Báo cáo, tổng quan và triển khai | Xuất bảng tính theo đợt; hiển thị tiến độ đề tài, nhóm, báo cáo và chấm điểm; chuẩn bị cấu hình chạy thật, lưu tệp và hướng dẫn cài đặt. | Có ít nhất một báo cáo xuất được, một trang tổng quan từ dữ liệu thật và một địa chỉ ứng dụng dùng để minh họa. |

Ngô Đức Thọ phải tự chuẩn bị tài khoản từng vai trò, dữ liệu cho trang tổng quan và báo cáo, địa chỉ thư thử nghiệm và môi trường chạy không chứa mật khẩu thật.

---

## 8. Dương Chấn Hưng — Bảo mật, hội đồng, điểm và công bố

| Mã | Chức năng | Cần làm | Kết quả cần đạt |
| --- | --- | --- | --- |
| HUNG-01 | Đăng nhập, đăng xuất và phân quyền | Kiểm tra mã hóa mật khẩu, bảo vệ đường dẫn, quyền tại phần xử lý nghiệp vụ và chống thay mã dữ liệu trên yêu cầu; dùng giá trị vai trò dùng chung thay cho chữ lặp lại. | Người dùng chỉ truy cập và thay đổi dữ liệu đúng vai trò, kể cả khi gửi yêu cầu trực tiếp. |
| HUNG-02 | Phân công người chấm | Cho Trưởng khoa phân công người chấm theo loại đợt; loại trừ toàn bộ giảng viên hướng dẫn; kiểm tra lại trước khi nhận điểm. | Giảng viên hướng dẫn không thể được phân công hoặc tự gửi điểm cho đề tài mình hướng dẫn. |
| HUNG-03 | Hội đồng khóa luận | Tạo hội đồng gồm 03–05 giảng viên khác nhau, đúng một Chủ tịch và một Thư ký; không có giảng viên hướng dẫn trong hội đồng. | Hội đồng sai số lượng, trùng thành viên, thiếu vai trò hoặc có xung đột đều bị từ chối. |
| HUNG-04 | Chấm điểm | Chỉ mở chấm cho đăng ký đã duyệt và có báo cáo; cho lưu nháp, gửi điểm, nhập nhận xét và kiểm tra điểm từ 0 đến 10. | Người được phân công chấm đúng đề tài; điểm đã gửi bị khóa theo quy định. |
| HUNG-05 | Tính và chốt kết quả | Chỉ tính trung bình cộng khi tất cả người chấm bắt buộc đã gửi điểm; làm tròn hai chữ số; riêng khóa luận chỉ Chủ tịch được chốt. | Không tạo kết quả thiếu điểm và kết quả cuối khớp với các điểm thành phần. |
| HUNG-06 | Công bố và thu hồi | Chỉ Trưởng khoa được công bố khi mọi kết quả hợp lệ; thu hồi phải có lý do, người thực hiện và thời điểm; phát thông tin gửi thư sau khi lưu thành công. | Sinh viên chỉ thấy điểm sau công bố; thu hồi có lịch sử; lỗi gửi thư không làm quay lại giao dịch công bố. |
| HUNG-07 | Tích hợp toàn bộ quy trình | Kiểm tra luồng từ đợt, đề tài, nhóm, báo cáo đến chấm và công bố; bổ sung thông báo lỗi cho các trường hợp thiếu điều kiện. | Quy trình chạy liền mạch và không thể bỏ qua bước bằng cách gọi thẳng đường dẫn hoặc yêu cầu thay đổi dữ liệu. |

Dương Chấn Hưng phải tự chuẩn bị trường hợp phân công hợp lệ, xung đột giảng viên hướng dẫn, điểm nháp, điểm đã gửi, thiếu điểm và đợt đủ điều kiện công bố.

---

## 9. Điểm nối giữa các phần chức năng

1. Phạm Minh Trí cung cấp quy tắc giai đoạn để phần đề tài, nhóm và báo cáo khóa thao tác đúng thời gian.
2. Trần Nhựt Tiến bảo đảm chỉ đề tài đã duyệt và được công bố mới xuất hiện cho Huỳnh Thôn xử lý đăng ký nhóm.
3. Huỳnh Thôn cung cấp đăng ký đã duyệt và báo cáo hiện hành để Dương Chấn Hưng mở chấm điểm.
4. Dương Chấn Hưng lưu công bố thành công rồi mới chuyển thông tin để Ngô Đức Thọ gửi thư.
5. Khi thay đổi dữ liệu dùng chung, người phụ trách phần tạo dữ liệu phải thông báo cấu trúc và điều kiện sử dụng cho người phụ trách phần nhận dữ liệu.

---

## 10. Thứ tự thực hiện

1. Chốt trạng thái và dữ liệu của đợt, đề tài, nhóm, báo cáo, điểm và công bố.
2. Hoàn thiện đợt hai giai đoạn và các quy tắc khóa theo thời gian.
3. Hoàn thiện đề tài, nhóm 03–05 sinh viên và đăng ký đề tài.
4. Hoàn thiện người chấm độc lập, hội đồng, tính điểm và công bố.
5. Hoàn thiện tra cứu điểm và gửi thư cho sinh viên.
6. Sửa giao diện, tìm kiếm, xuất báo cáo, trang tổng quan và triển khai.
7. Chuẩn bị dữ liệu, ảnh minh chứng, báo cáo và luyện bảo vệ.

---

## 11. Phân công nội dung báo cáo

| Nội dung | Người thực hiện |
| --- | --- |
| Giới thiệu, mục tiêu, tổng hợp và định dạng báo cáo | Ngô Đức Thọ |
| Yêu cầu chức năng, đợt đăng ký và quy tắc thời gian | Phạm Minh Trí |
| Đề tài, giảng viên hướng dẫn và nghiệp vụ giảng viên | Trần Nhựt Tiến |
| Nhóm sinh viên, nộp báo cáo và tra cứu kết quả | Huỳnh Thôn |
| Cơ sở dữ liệu, bảo mật, chấm điểm và công bố | Dương Chấn Hưng |
| Gửi thư, xuất báo cáo, trang tổng quan và triển khai | Ngô Đức Thọ |
| Trường hợp kiểm tra và ảnh minh chứng | Mỗi thành viên tự làm phần chức năng của mình |

Mỗi thành viên tự viết và tự giải thích phần được phân công. Nhóm trưởng chỉ ghép các phần thành báo cáo chung.

---

## 12. Kịch bản minh họa

| Thứ tự | Người trình bày | Nội dung | Thời lượng |
| --- | --- | --- | --- |
| 1 | Ngô Đức Thọ | Giới thiệu hệ thống, vai trò và trang chủ | 1–2 phút |
| 2 | Phạm Minh Trí | Tạo đợt, hai giai đoạn và khóa theo thời gian | 2 phút |
| 3 | Trần Nhựt Tiến | Đề xuất, duyệt đề tài và quản lý giảng viên hướng dẫn | 2–3 phút |
| 4 | Huỳnh Thôn | Tạo nhóm 03–05 người, đăng ký, nộp báo cáo và xem điểm | 3 phút |
| 5 | Dương Chấn Hưng | Phân công chấm, hội đồng, tính điểm và công bố | 3 phút |
| 6 | Ngô Đức Thọ | Gửi thư, xuất báo cáo, trang tổng quan và ứng dụng đã triển khai | 2 phút |

Các trường hợp lỗi bắt buộc phải minh họa được: nhóm thiếu hoặc vượt số người, sinh viên tham gia hai nhóm, giảng viên hướng dẫn tự chấm, chấm khi chưa có báo cáo, công bố khi thiếu điểm và sinh viên xem điểm nhóm khác.

---

## 13. Điều kiện hoàn thành chung

Một chức năng chỉ được xem là hoàn thành khi:

- Đúng yêu cầu nghiệp vụ và tiêu chí chấm.
- Kiểm tra quyền và dữ liệu tại phía máy chủ, không chỉ trên giao diện.
- Có thông báo tiếng Việt rõ ràng khi dữ liệu sai hoặc chưa có dữ liệu.
- Không làm lộ dữ liệu khi thay mã trên đường dẫn hoặc gửi yêu cầu trực tiếp.
- Không ghi cứng mật khẩu, địa chỉ môi trường hoặc giá trị nghiệp vụ dùng lặp lại.
- Tuân thủ cấu trúc điều khiển, xử lý nghiệp vụ và truy cập dữ liệu của project.
- Giao diện nhất quán với `DESIGN.md` và dùng được trên màn hình hẹp.
- Có dữ liệu minh họa cho trường hợp đúng và ít nhất một trường hợp lỗi.
- Có ảnh đưa vào báo cáo và người phụ trách tự giải thích được source.

Quy ước code và hướng dẫn chi tiết nằm trong [Hướng dẫn phát triển project](huong-dan-phat-trien.md).

---

## 14. Đối chiếu với tiêu chí chấm

| Yêu cầu | Người chịu trách nhiệm | Minh chứng chính |
| --- | --- | --- |
| Quản lý đề tài | Trần Nhựt Tiến | Tạo, sửa, duyệt, từ chối và danh sách đề tài |
| Quản lý giảng viên, sinh viên, bộ môn và thông báo | Ngô Đức Thọ | Các màn hình quản lý và kiểm tra dữ liệu đầu vào |
| Quản lý đợt đăng ký | Phạm Minh Trí | Biểu mẫu đợt, mốc thời gian và trạng thái hiện tại |
| Đăng nhập, đăng xuất và bảo mật | Dương Chấn Hưng | Phân quyền theo vai trò và thử yêu cầu trái phép |
| Đợt gồm hai giai đoạn | Phạm Minh Trí | Đề tài được duyệt trước khi sinh viên được đăng ký |
| Nhóm 03–05 sinh viên, một nhóm trưởng, không trùng nhóm | Huỳnh Thôn | Tạo nhóm và các trường hợp bị từ chối |
| Nhóm trưởng nộp báo cáo | Huỳnh Thôn | Nộp tệp và lịch sử các lần nộp |
| Hội đồng 03–05 giảng viên | Dương Chấn Hưng | Tạo hội đồng đúng số lượng và vai trò |
| Giảng viên hướng dẫn không tự chấm | Dương Chấn Hưng | Từ chối khi phân công và khi gửi điểm |
| Tính điểm trung bình cộng | Dương Chấn Hưng | Điểm thành phần và kết quả cuối |
| Công bố kết quả | Dương Chấn Hưng | Công bố, thu hồi và lịch sử thực hiện |
| Gửi thư cho từng sinh viên | Ngô Đức Thọ | Thư nhận được và bản ghi trường hợp gửi lỗi |
| Sinh viên chỉ tra cứu điểm đợt đã tham gia | Huỳnh Thôn | Danh sách đợt của sinh viên và thử xem nhóm khác |
| Xuất báo cáo và trang tổng quan | Ngô Đức Thọ | Tệp bảng tính và số liệu trực quan theo đợt |
| Tìm kiếm nâng cao | Trần Nhựt Tiến | Lọc đề tài theo nhiều điều kiện |
| Ứng dụng đã triển khai | Ngô Đức Thọ | Địa chỉ truy cập và hướng dẫn cấu hình |

---

## 15. Bàn giao của mỗi thành viên

Mỗi thành viên bàn giao đúng phần của mình:

1. Source và thay đổi cơ sở dữ liệu liên quan.
2. Danh sách màn hình, đường dẫn và quyền truy cập.
3. Dữ liệu minh họa trường hợp đúng và sai.
4. Ảnh minh chứng và nội dung báo cáo.
5. Câu hỏi, câu trả lời bảo vệ và kịch bản trình bày không quá ba phút.
6. Ghi chú cấu hình cần thiết để chạy chức năng.

Khi thay đổi người phụ trách hoặc nghiệp vụ, phải cập nhật lại tài liệu này để bảng phân công và báo cáo luôn thống nhất.
