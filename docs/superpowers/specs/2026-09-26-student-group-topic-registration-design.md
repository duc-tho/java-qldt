# Thiết kế phân hệ nhóm sinh viên và đăng ký đề tài

## Mục tiêu

Cho phép sinh viên tham gia thực hiện đề tài theo nhóm trong một đợt đăng ký. Nhóm trưởng quản lý nhóm và đăng ký đề tài; giảng viên hướng dẫn duyệt đăng ký; trưởng khoa duyệt các đề tài được công bố.

## Phạm vi

- Quản lý nhóm sinh viên trong một đợt đăng ký.
- Sinh viên chọn đề tài đã được trưởng khoa duyệt.
- Giảng viên hướng dẫn duyệt hoặc từ chối đăng ký của các nhóm chọn đề tài mình hướng dẫn.
- Quản lý trạng thái đề tài `PROPOSED` và `APPROVED`.

Không thuộc phạm vi này: nộp báo cáo, phản biện, chấm điểm và xử lý ngoại lệ sau khi hết hạn đăng ký.

## Quy tắc nghiệp vụ

1. Mỗi nhóm thuộc đúng một đợt đăng ký và có từ một đến ba thành viên.
2. Mỗi nhóm có đúng một nhóm trưởng.
3. Một sinh viên chỉ được thuộc một nhóm trong cùng một đợt đăng ký.
4. Nhóm trưởng là người tạo nhóm và được thêm hoặc xóa thành viên bằng mã sinh viên.
5. Thành viên không được tự rời nhóm. Nhóm trưởng chỉ có thể bị xóa sau khi đã chuyển vai trò nhóm trưởng cho thành viên khác.
6. Nhóm trưởng có thể đổi nhóm trưởng trong thời hạn đăng ký.
7. Nhóm có thể chưa chọn đề tài. Khi đã chọn, nhóm chỉ có một đăng ký đề tài hiện thời.
8. Nhiều nhóm có thể đăng ký cùng một đề tài.
9. Sinh viên chỉ nhìn thấy các đề tài `APPROVED` của đúng đợt đăng ký.
10. Mọi thay đổi đề tài, thành viên hoặc nhóm trưởng trong thời hạn sẽ đưa đăng ký về trạng thái `PENDING`.
11. Khi hết thời hạn đăng ký sinh viên, không tài khoản nào được thay đổi nhóm, thành viên, nhóm trưởng hoặc đề tài qua hệ thống.
12. Ở giai đoạn nộp báo cáo sau này, chỉ nhóm trưởng được nộp báo cáo.

## Vai trò và quyền

| Vai trò | Quyền |
|---|---|
| Sinh viên chưa có nhóm | Tạo nhóm trong đợt còn mở. |
| Nhóm trưởng | Thêm/xóa thành viên, chuyển nhóm trưởng, chọn/đổi đề tài trong hạn; xem trạng thái và lý do từ chối. |
| Thành viên | Chỉ xem nhóm, đề tài và trạng thái đăng ký. |
| Giảng viên hướng dẫn | Xem, duyệt hoặc từ chối các đăng ký của nhóm chọn đề tài do mình hướng dẫn trong hạn. |
| Trưởng khoa | Duyệt đề tài được giảng viên đề xuất. |

## Luồng sinh viên

1. Sinh viên mở mục **Nhóm đề tài** của đợt đăng ký.
2. Nếu chưa thuộc nhóm, hệ thống hiển thị nút **Tạo nhóm**. Người tạo trở thành nhóm trưởng và là thành viên đầu tiên.
3. Nhóm trưởng mở chi tiết nhóm để quản lý tối đa ba thành viên và chuyển nhóm trưởng khi cần.
4. Nhóm trưởng mở danh sách đề tài đã duyệt, tìm theo tên hoặc lọc theo bộ môn, rồi chọn một đề tài.
5. Việc chọn đề tài tự động tạo hoặc cập nhật đăng ký ở trạng thái `PENDING`; không có nút gửi đăng ký riêng.
6. Giảng viên hướng dẫn duyệt hoặc từ chối. Khi từ chối, giảng viên phải nhập lý do.
7. Trong thời hạn, nhóm trưởng có thể chỉnh sửa và đăng ký được trả về `PENDING`. Sau hạn, tất cả thao tác thay đổi bị khóa.

