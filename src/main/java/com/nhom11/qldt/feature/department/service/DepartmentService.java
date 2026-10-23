package com.nhom11.qldt.feature.department.service;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.department.repository.DepartmentRepository;
import com.nhom11.qldt.feature.topic.repository.TopicRepository;
import org.springframework.stereotype.Service;

@Service
/** Xử lý nghiệp vụ tạo, sửa, xóa bộ môn và kiểm tra ràng buộc với đề tài. */
public class DepartmentService extends BaseCrudService<Department, Long> {

  private final DepartmentRepository departmentRepository;
  private final TopicRepository topicRepository;

  public DepartmentService(
      DepartmentRepository departmentRepository, TopicRepository topicRepository) {
    this.departmentRepository = departmentRepository;
    this.topicRepository = topicRepository;
  }

  @Override
  /** Trả về repository để lớp base đọc và ghi dữ liệu bộ môn. */
  public BaseRepository<Department, Long> getRepository() {
    return departmentRepository;
  }

  /** Kiểm tra mã bộ môn đã tồn tại trong database hay chưa. */
  public boolean existsByCode(String code) {
    return departmentRepository.existsByCode(code);
  }

  /** Kiểm tra mã bộ môn đã bị record khác sử dụng, bỏ qua bộ môn đang chỉnh sửa. */
  public boolean existsByCodeAndIdNot(String code, Long id) {
    return departmentRepository.existsByCodeAndIdNot(code, id);
  }

  @Override
  /** Xóa bộ môn khi chưa có đề tài tham chiếu đến bộ môn đó. */
  public void delete(Long id) {
    // Giữ nguyên bộ môn khi còn đề tài tham chiếu để tránh làm mất quan hệ nghiệp vụ.
    if (topicRepository.existsByDepartmentId(id)) {
      throw new IllegalStateException("Department has topics");
    }

    super.delete(id);
  }
}
