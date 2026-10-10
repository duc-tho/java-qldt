# Đặc tả chức năng, route và phân quyền

Tài liệu này quy định route, màn hình, nội dung hiển thị và quyền thao tác của hệ thống. Tài liệu dùng các route tiếng Việt không dấu, viết bằng kebab-case. Một tài khoản có **đúng một** vai trò: `ADMIN`, `HEAD_OF_FACULTY`, `LECTURER` hoặc `STUDENT`.

`ADMIN` có toàn quyền: được truy cập và thực hiện mọi thao tác của các vai trò khác, đồng thời có quyền quản trị tài khoản. Màn hình tạo/sửa tài khoản phải chọn vai trò bằng **radio hoặc select một lựa chọn**; không dùng checkbox hay chọn nhiều vai trò.

## 1. Quy ước truy cập và điều hướng

- `/` là trang chủ giới thiệu công khai; không tự chuyển đến trang đăng nhập.
- `/dang-nhap` là trang đăng nhập. Người đã đăng nhập truy cập route này được chuyển đến dashboard theo vai trò.
- `/dang-xuat` là endpoint `POST` đăng xuất.
- Route chuyên biệt phải dùng tiền tố theo người dùng: `/quan-tri`, `/truong-khoa`, `/giang-vien`, `/sinh-vien`.
- Route dùng chung không cần tiền tố vai trò: `/thong-bao`, `/ho-so`, `/doi-mat-khau`.
- Người không đăng nhập vào route cần xác thực được chuyển đến `/dang-nhap`.
- Người đã đăng nhập nhưng không có quyền truy cập route nhận trang `403`.
- `ADMIN` truy cập route chuyên biệt của các vai trò khác để thực hiện quyền toàn quyền; sidebar của Admin hiển thị cả các mục quản trị và nghiệp vụ.

## 2. Bảng quyền tổng hợp

Ký hiệu: **Xem** = xem dữ liệu; **QL** = tạo/sửa/xóa hoặc thực hiện quy trình; **Chấm** = nhập/chốt điểm; `—` = không được truy cập.

| Nhóm chức năng | Admin | Trưởng khoa | Giảng viên | Sinh viên |
| --- | --- | --- | --- | --- |
| Trang chủ, đăng nhập | Xem | Xem | Xem | Xem |
| Dashboard theo vai trò | Xem | Xem | Xem | Xem |
| Hồ sơ, đổi mật khẩu | QL bản thân | QL bản thân | QL bản thân | QL bản thân |
| Tài khoản người dùng | QL | — | — | — |
| Thông báo nội bộ | QL | QL | Xem theo đối tượng | Xem theo đối tượng |
| Đợt đăng ký | QL | QL | Xem | Xem đợt đang tham gia |
| Duyệt đề tài | QL | QL | — | — |
| Đề xuất đề tài | QL | Xem | QL đề tài của mình | — |
| Nhóm sinh viên | Xem/QL thay mặt | Xem | Xem nhóm thuộc đề tài mình phụ trách | QL nhóm của mình khi là trưởng nhóm; Xem nhóm của mình |
| Đăng ký đề tài | Xem/QL thay mặt | Xem | Duyệt/từ chối đề tài mình phụ trách | Tạo/rút/đổi khi là trưởng nhóm và còn thời hạn; Xem nhóm mình |
| Nộp bài/báo cáo | Xem | Xem | Xem đề tài mình phụ trách/chấm | Nộp/thay thế khi là trưởng nhóm và còn thời hạn; Xem nhóm mình |
| Phân công GVPB TLCN | QL | QL | Xem khi được phân công | — |
| Hội đồng KLTN | QL | QL | Xem/chấm khi là thành viên | — |
| Chấm điểm Môn học/NCKH | Chấm | — | Chấm khi là GV phụ trách/GVHD | — |
| Chấm điểm TLCN | Chấm | — | Chấm khi là GVPB được phân công | — |
| Chấm/chốt điểm KLTN | Chấm/chốt | — | Chấm khi là thành viên; chốt khi là Chủ tịch | — |
| Công bố/thu hồi kết quả | QL | QL | — | — |
| Kết quả | Xem | Xem | Xem đề tài liên quan | Xem kết quả nhóm mình sau công bố |

