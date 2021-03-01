package com.company.service;

import com.company.dto.DatasetDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class XMLServiceImpl implements XMLService {

    private final static XmlMapper XML_MAPPER = new XmlMapper();

    @Override
    public DatasetDto parseXML(String xml) {
        if (xml == null || xml.isEmpty()) {
            return new DatasetDto();
        }
        try {
            return XML_MAPPER.readValue(xml, DatasetDto.class);
        } catch (JsonProcessingException e) {
            log.warn("Could not parse XML, {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

    }

}
