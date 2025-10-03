package com.marbl.declarative_batct.spring_declarative_batch.factory.step;

import com.marbl.declarative_batct.spring_declarative_batch.exception.InvalidBeanException;
import com.marbl.declarative_batct.spring_declarative_batch.factory.component.ListenerFactory;
import com.marbl.declarative_batct.spring_declarative_batch.factory.component.ProcessorFactory;
import com.marbl.declarative_batct.spring_declarative_batch.factory.component.ReaderFactory;
import com.marbl.declarative_batct.spring_declarative_batch.factory.component.WriterFactory;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.ListenerConfig;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.StepsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class StepFactory {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    // factories from the library (already in the classpath)
    private final ReaderFactory readerFactory;
    private final ProcessorFactory processorFactory;
    private final WriterFactory writerFactory;
    private final ListenerFactory listenerFactory;

    /**
     * Build a typed Step from an AbstractSteplet implementation.
     * This method uses the library factories to build reader/processor/writer and attaches listeners.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <I, O> Step createStep(StepsConfig config, ItemReader<I> reader,
                                  ItemProcessor<I, O> processor,
                                  ItemWriter<O> writer) throws Exception {
        log.info("Creating step step '{}' ", config.getName());

        // --- Validate components passed by Steplet against YAML config ---
        if (reader != null && !readerFactory.isAllowedReader(reader, config.getReader().getType())) {
            throw new InvalidBeanException(
                    "Reader passed from Steplet does not match YAML type: expected " + config.getReader().getType()
            );
        }

        if (processor != null && !processorFactory.isAllowedProcessor(processor, config.getProcessor().getType())) {
            throw new InvalidBeanException(
                    "Processor passed from Steplet does not match YAML type: expected " + config.getProcessor().getType()
            );
        }

        if (writer != null && !writerFactory.isAllowedWriter(writer, config.getWriter().getType())) {
            throw new InvalidBeanException(
                    "Writer passed from Steplet does not match YAML type: expected " + config.getWriter().getType()
            );
        }


        // --- create typed components using the library factories (they return raw types) ---
        ItemReader<I> finalReader = reader != null
                ? reader
                : readerFactory.createReader(config.getReader(), config.getChunk());

        ItemProcessor<I, O> finalProcessor = processor != null
                ? processor
                : processorFactory.createProcessor(config.getProcessor());

        ItemWriter<O> finalWriter = writer != null
                ? writer
                : writerFactory.createWriter(config.getWriter());

        // --- build step with chunkStep and optional fault-tolerance ---
        StepBuilder stepBuilder = new StepBuilder(config.getName(), jobRepository);
        SimpleStepBuilder<I, O> chunkStep = stepBuilder
                .<I, O>chunk(config.getChunk(), transactionManager)
                .reader(finalReader)
                .processor(finalProcessor)
                .writer(finalWriter);

        // --- Attach listeners ---
        if (config.getListeners() != null) {
            for (ListenerConfig listenerConfig : config.getListeners()) {
                try {
                    Object listener = listenerFactory.createStepListener(listenerConfig);
                    if (listener == null) continue;

                    if (listener instanceof StepExecutionListener sel) {
                        chunkStep.listener(sel);
                        log.info("Attached StepExecutionListener '{}' to step '{}'",
                                listenerConfig.getName(), config.getName());
                    } else if (listener instanceof ItemReadListener irl) {
                        chunkStep.listener(irl);
                        log.info("Attached ItemReadListener '{}' to step '{}'",
                                listenerConfig.getName(), config.getName());
                    } else if (listener instanceof ItemWriteListener iwl) {
                        chunkStep.listener(iwl);
                        log.info("Attached ItemWriteListener '{}' to step '{}'",
                                listenerConfig.getName(), config.getName());
                    } else if (listener instanceof ItemProcessListener ipl) {
                        chunkStep.listener(ipl);
                        log.info("Attached ItemProcessListener '{}' to step '{}'",
                                listenerConfig.getName(), config.getName());
                    } else {
                        log.warn("Listener '{}' of type '{}' is not handled",
                                listenerConfig.getName(), listenerConfig.getType());
                    }
                } catch (Exception e) {
                    log.error("Error attaching listener '{}' to step '{}'",
                            listenerConfig.getName(), config.getName(), e);
                }
            }
        }

        // --- Fault-tolerance: retry & skip ---
        if (config.getRetry() != null || config.getSkip() != null || config.getTransaction() != null) {
            FaultTolerantStepBuilder<I, O> faultStep = chunkStep.faultTolerant();

            // Retry configuration
            if (config.getRetry() != null) {
                faultStep.retryLimit(config.getRetry().getLimit());
                if (config.getRetry().getExceptions() != null) {
                    for (Class<? extends Throwable> ex : config.getRetry().getExceptions()) {
                        faultStep.retry(ex);
                    }
                }
                log.info("Configured retry for step '{}': limit={}, exceptions={}",
                        config.getName(),
                        config.getRetry().getLimit(),
                        config.getRetry().getExceptions());
            }

            // Skip configuration
            if (config.getSkip() != null) {
                faultStep.skipLimit(config.getSkip().getLimit());
                if (config.getSkip().getExceptionsToSkip() != null) {
                    for (Class<? extends Throwable> ex : config.getSkip().getExceptionsToSkip()) {
                        faultStep.skip(ex);
                    }
                }
                if (config.getSkip().getExceptionsNoSkip() != null) {
                    for (Class<? extends Throwable> ex : config.getSkip().getExceptionsNoSkip()) {
                        faultStep.noSkip(ex);
                    }
                }
                log.info("Configured skip for step '{}': limit={}, exceptions={}, noSkipExceptions={}",
                        config.getName(),
                        config.getSkip().getLimit(),
                        config.getSkip().getExceptionsToSkip(),
                        config.getSkip().getExceptionsNoSkip());
            }

            // Rollback configuration
            if (config.getTransaction() != null) {
                if (config.getTransaction().getNoRollbackExceptions() != null) {
                    for (Class<? extends Throwable> ex : config.getTransaction().getNoRollbackExceptions()) {
                        faultStep.noRollback(ex);
                    }
                }
                if (config.getTransaction().isReaderInTransaction()) {
                    faultStep.readerIsTransactionalQueue();
                }
                log.info("Configured no rollback for step '{}': noRollbackExceptions={}, isReaderInTransactionQueue={}",
                        config.getName(),
                        config.getTransaction().getNoRollbackExceptions(),
                        config.getTransaction().isReaderInTransaction());
            }


            chunkStep = faultStep; // reassign for chaining listeners
        }

        Step step = chunkStep.build();
        log.info("Typed step '{}'", config.getName());
        return step;
    }
}
