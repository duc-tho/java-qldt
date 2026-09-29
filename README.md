# QLDT

Ứng dụng web hỗ trợ Khoa Công nghệ Thông tin quản lý đề tài sinh viên: đề tài môn học, nghiên cứu khoa học (NCKH), tiểu luận chuyên ngành (TLCN) và khóa luận tốt nghiệp (KLTN).

## Chức năng chính

- Quản lý đợt đăng ký, bộ môn, tài khoản và thông báo.
- Giảng viên đề xuất đề tài; Trưởng khoa duyệt và công bố đề tài.
- Sinh viên lập nhóm, đăng ký đề tài; giảng viên hướng dẫn duyệt hoặc từ chối đăng ký.
- Hỗ trợ phân quyền theo các vai trò `ADMIN`, `HEAD_OF_FACULTY`, `LECTURER` và `STUDENT`.

## Tech stack

| Thành phần             | Công nghệ                            |
| ---------------------- | ------------------------------------ |
| Backend                | Java 25, Spring Boot 4, Spring MVC   |
| Giao diện              | Thymeleaf, Tailwind CSS 4, daisyUI 5 |
| Frontend build         | Vite 8, Yarn                         |
| Cơ sở dữ liệu          | MySQL hoặc MariaDB                   |
| Truy cập dữ liệu       | Spring Data JPA / Hibernate          |
| Migration              | Flyway                               |
| Xác thực và phân quyền | Spring Security                      |
| Đóng gói               | Maven Wrapper, WAR                   |

## Yêu cầu môi trường

- JDK 25.
- Node.js và Yarn.
- MySQL hoặc MariaDB đang hoạt động.

Không cần cài Maven toàn cục vì repository đã có Maven Wrapper.

## Thiết lập môi trường phát triển

1. Tạo cơ sở dữ liệu `qldt` trên MySQL/MariaDB.

2. Kiểm tra cấu hình datasource trong profile đang dùng:
   - [`application.yaml`](src/main/resources/application.yaml) hiện kích hoạt profile `dev-win`.
   - [`application-dev-win.yaml`](src/main/resources/application-dev-win.yaml) kết nối MySQL cục bộ tại `127.0.0.1:3306`.
   - [`application-dev-example.yaml`](src/main/resources/application-dev-example.yaml) là mẫu kết nối MariaDB tại host `mariadb`.

   Thay đổi URL, username và password datasource cho phù hợp máy cá nhân. Không commit thông tin đăng nhập thật.

3. Cài dependency frontend và tạo bundle:

   ```powershell
   yarn install
   yarn build
   ```

4. Chạy ứng dụng:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   Mở [http://localhost:8080](http://localhost:8080). Flyway tự chạy các migration trong `src/main/resources/db/migration` khi ứng dụng khởi động.

## Phát triển giao diện

Chạy lệnh sau ở một terminal riêng để Vite tự build lại assets khi thay đổi CSS/JavaScript:

```powershell
yarn dev
```

Backend có thể chạy đồng thời bằng `./mvnw.cmd spring-boot:run` ở terminal khác.

## Tài khoản và cấu hình mặc định

| Hạng mục                                | Giá trị                                                                   |
| --------------------------------------- | ------------------------------------------------------------------------- |
| Database mặc định của profile `dev-win` | `qldt`                                                                    |
| MySQL mặc định của profile `dev-win`    | `127.0.0.1:3306`, user `root`, password `root`                            |
| Tài khoản ứng dụng mặc định             | username `admin`, pass `admin123`, email `admin@qldt.local`, role `ADMIN` |

Mật khẩu của tài khoản `admin`: `admin123`

## Lệnh hữu ích

```powershell
# Chạy test backend
.\mvnw.cmd test

# Build WAR (bao gồm test)
.\mvnw.cmd clean package

# Build frontend một lần
yarn build
```

File WAR sau khi đóng gói được tạo trong thư mục `target/`.

## Cấu trúc thư mục

```text
src/main/java/com/nhom11/qldt/     # Mã nguồn Spring Boot
src/main/resources/templates/      # Thymeleaf templates
src/main/resources/style/          # CSS nguồn
src/main/resources/script/         # JavaScript nguồn
src/main/resources/db/migration/   # Flyway migrations
src/main/resources/static/         # Assets đã build bởi Vite
```
