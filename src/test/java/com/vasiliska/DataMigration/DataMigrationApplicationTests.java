package com.vasiliska.DataMigration;

import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = "com.vasiliska.DataMigration.models")
@ComponentScan(basePackages = "com.vasiliska.DataMigration.batch")
public class DataMigrationApplicationTests {
    @Bean
    JobLauncherTestUtils jobLauncherTestUtils() {
        return new JobLauncherTestUtils();
    }

}
