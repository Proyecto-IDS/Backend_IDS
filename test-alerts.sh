#!/bin/bash

# Script to test creating multiple alerts with different incident IDs

BASE_URL="http://localhost:8080"

echo "Creating 5 critical alerts with different incident IDs..."

# Alert 1
echo "Creating alert 1 (INC-001)..."
curl -X POST "$BASE_URL/api/alerts" \
  -H "Content-Type: application/json" \
  -d '{
    "packetId": "pkt-001",
    "incidentId": "INC-001",
    "severity": "critica",
    "score": 0.95,
    "modelVersion": "v1.2.3"
  }'
echo -e "\n"

# Alert 2
echo "Creating alert 2 (INC-002)..."
curl -X POST "$BASE_URL/api/alerts" \
  -H "Content-Type: application/json" \
  -d '{
    "packetId": "pkt-002",
    "incidentId": "INC-002",
    "severity": "critica",
    "score": 0.92,
    "modelVersion": "v1.2.3"
  }'
echo -e "\n"

# Alert 3
echo "Creating alert 3 (INC-003)..."
curl -X POST "$BASE_URL/api/alerts" \
  -H "Content-Type: application/json" \
  -d '{
    "packetId": "pkt-003",
    "incidentId": "INC-003",
    "severity": "alta",
    "score": 0.85,
    "modelVersion": "v1.2.3"
  }'
echo -e "\n"

# Alert 4
echo "Creating alert 4 (INC-004)..."
curl -X POST "$BASE_URL/api/alerts" \
  -H "Content-Type: application/json" \
  -d '{
    "packetId": "pkt-004",
    "incidentId": "INC-004",
    "severity": "critica",
    "score": 0.98,
    "modelVersion": "v1.2.3"
  }'
echo -e "\n"

# Alert 5
echo "Creating alert 5 (INC-005)..."
curl -X POST "$BASE_URL/api/alerts" \
  -H "Content-Type: application/json" \
  -d '{
    "packetId": "pkt-005",
    "incidentId": "INC-005",
    "severity": "alta",
    "score": 0.78,
    "modelVersion": "v1.2.3"
  }'
echo -e "\n"

echo "Getting all alerts..."
curl -X GET "$BASE_URL/api/alerts?limit=100"
echo -e "\n\nDone!"
