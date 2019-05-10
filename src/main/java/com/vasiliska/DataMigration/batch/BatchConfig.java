package com.vasiliska.DataMigration.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.persistence.EntityManagerFactory;
import java.util.List;

@EnableBatchProcessing
@Configuration
public class BatchConfig {
    private final Logger logger = LoggerFactory.getLogger("Batch");

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public EntityManagerFactory entityManagerFactory;

    @Autowired
    public MongoTemplate mongoTemplate;

    @Bean
    public JpaPagingItemReader<com.vasiliska.DataMigration.models.jpa.Book> reader() {
        String jpqlQuery = "SELECT b FROM Book b";
        return new JpaPagingItemReaderBuilder<com.vasiliska.DataMigration.models.jpa.Book>()
                .name("jpaDatabaseReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(jpqlQuery)
                .pageSize(100)
                .saveState(true)
                .build();
    }

    @Bean
    public ItemProcessor processor() {
        return (ItemProcessor<com.vasiliska.DataMigration.models.jpa.Book, com.vasiliska.DataMigration.models.mongo.Book>) bookJpa -> {
            com.vasiliska.DataMigration.models.jpa.Author authorJpa = bookJpa.getAuthor();
            com.vasiliska.DataMigration.models.mongo.Author authorMongo = new com.vasiliska.DataMigration.models.mongo.Author(authorJpa.getAuthorName());
            com.vasiliska.DataMigration.models.jpa.Genre genreJpa = bookJpa.getGenre();
            com.vasiliska.DataMigration.models.mongo.Genre genreMongo = new com.vasiliska.DataMigration.models.mongo.Genre(genreJpa.getGenreName());
            return new com.vasiliska.DataMigration.models.mongo.Book(bookJpa.getBookName(), authorMongo, genreMongo);
        };
    }

    @Bean
    public MongoItemWriter<com.vasiliska.DataMigration.models.mongo.Book> writer() {
        MongoItemWriter<com.vasiliska.DataMigration.models.mongo.Book> mongoItemWriter = new MongoItemWriter<>();
        mongoItemWriter.setCollection("books");
        mongoItemWriter.setTemplate(mongoTemplate);
        return mongoItemWriter;
    }

    @Bean
    public Job importBooksJob(Step step1) {
        return jobBuilderFactory.get("importBooksJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
                .listener(new JobExecutionListener() {
                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        logger.info("Start job");
                    }

                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        logger.info("Finish job");
                    }
                })
                .build();
    }

    @Bean
    public Step migrate(ItemReader reader, ItemProcessor processor, ItemWriter writer) {
        return stepBuilderFactory.get("migrate")
                .chunk(5)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(new ItemReadListener() {
                    public void beforeRead() {
                        logger.info("Start read");
                    }
                    public void afterRead(Object o) {
                        logger.info("Finish read");
                    }
                    public void onReadError(Exception e) {
                        logger.info("Read error!");
                    }
                })
                .listener(new ItemWriteListener() {
                    public void beforeWrite(List list) {
                        logger.info("Start write");
                    }
                    public void afterWrite(List list) {
                        logger.info("Finish write");
                    }
                    public void onWriteError(Exception e, List list) {
                        logger.info("Write error!");
                    }
                })
                .build();
    }

}
