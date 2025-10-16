package com.example.app1xmlreceiver.service;

import com.example.app1xmlreceiver.model.DataMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Service;


import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class XmlProcessingService {
    
    private final XmlMapper xmlMapper;
    private final ObjectMapper jsonMapper;
    private final ConcurrentHashMap<String, ReentrantLock> fileLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> recordCounts = new ConcurrentHashMap<>();
    
    public XmlProcessingService() {
        this.xmlMapper = new XmlMapper();
        this.jsonMapper = new ObjectMapper();
    }
    
    /**
     * Обрабатывает XML сообщение: конвертирует в JSON и сохраняет в файл
     */
    public String processXmlMessage(String xmlContent) throws Exception {
        // Парсим XML
        DataMessage dataMessage = xmlMapper.readValue(xmlContent, DataMessage.class);
        
        // Конвертируем в JSON
        String jsonContent = jsonMapper.writeValueAsString(dataMessage);
        
        // Сохраняем в файл по типу
        saveToFile(dataMessage.getType(), jsonContent);
        
        return jsonContent;
    }
    
    /**
     * Сохраняет JSON запись в файл по типу
     */
    private void saveToFile(String type, String jsonContent) throws IOException {
        String fileName = generateFileName(type);
        Path filePath = Paths.get("data", fileName);
        
        System.out.println("Сохраняем в файл: " + filePath.toAbsolutePath());
        
        // Создаем директорию если не существует
        Files.createDirectories(filePath.getParent());
        
        // Получаем блокировку для файла
        ReentrantLock lock = fileLocks.computeIfAbsent(fileName, k -> new ReentrantLock());
        
        lock.lock();
        try {
            // Читаем текущее количество записей
            int currentCount = getRecordCount(fileName);
            
            // Записываем в файл
            try (FileWriter writer = new FileWriter(filePath.toFile(), true)) {
                if (currentCount == 0) {
                    // Если файл новый, записываем заголовок с количеством записей
                    writer.write("RECORD_COUNT:0\n");
                }
                writer.write(jsonContent + "\n");
            }
            
            // Обновляем счетчик записей
            currentCount++;
            recordCounts.put(fileName, currentCount);
            
            // Обновляем заголовок файла с новым количеством записей
            updateRecordCountInFile(filePath, currentCount);
            
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Генерирует имя файла на основе типа и текущей даты
     */
    private String generateFileName(String type) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return type + "-" + date + ".log";
    }
    
    /**
     * Получает текущее количество записей в файле
     */
    private int getRecordCount(String fileName) {
        return recordCounts.computeIfAbsent(fileName, k -> {
            try {
                Path filePath = Paths.get("data", fileName);
                if (Files.exists(filePath)) {
                    try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                        String firstLine = reader.readLine();
                        if (firstLine != null && firstLine.startsWith("RECORD_COUNT:")) {
                            return Integer.parseInt(firstLine.substring("RECORD_COUNT:".length()));
                        }
                    }
                }
            } catch (Exception e) {
                // Игнорируем ошибки, возвращаем 0
            }
            return 0;
        });
    }
    
    /**
     * Обновляет количество записей в заголовке файла
     */
    private void updateRecordCountInFile(Path filePath, int count) throws IOException {
        if (!Files.exists(filePath)) {
            return;
        }
        
        // Читаем все строки
        java.util.List<String> lines = Files.readAllLines(filePath);
        
        if (!lines.isEmpty() && lines.get(0).startsWith("RECORD_COUNT:")) {
            // Обновляем первую строку
            lines.set(0, "RECORD_COUNT:" + count);
            
            // Записываем обратно
            Files.write(filePath, lines);
        }
    }
}
