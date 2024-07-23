package com.ssafy11.springbatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class StepNextConditionJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    @Bean
    public Job stepNextConditionJob() {
        return new JobBuilder("stepNextConditionJob", jobRepository)
            .start(conditionStep1())
                .on("FAILED")// BatchStatus X, ExitStatus O
                .end()
            .from(conditionStep1())
                .on("COMPLETED WITH SKIPS")
                .to(conditionStep3())
                .end()
            .build();
    }

    @Bean
    public Step conditionStep1() {
        return new StepBuilder("conditionStep1", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">>>>>> condition1");
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .listener(skipCheckingListener())
                .build();
    }

    @Bean
    public Step conditionStep2() {
        return new StepBuilder("conditionStep2", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">>>>>> condition2");
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

    @Bean
    public Step conditionStep3() {
        return new StepBuilder("conditionStep3", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">>>>>> condition3");
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager).build();
    }

    @Bean
    public SkipCheckingListener skipCheckingListener() {
        return new SkipCheckingListener();
    }
    public class SkipCheckingListener implements StepExecutionListener {

        @Override
        public ExitStatus afterStep(StepExecution stepExecution) {
            String exitCode = stepExecution.getExitStatus().getExitCode();
            if (!exitCode.equals(ExitStatus.FAILED.getExitCode())) {
                log.info(">>>>>> skipCheckingListener: exitCode={}", exitCode);
                return new ExitStatus("COMPLETED WITH SKIPS");
            }
            else {
                return null;
            }
        }
    }

}
