package com.company.processor;

import com.company.document.DoormanData;
import com.company.service.XMLService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BatchProcessorImpl implements BatchProcessor {

    private final XMLService xmlService;
    private final EmailProcessor emailProcessor;
    private final ResourceProcessor resourceProcessor;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public BatchProcessorImpl(XMLService xmlService, EmailProcessor emailProcessor, ResourceProcessor resourceProcessor,
                              ReactiveMongoTemplate reactiveMongoTemplate) {
        this.xmlService = xmlService;
        this.emailProcessor = emailProcessor;
        this.resourceProcessor = resourceProcessor;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Scheduled(fixedDelayString = "${batch.process.delay}")
    public void processBatches() {
        log.info("Batch Processor started");
        Query query = new Query();
        query.addCriteria(Criteria.where("isProcessed").exists(false)
                .andOperator(
                        Criteria.where("batchUUID").exists(true)
                ));
        Update update = new Update();
        update.set("isProcessed", true);

        reactiveMongoTemplate.findAndModify(query, update, DoormanData.class)
                .map(doormanData -> xmlService.parseXML(doormanData.getXmlData()))
                .flatMapMany(resourceProcessor::process)
                .subscribe(datasetDto -> emailProcessor.process(datasetDto.getResources()),
                        throwable -> log.warn("Could not process resource. {}", throwable.getMessage()),
                        () -> log.info("Schedule task of processing batch successfully finished"));
    }

}
