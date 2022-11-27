package com.design.csvprocessor.service;


import com.design.csvprocessor.model.Visit;
import com.design.csvprocessor.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VisitService  {
    @Autowired
    VisitRepository repository;

    public List<Visit> getAllVisits() {
        return repository.findAll();
    }

    public List<Object[]> getUserVisit(){
        return repository.findByGroupByEmailAndPhone();
    }
}