## 3. Dashboard

| Route | Dành cho | Nội dung hiển thị | Hành động chính |
| --- | --- | --- | --- |
| `/quan-tri/bang-dieu-khien` | Admin | Tổng số tài khoản theo vai trò; tài khoản mới/đang khóa; các đợt đang diễn ra; việc nghiệp vụ cần xử lý; thông báo gần đây | Đi đến quản lý tài khoản hoặc các khu vực nghiệp vụ tương ứng |
| `/truong-khoa/bang-dieu-khien` | Trưởng khoa, Admin | Đợt theo trạng thái; đề tài chờ duyệt; đăng ký đề tài chờ xử lý; TLCN chưa phân công GVPB; KLTN chưa có hội đồng/đề tài chưa phân công; kết quả chờ công bố; deadline sắp tới | Duyệt đề tài, mở đợt, phân công GVPB/hội đồng, công bố kết quả |
| `/giang-vien/bang-dieu-khien` | Giảng viên, Admin | Đề tài đã đề xuất; yêu cầu đăng ký chờ duyệt; nhóm/đề tài đang phụ trách; bài đã nộp; công việc chấm; deadline liên quan | Đề xuất đề tài, xử lý yêu cầu, chấm điểm |
| `/sinh-vien/bang-dieu-khien` | Sinh viên, Admin | Đợt sinh viên có thể tham gia; nhóm hiện tại; trạng thái đăng ký đề tài; hạn đăng ký/hạn nộp bài; trạng thái bài nộp; kết quả đã công bố | Tạo/mở nhóm, chọn đề tài, nộp bài, xem kết quả |

Dashboard chỉ hiển thị dữ liệu người dùng có quyền xem. Card không có dữ liệu không hiển thị; các card hành động hết hạn vẫn hiển thị trạng thái và deadline nhưng thay nút thao tác bằng nhãn “Đã hết hạn”.

## 4. Route và màn hình công khai/dùng chung

| Route | Màn hình và nội dung | Ai được xem | Hiện/ẩn và thao tác |
| --- | --- | --- | --- |
| `/` | Trang giới thiệu hệ thống, mục tiêu, các loại đợt, hướng dẫn truy cập và nút đăng nhập | Mọi người | Không hiển thị dữ liệu nghiệp vụ nội bộ |
| `/dang-nhap` | Form email/mã đăng nhập và mật khẩu | Khách | Người đã đăng nhập được chuyển dashboard theo vai trò |
| `/ho-so` | Họ tên, email, số điện thoại, mã người dùng, bộ môn, vai trò | Mọi người đã đăng nhập | Chỉ sửa email, số điện thoại và mật khẩu; ẩn chức năng sửa mã người dùng, bộ môn, vai trò |
| `/doi-mat-khau` | Form mật khẩu hiện tại, mật khẩu mới, xác nhận mật khẩu | Mọi người đã đăng nhập | Chỉ đổi mật khẩu của chính mình |
| `/thong-bao` | Danh sách thông báo nội bộ được phép xem, tìm kiếm và lọc | Mọi người đã đăng nhập | Chỉ hiển thị thông báo có đối tượng chứa vai trò hiện tại; Admin/Trưởng khoa thấy nút tạo thông báo |
| `/thong-bao/{id}` | Nội dung, thời điểm và đối tượng thông báo | Người có quyền nhận thông báo; Admin/Trưởng khoa | Nút sửa/thu hồi chỉ hiện với Admin/Trưởng khoa |
| `/thong-bao/tao-moi` | Form tiêu đề, nội dung, đối tượng vai trò, trạng thái công bố | Admin, Trưởng khoa | Chọn một hoặc nhiều đối tượng nhận; không hiển thị cho Giảng viên/Sinh viên |

