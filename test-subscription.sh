#!/bin/bash

echo "Testing subscription functionality..."

# Subscribe number
echo "Subscribing number 48123456789..."
curl -v POST http://localhost:8080/sms \
-H "Content-Type: application/json" \
-d '{
    "sender": "48123456789",
    "receiver": "000000000",
    "message": "START"
}'

echo -e "\n\nWaiting 2 seconds...\n"
sleep 2

# Test normal message without phishing
echo "Sending normal message..."
curl -v POST http://localhost:8080/sms \
-H "Content-Type: application/json" \
-d '{
    "sender": "48987654321",
    "receiver": "48123456789",
    "message": "Hello, this is a test message"
}'

echo -e "\n\nWaiting 2 seconds...\n"
sleep 2

# Test message with suspicious URL
echo "Sending message with suspicious URL..."
curl -v POST http://localhost:8080/sms \
-H "Content-Type: application/json" \
-d '{
    "sender": "48987654321",
    "receiver": "48123456789",
    "message": "Check this link: https://suspicious-bank.com/login"
}'

echo -e "\n\nWaiting 2 seconds...\n"
sleep 2

echo -e "\nTest completed.\n"