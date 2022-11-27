package com.design.csvprocessor.repository;

import com.design.csvprocessor.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query(
            value = "SELECT email, phone, count(*) " +
                    "FROM visits " +
                    "GROUP BY email, phone",
            nativeQuery = true)
    List<Object[]> findByGroupByEmailAndPhone();

}
