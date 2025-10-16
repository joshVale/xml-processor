package com.example.app2batchprocessor.scheduler;

import com.example.app2batchprocessor.service.BatchProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchScheduler {
    
    @Autowired
    private BatchProcessingService batchProcessingService;
    
    /**
     * Автоматически обрабатывает файлы каждые 30 секунд
     */
    @Scheduled(fixedRate = 30000) // 30 секунд
    public void processFilesAutomatically() {
        System.out.println("Запуск автоматической обработки файлов...");
        try {
            batchProcessingService.processAllFiles();
        } catch (Exception e) {
            System.err.println("Ошибка при автоматической обработке: " + e.getMessage());
        }
    }
}
