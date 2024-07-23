package com.ssafy11.springbatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SimpleNextJob {

    @Bean
    public Job simpleNextJob1(JobRepository jobRepository, Step step1, Step step2) {
        return new JobBuilder("simpleNextJob1", jobRepository)
                .start(step1)
                .next(step2)
                .build();
    }
    @Bean
    public Step step1(JobRepository jobRepository, Tasklet tasklet, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("simpleStep1", jobRepository)
                .tasklet(tasklet, platformTransactionManager).build();
    }

    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("simpleStep2", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">>>>>> Lamda tasklet");
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager).build();
    }

    @Bean
    public Tasklet tasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> This is Step1");
            return RepeatStatus.FINISHED;
        };
    }
}
