package com.design.csvprocessor.controller;

import com.design.csvprocessor.helper.CSVHelper;
import com.design.csvprocessor.message.ResponseMessage;
import com.design.csvprocessor.service.VisitService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin("http://localhost:8081")
@Controller
@RequestMapping("/api/csv")
public class VisitController{

    @Autowired
    VisitService visitService;

    @Autowired
    @Qualifier("myJobLauncher")
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("importVisitJob")
    Job importVisitJob;


    @PostMapping("/upload")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
        String message = "";

        if (CSVHelper.hasCSVFormat(file)) {
            try {
                //save file
                visitService.save(file);

                // Async Batch processing
                JobParameters jobParameters = new JobParametersBuilder().addString("source", "Spring Boot")
                        .addString("filePath", file.getOriginalFilename())
                        .toJobParameters();
                jobLauncher.run(importVisitJob, jobParameters);

                message = "File " + file.getOriginalFilename() + " uploaded & processed successfully!";
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
            } catch (Exception e) {
                message = "Could not upload the file " + file.getOriginalFilename() + "!";
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
            }
        }
        message = "Please upload a csv file!";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));
    }

    @GetMapping("/visits")
    public ResponseEntity<Long> getVisitsCount() {
        try {
            long visits = visitService.getAllVisits().size();

            if (visits == 0) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(visits, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}