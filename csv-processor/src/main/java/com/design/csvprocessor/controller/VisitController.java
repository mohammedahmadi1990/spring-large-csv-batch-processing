package com.design.csvprocessor.controller;

import com.design.csvprocessor.message.ResponseMessage;
import com.design.csvprocessor.service.VisitService;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@CrossOrigin("http://localhost:8081")
@Controller
@RequestMapping("/api/csv")
public class VisitController {

    @Autowired
    VisitService visitService;
    @Autowired
    @Qualifier("myJobLauncher")
    private JobLauncher jobLauncher;
    @Autowired
    @Qualifier("importVisitJob")
    Job importVisitJob;
    public final static String tempDir = "C:\\tempUpload\\";

    @GetMapping("/process")
    public ResponseEntity<ResponseMessage> process() throws IOException {
        String message = "";
        try {
            JobParameters jobParameters = new JobParametersBuilder().toJobParameters();
            jobLauncher.run(importVisitJob, jobParameters);

            message = "File processed successfully!";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not processed the file!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<ResponseMessage> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String message = "";

        // Clean temp dir
        File folder = new File(tempDir);
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles.length>0)
            FileUtils.cleanDirectory(folder);

        String fileName = file.getOriginalFilename();
        try {
            // It is assumed that you have a physical folder C:\tempUpload
            file.transferTo(new File(tempDir + fileName));
            message = "File " + fileName + " uploaded & processed successfully!";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file " + fileName + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    @GetMapping("/visits")
    public ResponseEntity<List<Object[]>> getVisitsCount() {
        try {
            List<Object[]> result = visitService.getUserVisit();

            if (result.size() == 0) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}