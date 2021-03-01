package com.company.repository;

import com.company.document.Email;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface EmailDao extends ReactiveMongoRepository<Email, String> {

    Mono<Email> findEmailByEmail(String email);

}
