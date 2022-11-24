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
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@Configuration
@EnableBatchProcessing
public class BatchConfiguration
{
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private DataSource dataSource;

    /**
     * The method is used to read the data from the CSV file
     */
    @Bean
    @StepScope
    public FlatFileItemReader<Visit> reader(@Value("#{jobParameters[filePath]}") String pathToFile)
    {

        FlatFileItemReader<Visit> reader = new FlatFileItemReader<Visit>();
        reader.setResource(new FileSystemResource(pathToFile));
//        reader.setResource(new PathResource(pathToFile));
//        reader.setResource(new ClassPathResource("sample.csv")); // local
        reader.setStrict(false);
        reader.setLinesToSkip(1);
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

    @Bean(name = "myJobLauncher")
    public JobLauncher simpleJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    /**
     * Intermediate processor to do the operations after the reading the data from the CSV file and
     * before writing the data into SQL.
     */
    @Bean
    public VisitItemProcessor processor()
    {
        return new VisitItemProcessor();
    }

    /**
     * The method is used to write a data into the SQL.
     */
    @Bean
    public JdbcBatchItemWriter<Visit> writer()
    {
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
    public Step step1(){
        return stepBuilderFactory.get("step1").<Visit, Visit>chunk(4).reader(reader(null))
                .processor(processor()).writer(writer()).build();
    }
}