## 5. Quản trị tài khoản

| Route | Màn hình và nội dung | Ai được xem/thao tác | Hiện/ẩn và thao tác |
| --- | --- | --- | --- |
| `/quan-tri/tai-khoan` | Bảng tài khoản: mã, họ tên, email, bộ môn, vai trò, trạng thái | Admin | Tạo mới, tìm kiếm, lọc, mở chi tiết, khóa/mở khóa |
| `/quan-tri/tai-khoan/tao-moi` | Form tài khoản: mã, họ tên, email, số điện thoại, bộ môn, mật khẩu, vai trò | Admin | Vai trò là radio/select một lựa chọn bắt buộc; bộ môn chỉ hiện khi vai trò là Giảng viên hoặc Sinh viên |
| `/quan-tri/tai-khoan/{id}` | Thông tin tài khoản, trạng thái và lịch sử cơ bản | Admin | Sửa dữ liệu quản trị, đặt lại mật khẩu, khóa/mở khóa; không được tự xóa cứng tài khoản |

## 6. Chức năng Trưởng khoa

### 6.1. Đợt đăng ký

| Route | Màn hình và nội dung | Hiện/ẩn và thao tác |
| --- | --- | --- |
| `/truong-khoa/dot-dang-ky` | Danh sách đợt: tên, loại, mốc thời gian, trạng thái hiện tại | Nút tạo mới; lọc theo loại/trạng thái; Admin có cùng quyền |
| `/truong-khoa/dot-dang-ky/tao-moi` | Form tạo đợt | Luôn hiện trường chung và hạn nộp bài; chỉ `TLCN` hiện hạn GVPB nộp điểm; chỉ `KLTN` hiện hạn chốt điểm hội đồng và ngày báo cáo hội đồng |
| `/truong-khoa/dot-dang-ky/{id}` | Tổng quan đợt, timeline, số liệu đề tài/nhóm/bài nộp/kết quả | Hiện action theo trạng thái: sửa mốc, duyệt đề tài, xem đăng ký, phân công, công bố; action không hợp lệ theo timeline bị ẩn hoặc khóa kèm lý do |
| `/truong-khoa/dot-dang-ky/{id}/chinh-sua` | Form sửa thông tin và mốc thời gian đợt | Không đổi loại đợt khi đợt đã phát sinh đăng ký; lưu thay đổi vào lịch sử |

### 6.2. Đề tài và đăng ký đề tài

| Route | Màn hình và nội dung | Hiện/ẩn và thao tác |
| --- | --- | --- |
| `/truong-khoa/dot-dang-ky/{id}/de-tai` | Danh sách đề tài của đợt: bộ môn, GVHD/phụ trách, trạng thái, số nhóm | Lọc trạng thái; mở chi tiết; duyệt/từ chối khi đề tài chờ duyệt |
| `/truong-khoa/dot-dang-ky/{id}/de-tai/{deTaiId}` | Chi tiết đề tài, lịch sử duyệt/từ chối, GVHD/phụ trách, nhóm đã đăng ký | Duyệt hoặc từ chối bắt buộc nhập lý do; chỉ đề tài đã duyệt được công bố |
| `/truong-khoa/dot-dang-ky/{id}/dang-ky-de-tai` | Danh sách yêu cầu đăng ký của nhóm, trạng thái, giảng viên xử lý | Chỉ xem/giám sát; không thay giảng viên duyệt thay trừ khi Admin thực hiện quyền toàn quyền |

### 6.3. Phân công và kết quả

