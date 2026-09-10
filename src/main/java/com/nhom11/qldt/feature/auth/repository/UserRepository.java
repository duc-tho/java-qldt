package com.nhom11.qldt.feature.auth.repository;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.nhom11.qldt.core.repository.BaseRepositoryImpl;
import com.nhom11.qldt.feature.auth.model.entity.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class UserRepository extends BaseRepositoryImpl<User, Long> {

    private final EntityManager entityManager;

    public UserRepository(EntityManager entityManager) {
        super(User.class, entityManager);
        this.entityManager = entityManager;
    }

    public Page<User> search(String search, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        Predicate predicate = buildSearchPredicate(cb, root, search);
        query.where(predicate);
        query.orderBy(pageable.getSort().stream()
                .map(order -> order.isAscending()
                        ? cb.asc(root.get(order.getProperty()))
                        : cb.desc(root.get(order.getProperty())))
                .toList());

        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);
        countQuery.select(cb.count(countRoot)).where(buildSearchPredicate(cb, countRoot, search));

        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, total);
    }

    public Page<User> searchByColumns(Map<String, String> params, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        Predicate predicate = buildColumnPredicates(cb, root, params);
        query.where(predicate);
        query.orderBy(pageable.getSort().stream()
                .map(order -> order.isAscending()
                        ? cb.asc(root.get(order.getProperty()))
                        : cb.desc(root.get(order.getProperty())))
                .toList());

        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);
        countQuery.select(cb.count(countRoot)).where(buildColumnPredicates(cb, countRoot, params));

        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, total);
    }

    private Predicate buildSearchPredicate(CriteriaBuilder cb, Root<User> root, String search) {
        if (search == null || search.isBlank()) {
            return cb.conjunction();
        }
        String pattern = "%" + search.toLowerCase() + "%";
        return cb.or(
                cb.like(cb.lower(root.get("username")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("fullName")), pattern),
                cb.like(cb.lower(root.get("phone")), pattern));
    }

    private Predicate buildColumnPredicates(CriteriaBuilder cb, Root<User> root, Map<String, String> params) {
        Predicate predicate = cb.conjunction();
        if (params == null || params.isEmpty()) {
            return predicate;
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();
            if (value != null && !value.isBlank()) {
                String pattern = "%" + value.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.like(cb.lower(root.get(entry.getKey())), pattern));
            }
        }
        return predicate;
    }
}
