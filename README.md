# SMS Phishing Protection Service

A lightweight, scalable MVP application for detecting phishing attempts in SMS messages using Google Web Risk API.

---

## Table of Contents
1. [Overview](#overview)
1. [Terminology](#terminology)
1. [Client Outline](#client-outline)
1. [Main Functionalities](#main-functionalities)
1. [Assumptions](#assumptions)
1. [Architecture](#architecture)
1. [Database Schema](#database-schema)
1. [Tech Stack / Tools that I used and why](#tech-stack--tools-that-i-used-and-why)
1. [Main areas for future improvements](#main-areas-for-future-improvements)
1. [How to Run](#how-to-run)

---

## Overview
Application for detecting phishing in SMS messages, from several banks' clients.

---

## Terminology

### SMS Message
Message in JSON format sent to application API:

    {
      "sender": "<SENDER_NUMBER>",
      "receiver": "<RECEIVER_NUMBER>",
      "message": "<BODY_OF_MESSAGE>"
    }

### Message (Body)
The actual content of the SMS message in plain text -> <BODY_OF_MESSAGE> from above.

### Web Risk API
Used for URL validation:  
https://cloud.google.com/web-risk/docs/reference/rest/v1eap1/TopLevel/evaluateUri

---

## Client Outline
In this hypothetical scenario, the client is a group of banks that reach out to me (a telecom operator) to act as a middleman in handling malicious SMS messages for their individual clients.  
Each phone number is treated as a unique client.

---

## Main Functionalities

### 1. Phishing Detector
- Every message from a subscribed client is scanned for URLs.
- If a URL exists, it is validated using Google Web Risk API.
- If malicious → the SMS is **not** stored.
- If safe → stored in database.

### 2. Subscription API
Clients subscribe/unsubscribe by sending an SMS to **000000000** number with `START` or `STOP` message.

Format of SMS message:

    {
      "sender": "<SENDER_NUMBER>",
      "receiver": "000000000",
      "message": "START/STOP"
    }

- `START` → add number to subscribers table  
- `STOP` → remove number

### 3. SMS Message Storing
All validated SMS messages are stored in the database.  
Each client is guaranteed that all non-malicious SMS messages are persisted.

---

## Assumptions
- Solution should be as cost efficcient as it can, because it probably gonna scale much
- Solution should be prepared to scale easily - preferably scale out - and be rather lightweigt, to limit future costs when scaled
- App should be fairly readible and easy to understand and maintain for hypothetical future developers, should limit itself to main scenario but be prepared to do it properly, should be easy to spin up quickly and pushed to prod quickly
- Service is responsible for storing every validated message in database
- This application is treated as mvp, that should be prepared for future expansion

---
## Architecture

### High-Level Components
- **Phishing Detector** — checks SMS messages for presence of a URI and validates it using Google Web Risk API.
- **Subscription Handling** — processes START/STOP messages sent to a dedicated number (000000000) and updates the subscribers list.
- **Message Storage Component** — stores validated SMS messages in the database.
- **Asynchronous Processing Pipeline** — ensures SMS messages are processed without guaranteeing immediate handling.

### Flow Summary
1. Incoming SMS arrives in JSON format.
2. If it is sent to the subscription number:
   - `START` → number is written to the subscribers table  
   - `STOP` → number is deleted
3. Otherwise, if client is a subscriber, message is checked for the presence of a URI.
4. If a URI exists, it is validated using Google Web Risk API.
5. If the link is malicious → the message is **not** stored.
6. If the link is safe → the full original SMS is saved in the database.

---

## Tech Stack / Tools that I used and why

### Java
It's the JVM language that I have most expierience with.

### Kafka Streams
The use case seems to be perfect for a stream tool:
- it would benefit from being handled asynchroniously, hignly available and prepared for scaling out
- I chose Kafka, since it integrates well with Java, was listed as one of technologies in job offer and I recently been working with Kafka on my data engineering program.

### Redis
simmilary to Kafka, Redis seems ideal for such a use case of event-driven architecture:
- only two tables are required, so no need for a relational DB  
- low latency is ideal for SMS processing  
- high performance for simple read/write operations  
- good fit for future caching features  
- easier to implement for the MVP than SQL databases
- Redis was also listed as one of technologies in job offer

**TODO:** choose testing tools

### Docker
For running the app.

### K3S
For running Kafka and Zookeper. I chose it instead of K8s, since it will be more forgiving for my PC and I also plan for default setup only 1 pod for each tool. This way I resign from HA, but it's fairly easy to add more pods and therefore ensuring HA, in the same time for development I won't be testing for huge amounts of data.

## Database Schema

### subscribers
| field        | type   | description               |
|--------------|--------|---------------------------|
| phone_number | string | unique client identifier  |

### sms
| field     | type     | description                           |
|-----------|----------|----------------------------------------|
| sender    | string   | sender phone number                    |
| receiver  | string   | receiver phone number                  |
| message   | string   | full SMS body                          |
| timestamp | datetime | time of processing (optional, MVP)     |

---

## Main areas for future improvements
- Cache URL validation results to reduce Google API cost
- Performance tuning for high load
- Monitoring & alerting
- Message processing metrics

---

## How to Run

> **TODO:** Add Docker setup instructions and example commands.

---