| Route | Màn hình và nội dung | Hiện/ẩn và thao tác |
| --- | --- | --- |
| `/truong-khoa/dot-dang-ky/{id}/phan-cong-gvpb` | Danh sách đề tài TLCN đã được duyệt, GVPB hiện tại, hạn nộp điểm | Chỉ có với `TLCN`, sau khi hết hạn đăng ký sinh viên; chọn một GVPB, không cho chọn GVHD của cùng đề tài |
| `/truong-khoa/dot-dang-ky/{id}/hoi-dong` | Danh sách hội đồng KLTN, thành viên, số đề tài được giao | Chỉ có với `KLTN`; nút thành lập hội đồng chỉ trước ngày báo cáo hội đồng |
| `/truong-khoa/dot-dang-ky/{id}/hoi-dong/tao-moi` | Form tên hội đồng, danh sách 3–5 giảng viên, Chủ tịch, Thư ký | Chỉ `KLTN`; Chủ tịch/Thư ký phải là thành viên khác nhau; không cho thêm GVHD của đề tài sẽ giao cho hội đồng |
| `/truong-khoa/dot-dang-ky/{id}/hoi-dong/{hoiDongId}` | Thành viên hội đồng và đề tài được phân công | Giao/bỏ giao đề tài trước ngày báo cáo; không hiển thị cho đợt không phải KLTN |
| `/truong-khoa/dot-dang-ky/{id}/ket-qua` | Danh sách kết quả, trạng thái hoàn tất/chốt/công bố | Chỉ công bố khi mọi kết quả bắt buộc đã hoàn tất; hiển thị nút công bố/thu hồi theo trạng thái |

## 7. Chức năng Giảng viên

| Route | Màn hình và nội dung | Hiện/ẩn và thao tác |
| --- | --- | --- |
| `/giang-vien/de-tai` | Danh sách đề tài do giảng viên đề xuất/phụ trách; trạng thái và đợt | Nút đề xuất chỉ hiện trong thời gian đăng ký giảng viên |
| `/giang-vien/de-tai/tao-moi` | Chọn đợt đang mở, tiêu đề, nội dung, bộ môn, số nhóm tối đa, GVHD/phụ trách | Chỉ chọn đợt còn mở và tối đa hai GVHD/phụ trách; không cho tạo ngoài thời hạn |
| `/giang-vien/de-tai/{id}` | Chi tiết đề tài, trạng thái duyệt, nhóm đăng ký, bài nộp | Chỉ chủ đề tài/GVHD được xem dữ liệu nhóm và bài nộp; sửa/gửi lại chỉ trước khi được duyệt hoặc theo trạng thái cho phép |
| `/giang-vien/yeu-cau-dang-ky` | Các yêu cầu nhóm đăng ký đề tài do mình phụ trách | Duyệt/từ chối; từ chối bắt buộc lý do; không hiển thị yêu cầu của đề tài khác |
| `/giang-vien/bai-nop` | Bài/báo cáo của đề tài mình phụ trách hoặc được phân công chấm | Chỉ xem, tải tệp và đi đến màn chấm khi đã có bài nộp |
| `/giang-vien/cham-diem` | Danh sách công việc chấm theo vai trò: GVHD/GV phụ trách, GVPB TLCN, thành viên hội đồng KLTN | Chỉ hiện đề tài được phân công; khóa nhập sau deadline tương ứng; Chủ tịch KLTN có thêm nút chốt khi đủ điểm thành viên |
| `/giang-vien/cham-diem/{dangKyId}` | Form điểm và nhận xét | Môn học/NCKH: GVHD/GV phụ trách chấm; TLCN: chỉ GVPB; KLTN: chỉ thành viên hội đồng, không có GVHD |

## 8. Chức năng Sinh viên

