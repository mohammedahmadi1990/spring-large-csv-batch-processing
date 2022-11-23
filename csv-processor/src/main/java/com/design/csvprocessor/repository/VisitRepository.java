package com.design.csvprocessor.repository;

import com.design.csvprocessor.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRepository extends JpaRepository<Visit, Long> {

}
