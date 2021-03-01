package com.company.controller;

import com.company.document.DoormanData;
import com.company.document.Email;
import com.company.repository.DoormanDao;
import com.company.repository.EmailDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api")
public class EmailController {

    private final DoormanDao doormanDao;
    private final EmailDao emailDao;

    public EmailController(DoormanDao doormanDao, EmailDao emailDao) {
        this.doormanDao = doormanDao;
        this.emailDao = emailDao;
    }

    @GetMapping(path = "/emails", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public @ResponseBody
    Flux<Email> getEmails() {
        return emailDao.findAll().delayElements(Duration.ofMillis(200));
    }

    @GetMapping(path = "/emails/{email}")
    public @ResponseBody
    Mono<Long> getEmailCount(@PathVariable String email) {
        return emailDao.findEmailByEmail(email)
                .map(Email::getCount);
    }

    @PostMapping("/emails")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveEmailData(@RequestBody String xml) {
        DoormanData data = new DoormanData();
        data.setXmlData(xml);
        data.setDateTime(new Date());

        doormanDao.save(data)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(s -> log.debug("Saving"),
                        throwable -> log.warn("Could not save input XML data, cause: {}", throwable.getMessage()),
                        () -> log.debug("XML saved in DB"));
    }

}
