package com.example.app1xmlreceiver.controller;

import com.example.app1xmlreceiver.service.XmlProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class XmlReceiverController {
    
    @Autowired
    private XmlProcessingService xmlProcessingService;
    
    /**
     * Эндпоинт для приема XML сообщений
     */
    @PostMapping(value = "/xml",
                 consumes = MediaType.APPLICATION_XML_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> receiveXml(@RequestBody String xmlContent) {
        try {
            // Обрабатываем XML сообщение
            String jsonResult = xmlProcessingService.processXmlMessage(xmlContent);
            
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(jsonResult);
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"Failed to process XML: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Эндпоинт для проверки состояния сервиса
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"OK\", \"service\": \"XML Receiver\"}");
    }
}
