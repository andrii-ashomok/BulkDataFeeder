package com.company.processor;

import com.company.document.Email;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmailProcessorImpl implements EmailProcessor {

    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();
    private static final List<String> EMAIL_DOMAINS = Arrays.asList("comeon.com", "cherry.se");

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public EmailProcessorImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public void process(List<String> incomingEmails) {
        log.info("Start to process {} emails", incomingEmails.size());
        List<Email> emails = filterAndCountEmails(incomingEmails);
        if (!emails.isEmpty()) {
            upsertEmails(emails);
        }
    }

    private List<Email> filterAndCountEmails(List<String> incomingEmails) {
        Map<String, Email> emailsMap = new HashMap<>();
        int invalidEmails = 0;
        for (String incomingEmail : incomingEmails) {
            if (isEmailDomainCorrect(incomingEmail) && EMAIL_VALIDATOR.isValid(incomingEmail)) {
                Email email = emailsMap.get(incomingEmail);
                if (email == null) {
                    emailsMap.put(incomingEmail, new Email(incomingEmail, 1));
                } else {
                    email.setCount(email.getCount() + 1);
                }
            } else {
                invalidEmails++;
            }
        }

        log.info("{} invalid emails found", invalidEmails);

        return new ArrayList<>(emailsMap.values());
    }

    private void upsertEmails(List<Email> emails) {
        List<Mono<UpdateResult>> monos = new ArrayList<>();
        for (Email email : emails) {
            Query query = new Query();
            query.addCriteria(Criteria.where("email").is(email.getEmail()));

            Update update = new Update();
            update.inc("count", email.getCount());
            monos.add(reactiveMongoTemplate.upsert(query, update, Email.class));
        }

        Flux.concat(monos)
                .subscribe(updateResult -> log.info("Inserted: {}, matched: {} emails", updateResult.getModifiedCount(),
                        updateResult.getMatchedCount()),
                        throwable -> log.warn("Could not upsert emails. {}", throwable.getMessage()),
                        () -> log.info("Emails successfully saved"));
    }

    private static boolean isEmailDomainCorrect(String email) {
        return email.endsWith(EMAIL_DOMAINS.get(0)) || email.endsWith(EMAIL_DOMAINS.get(1));
    }

}
