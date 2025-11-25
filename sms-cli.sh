#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# Base URL
BASE_URL="http://localhost:8080/sms"

# Function to send SMS
send_sms() {
    local sender=$1
    local receiver=$2
    local message=$3

    echo -e "${GREEN}Sending SMS:${NC}"
    echo "From: $sender"
    echo "To: $receiver"
    echo "Message: $message"

    response=$(curl -s -X POST $BASE_URL \
        -H "Content-Type: application/json" \
        -d "{
            \"sender\": \"$sender\",
            \"receiver\": \"$receiver\",
            \"message\": \"$message\"
        }")

    echo -e "${GREEN}Response:${NC} $response"
}

# Function to check database
check_db() {
    echo -e "${GREEN}Database contents:${NC}"
    echo -e "\n${GREEN}Subscribers:${NC}"
    docker exec -it phishing_defender_recruitment_task-mongodb-1 mongo sms_db --eval 'db.subscribers.find().pretty()' --quiet
    echo -e "\n${GREEN}Messages:${NC}"
    docker exec -it phishing_defender_recruitment_task-mongodb-1 mongo sms_db --eval 'db.messages.find().pretty()' --quiet
}

# Main menu
while true; do
    echo -e "\n${GREEN}SMS Phishing Defender CLI${NC}"
    echo "1. Subscribe number (START)"
    echo "2. Unsubscribe number (STOP)"
    echo "3. Send a message"
    echo "4. Check database"
    echo "5. Exit"

    read -p "Choose option (1-5): " choice

    case $choice in
        1)
            read -p "Enter phone number to subscribe: " phone
            send_sms "$phone" "000000000" "START"
            ;;
        2)
            read -p "Enter phone number to unsubscribe: " phone
            send_sms "$phone" "000000000" "STOP"
            ;;
        3)
            read -p "From number: " sender
            read -p "To number: " receiver
            read -p "Message: " message
            send_sms "$sender" "$receiver" "$message"
            ;;
        4)
            check_db
            ;;
        5)
            exit 0
            ;;
        *)
            echo -e "${RED}Invalid option${NC}"
            ;;
    esac
done