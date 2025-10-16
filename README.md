# Инструкция по тестированию XML Processor System

## Структура проекта
```
xml-processor/
├── app1-xml-receiver/                 # Приложение 1: прием XML → JSON, запись в файлы
│   ├── src/main/java/com/example/app1xmlreceiver/
│   │   ├── controller/XmlReceiverController.java
│   │   ├── service/XmlProcessingService.java
│   │   └── model/DataMessage.java
│   ├── src/main/resources/application.properties  # server.port=9091
│   ├── pom.xml
│   └── data/                           # Выходные файлы приложения 1 (создается автоматически)
│
├── app2-batch-processor/              # Приложение 2: батч-обработка логов
│   ├── src/main/java/com/example/app2batchprocessor/
│   │   ├── controller/BatchController.java
│   │   ├── scheduler/BatchScheduler.java
│   │   └── service/BatchProcessingService.java
│   ├── src/main/resources/application.properties  # порт по умолчанию 8080
│   ├── pom.xml
│
├── data/                               # Общая директория для батч-обработки (использует App2)
├── processing_state.json               # Файл состояния App2 (создается автоматически)
├── README.md
└── TESTING.md
```

Важное замечание по директориям:
- Приложение 1 пишет файлы в `app1-xml-receiver/data/`.
- Приложение 2 читает и пишет батч-файлы в `data/` (в корне репозитория).
- Перед запуском обработки в Приложении 2 скопируйте нужные файлы из `app1-xml-receiver/data/` в корневую `data/`.

## Требования
- Java 17+
- Maven 3.6+

## Шаг 1. Запуск Приложения 1 (XML Receiver)
- Назначение: принимает XML (HTTP), конвертирует в JSON, пишет в файлы по типу: `{Type}-{date}.log` с заголовком `RECORD_COUNT:<N>`

Команды:
```bash
cd app1-xml-receiver
mvn spring-boot:run
```
Проверка:
```bash
curl -s http://127.0.0.1:9091/api/health
# Ожидаемый ответ: {"status": "OK", "service": "XML Receiver"}
```

## Шаг 2. Отправка запросов в Приложение 1
- Эндпоинт: `POST http://127.0.0.1:9091/api/xml`
- Content-Type: `application/xml`

Минимальный пример:
```bash
curl -X POST http://127.0.0.1:9091/api/xml \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8" ?>
<Data>
<Type>Information</Type>
<Layer>TestLayer</Layer>
</Data>'
```
Ожидаемый ответ (JSON):
```json
{"method":null,"process":null,"layer":"TestLayer","creation":null,"type":"Information"}
```

Полный пример из ТЗ:
```bash
curl -X POST http://127.0.0.1:9091/api/xml \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8" ?>
<Data>
<Method>
<Name>Order</Name>
<Type>Services</Type>
<Assembly>ServiceRepository, Version=1.0.0.1, Culture=neutral, PublicKeyToken=null</Assembly>
</Method>
<Process>
<Name>scheduler.exe</Name>
<Id>185232</Id>
<Start>
<Epoch>1464709722277</Epoch>
<Date>2016-05-31T12:07:42.2771759+03:00</Date>
</Start>
</Process>
<Layer>DailyScheduler</Layer>
<Creation>
<Epoch>1464709728500</Epoch>
<Date>2016-05-31T07:48:21.5007982+03:00</Date>
</Creation>
<Type>Information</Type></Data>'
```
Результат:
- Файл появится в `app1-xml-receiver/data/Information-YYYY-MM-DD.log`
- Первая строка: `RECORD_COUNT:<число_записей>`

Посмотреть файл:
```bash
head -10 app1-xml-receiver/data/Information-$(date +%F).log
```

## Шаг 3. Подготовка данных для Приложения 2
- Скопируйте нужные файлы из `app1-xml-receiver/data/` в корневую `data/`:
```bash
mkdir -p data
cp app1-xml-receiver/data/*.log data/
```

## Шаг 4. Запуск Приложения 2 (Batch Processor)
- Назначение: читает файлы из `data/` и пакетирует каждые 100 записей в файлы `*-0001.log`, `*-0002.log`, ...
- Состояние прогресса хранится в `processing_state.json`

Команды:
```bash
cd app2-batch-processor
mvn spring-boot:run
```
Проверка:
```bash
curl -s http://127.0.0.1:8080/api/health
# Ожидаемый ответ: {"status": "OK", "service": "Batch Processor"}
```

## Шаг 5. Запуск пакетной обработки
Эндпоинт:
- `POST http://127.0.0.1:8080/api/process`

Команда:
```bash
curl -X POST http://127.0.0.1:8080/api/process
```
Ожидаемый результат:
- В `data/` появятся файлы вида: `Information-YYYY-MM-DD-0001.log`, `Information-YYYY-MM-DD-0002.log`, ...
- В каждом батч-файле первая строка: `BATCH_SIZE:<кол-во_записей_в_батче>`
- Обновится `processing_state.json`

Проверка файлов:
```bash
ls -la data/
head -5 data/Information-$(date +%F)-0001.log
cat processing_state.json
```

## Эндпоинты (сводно)
- Приложение 1 (порт 9091):
  - `GET /api/health` — проверка состояния
  - `POST /api/xml` — прием XML, ответ JSON
- Приложение 2 (порт 8080):
  - `GET /api/health` — проверка состояния
  - `POST /api/process` — запустить обработку

## Примеры XML для тестирования
Information:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<Data>
<Method>
<Name>Order</Name>
<Type>Services</Type>
<Assembly>ServiceRepository, Version=1.0.0.1, Culture=neutral, PublicKeyToken=null</Assembly>
</Method>
<Process>
<Name>scheduler.exe</Name>
<Id>185232</Id>
<Start>
<Epoch>1464709722277</Epoch>
<Date>2016-05-31T12:07:42.2771759+03:00</Date>
</Start>
</Process>
<Layer>DailyScheduler</Layer>
<Creation>
<Epoch>1464709728500</Epoch>
<Date>2016-05-31T07:48:21.5007982+03:00</Date>
</Creation>
<Type>Information</Type></Data>
```

Trace:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<Data>
<Method>
<Name>Process</Name>
<Type>System</Type>
<Assembly>SystemCore, Version=2.0.0.1, Culture=neutral, PublicKeyToken=null</Assembly>
</Method>
<Process>
<Name>worker.exe</Name>
<Id>185233</Id>
<Start>
<Epoch>1464709723000</Epoch>
<Date>2016-05-31T12:08:00.0000000+03:00</Date>
</Start>
</Process>
<Layer>WorkerLayer</Layer>
<Creation>
<Epoch>1464709729000</Epoch>
<Date>2016-05-31T07:49:00.0000000+03:00</Date>
</Creation>
<Type>Trace</Type></Data>
```

## Устранение неполадок
- Порт занят:
```bash
lsof -ti:9091 | xargs kill -9
lsof -ti:8080 | xargs kill -9
```
- Файлы не создаются в App1:
  - смотрите логи запуска, проверьте права на запись
- App2 не видит файлы:
  - убедитесь, что исходные файлы скопированы в корневую `data/`
  - смотрите `batch-processor.log` и `processing_state.json`
