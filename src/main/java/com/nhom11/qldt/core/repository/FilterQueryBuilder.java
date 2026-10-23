package com.nhom11.qldt.core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Dựng truy vấn Criteria có tìm kiếm, lọc, sắp xếp và phân trang cho một kiểu thực thể. */
public class FilterQueryBuilder<T> {
  private final Class<T> entityType;
  private final EntityManager entityManager;

  public FilterQueryBuilder(Class<T> entityType, EntityManager entityManager) {
    this.entityType = entityType;
    this.entityManager = entityManager;
  }

  /** Thực thi truy vấn dữ liệu và truy vấn đếm tương ứng để tạo kết quả phân trang. */
  public Page<T> findAll(
      Pageable pageable,
      String search,
      List<String> searchableFields,
      Map<String, String> filters) {

    // Truy vấn chính lấy dữ liệu sau khi ghép điều kiện tìm kiếm và bộ lọc.
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<T> selectQuery = criteriaBuilder.createQuery(entityType);

    Root<T> selectRoot = selectQuery.from(entityType);
    selectQuery
        .select(selectRoot)
        .where(buildConditions(criteriaBuilder, selectRoot, search, searchableFields, filters));

    applySort(criteriaBuilder, selectQuery, selectRoot, pageable.getSort());

    // Chỉ lấy lát dữ liệu thuộc trang hiện tại thay vì tải toàn bộ kết quả.
    TypedQuery<T> typedQuery = entityManager.createQuery(selectQuery);
    typedQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
    typedQuery.setMaxResults(pageable.getPageSize());

    // Truy vấn đếm riêng giúp Spring tính đúng tổng số trang.
    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<T> countRoot = countQuery.from(entityType);
    // Dựng lại điều kiện vì truy vấn đếm sử dụng một root Criteria khác truy vấn dữ liệu.
    countQuery
        .select(criteriaBuilder.count(countRoot))
        .where(buildConditions(criteriaBuilder, countRoot, search, searchableFields, filters));

    long total = entityManager.createQuery(countQuery).getSingleResult();

    return new PageImpl<>(typedQuery.getResultList(), pageable, total);
  }

  /** Kết hợp điều kiện tìm kiếm tự do và các bộ lọc trường bằng phép AND. */
  private Predicate buildConditions(
      CriteriaBuilder criteriaBuilder,
      Root<T> root,
      String search,
      List<String> searchableFields,
      Map<String, String> filters) {

    List<Predicate> conditions = new ArrayList<>();
    addSearchCondition(criteriaBuilder, root, search, searchableFields, conditions);
    addFilterConditions(criteriaBuilder, root, filters, conditions);

    if (conditions.isEmpty()) {
      return criteriaBuilder.conjunction();
    }

    return criteriaBuilder.and(conditions.toArray(Predicate[]::new));
  }

  /** Thêm điều kiện OR tìm chuỗi trên toàn bộ các trường được phép tìm kiếm. */
  private void addSearchCondition(
      CriteriaBuilder criteriaBuilder,
      Root<T> root,
      String search,
      List<String> searchableFields,
      List<Predicate> conditions) {

    if (search == null || search.isBlank() || searchableFields.isEmpty()) {
      return;
    }

    Predicate searchCondition =
        criteriaBuilder.or(
            searchableFields.stream()
                .map(field -> containsText(criteriaBuilder, root, field, search))
                .toArray(Predicate[]::new));

    // Từ khóa chỉ cần xuất hiện ở một trong các trường được phép tìm kiếm.
    conditions.add(searchCondition);
  }

  /** Thêm điều kiện lọc chứa chuỗi cho từng trường có giá trị hợp lệ. */
  private void addFilterConditions(
      CriteriaBuilder criteriaBuilder,
      Root<T> root,
      Map<String, String> filters,
      List<Predicate> conditions) {
    if (filters == null || filters.isEmpty()) {
      return;
    }

    for (Map.Entry<String, String> filter : filters.entrySet()) {
      String value = filter.getValue();
      if (value == null || value.isBlank()) {
        continue;
      }

      conditions.add(containsText(criteriaBuilder, root, filter.getKey(), value));
    }
  }

  /** Tạo điều kiện so khớp chuỗi không phân biệt hoa thường cho một trường văn bản. */
  private Predicate containsText(
      CriteriaBuilder criteriaBuilder, Root<T> root, String field, String value) {
    Path<?> fieldPath = root.get(field);

    if (!String.class.isAssignableFrom(fieldPath.getJavaType())) {
      throw new IllegalStateException("Search/filter field must be a String: " + field);
    }

    // Chuẩn hóa cả dữ liệu và từ khóa để so khớp không phân biệt hoa thường.
    String pattern = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";

    return criteriaBuilder.like(criteriaBuilder.lower(root.<String>get(field)), pattern);
  }

  /** Áp dụng toàn bộ tiêu chí sắp xếp từ yêu cầu phân trang lên truy vấn. */
  private void applySort(
      CriteriaBuilder criteriaBuilder, CriteriaQuery<T> query, Root<T> root, Sort sort) {
    if (sort.isUnsorted()) {
      return;
    }

    List<Order> ordering =
        sort.stream().map(order -> createOrder(criteriaBuilder, root, order)).toList();
    query.orderBy(ordering);
  }

  /** Chuyển một tiêu chí sắp xếp của Spring thành biểu thức sắp xếp Criteria. */
  private Order createOrder(CriteriaBuilder criteriaBuilder, Root<T> root, Sort.Order order) {
    Path<?> field = root.get(order.getProperty());

    return order.isAscending() ? criteriaBuilder.asc(field) : criteriaBuilder.desc(field);
  }
}
