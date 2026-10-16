# HƯỚNG DẪN PHÁT TRIỂN PROJECT

Tài liệu này mô tả các cơ chế đang có, quy ước và lưu ý mà mọi thành viên phải biết trước khi thay đổi source.

## 1. Tổng quan và cách chạy project

Phần này mô tả các cơ chế đang tồn tại trong source. Thành viên phải đọc trước khi tạo controller, service, repository, form, table, template hoặc migration mới.

### 1.1. Công nghệ và phiên bản

| Thành phần | Công nghệ đang dùng |
| --- | --- |
| Ngôn ngữ backend | Java 25 |
| Framework | Spring Boot 4.1.1, Spring MVC |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL/MariaDB |
| Migration | Flyway |
| Security | Spring Security |
| Template | Thymeleaf và Thymeleaf Layout Dialect |
| Frontend | Tailwind CSS 4, daisyUI 5, Alpine.js |
| Icon | Iconify với Bootstrap Icons `bi:*` |
| Client validation | JustValidate |
| Frontend build | Vite 8, Yarn |
| Đóng gói | Maven Wrapper, WAR |

### 1.2. Lệnh chạy project

Trên Linux/macOS:

```bash
yarn install
yarn build
./mvnw spring-boot:run
```

Trong quá trình phát triển giao diện, chạy hai terminal:

```bash
yarn dev
```

```bash
./mvnw spring-boot:run
```

`yarn dev` theo dõi source CSS/JS bằng polling để hoạt động ổn định trong dev container.
`spring-boot:run` đọc trực tiếp template và asset từ `src/main/resources`, nên chỉ cần tải
lại trang sau khi lưu thay đổi giao diện; không cần chạy Maven để chép sang `target/classes`.
Khi thay đổi Java, cần biên dịch lại để Spring DevTools khởi động lại ứng dụng.
Sau khi thay đổi cấu hình `pom.xml` hoặc `vite.config.js`, hãy khởi động lại hai lệnh trên một lần.

Các lệnh kiểm tra trước khi bàn giao:

```bash
./mvnw test
yarn build
./mvnw clean package
```

Trên Windows dùng `mvnw.cmd` thay cho `./mvnw`.

Không yêu cầu thành viên phải làm TDD, nhưng thay đổi phải được kiểm tra phù hợp trước khi bàn giao. Thay đổi backend chạy test liên quan; thay đổi frontend chạy `yarn build`; thay đổi cả hai chạy cả Maven test và Vite build.

### 1.3. Cấu hình local

- Không hardcode credential vào Java hoặc template.
- Không commit `application-dev.yaml` chứa thông tin cá nhân.
- Bắt đầu từ `src/main/resources/application-dev-example.yaml` và tạo cấu hình local phù hợp.
- `application.yaml` hiện kích hoạt profile `dev-win`; thành viên phải kiểm tra profile thực tế trước khi chạy.
- Thông tin database, SMTP, storage, URL production và secret phải lấy từ configuration hoặc environment variable.
- Flyway tự chạy migration khi ứng dụng khởi động.
- Thư mục lưu báo cáo mặc định là `./local-report-storage`; cấu hình `REPORT_STORAGE_ROOT` theo mount path khi deploy. Với Cloud Run, mount ephemeral disk tại `/mnt/report-storage` và đặt `REPORT_STORAGE_ROOT=/mnt/report-storage`. Dữ liệu trên ephemeral disk không được đảm bảo tồn tại sau khi instance dừng.

---

## 2. Cấu trúc source và ranh giới các tầng

### 2.1. Cấu trúc package

```text
src/main/java/com/nhom11/qldt/
├── config/                       # Security, locale, MVC/model advice
├── core/                         # Cơ chế dùng chung
│   ├── controller/
│   ├── entity/
│   ├── form/
│   ├── repository/
│   ├── service/
│   ├── table/
│   ├── validation/
│   └── view/
└── feature/
    └── <feature>/
        ├── controller/
        ├── model/dto/
        ├── model/entity/
        ├── model/enums/
        ├── repository/
        ├── service/
        └── view/
```

Code mới phải nằm trong feature tương ứng. Không đưa business logic của một feature vào `core`. Chỉ đưa vào `core` khi có ít nhất nhiều feature thực sự dùng chung cùng một hợp đồng.