| Route | Màn hình và nội dung | Hiện/ẩn và thao tác |
| --- | --- | --- |
| `/sinh-vien/dot-dang-ky` | Đợt đang/chuẩn bị mở cho sinh viên, mốc thời gian, trạng thái tham gia | Chỉ hiện đợt trong phạm vi sinh viên được tham gia |
| `/sinh-vien/nhom` | Nhóm của sinh viên theo đợt, thành viên, trưởng nhóm, trạng thái khóa | Trưởng nhóm thấy action quản lý trong thời hạn; thành viên chỉ xem |
| `/sinh-vien/nhom/tao-moi` | Tạo nhóm trong một đợt đang mở | Chỉ hiện nếu sinh viên chưa thuộc nhóm của đợt; hết hạn thì ẩn/khóa |
| `/sinh-vien/nhom/{id}` | Thành viên nhóm, lời mời/yêu cầu tham gia, đề tài đã chọn, trạng thái đăng ký | Trưởng nhóm thêm/xóa/chuyển trưởng nhóm trong thời hạn và trước khi cấu trúc nhóm bị khóa; thành viên chỉ xem |
| `/sinh-vien/nhom/{id}/de-tai` | Đề tài đã công bố có thể chọn, bộ môn, GVHD, sức chứa | Trưởng nhóm chọn/rút/đổi đề tài khi còn hạn và đăng ký đang cho phép; thành viên chỉ xem |
| `/sinh-vien/nhom/{id}/nop-bai` | Lịch sử bài nộp, tệp đính kèm, thời điểm nộp, deadline | Chỉ trưởng nhóm nộp/thay thế trước deadline; các thành viên chỉ xem/tải tệp của nhóm |
| `/sinh-vien/ket-qua` | Kết quả và nhận xét của đề tài/nhóm sinh viên tham gia | Chỉ hiển thị sau khi Trưởng khoa công bố; không xem kết quả nhóm khác |

## 9. Điều kiện hiện/ẩn dùng chung

| Điều kiện | Thành phần UI phải xử lý |
| --- | --- |
| Chưa đến thời gian thao tác | Ẩn action hoặc hiển thị nút khóa cùng mốc mở; không chỉ dựa vào UI, server cũng phải từ chối |
| Đã hết hạn thao tác | Khóa action và hiển thị deadline đã qua; server không cho ghi dữ liệu |
| Không phải chủ sở hữu/liên quan | Không hiển thị dữ liệu chi tiết hoặc action; route trả `403`/`404` phù hợp |
| Đợt không phải TLCN | Ẩn toàn bộ menu, route action và dữ liệu GVPB |
| Đợt không phải KLTN | Ẩn toàn bộ menu, route action và dữ liệu hội đồng |
| Không phải trưởng nhóm | Ẩn mọi nút thay đổi nhóm, đăng ký đề tài và nộp bài |
| Không phải người chấm được phân công | Ẩn nút nhập/chỉnh sửa điểm |
| Kết quả chưa công bố | Sinh viên không thấy điểm, nhận xét hay route chi tiết kết quả |

## 10. Thành phần điều hướng theo vai trò

| Vai trò | Sidebar/menu chính |
| --- | --- |
| Admin | Dashboard quản trị; Tài khoản; Dashboard Trưởng khoa; Đợt đăng ký; Dashboard Giảng viên; Đề tài; Dashboard Sinh viên; Thông báo; Hồ sơ |
| Trưởng khoa | Dashboard; Đợt đăng ký; Duyệt đề tài; Phân công GVPB; Hội đồng KLTN; Kết quả; Thông báo; Hồ sơ |
| Giảng viên | Dashboard; Đề tài; Yêu cầu đăng ký; Bài nộp; Chấm điểm; Thông báo; Hồ sơ |
| Sinh viên | Dashboard; Đợt đăng ký; Nhóm; Kết quả; Thông báo; Hồ sơ |

## 11. Route thao tác

Các route trong các bảng màn hình là route `GET`. Form không dùng thao tác thay đổi dữ liệu qua `GET`; các thao tác ghi dữ liệu dùng `POST` tại endpoint dưới đây. Server luôn kiểm tra quyền và điều kiện thời gian, kể cả khi nút trên UI đã bị ẩn/khóa.

