#!/bin/bash

echo "Testing subscription/unsubscription flow..."

# Subscribe first number
echo "1. Subscribing number 48111222333..."
curl -v -X POST http://localhost:8080/sms \
-H "Content-Type: application/json" \
-d '{
    "sender": "48111222333",
    "receiver": "000000000",
    "message": "START"
}'

echo -e "\n\nWaiting 2 seconds...\n"
sleep 2

# Subscribe second number
echo "2. Subscribing number 48444555666..."
curl -v -X POST http://localhost:8080/sms \
-H "Content-Type: application/json" \
-d '{
    "sender": "48444555666",
    "receiver": "000000000",
    "message": "START"
}'

echo -e "\n\nChecking database after subscriptions..."
docker exec -it phishing_defender_recruitment_task-mongodb-1 mongo sms_db --eval 'db.subscribers.find().toArray()' --quiet

echo -e "\nWaiting 2 seconds...\n"
sleep 2

# Unsubscribe first number
echo "3. Unsubscribing number 48111222333..."
curl -v -X POST http://localhost:8080/sms \
-H "Content-Type: application/json" \
-d '{
    "sender": "48111222333",
    "receiver": "000000000",
    "message": "STOP"
}'

echo -e "\n\nChecking final state of database..."
docker exec -it phishing_defender_recruitment_task-mongodb-1 mongo sms_db --eval 'db.subscribers.find().toArray()' --quiet

echo -e "\nTest completed.\n"