### 2.2. Trách nhiệm từng tầng

| Tầng | Trách nhiệm | Không được làm |
| --- | --- | --- |
| Controller | Mapping HTTP, nhận form/path/query, kiểm tra binding, chọn view hoặc redirect | Không chứa business rule dài, không truy vấn database trực tiếp |
| Service | Business rule, authorization theo quan hệ dữ liệu, transaction | Không render HTML, không phụ thuộc trực tiếp template |
| Repository | Query và persistence | Không quyết định nghiệp vụ hoặc quyền người dùng |
| DTO/Form | Dữ liệu đầu vào cần thiết và Jakarta Validation | Không chứa service/repository hoặc business workflow |
| Entity | Mapping dữ liệu và quan hệ | Không dùng trực tiếp làm form khi có dữ liệu người dùng nhập |
| Table definition | Cột, search/filter/sort và action của CRUD table | Không chạy query hoặc thay đổi dữ liệu |
| Template | Hiển thị dữ liệu và action đã được phép | Không được xem là lớp bảo mật |

### 2.3. Dependency Injection

- Dùng constructor injection.
- Không dùng field injection với `@Autowired`.
- Service phụ thuộc repository/service cần thiết; tránh dependency vòng.
- Không tạo utility hoặc abstraction mới cho logic chỉ dùng một lần.

---

## 3. Cơ chế CRUD dùng chung

### 3.1. Base repository

Repository CRUD chuẩn kế thừa:

```java
public interface DepartmentRepository extends BaseRepository<Department, Long> {
}
```

`QldtApplication` cấu hình `BaseRepositoryImpl` làm repository base class cho toàn project. Cơ chế này cung cấp:

- Phân trang.
- Search/filter cơ bản.
- Soft delete.
- Soft delete theo danh sách.
- Permanent delete khi thực sự cần.

Không tạo lại các hàm CRUD đã có trong `JpaRepository` hoặc `BaseRepository` nếu hành vi giống nhau.

### 3.2. Base CRUD service

Service CRUD chuẩn kế thừa `BaseCrudService<T, ID>` và cung cấp repository:

```java
@Service
public class DepartmentService extends BaseCrudService<Department, Long> {
    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public BaseRepository<Department, Long> getRepository() {
        return repository;
    }
}
```

Các hook sẵn có:

- `beforeCreate`.
- `afterCreate`.
- `beforeUpdate`.
- `afterUpdate`.
- `beforeDelete`.
- `afterDelete`.

Dùng hook cho validation hoặc side effect gắn trực tiếp với CRUD. Không override `create`, `update`, `delete` chỉ để gọi lại `super` mà không thêm hành vi.

Không ép workflow service như nhóm, báo cáo, hội đồng hoặc công bố kết quả kế thừa CRUD base nếu nghiệp vụ của chúng không phải CRUD đơn giản.

### 3.3. CrudViewSupport và form dùng chung

`CrudViewSupport` chuẩn bị:

- Model của CRUD table.
- `CrudFormModel`.
- Action URL.
- Back URL.
- Readonly state.
- Template chứa fields.
- Client validation rules.

Form feature chỉ khai báo fields trong fragment riêng. Nút lưu, hủy, global error và layout form dùng fragment `fragment/crud/form.html`.

Khi thêm CRUD mới, ưu tiên dùng cơ chế này thay vì sao chép nguyên form/table sang feature.

---

## 4. Soft delete và auditing

### 4.1. Auditing

Entity kế thừa `AuditableEntity` tự có:

- `createdAt` với `@CreatedDate`.
- `updatedAt` với `@LastModifiedDate`.

JPA auditing được bật trong `QldtApplication`. Không tự gán `createdAt` hoặc `updatedAt` trong controller/service nếu không có lý do nghiệp vụ đặc biệt.

### 4.2. Soft delete

Entity kế thừa `SoftDeleteEntity` có `deletedAt` và `@SQLRestriction("deleted_at IS NULL")`.

Hệ quả:

- Các query JPA thông thường không nhìn thấy bản ghi đã soft delete.
- `BaseCrudService.delete(id)` thực hiện soft delete.
- Permanent delete chỉ dùng cho trường hợp đã được yêu cầu rõ và đã kiểm tra foreign key.
- Không gọi `repository.delete(...)` tùy tiện cho entity có soft delete.
- Không tự thêm điều kiện `deleted_at IS NULL` vào mọi query JPA nếu Hibernate restriction đã áp dụng.

Migration tạo bảng mới cho entity soft delete phải có `created_at`, `updated_at` và `deleted_at` phù hợp mapping.

---

## 5. CRUD table, search, filter và action

### 5.1. Table definition

Mỗi CRUD table định nghĩa cột trong lớp kế thừa `BaseTableDefinition<T>`.

Mỗi `ColumnDef` gồm:

- Tên field entity.
- Message key của label.
- Hàm lấy giá trị hiển thị.
- Có search hay không.
- Có filter hay không.
- Có sort hay không.

Action dùng các loại có sẵn:

- `ActionDef.Link`.
- `ActionDef.Post`.
- `ActionDef.Reject`.
- `ActionDef.Delete`.

Không tạo HTML action riêng trong từng table nếu `ActionDef` hiện có đáp ứng được.

### 5.2. TableQuery

`TableQuery` xử lý các query parameter:

- `search`.
- `page`.
- `size`.
- `sort`.
- `dir`.
- `filters[field]`.

Chỉ field được table definition đánh dấu mới được dùng để filter hoặc sort. Không truyền trực tiếp field tùy ý từ request vào JPA sort/query.

### 5.3. Giới hạn search hiện tại

`FilterQueryBuilder` hiện chỉ hỗ trợ:

- Field trực tiếp của entity.
- Kiểu `String`.
- Tìm kiếm `contains`, không phân biệt chữ hoa/chữ thường.

Không đánh dấu searchable/filterable cho:

- Association như `department.name`.
- Collection như `advisers`.
- Enum.
- Number hoặc date.

nếu chưa viết custom query phù hợp. Với search nâng cao hoặc field quan hệ, tạo repository query/projection riêng thay vì cố ép `FilterQueryBuilder`.

### 5.4. Lưu ý CRUD table template

- Cột tên `status` được fragment render dưới dạng badge và giả định entity có `getStatus()`.
- Mọi cell không phải status hiện có link đến `baseUrl/{id}`.
- Nút thêm được điều khiển bởi `table.canCreate`.
- Bulk delete mặc định tắt.
- Table được bọc `overflow-x-auto`; không tạo page overflow ở mobile.

Khi dùng table definition cho màn hình chỉ đọc, phải override `canCreate()` và action theo quyền hiện tại.

---

## 6. Form và validation

### 6.1. Server-side validation là bắt buộc

- Form/DTO dùng Jakarta Validation như `@NotBlank`, `@NotNull`, `@Size`, `@Email`, `@Min`, `@Max`.
- Controller nhận `@Valid` và `BindingResult` ngay sau form parameter.
- Business validation nằm trong service hoặc domain workflow.
- Không chỉ dựa vào thuộc tính HTML, JavaScript hoặc disabled button.

### 6.2. Client validation hiện tại

`ClientValidationRuleRenderer` đọc annotation trên form và chuyển thành JSON rule cho JustValidate. Template đặt rule vào `validate-data`; Alpine component `formValidation` chạy kiểm tra trước khi submit.

Các annotation đang được chuyển sang client rule gồm:

- `@NotBlank`, `@NotEmpty`, `@NotNull`.
- `@Email`.
- `@Min`, `@Max`.
- `@DecimalMin`, `@DecimalMax` khi inclusive.
- `@Pattern`.
- `@Size`.

Client validation chỉ cải thiện UX. Request vẫn phải qua validation phía server.

### 6.3. Form mapping

- Form implement `CrudForm<T>` khi phù hợp.
- `toEntity()` chỉ map dữ liệu trực tiếp; quan hệ như role, department, adviser phải được resolve trong service.
- `fromEntity()` dùng khi hiển thị detail/edit.
- Không nhận entity trực tiếp từ request cho form có quan hệ hoặc field nhạy cảm.
- Khi update, không để dữ liệu request ghi đè field audit, creator, status hoặc quan hệ không được phép sửa.

---

## 7. Security và authorization

### 7.1. Role hiện tại

