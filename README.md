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
The use case (especially taking under consideration growing subscribers' quantity) seems to be perfect for a stream tool:
- it would benefit from being handled asynchroniously
- it asks for  high availability and scaling out -> since application is treated as MVP, I did not apply these two notions, to save my PC some resources - this way I also can omit orchestration tool such as k8s
I chose Kafka, since it integrates well with Java, was listed as one of technologies in job offer and I recently been working with Kafka on my data engineering program.

### MongoDB
Similarly to Kafka, MongoDB seems ideal for such a use case:
- Document-based structure perfect for JSON messages
- Persistent storage by default
- Easily scalable
- Good support for Java
- High performance for read/write operations
- Natural fit for event-driven architecture

**TODO:** choose testing tools

### Docker
For running the app.


## Database Schema

### subscribers collection
```json
{
    "_id": ObjectId,
    "phoneNumber": "string"  // indexed
}
```

### messages collection
```json
{
    "_id": ObjectId,
    "sender": "string",
    "receiver": "string",    // indexed
    "message": "string",
    "timestamp": "date"
}
```
---

## Main areas for future improvements
- introducing high availability with e.g. k8s and more kafka instances
- Cache URL validation results to reduce Google API cost
- Performance tuning for high load
- Monitoring & alerting
- Message processing metrics
- More descriptive status messages for controller
- GUI/CLI for communication with backend

---

## How to Run
  
> **TODO:** Add Docker setup instructions and example commands.

---
