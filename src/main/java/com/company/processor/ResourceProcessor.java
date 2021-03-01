package com.company.processor;

import com.company.dto.DatasetDto;
import reactor.core.publisher.Flux;

public interface ResourceProcessor {

    Flux<DatasetDto> process(DatasetDto datasetDto);

}