- `ADMIN`.
- `HEAD_OF_FACULTY`.
- `LECTURER`.
- `STUDENT`.

`DatabaseUserDetailsService` chuyển role thành authority có prefix `ROLE_`. Khi dùng `hasRole`, chỉ truyền tên như `ADMIN`, không thêm `ROLE_`.

### 7.2. Hai lớp kiểm tra quyền

1. `WebSecurityConfig` kiểm tra quyền truy cập route theo role.
2. Service kiểm tra quyền nghiệp vụ theo dữ liệu thực tế.

Ví dụ: role `STUDENT` cho phép truy cập route nhóm, nhưng service vẫn phải kiểm tra sinh viên có thực sự thuộc nhóm trong URL hay không.

### 7.3. CSRF

- Spring Security bật CSRF mặc định.
- Form Thymeleaf POST thường được chèn token tự động.
- JavaScript gửi POST/fetch phải lấy token và header từ meta `_csrf`, `_csrf_header` trong `layout/master.html`.
- Không disable CSRF để xử lý lỗi form.

### 7.4. Mật khẩu và dữ liệu nhạy cảm

- Mật khẩu được hash bằng BCrypt.
- Không log mật khẩu, token, credential, nội dung file hoặc dữ liệu cá nhân không cần thiết.
- Không trả entity người dùng chứa password ra response.
- Không hardcode tài khoản production.

### 7.5. Role constants

Source hiện vẫn còn role string lặp lại. Đây là technical debt đã phân công xử lý. Trước khi thêm role check mới trong Java, tìm và tái sử dụng enum/constants đã được tạo; không tiếp tục nhân bản literal.

---

## 8. Exception và transaction

### 8.1. Exception

Coding convention yêu cầu exception ứng dụng được chuyển thành response/view ở một cơ chế tập trung. Tuy nhiên source hiện tại chưa có Global Exception Handler hoàn chỉnh và nhiều controller đang bắt `IllegalArgumentException` để đưa message key vào model/flash attribute.

Vì vậy:

- Không tự tạo thêm pattern exception thứ ba.
- Trong phạm vi feature hiện tại, giữ nhất quán với luồng đang dùng nếu chưa có refactor chung.
- Nếu triển khai Global Exception Handler, phải thống nhất toàn nhóm trước và chuyển dần theo một kế hoạch riêng.
- Không catch, log rồi throw lại cùng exception.
- Không swallow exception.
- Exception nghiệp vụ nên mang error/message code ổn định thay vì câu tiếng Việt hardcode.

### 8.2. Transaction

Dùng `@Transactional` tại service cho thao tác thay đổi nhiều entity hoặc cần duy trì invariant, ví dụ:

- Tạo nhóm và tạo nhóm trưởng.
- Chuyển nhóm trưởng.
- Thay đổi thành viên và reset trạng thái đăng ký.
- Lưu điểm và tạo kết quả cuối.
- Công bố kết quả và ghi lịch sử.

Không đặt transaction boundary ở template hoặc repository. Không thêm `@Transactional` cho method chỉ đọc đơn giản nếu không cần.

Event gửi email phải chạy sau khi transaction công bố commit thành công. Không gọi SMTP trực tiếp giữa transaction lưu publication.

---

## 9. Thymeleaf layout và model dùng chung

### 9.1. Layout hierarchy

```text
layout/master.html
└── layout/app.html
    └── layout/page.html
        └── feature page
```

- `master.html`: metadata, CSRF meta, CSS và JS bundle.
- `app.html`: navbar, sidebar, main content và footer.
- `page.html`: back action, breadcrumb, page title, page icon, action group và content.

Trang nghiệp vụ thông thường phải dùng `layout/page.html`. Không sao chép navbar/sidebar/footer vào từng template.

### 9.2. Model attribute dùng chung

`CurrentUserModelAdvice` tự thêm:

- `currentPath` để đánh dấu menu hiện tại.
- `currentUser` cho người dùng đã đăng nhập.

Controller không cần query lại current user chỉ để hiển thị navbar. Khi business logic cần user entity, service/controller vẫn phải resolve một cách rõ ràng.

### 9.3. Page model contract

Các trang dùng `layout/page.html` thường cung cấp:

- `pageTitle`: message key.
- `pageIcon`: Iconify name như `bi:calendar-range`.
- `pageDesc`: message key, nếu cần.
- `breadcrumbParentUrl` và `breadcrumbParentLabel` cho màn hình con.
- Fragment `actions` cho page-level action.
- Fragment `page-content` cho nội dung chính.

Không truyền text đã dịch vào `pageTitle` nếu layout đang gọi `#messages.msg(pageTitle)`.

---

## 10. I18n

- Message bundle nằm tại `src/main/resources/i18n/messages.properties` và `messages_vi.properties`.
- Locale mặc định là tiếng Việt.
- Người dùng có thể đổi locale qua query parameter `lang`.
- Mọi label, button, validation message, status và empty state phải dùng message key.
- Thêm key mới vào cả hai bundle trong cùng thay đổi.
- Không thêm hậu tố `_vi` vào key; `??key_vi??` là cách Spring báo key `key` bị thiếu ở locale Việt.
- Dynamic message key phải được kiểm tra cho mọi enum value.
- Không hardcode câu tiếng Việt trực tiếp trong Java/template nếu text cần tái sử dụng hoặc dịch.

Quy ước key nên theo feature:

```text
<feature>.page.<screen>.title
<feature>.field.<field-name>
<feature>.action.<action-name>
<feature>.validation.<rule>
<feature>.error.<error-name>
<feature>.status.<ENUM_VALUE>
```

---

## 11. Frontend build và JavaScript

### 11.1. Source và output

- CSS nguồn: `src/main/resources/style/app.css`.
- JavaScript nguồn: `src/main/resources/script/`.
- Entry point: `src/main/resources/script/app.js`.
- Vite output: `src/main/resources/static/assets/` và các public asset liên quan.

Không sửa trực tiếp `src/main/resources/static/assets/app.css` hoặc `app.js`, vì lần build sau sẽ ghi đè. Sửa source rồi chạy `yarn build`.

### 11.2. Cơ chế JavaScript hiện có

- `drawer-state.js`: lưu trạng thái sidebar trong `localStorage`.
- `form-controller.js`: tích hợp Alpine và JustValidate.
- `form-validation.js`: tạo client validation rule.
- `crud-table.js`: submit search, filter, sort, page và chọn dòng.
- `crud-delete-modal.js`: modal xác nhận xóa dùng chung.
- `page-back.js`: quay lại history hoặc về trang chủ.

Tái sử dụng cơ chế trên trước khi tạo script mới. Script feature mới phải được import từ `app.js` hoặc entry phù hợp để Vite bundle.

### 11.3. Alpine.js

Alpine hiện được load qua CDN trong `layout/master.html`, không nằm trong dependency Yarn. Điều này có nghĩa môi trường chạy cần truy cập CDN. Nếu chuyển sang bundling local, phải thực hiện như một thay đổi chung và kiểm tra toàn bộ Alpine component.

---

## 12. Quy chuẩn UI

`DESIGN.md` ở root là nguồn chuẩn cho mọi thay đổi UI, Tailwind, daisyUI, Thymeleaf template và shared layout. Hiện file đang bị đánh dấu xóa trong worktree; phải khôi phục hoặc lưu lại file trước khi nhóm bắt đầu thay đổi UI.

Các nguyên tắc bắt buộc:

- Dùng semantic daisyUI color như `primary`, `base-100`, `base-200`, `success`, `warning`, `error`.
- Không dùng raw Tailwind color cho text/surface thông thường nếu semantic token đáp ứng được.
- Mỗi trang có một primary action mạnh nhất.
- Destructive action dùng `btn-error btn-outline` hoặc confirmation dialog.
- Status luôn có text, không chỉ biểu diễn bằng màu.
- Table dùng toolbar responsive và wrapper `overflow-x-auto`.
- Form có label trên input, lỗi ngay dưới field.
- Icon dùng Iconify Bootstrap:

```html
<span class="iconify inline" data-icon="bi:sort-numeric-down"></span>
```

- Icon-only button phải có `title` hoặc `aria-label`.
- Không thêm thư viện UI, icon family, remote font hoặc animation library mới nếu chưa thống nhất.
- Không sửa shared layout mà chưa kiểm tra tất cả màn hình sử dụng.