| Endpoint `POST` | Thao tác | Vai trò được phép |
| --- | --- | --- |
| `/dang-nhap` | Đăng nhập | Khách |
| `/dang-xuat` | Đăng xuất | Mọi người đã đăng nhập |
| `/ho-so` | Cập nhật email, số điện thoại | Chủ tài khoản |
| `/doi-mat-khau` | Đổi mật khẩu | Chủ tài khoản |
| `/quan-tri/tai-khoan` | Tạo tài khoản | Admin |
| `/quan-tri/tai-khoan/{id}/cap-nhat` | Cập nhật, đặt lại mật khẩu, khóa/mở khóa tài khoản | Admin |
| `/thong-bao` | Tạo thông báo | Admin, Trưởng khoa |
| `/thong-bao/{id}/cap-nhat` | Sửa hoặc thu hồi thông báo | Admin, Trưởng khoa |
| `/truong-khoa/dot-dang-ky` | Tạo đợt | Admin, Trưởng khoa |
| `/truong-khoa/dot-dang-ky/{id}/cap-nhat` | Cập nhật mốc/thông tin đợt | Admin, Trưởng khoa |
| `/truong-khoa/dot-dang-ky/{id}/de-tai/{deTaiId}/duyet` | Duyệt đề tài | Admin, Trưởng khoa |
| `/truong-khoa/dot-dang-ky/{id}/de-tai/{deTaiId}/tu-choi` | Từ chối đề tài, kèm lý do | Admin, Trưởng khoa |
| `/truong-khoa/dot-dang-ky/{id}/phan-cong-gvpb/{deTaiId}` | Phân công GVPB TLCN | Admin, Trưởng khoa |
| `/truong-khoa/dot-dang-ky/{id}/hoi-dong` | Tạo hội đồng KLTN | Admin, Trưởng khoa |
| `/truong-khoa/dot-dang-ky/{id}/hoi-dong/{hoiDongId}/de-tai/{deTaiId}` | Giao hoặc bỏ giao đề tài cho hội đồng | Admin, Trưởng khoa |
| `/truong-khoa/dot-dang-ky/{id}/ket-qua/cong-bo` | Công bố kết quả | Admin, Trưởng khoa |
| `/truong-khoa/dot-dang-ky/{id}/ket-qua/thu-hoi` | Thu hồi công bố kết quả | Admin, Trưởng khoa |
| `/giang-vien/de-tai` | Tạo/gửi đề xuất đề tài | Admin, Giảng viên |
| `/giang-vien/de-tai/{id}/cap-nhat` | Cập nhật đề tài được phép sửa | Admin, Giảng viên sở hữu |
| `/giang-vien/yeu-cau-dang-ky/{id}/duyet` | Duyệt đăng ký đề tài | Admin, Giảng viên phụ trách |
| `/giang-vien/yeu-cau-dang-ky/{id}/tu-choi` | Từ chối đăng ký, kèm lý do | Admin, Giảng viên phụ trách |
| `/giang-vien/cham-diem/{dangKyId}` | Lưu điểm và nhận xét | Admin, người chấm được phân công |
| `/giang-vien/cham-diem/{dangKyId}/chot` | Chốt kết quả KLTN | Admin, Chủ tịch hội đồng |
| `/sinh-vien/nhom` | Tạo nhóm | Admin, Sinh viên đủ điều kiện |
| `/sinh-vien/nhom/{id}/thanh-vien` | Thêm/xóa thành viên hoặc chuyển trưởng nhóm | Admin, Trưởng nhóm |
| `/sinh-vien/nhom/{id}/de-tai/{deTaiId}` | Chọn, rút hoặc đổi đề tài | Admin, Trưởng nhóm |
| `/sinh-vien/nhom/{id}/nop-bai` | Nộp hoặc thay thế bài/báo cáo | Admin, Trưởng nhóm |

## 12. Ngoài phạm vi

- Đăng ký công khai, tự tạo tài khoản và quên mật khẩu qua email.
- Email, push notification và thông báo cá nhân.
- CRUD role, permission và bộ môn.
- Lịch chi tiết theo phòng/ca của hội đồng.
