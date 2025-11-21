#!/bin/bash

echo "Testing non-subscriber functionality..."

# Send message to non-subscribed number
echo "Sending message to non-subscribed number..."
curl -X POST http://localhost:8080/sms \
-H "Content-Type: application/json" \
-d '{
    "sender": "48987654321",
    "receiver": "48111222333",
    "message": "This message should be stored without phishing check"
}'

echo -e "\nTest completed.\n"