Lưu ý: theme hiện tại trong `app.css` có thể chưa đồng bộ hoàn toàn với token Forest Ledger đã mô tả trong `DESIGN.md`. Khi khôi phục file, người sở hữu shared UI phải đối chiếu và thực hiện thay đổi theme trong một task riêng, không trộn vào feature business.

---

## 13. Flyway và thay đổi database

- Migration nằm tại `src/main/resources/db/migration/`.
- Không sửa migration đã được chạy trên database của thành viên khác.
- Tạo migration tiếp theo theo thứ tự version hiện tại, ví dụ sau `V1_16` là `V1_17__<description>.sql`.
- Tên migration dùng tiếng Anh, chữ thường và dấu gạch dưới trong description.
- Mỗi thay đổi entity liên quan schema phải có migration tương ứng.
- Cân nhắc foreign key, unique constraint, index, nullable và delete behavior.
- Business invariant quan trọng nên có database constraint nếu mô hình cho phép.
- Không dùng `ddl-auto` thay thế Flyway để chia sẻ schema.
- Migration phải chạy được trên MySQL/MariaDB mà project sử dụng.

Khi thay đổi nhóm 03–05 hoặc thêm người chấm độc lập, người phụ trách ERD và migration phải phối hợp trước khi tạo entity để tránh nhiều migration sửa cùng bảng trong cùng thời điểm.

---

## 14. File upload và storage

- File báo cáo không lưu trực tiếp trong database; database lưu metadata và storage path.
- Tên file lưu phải do hệ thống tạo, không dùng nguyên filename từ người dùng làm path.
- Luôn normalize path và kiểm tra path vẫn nằm trong storage root trước khi đọc.
- Kiểm tra extension, số file, kích thước và trạng thái đăng ký ở backend.
- Chỉ người có quan hệ hợp lệ với đề tài mới tải được file.
- Không log nội dung file hoặc path nhạy cảm.
- Local filesystem có thể mất dữ liệu trên hosting ephemeral; Cloud Run có thể mount ephemeral disk cho storage tạm thời, còn dữ liệu cần lưu bền vững phải dùng persistent volume hoặc object storage.
- Không commit file trong `local-report-storage`.

---

## 15. Quy tắc code Java

- Tên class PascalCase; field/method camelCase.
- Một public type mỗi file.
- Controller/Service/Repository đặt tên theo trách nhiệm.
- DTO/Form có suffix thể hiện mục đích như `TopicForm`, `EvaluationForm`.
- Constructor injection, không field injection.
- Ưu tiên early return, method ngắn và tên diễn đạt rõ ý định.
- Không catch exception nếu không xử lý được tại tầng đó.
- Không log rồi throw lại cùng exception.
- Không log dữ liệu nhạy cảm.
- Dùng enum cho tập giá trị cố định.
- Tái sử dụng constants/framework APIs trước khi tạo constant mới.
- Không tạo constant cho annotation value hoặc literal chỉ dùng một lần.
- Không tạo helper cho logic đơn giản chỉ dùng một chỗ.
- Không wildcard import, raw type hoặc suppressed warning thiếu lý do.
- Chỉ dùng `Optional` ở boundary phù hợp; không trả `null` cho collection.
- Dùng `Clock` hoặc configuration khi logic thời gian cần kiểm thử ổn định.
- Giữ formatting của vùng đang sửa; không format lại file không liên quan.

Project hiện có chênh lệch giữa hướng dẫn Java indentation trong quy ước nhóm và `.editorconfig` đang đặt hai spaces. Trước khi chạy formatter toàn project phải thống nhất một chuẩn. Trong thời gian chưa chốt, không reformat hàng loạt; chỉ giữ style của file đang sửa.

---

## 16. Quy tắc làm việc chung

### 16.1. Trước khi code

1. Đọc `AGENTS.md`.
2. Đọc `docs/specs/yeu_cau.md` và phần phân công của mình.
3. Nếu sửa UI, đọc `DESIGN.md` sau khi file được khôi phục.
4. Tìm cơ chế, constant, service, fragment và component đã có.
5. Xác định route, role, business state và tiêu chí nghiệm thu.
6. Trao đổi trước nếu cần sửa shared file do thành viên khác sở hữu.

