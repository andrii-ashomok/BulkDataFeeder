package com.company.service;

import com.company.document.DoormanData;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DoormanDataServiceImpl implements DoormanDataService {

    @Value("${batch.interval.value}")
    private int batchInterval;

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public DoormanDataServiceImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Scheduled(fixedDelayString = "${batch.interval.value}")
    public void combineDataInBatch() {
        log.info("Schedule task of batch combination started");
        Query findFirstQuery = new Query();
        findFirstQuery.addCriteria(Criteria.where("batchUUID").exists(false)).fields().include("dateTime");

        final String batchUUID = UUID.randomUUID().toString();

        reactiveMongoTemplate.find(findFirstQuery, DoormanData.class)
            .map(DoormanData::getDateTime)
            .flatMap(date -> markDataWithBatchUUID(date, batchUUID))
            .subscribe(updateResult -> log.info("Batch marked. Update result found: {}, updated: {}",
                    updateResult.getMatchedCount(), updateResult.getModifiedCount()),
                    throwable -> log.warn("Could not update data in doorman_data collection, {}", throwable.toString()),
                    () -> log.info("Schedule task of batch combination finished"));
    }

    private Mono<UpdateResult> markDataWithBatchUUID(Date time, String batchUUID) {
        Instant startTime = time.toInstant();
        Instant nowTime = Instant.now();

        long seconds = Duration.between(startTime, nowTime).getSeconds();
        long durationTime = TimeUnit.SECONDS.toMillis(seconds);

        if (durationTime < batchInterval) {
            log.warn("Batch not in {} ms interval", batchInterval);
            return Mono.empty();
        }

        long endTime = Duration.ofSeconds(startTime.getEpochSecond()).plusMillis(batchInterval).toMillis();

        Query query = new Query();
        query.addCriteria(
                Criteria.where("batchUUID").exists(false)
                    .andOperator(Criteria.where("dateTime").gte(time).andOperator(
                            Criteria.where("dateTime").lt(new Date(endTime))
                    )
                ));
        Update update = new Update();
        update.set("batchUUID", batchUUID);

        log.info("Start to mark elements with batch UUID: {}. From {} to {} interval", batchUUID, time, endTime);
        return reactiveMongoTemplate.updateMulti(query, update, DoormanData.class);
    }

}
