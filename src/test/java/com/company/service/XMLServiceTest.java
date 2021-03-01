package com.company.service;

import com.company.dto.DatasetDto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class XMLServiceTest {

    private XMLService XMLService;

    @Before
    public void init() {
        XMLService = new XMLServiceImpl();
    }

    @Test
    public void parseXML_ReadFromFile_Success() throws IOException {
        File resource = new ClassPathResource(
                "test_data.xml").getFile();
        String xml = new String(Files.readAllBytes(resource.toPath()));
        DatasetDto datasetDto = XMLService.parseXML(xml);
        Assert.assertEquals(1, datasetDto.getResources().size());
        Assert.assertEquals(1, datasetDto.getEmails().size());
    }

}
