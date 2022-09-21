# Tracing PoC

## Problem 

На данный момент при возникновении проблемы приходится задействоать 
обе команды для локализации проблемы. Так же по логам сложно найти 
записи, которые относятся к конкретному запросу (ошибка видна в логах,
но что привело к ошибке неочевидно или вовсе теряется на фоне большого
объема логов).

## Solution

Обычной практикой в микросервисной архитектуре вводить систему
трайсинга запросов, который позволяет составить иерархию запросов. 
В такой иерахии с легкостью можно найти источник проблемы.
Так же идентификаторы трэйсов добавляются в систему логирования, 
что позволяет найти все логи по одному трэйсу по простому запросу. 

## How apply solution

TODO...

## Service list

- Analytic service
- City service 
- Event service
- Place service
- Route service
- Trip builder service (aka trip-builder)
- Trip service (aka nology-backend)

## Run

### Services

#### Analytic service

```shell
cd analytic-service
./gradlew bootRun
```

#### City service

```shell
cd city-service
./gradlew bootRun
```

#### Event service

```shell
cd event-service
./gradlew bootRun
```

#### Place service

```shell
cd place-service
./gradlew bootRun
```

#### Route service

```shell
cd route-service
./gradlew bootRun
```

#### Trip service

```shell
cd trip-service
./gradlew bootRun
```

#### Trip builder service

```shell
cd trip-builder-service
python main.py
```

### Zipkin

#### Elasticsearch

```shell
docker-compose -f docker-compose-elasticsearch.yml
```

#### MySQL

```shell
docker-compose -f docker-compose-mysql.yml
```

## Remarks

### py_zipkin

В `py_zipkin` пришлось вносить правки согласно с моделью данных zipkin.
По этой причине необходимо форкать проект `py_zipkin` и исправленую версию хранить в локальном репозитории

### Zipkin on Elasticsearch

Не работает dependencies.

### Как много хранить трэйсов?

На продуктивие стоит логировать малую долю трейсов, дабы не 
снизить производительность высонагруженой системы.

### Как долго хранить трэйсы?

Долгое хранение трейсов приведет к избыточному объемы данный.
Поэтому их переодически необходимо удалять. 
Но вознкновении проблемы из-за среднего или никого приоритета
задачу могу начать решать после того, как трэйсы будут 
удалены, для этого вводится `архив`. Архив это еще один 
инстанс `zipkin` в котором трейсы храняться "вечно". В данный
инстанс трэйсы поадают только по команде пользователя (разработчика, 
тестировщика и т.п.).

