package com.example.app2batchprocessor.controller;

import com.example.app2batchprocessor.service.BatchProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class BatchController {
    
    @Autowired
    private BatchProcessingService batchProcessingService;
    
    /**
     * Запускает пакетную обработку файлов
     */
    @PostMapping("/process")
    public ResponseEntity<String> startProcessing() {
        try {
            batchProcessingService.processAllFiles();
            return ResponseEntity.ok("{\"status\": \"Processing completed successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Processing failed: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Проверяет состояние сервиса
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"OK\", \"service\": \"Batch Processor\"}");
    }
}
