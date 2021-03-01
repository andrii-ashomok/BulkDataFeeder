package com.company.repository;

import com.company.document.DoormanData;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoormanDao extends ReactiveMongoRepository<DoormanData, String> {
}