### 16.2. Trong khi code

- Chỉ sửa dòng liên quan trực tiếp đến task.
- Không refactor code của người khác nếu không cần cho chức năng.
- Không copy nguyên CRUD controller/table/form khi project đã có base mechanism.
- Không tạo hai cách xử lý cùng một vấn đề.
- Khi phát hiện tài liệu và code mâu thuẫn, dừng và báo nhóm trưởng.
- Khi thay đổi interface/service dùng chung, thông báo cho các module phụ thuộc.
- Không commit `target/`, `node_modules/`, cấu hình local, credential hoặc file báo cáo upload.

### 16.3. Trước khi bàn giao

1. Xem toàn bộ diff của mình.
2. Bỏ import, variable, method và file do thay đổi của mình làm thừa.
3. Kiểm tra message key ở cả hai locale.
4. Kiểm tra role và authorization bằng request trực tiếp, không chỉ UI.
5. Chạy verification phù hợp.
6. Kiểm tra responsive nếu thay đổi template.
7. Chuẩn bị dữ liệu demo và edge case.
8. Cập nhật tài liệu và ảnh minh chứng.
9. Ghi rõ command đã chạy và kết quả khi bàn giao cho reviewer.

---

## 17. Các điểm dễ sai trong source hiện tại

| Điểm dễ sai | Hậu quả | Cách xử lý |
| --- | --- | --- |
| Dùng `FilterQueryBuilder` cho enum hoặc field quan hệ | Runtime exception hoặc query sai | Viết custom repository query |
| Chỉ ẩn nút bằng `sec:authorize` | Có thể bypass bằng POST trực tiếp | Kiểm tra lại trong service |
| Thêm message key vào một bundle | Hiển thị `??key_locale??` | Thêm vào cả English và Vietnamese |
| Sửa `static/assets` trực tiếp | Mất thay đổi sau Vite build | Sửa source CSS/JS |
| Gọi delete JPA trực tiếp | Bỏ qua soft delete/hook | Dùng service/base repository đúng cơ chế |
| Dùng entity làm form | Mass assignment hoặc ghi đè field nhạy cảm | Dùng DTO/Form |
| Dùng `LocalDateTime.now()` rải rác | Khó kiểm thử timeline | Gom logic thời gian hoặc dùng `Clock` khi refactor |
| Gửi email trong transaction công bố | SMTP lỗi làm rollback dữ liệu | Event sau commit |
| Sửa GVHD mà không kiểm tra người chấm | GVHD có thể tự chấm | Kiểm tra conflict hai chiều |
| Chỉ query đợt đang mở cho sinh viên | Không xem lại kết quả | Query lịch sử theo membership |
| Sửa migration cũ | Database các thành viên lệch nhau | Tạo migration mới |
| Sửa shared layout không thông báo | Xung đột và lỗi nhiều trang | Trao đổi với owner shared UI |
| Tin vào role/ID từ client | Lộ hoặc sửa dữ liệu trái quyền | Resolve và xác thực lại từ database |
| Bắt mọi exception trong controller | Mất nguyên nhân và pattern không nhất quán | Chỉ xử lý exception có thể chuyển thành feedback phù hợp |

---

## 18. Mẫu checklist khi thêm một feature

```text
[ ] Xác định owner và reviewer
[ ] Xác định role được xem/thao tác
[ ] Xác định state và transition hợp lệ
[ ] Tạo/cập nhật migration nếu cần
[ ] Tạo entity/repository query tối thiểu
[ ] Tạo Form/DTO và Jakarta Validation
[ ] Viết business rule tại service
[ ] Đặt transaction boundary phù hợp
[ ] Thêm controller route và kiểm tra binding
[ ] Thêm authorization theo quan hệ dữ liệu
[ ] Tái sử dụng layout/form/table/component hiện có
[ ] Thêm message key ở cả hai locale
[ ] Kiểm tra empty/error/success state
[ ] Kiểm tra request bypass UI
[ ] Chạy Maven test liên quan
[ ] Chạy yarn build nếu sửa frontend
[ ] Chuẩn bị dữ liệu và kịch bản demo
[ ] Cập nhật tài liệu và ảnh minh chứng
[ ] Reviewer xác nhận tiêu chí nghiệm thu
```
