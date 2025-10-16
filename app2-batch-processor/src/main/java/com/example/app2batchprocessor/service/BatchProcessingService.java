package com.example.app2batchprocessor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class BatchProcessingService {
    
    private final ObjectMapper objectMapper;
    private final Map<String, ProcessingState> processingStates = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> fileLocks = new ConcurrentHashMap<>();
    private static final int BATCH_SIZE = 100;
    private static final String STATE_FILE = "processing_state.json";
    
    public BatchProcessingService() {
        this.objectMapper = new ObjectMapper();
        loadProcessingStates();
    }
    
    /**
     * Обрабатывает все файлы в директории data
     */
    public void processAllFiles() {
        try {
            Path dataDir = Paths.get("../data");
            if (!Files.exists(dataDir)) {
                System.out.println("Директория data не найдена: " + dataDir.toAbsolutePath());
                return;
            }
            
            // Получаем все .log файлы
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir, "*.log")) {
                for (Path file : stream) {
                    if (!file.getFileName().toString().contains("-000")) { // Исключаем уже обработанные файлы
                        processFile(file);
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Ошибка при обработке файлов: " + e.getMessage());
        }
    }
    
    /**
     * Обрабатывает один файл
     */
    private void processFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        System.out.println("Обрабатываем файл: " + fileName);
        
        ReentrantLock lock = fileLocks.computeIfAbsent(fileName, k -> new ReentrantLock());
        lock.lock();
        
        try {
            ProcessingState state = processingStates.computeIfAbsent(fileName, k -> new ProcessingState());
            
            List<String> records = readRecordsFromFile(filePath, state);
            
            if (records.isEmpty()) {
                System.out.println("Нет новых записей для обработки в файле: " + fileName);
                return;
            }
            
            // Группируем записи по 100 штук
            List<List<String>> batches = createBatches(records, BATCH_SIZE);
            
            // Сохраняем каждую группу в отдельный файл
            for (int i = 0; i < batches.size(); i++) {
                List<String> batch = batches.get(i);
                String outputFileName = generateOutputFileName(fileName, state.getNextBatchNumber() + i);
                saveBatchToFile(batch, outputFileName);
            }
            
            // Обновляем состояние
            state.setProcessedRecords(state.getProcessedRecords() + records.size());
            state.setNextBatchNumber(state.getNextBatchNumber() + batches.size());
            
            // Сохраняем состояние
            saveProcessingStates();
            
            System.out.println("Обработано " + records.size() + " записей из файла: " + fileName);
            
        } catch (Exception e) {
            System.err.println("Ошибка при обработке файла " + fileName + ": " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Читает записи из файла, начиная с последней обработанной позиции
     */
    private List<String> readRecordsFromFile(Path filePath, ProcessingState state) throws IOException {
        List<String> records = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            int lineNumber = 0;
            
            // Пропускаем заголовок с количеством записей
            String firstLine = reader.readLine();
            if (firstLine != null && firstLine.startsWith("RECORD_COUNT:")) {
                lineNumber++;
            }
            
            // Пропускаем уже обработанные записи
            while (lineNumber < state.getProcessedRecords() && (line = reader.readLine()) != null) {
                lineNumber++;
            }
            
            // Читаем новые записи
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    records.add(line);
                }
            }
        }
        
        return records;
    }
    
    /**
     * Создает батчи из списка записей
     */
    private List<List<String>> createBatches(List<String> records, int batchSize) {
        List<List<String>> batches = new ArrayList<>();
        
        for (int i = 0; i < records.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, records.size());
            batches.add(records.subList(i, endIndex));
        }
        
        return batches;
    }
    
    /**
     * Генерирует имя выходного файла
     */
    private String generateOutputFileName(String originalFileName, int batchNumber) {
        String baseName = originalFileName.replace(".log", "");
        return String.format("%s-%04d.log", baseName, batchNumber);
    }
    
    /**
     * Сохраняет батч в файл
     */
    private void saveBatchToFile(List<String> batch, String fileName) throws IOException {
        Path outputPath = Paths.get("../data", fileName);
        
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("BATCH_SIZE:" + batch.size() + "\n");
            for (String record : batch) {
                writer.write(record + "\n");
            }
        }
        
        System.out.println("Создан файл: " + fileName + " с " + batch.size() + " записями");
    }
    
    /**
     * Загружает состояния обработки из файла
     */
    private void loadProcessingStates() {
        try {
            Path stateFile = Paths.get(STATE_FILE);
            if (Files.exists(stateFile)) {
                String json = Files.readString(stateFile);
                Map<String, ProcessingState> states = objectMapper.readValue(json, 
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, ProcessingState.class));
                processingStates.putAll(states);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке состояний: " + e.getMessage());
        }
    }
    
    /**
     * Сохраняет состояния обработки в файл
     */
    private void saveProcessingStates() {
        try {
            String json = objectMapper.writeValueAsString(processingStates);
            Files.write(Paths.get(STATE_FILE), json.getBytes());
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении состояний: " + e.getMessage());
        }
    }
    
    /**
     * Класс для хранения состояния обработки файла
     */
    public static class ProcessingState {
        private int processedRecords = 0;
        private int nextBatchNumber = 1;
        
        public ProcessingState() {}
        
        public int getProcessedRecords() {
            return processedRecords;
        }
        
        public void setProcessedRecords(int processedRecords) {
            this.processedRecords = processedRecords;
        }
        
        public int getNextBatchNumber() {
            return nextBatchNumber;
        }
        
        public void setNextBatchNumber(int nextBatchNumber) {
            this.nextBatchNumber = nextBatchNumber;
        }
    }
}
