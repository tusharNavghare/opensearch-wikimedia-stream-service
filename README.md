# Kafka Streaming Data Pipeline – Wikimedia Event Stream

A real-time event streaming application built using **Apache Kafka and OpenSearch** that consumes live events from the Wikimedia Event Stream and processes them through a **Kafka-based distributed streaming pipeline**.

This project focuses on demonstrating **Kafka producer-consumer architecture**, **topic-based message streaming**, and **real-time event processing**, with OpenSearch used as the final storage layer for indexing streamed events.

The main goal is to showcase **how Apache Kafka can be used as the backbone of scalable event-driven systems**.

---

# Project Overview

This system processes **live streaming data from the Wikimedia public event stream** and builds a Kafka-based event pipeline.

The project demonstrates how to:

* Ingest live events into **Apache Kafka**
* Build a **Kafka Producer** to publish events
* Implement a **Kafka Consumer** to process events
* Stream and process events in **real-time**
* Index processed data into **OpenSearch**

Kafka acts as the **central event streaming platform** that decouples the producer and consumer services.

---

# Architecture

The system follows a **Kafka-driven event streaming architecture**.

Core components include:

* **Wikimedia Event Stream API** – External source of real-time events
* **Kafka Producer Service** – Reads streaming events and publishes them to Kafka
* **Kafka Broker** – Central message streaming platform
* **Kafka Topic** – Stores the event stream for consumers
* **Kafka Consumer Service** – Reads events from Kafka and processes them
* **OpenSearch** – Stores processed events for search and analytics

Kafka enables **asynchronous, decoupled communication between services**, allowing producers and consumers to scale independently.

---

# Architecture Diagram

```
Wikimedia Event Stream
        │
        ▼
Kafka Producer
        │
        ▼
Kafka Topic (wikimedia-events)
        │
        ▼
Kafka Consumer
        │
        ▼
OpenSearch
        │
        ▼
Search / Analytics
```

Kafka acts as the **central event streaming backbone**, ensuring reliable message delivery between producers and consumers.

---

# System Architecture Flow

### Kafka Event Streaming Flow

1. The application connects to the **Wikimedia Event Stream API**
2. A **Kafka Producer** consumes live Wikimedia change events
3. Each event is published to a **Kafka topic**
4. Kafka stores the event stream and manages message distribution
5. A **Kafka Consumer** subscribes to the topic
6. The consumer processes incoming events
7. Processed events are indexed into **OpenSearch**

This architecture enables:

* **Real-time event ingestion**
* **Decoupled microservices**
* **Scalable event processing**
* **Reliable message streaming**

---

# Tech Stack

## Streaming Platform

* Apache Kafka
* Kafka Producer API
* Kafka Consumer API

## Backend

* Java

## Search Engine

* OpenSearch

## Infrastructure

* Docker Compose

## Build Tool

* Gradle

---

# Kafka Concepts Demonstrated

This project demonstrates several core **Kafka concepts**:

* **Kafka Producer** – Publishes streaming events
* **Kafka Consumer** – Subscribes to topics and processes events
* **Kafka Topic** – Logical stream of events
* **Event Streaming** – Continuous flow of messages
* **Producer-Consumer Decoupling** – Independent scaling
* **Real-Time Processing** – Immediate event handling

Kafka acts as the **central communication layer of the system**.

---

# Project Structure

```
.
├── KafkaConsumerOpenSearch
│   └── src
│       └── main
│           └── java
│               └── io
│                   └── eventstream
│                       └── opensearch
│                           └── OpenSearchConsumer.java
│
├── KafkaProducerWikimedia
│   └── src
│       └── main
│           └── java
│               └── io
│                   └── eventstream
│                       └── wikimedia
│                           ├── WikimediaChangeHandler.java
│                           └── WikimediaChangesProducer.java
│
├── KafkaWithConduktorDockerCompose
│   └── docker-compose.yml
│
├── OpenSearchDockerCompose
│   └── docker-compose.yml
│
└── README.md
```

Infrastructure services such as **Kafka and OpenSearch** are managed through Docker Compose.

---

# Running the Project

Before running the application, the required infrastructure services must be started.

---

# 1. Start Kafka Infrastructure

Kafka must be started before running the application.

```
cd KafkaWithConduktorDockerCompose
docker compose up -d
```

Services started:

* Kafka

Kafka will be available for producers and consumers.

---

# 2. Start OpenSearch

Start OpenSearch for storing indexed streaming events.

```
cd OpenSearchDockerCompose
docker compose up -d
```

Services started:

* OpenSearch Node

---

# 3. Run the Application

After Kafka and OpenSearch are running, start the producer and consumer applications independently using Gradle.

Run the WikimediaChangesProducer to publish events to Kafka:

```
./gradlew runWikimediaChangesProducer
```

Run the OpenSearchConsumer to consume Kafka events and index them into OpenSearch:

```
./gradlew runOpenSearchConsumer
```

The application will:

* Connect to the Wikimedia event stream
* Publish events into Kafka topics
* Consume events from Kafka
* Index events into OpenSearch

---

# Querying Data from OpenSearch

Once the application is running and events are being streamed through Kafka, the processed data will be indexed into **OpenSearch** by the Kafka consumer.

To verify that the data is successfully indexed, open the following page in your browser:

http://localhost:5601/app/dev_tools#/console

This will open the **OpenSearch Dev Tools Console**.

You can run the following query to retrieve a specific document:

```
GET /wikimedia/_doc/1604c28d-0568-4c07-a8b8-32fae95fbb7f
```

In this query, we are searching for a document using the ID **1604c28d-0568-4c07-a8b8-32fae95fbb7f**.

This ID originates from the **Wikimedia event message structure**. The identifier is extracted from the **`meta`**** section** of the Wikimedia event payload and is used as the document ID when indexing events into OpenSearch.

If the document is returned successfully, it confirms that the **Kafka producer, Kafka consumer, and OpenSearch indexing pipeline are functioning correctly**.

---

# Learning Objectives

This project demonstrates:

* Building **Kafka Producer applications**
* Implementing **Kafka Consumers**
* Designing **real-time event streaming pipelines**
* Using Kafka as a **distributed message broker**
* Integrating Kafka with downstream systems such as **OpenSearch**
* Running Kafka infrastructure locally using **Docker Compose**

---

# Future Improvements

* Introduce dead-letter topics
* Add monitoring with Prometheus and Grafana

---

# Design Decisions

* Kafka is used as the **core event streaming platform**
* Producer and consumer services are **decoupled and to be used independently**
* OpenSearch is used as the **final storage and search layer**
* Docker Compose simplifies **local Kafka infrastructure setup**

---

# Note

* Kafka infrastructure must be started **before running the application**
* Docker Compose is used to run:

  * Kafka Broker
  * OpenSearch
* The project primarily focuses on **demonstrating Kafka streaming architecture and producer-consumer design patterns**.

---

# Author

**Tushar Navghare**
Backend Engineer | Java | Spring Boot | Microservices
