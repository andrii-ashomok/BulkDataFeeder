package com.company.processor;

import com.company.dto.DatasetDto;
import com.company.service.XMLService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@Slf4j
@Service
public class ResourceProcessorImpl implements ResourceProcessor {

    private final XMLService xmlService;
    private final EmailProcessor emailProcessor;

    public ResourceProcessorImpl(XMLService xmlService, EmailProcessor emailProcessor) {
        this.xmlService = xmlService;
        this.emailProcessor = emailProcessor;
    }

    @Override
    public Flux<DatasetDto> process(DatasetDto datasetDto) {
        if (datasetDto.getEmails() != null && !datasetDto.getEmails().isEmpty()) {
            emailProcessor.process(datasetDto.getEmails());
        }

        if (datasetDto.getResources() == null || datasetDto.getResources().isEmpty()) {
            return Flux.empty();
        }

        return Flux.fromIterable(datasetDto.getResources())
                .subscribeOn(Schedulers.fromExecutor(Executors.newFixedThreadPool(10)))
                .map(WebClient::create)
                .map(WebClient::get)
                .flatMap(requestHeadersUriSpec -> requestHeadersUriSpec.retrieve().bodyToMono(String.class))
                .onErrorReturn("")
                .map(xmlService::parseXML)
                .flatMap(this::process);
    }

}
