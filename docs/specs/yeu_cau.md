# Yêu cầu nghiệp vụ chốt

Tài liệu này là nguồn thống nhất cho phạm vi nghiệp vụ, các loại đợt đăng ký và tiến trình xử lý đề tài. Khi có khác biệt với các mô tả trước đó, tài liệu này được ưu tiên áp dụng.

## 1. Phạm vi hệ thống

Hệ thống quản lý đề tài sinh viên theo bốn loại đợt:

| Loại đợt | Mã loại | Hạn nộp bài | Hạn chấm/chốt điểm về khoa | Ngày báo cáo hội đồng | Hội đồng chấm |
| --- | --- | --- | --- | --- | --- |
| Môn học | `MON_HOC` | Bắt buộc | Không áp dụng | Không áp dụng | Không áp dụng |
| Nghiên cứu khoa học | `NCKH` | Bắt buộc | Không áp dụng | Không áp dụng | Không áp dụng |
| Tiểu luận chuyên ngành | `TLCN` | Bắt buộc | Hạn GVPB nộp điểm | Không áp dụng | Không áp dụng |
| Khóa luận tốt nghiệp | `KLTN` | Bắt buộc | Hạn chốt điểm hội đồng | Bắt buộc | Bắt buộc |

Hội đồng chỉ áp dụng cho `KLTN`. Một hội đồng có từ 03 đến 05 giảng viên, gồm đúng một Chủ tịch và đúng một Thư ký; các thành viên còn lại có vai trò Thành viên.

## 2. Tạo đợt đăng ký

Trưởng khoa tạo và quản lý đợt đăng ký. Mỗi đợt luôn có các trường chung:

- Tên đợt và loại đợt.
- Thời gian bắt đầu/kết thúc để giảng viên đề xuất đề tài.
- Thời điểm công bố đề tài.
- Thời gian bắt đầu/kết thúc để sinh viên đăng ký đề tài.
- Hạn nộp bài/báo cáo của sinh viên.

Trường hiển thị theo loại đợt:

- `TLCN`: hiển thị và bắt buộc nhập hạn GVPB nộp điểm về khoa.
- `KLTN`: hiển thị và bắt buộc nhập hạn chốt điểm hội đồng cùng ngày báo cáo hội đồng.
- `MON_HOC` và `NCKH`: không hiển thị hạn nộp điểm về khoa và ngày báo cáo hội đồng.

Thứ tự thời gian tối thiểu phải thỏa mãn:

`GV đăng ký kết thúc < công bố đề tài < SV đăng ký bắt đầu <= SV đăng ký kết thúc < hạn nộp bài`.

Với `TLCN`, hạn nộp bài phải không muộn hơn hạn GVPB nộp điểm về khoa. Với `KLTN`, ngày báo cáo hội đồng phải sau hạn nộp bài và hạn chốt điểm hội đồng phải không sớm hơn ngày báo cáo hội đồng.

## 3. Timeline nghiệp vụ

### Giai đoạn 1 — Đề xuất và công bố đề tài

Diễn ra trong khoảng đăng ký của giảng viên.

- Giảng viên tạo, chỉnh sửa và gửi đề xuất đề tài trong đợt.
- Trưởng khoa duyệt hoặc từ chối từng đề tài; mọi từ chối phải có lý do.
- Chỉ đề tài đã được duyệt mới được công bố tại thời điểm công bố đề tài.
- Khi đề tài được công bố, hệ thống chuyển sang giai đoạn sinh viên đăng ký.

### Giai đoạn 2 — Nhóm sinh viên đăng ký đề tài

Diễn ra trong khoảng đăng ký của sinh viên.

- Sinh viên tạo một nhóm thuộc một đợt; mỗi sinh viên chỉ thuộc một nhóm trong cùng đợt.
- Nhóm trưởng quản lý thành viên của nhóm và chọn một đề tài đã công bố.
- Nhóm gửi yêu cầu đăng ký đề tài cho giảng viên phụ trách/hướng dẫn.
- Giảng viên duyệt hoặc từ chối yêu cầu; mọi từ chối phải có lý do.
- Khi hết hạn đăng ký, không cho tạo nhóm, thay đổi thành viên, chọn đề tài hoặc gửi yêu cầu duyệt mới.

### Giai đoạn 3 — Thực hiện và nộp bài

Diễn ra sau khi giai đoạn 2 kết thúc và trước hạn nộp bài.

- Nhóm có đăng ký đề tài được duyệt thực hiện đề tài dưới sự hướng dẫn của giảng viên.
- Nhóm trưởng nộp bài/báo cáo lên hệ thống trước hạn nộp bài của đợt.
- Hệ thống khóa thao tác nộp hoặc thay thế bài sau hạn nộp bài.
- Với `TLCN`, sau khi kết thúc đăng ký sinh viên và khi nhóm bắt đầu thực hiện đề tài, Trưởng khoa phân công một GVPB cho đề tài.
- Với `KLTN`, sau khi kết thúc đăng ký sinh viên và trước ngày báo cáo hội đồng, Trưởng khoa thành lập hội đồng và phân công đề tài cho hội đồng phù hợp.
- Giảng viên hướng dẫn của một đề tài không được là thành viên hội đồng chấm đề tài đó.

### Giai đoạn 4 — Chấm điểm và công bố kết quả

- Việc chấm điểm chỉ diễn ra sau khi bài/báo cáo đã được nộp.
- Với `MON_HOC` và `NCKH`, toàn bộ giảng viên hướng dẫn/phụ trách chấm điểm; điểm cuối là trung bình cộng điểm của các giảng viên này.
- Với `TLCN`, GVPB chấm điểm và nộp điểm trước hạn GVPB nộp điểm về khoa.
- Với `KLTN`, thành viên hội đồng chấm các đề tài được phân công; hệ thống tính điểm cuối cùng bằng trung bình cộng điểm của các thành viên hội đồng. Chủ tịch hội đồng chốt kết quả cuối cùng.
- Thành viên hội đồng KLTN không được xem là GVPB và KLTN không có GVPB riêng.
- Trưởng khoa công bố kết quả sau khi kết quả của các đề tài liên quan đã được hoàn tất. Sau khi công bố, sinh viên chỉ xem được kết quả của đề tài/nhóm mình tham gia.

## 4. Phân quyền chính

| Vai trò | Trách nhiệm |
| --- | --- |
| Trưởng khoa | Tạo và quản lý đợt; duyệt/từ chối đề tài; phân công GVPB cho TLCN; thành lập và phân công hội đồng KLTN; công bố kết quả. |
| Giảng viên | Đề xuất đề tài; hướng dẫn/phụ trách đề tài; duyệt hoặc từ chối yêu cầu đăng ký đề tài; chấm theo vai trò được phân công. |
| Nhóm trưởng | Tạo nhóm, quản lý thành viên, chọn đề tài, gửi yêu cầu duyệt và nộp bài/báo cáo. |
| Sinh viên | Tham gia một nhóm trong đợt và xem kết quả của nhóm sau khi được công bố. |

## 5. Quy tắc ngoài phạm vi

- Không tạo hội đồng cho `MON_HOC`, `NCKH` hoặc `TLCN`.
