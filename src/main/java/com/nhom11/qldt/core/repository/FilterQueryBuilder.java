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

public class FilterQueryBuilder<T> {
  private final Class<T> entityType;
  private final EntityManager entityManager;

  public FilterQueryBuilder(Class<T> entityType, EntityManager entityManager) {
    this.entityType = entityType;
    this.entityManager = entityManager;
  }

  public Page<T> findAll(
      Pageable pageable,
      String search,
      List<String> searchableFields,
      Map<String, String> filters) {

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<T> selectQuery = criteriaBuilder.createQuery(entityType);

    Root<T> selectRoot = selectQuery.from(entityType);
    selectQuery
        .select(selectRoot)
        .where(buildConditions(criteriaBuilder, selectRoot, search, searchableFields, filters));

    applySort(criteriaBuilder, selectQuery, selectRoot, pageable.getSort());

    TypedQuery<T> typedQuery = entityManager.createQuery(selectQuery);
    typedQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
    typedQuery.setMaxResults(pageable.getPageSize());

    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<T> countRoot = countQuery.from(entityType);
    // Build these conditions again for the count query because it uses a different
    // root.
    countQuery
        .select(criteriaBuilder.count(countRoot))
        .where(buildConditions(criteriaBuilder, countRoot, search, searchableFields, filters));

    long total = entityManager.createQuery(countQuery).getSingleResult();

    return new PageImpl<>(typedQuery.getResultList(), pageable, total);
  }

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

    conditions.add(searchCondition);
  }

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

  private Predicate containsText(
      CriteriaBuilder criteriaBuilder, Root<T> root, String field, String value) {
    Path<?> fieldPath = root.get(field);

    if (!String.class.isAssignableFrom(fieldPath.getJavaType())) {
      throw new IllegalStateException("Search/filter field must be a String: " + field);
    }

    String pattern = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";

    return criteriaBuilder.like(criteriaBuilder.lower(root.<String>get(field)), pattern);
  }

  private void applySort(
      CriteriaBuilder criteriaBuilder, CriteriaQuery<T> query, Root<T> root, Sort sort) {
    if (sort.isUnsorted()) {
      return;
    }

    List<Order> ordering =
        sort.stream().map(order -> createOrder(criteriaBuilder, root, order)).toList();
    query.orderBy(ordering);
  }

  private Order createOrder(CriteriaBuilder criteriaBuilder, Root<T> root, Sort.Order order) {
    Path<?> field = root.get(order.getProperty());

    return order.isAscending() ? criteriaBuilder.asc(field) : criteriaBuilder.desc(field);
  }
}
