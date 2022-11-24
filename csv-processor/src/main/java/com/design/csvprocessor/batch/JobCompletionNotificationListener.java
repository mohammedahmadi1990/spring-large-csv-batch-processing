package com.design.csvprocessor.batch;

import com.design.csvprocessor.model.Visit;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport
{
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterJob(JobExecution jobExecution)
    {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED)
        {
            List<Visit> results = jdbcTemplate.query("SELECT email,phone,source FROM visits",
                    new RowMapper<Visit>()
                    {
                        @Override
                        public Visit mapRow(ResultSet rs, int row) throws SQLException
                        {
                            return new Visit(0, rs.getString(1), rs.getString(2),rs.getString(3));
                        }
                    });

            for (Visit visit : results)
            {
                System.out.println("Found <" + visit + "> in the database.");
            }
        }

    }
}
