#!/bin/bash

# Script for testing Kafka setup
# Creates topic, sends message, reads message, then cleans up

echo "Starting Kafka test..."

# Container name - adjust if needed
KAFKA_CONTAINER="phishing_defender_recruitment_task-kafka-1"

# Create test topic
echo "Creating test topic..."
docker exec -it $KAFKA_CONTAINER /bin/kafka-topics \
    --create \
    --topic test-topic \
    --bootstrap-server localhost:9092

# Send test message
echo "Test message from $(date)" | docker exec -i $KAFKA_CONTAINER \
    /bin/kafka-console-producer \
    --topic test-topic \
    --bootstrap-server localhost:9092

# Wait for message to be processed
sleep 2

# Read messages from topic
echo "Reading messages from topic..."
docker exec -it $KAFKA_CONTAINER \
    /bin/kafka-console-consumer \
    --topic test-topic \
    --from-beginning \
    --bootstrap-server localhost:9092 \
    --max-messages 1

# Clean up - delete test topic
echo "Cleaning up - deleting test topic..."
docker exec -it $KAFKA_CONTAINER /bin/kafka-topics \
    --delete \
    --topic test-topic \
    --bootstrap-server localhost:9092

echo "Kafka test completed."