## Giao diện

### Trang nhóm đề tài

- Sinh viên chưa có nhóm: trạng thái trống và nút **Tạo nhóm**.
- Sinh viên đã có nhóm: điều hướng đến trang chi tiết nhóm.

### Trang chi tiết nhóm

Gồm ba khối:

1. **Thông tin và trạng thái:** tên nhóm, đợt đăng ký, hạn đăng ký, trạng thái đăng ký và lý do từ chối nếu có.
2. **Thành viên:** danh sách thành viên, nhãn nhóm trưởng. Nhóm trưởng có trường nhập mã sinh viên, thao tác xóa thành viên và chuyển nhóm trưởng khi còn hạn.
3. **Đề tài:** đề tài đã chọn cùng bộ môn, mô tả và giảng viên hướng dẫn. Nhóm trưởng có nút **Chọn đề tài** hoặc **Đổi đề tài** khi còn hạn.

### Màn hình chọn đề tài

- Chỉ liệt kê đề tài `APPROVED` thuộc đợt hiện tại.
- Có tìm kiếm theo tên và lọc theo bộ môn.
- Một đề tài không bị ẩn khi đã có nhóm khác chọn.
- Xác nhận lựa chọn sẽ cập nhật đăng ký sang `PENDING`.

### Màn hình giảng viên duyệt đăng ký

- Danh sách nhóm đã chọn các đề tài do giảng viên hướng dẫn.
- Trang chi tiết hiển thị đề tài và thành viên nhóm.
- Có thao tác **Duyệt** và **Từ chối**; từ chối yêu cầu nhập lý do.

## Mô hình dữ liệu

| Thực thể | Thuộc tính chính | Ràng buộc |
|---|---|---|
| `StudentGroup` | `registrationPeriod`, `name` | Thuộc một đợt đăng ký. |
| `GroupMember` | `studentGroup`, `student`, `isLeader` | Một SV thuộc tối đa một nhóm mỗi đợt; đúng một trưởng nhóm; tối đa ba thành viên. |
| `TopicRegistration` | `studentGroup`, `topic`, `status`, `rejectionReason`, `reviewedBy`, `reviewedAt` | Mỗi nhóm có tối đa một đăng ký hiện thời; một đề tài có nhiều đăng ký nhóm. |
| `Topic` (mở rộng) | `status` | Chỉ `APPROVED` được SV chọn. |

## Trạng thái

Trạng thái đề tài:

```text
PROPOSED -> APPROVED
```

Trạng thái đăng ký nhóm:

```text
(chưa chọn đề tài) -> PENDING -> APPROVED
                             -> REJECTED
```

- Chọn đề tài hoặc thay đổi nhóm khi còn hạn: `PENDING`.
- Giảng viên hướng dẫn có thể chuyển `PENDING` thành `APPROVED` hoặc `REJECTED`.
- Mọi thay đổi hợp lệ trong hạn sau khi đã duyệt hoặc từ chối sẽ xóa kết quả xét cũ và trở về `PENDING`.

## Kiểm thử cần có

- Không thể thêm thành viên thứ tư hoặc sinh viên đã thuộc nhóm khác trong cùng đợt.
- Không thể tạo nhóm mới khi sinh viên đã là thành viên nhóm trong đợt.
- Luôn còn đúng một nhóm trưởng và không thể xóa nhóm trưởng khi chưa chuyển quyền.
- Chỉ đề tài `APPROVED` của đúng đợt được chọn.
- Nhiều nhóm có thể chọn cùng một đề tài.
- Chọn hoặc chỉnh sửa hợp lệ trong hạn đưa đăng ký về `PENDING`.
- Chỉ giảng viên hướng dẫn của đề tài được duyệt/từ chối đăng ký tương ứng.
- Mọi thao tác sửa bị từ chối khi hết hạn đăng ký sinh viên.
