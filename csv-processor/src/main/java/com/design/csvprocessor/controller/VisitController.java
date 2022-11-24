package com.design.csvprocessor.controller;

import com.design.csvprocessor.helper.CSVHelper;
import com.design.csvprocessor.message.ResponseMessage;
import com.design.csvprocessor.service.VisitService;
import org.springframework.batch.core.Job;
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

@CrossOrigin("http://localhost:8081")
@Controller
@RequestMapping("/api/csv")
public class VisitController{

    @Autowired
    VisitService visitService;

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    @Qualifier("importVisitJob")
    Job importVisitJob;

    @GetMapping("/run-batch-job")
    public String handle() throws Exception
    {

        JobParameters jobParameters = new JobParametersBuilder().addString("source", "Spring Boot")
                .toJobParameters();
        jobLauncher.run(importVisitJob, jobParameters);

        return "Batch job has been invoked";
    }

    @PostMapping("/upload")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
        String message = "";

        if (CSVHelper.hasCSVFormat(file)) {
            try {
                visitService.save(file);

                message = "File " + file.getOriginalFilename() + " uploaded successfully!";
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