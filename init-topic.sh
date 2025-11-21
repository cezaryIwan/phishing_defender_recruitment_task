#!/bin/bash

echo "Creating topic..."
kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists \
  --topic sms-messages --partitions 1 --replication-factor 1

echo "Topic created."
