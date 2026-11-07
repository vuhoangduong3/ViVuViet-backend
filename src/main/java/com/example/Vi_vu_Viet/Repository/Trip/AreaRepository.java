package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AreaRepository extends JpaRepository<Area, UUID>, JpaSpecificationExecutor<Area> {

    default List<Area> findActiveAreas(String keyword, UUID parentId, String type) {
        Specification<Area> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.upper(root.<String>get("status")),
                    "ACTIVE"
            ));

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.<String>get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.<String>get("code")), pattern)
                ));
            }

            if (parentId != null) {
                predicates.add(criteriaBuilder.equal(root.get("parent_id").get("id"), parentId));
            }

            if (type != null && !type.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.<String>get("type")),
                        type.trim().toLowerCase(Locale.ROOT)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(Sort.Order.asc("name"), Sort.Order.asc("code"));
        return findAll(specification, sort);
    }

    @Query("select area from Area area " +
            "where area.id = :areaId and upper(area.status) = 'ACTIVE'")
    Optional<Area> findActiveById(@Param("areaId") UUID areaId);
}
