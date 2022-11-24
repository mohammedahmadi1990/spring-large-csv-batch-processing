package com.design.csvprocessor.config;

import javax.sql.DataSource;

import com.design.csvprocessor.batch.JobCompletionNotificationListener;
import com.design.csvprocessor.batch.VisitItemProcessor;
import com.design.csvprocessor.model.Visit;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;


@Configuration
@EnableBatchProcessing
public class BatchConfiguration
{
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    /**
     * The reader() method is used to read the data from the CSV file
     */
    @Bean
    public FlatFileItemReader<Visit> reader()
    {
        System.out.println("-----------Inside reader() method--------");
        FlatFileItemReader<Visit> reader = new FlatFileItemReader<Visit>();
        reader.setResource(new ClassPathResource("sample_visits.csv"));
        reader.setLineMapper(new DefaultLineMapper<Visit>()
        {
            {
                setLineTokenizer(new DelimitedLineTokenizer()
                {
                    {
                        setNames(new String[] { "email", "phone", "source" });
                    }
                });
                setFieldSetMapper(new BeanWrapperFieldSetMapper<Visit>()
                {
                    {
                        setTargetType(Visit.class);
                    }
                });
            }
        });
        return reader;
    }

    /**
     * Intermediate processor to do the operations after the reading the data from the CSV file and
     * before writing the data into SQL.
     */
    @Bean
    public VisitItemProcessor processor()
    {
        System.out.println("-----------Inside  processor() method--------");
        return new VisitItemProcessor();
    }

    /**
     * The writer() method is used to write a data into the SQL.
     */
    @Bean
    public JdbcBatchItemWriter<Visit> writer()
    {
        System.out.println("-----------Inside writer() method--------");
        JdbcBatchItemWriter<Visit> writer = new JdbcBatchItemWriter<Visit>();
        writer.setItemSqlParameterSourceProvider(
                new BeanPropertyItemSqlParameterSourceProvider<Visit>());
        writer.setSql("INSERT INTO visits (email, phone, source) VALUES (:email, :phone, :source)");
        writer.setDataSource(dataSource);
        return writer;
    }

    @Bean
    public Job importVisitJob(JobCompletionNotificationListener listener)
    {
        return jobBuilderFactory.get("importVisitJob").incrementer(new RunIdIncrementer())
                .listener(listener).flow(step1()).end().build();
    }

    @Bean
    public Step step1()
    {
        return stepBuilderFactory.get("step1").<Visit, Visit>chunk(4).reader(reader())
                .processor(processor()).writer(writer()).build();
    }
}
