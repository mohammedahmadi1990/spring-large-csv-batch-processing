package com.design.csvprocessor.service;

import com.design.csvprocessor.helper.CSVHelper;
import com.design.csvprocessor.model.Visit;
import com.design.csvprocessor.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class VisitService  {
    @Autowired
    VisitRepository repository;

    public void save(MultipartFile file) {
        try {
            List<Visit> visits = CSVHelper.csvToVisits(file.getInputStream());
            repository.saveAll(visits);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save CSV data " + e.getMessage());
        }
    }

    public List<Visit> getAllVisits() {
        return repository.findAll();
    }
}