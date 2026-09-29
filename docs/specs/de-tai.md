# Đề tài

Hệ thống quản lý đề tài sinh viên
Hệ thống quản lý **đề tài của môn học:**

- **Nghiên cứu khoa học (NCKH)**
- **Tiểu luận chuyên ngành (TLCN)**
- **Khóa luận tốt nghiệp (KLTN)**

Xây dựng nhằm tự động hóa toàn bộ quy trình quản lý đề tài trong khoa CNTT, bao gồm các giai đoạn sau:

1. Tạo đợt đăng ký
2. GV và SV đăng ký đề tài
3. Phê duyệt đề tài
4. Phân công GVHD, GVPB, GV chấm điểm
5. Khoa CNTT công bố kết quả.

Hệ thống cho phép quản lý các đợt đăng ký đề tài theo từng giai đoạn.

Trong mỗi đợt đăng ký:

- Hệ thống mở cho phép đăng ký đề tài theo đúng thời gian quy định.
- Các đợt đăng ký được tổ chức theo hai giai đoạn riêng biệt.
  - Giai đoạn thứ nhất nhằm xây dựng và công bố danh sách đề tài.
  - Giai đoạn thứ hai cho phép các nhóm SV đăng ký thực hiện một đề tài cụ thể trong danh sách đề tài đã được công bố.

Trưởng khoa sẽ là người tạo đợt đăng ký bao gồm các thông tin:

- Tên đợt đăng ký
- Loại (đợt này dành cho môn học/NCKH/TLCN/KLTN)
- Thời gian bắt đầu và kết thúc cho GV đăng ký
- Thời gian bắt đầu và kết thúc cho SV đăng ký
- Ngày hạn chót GVPB phải nộp điểm về Khoa (chỉ thiết lập khi đợt hiện tại là loại TLCN hoặc KLTN)
- Ngày báo cáo hội đồng (chỉ thiết lập khi đợt hiện tại là KLTN).

Danh sách đề tài được quản lý theo từng bộ môn.

Mỗi đề tài thuộc một bộ môn cụ thể, có nội dung rõ ràng và chỉ được hướng dẫn bởi ít nhất một GV và tối đa hai GV.

1. SV tham gia thực hiện đề tài theo hình thức nhóm. Mỗi nhóm có tối đa 03 thành viên, trong đó
   có một nhóm trưởng.
2. Mỗi SV chỉ được tham gia duy nhất một nhóm trong suốt quá trình thực hiện đề tài.
3. Mỗi nhóm chỉ được đăng ký duy nhất một đề tài từ danh sách đề tài đã được công bố.
4. Sau khi đăng ký và được chấp thuận, nhóm tiến hành thực hiện đề tài theo sự hướng dẫn của GV
   được phân công.
5. Việc nộp báo cáo đề tài chỉ được thực hiện bởi nhóm trưởng.

Sau khi hoàn thành đề tài, nhóm SV phải tham gia phản biện theo kế hoạch.

1. Hệ thống quản lý việc thành lập các hội đồng phản biện, mỗi hội đồng gồm từ 03 đến 05 GV bao gồm một chủ tịch hội đồng và một thư ký hội đồng.
   1. Chủ tịch hội đồng có trách nhiệm tổng hợp các đánh giá và điểm số của các thành viên trong hội đồng để đưa ra kết quả phản biện cuối cùng cho đề tài.
   2. Điểm cuối cùng của đề tài được tính bằng trung bình cộng các điểm thành phần.
   3. Một GV có thể tham gia chấm nhiều đề tài khác nhau, nhưng không được chấm đề tài mà mình đang hướng dẫn.

Sau khi quá trình phản biện và chấm điểm hoàn tất, kết quả đánh giá được công bố trên hệ thống.

1. Sinh viên có thể xem điểm và kết quả đánh giá của đề tài mà mình tham gia thực hiện.

Ngoài các chức năng liên quan đến đề tài, hệ thống còn hỗ trợ quản lý tài khoản người dùng và
đăng tải các thông báo chính thức của nhà trường và khoa để cung cấp thông tin kịp thời cho
người sử dụng cũng như có các giải pháp về phân quyền, bảo mật dữ liệu.
