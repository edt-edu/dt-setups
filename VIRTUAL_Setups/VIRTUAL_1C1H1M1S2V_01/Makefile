SHELL := /bin/bash

PROJECT_ROOT := $(shell cd ../.. && pwd)
PROJECT_VIRTUAL_CONFIG := VIRTUAL_Setups/VIRTUAL_1C1H1M1S2V_01/config

MOSQUITO_PATH := ${PROJECT_ROOT}/VIRTUAL_Setups/VIRTUAL_shared/mosquitto
KAFKA_PATH := ${PROJECT_ROOT}/VIRTUAL_Setups/VIRTUAL_shared/kafka
FRONTEND_PATH := ${PROJECT_ROOT}/factoryscada/frontend
BACKEND_PATH := ${PROJECT_ROOT}/factoryscada/backend
GATEWAY_PATH := ${PROJECT_ROOT}/REN_Setups/dt-support/gateway
PLC_PATH := ${PROJECT_ROOT}/${PROJECT_VIRTUAL_CONFIG}/PLC1
GRAFANA_PATH := ${PROJECT_ROOT}/${PROJECT_VIRTUAL_CONFIG}/server/gateway
INFLUXDB_PATH := ${PROJECT_ROOT}/${PROJECT_VIRTUAL_CONFIG}/server/gateway
CONDA_PATH := $(shell conda info --base)

MISSIONS_CONFIG_PATH := ${PROJECT_ROOT}/${PROJECT_VIRTUAL_CONFIG}/server/factoryscada/services/scada-backend/config/missions-parallelized-configuration.yml
BACKEND_CONFIG_PATH := ${PROJECT_ROOT}/${PROJECT_VIRTUAL_CONFIG}/server/factoryscada/services/scada-backend/config/configuration.yml
GATEWAY_CONFIG_PATH := ${PROJECT_ROOT}/${PROJECT_VIRTUAL_CONFIG}/server/gateway/services/gateway/config/configuration.yml

.PHONY: up down restart status attach \
        mqtt plc scada-frontend scada-backend gateway cep

# ------------------------------------------------------------------------------
# High-level targets
# ------------------------------------------------------------------------------

up: restart status
	@echo ""
	@echo "🚀 All services are running (fresh start)."

restart: mqtt scada-frontend scada-backend plc gateway kafka

down:
	@echo "🛑 Stopping all services..."
	-@tmux kill-session -t mqtt
	-@tmux kill-session -t plc
	-@tmux kill-session -t scada-frontend
	-@tmux kill-session -t scada-backend
	-@tmux kill-session -t gateway
	-@tmux kill-session -t kafka
	@echo "✔ All tmux sessions stopped"

# ------------------------------------------------------------------------------
# Services (always restarted)
# ------------------------------------------------------------------------------

mqtt:
	@echo "🔄 Restarting MQTT broker..."
	-@tmux kill-session -t mqtt 2>/dev/null || \
		echo "ℹ️  MQTT session not running (nothing to stop)"
	@tmux new-session -d -s mqtt \
		"cd ${MOSQUITO_PATH} && docker compose up"
	@echo "✔ MQTT broker started"
	@echo "  → Logs: tmux attach -t mqtt"

plc:
	@echo "🔄 Restarting simulated PLC controller..."
	-@tmux kill-session -t plc 2>/dev/null || \
		echo "ℹ️  PLC session not running (nothing to stop)"
	@tmux new-session -d -s plc \
		"source ${CONDA_PATH}/etc/profile.d/conda.sh && \
		 conda activate rppmcontroller && \
		 cd ${PLC_PATH} && \
		 python3 SimulatedMachines_Controller.py"
	@echo "✔ PLC controller started (conda env: rppmcontroller)"
	@echo "  → Logs: tmux attach -t plc"

scada-frontend:
	@echo "🔄 Restarting SCADA frontend..."
	-@tmux kill-session -t scada-frontend 2>/dev/null || \
		echo "ℹ️  SCADA frontend session not running (nothing to stop)"
	@tmux new-session -d -s scada-frontend \
		"cd ${FRONTEND_PATH} && npm run start"
	@echo "✔ SCADA frontend started"
	@echo "  → Logs: tmux attach -t scada-frontend"

scada-backend:
	@echo "🔄 Restarting SCADA backend..."
	-@tmux kill-session -t scada-backend 2>/dev/null || \
		echo "ℹ️  SCADA backend session not running (nothing to stop)"
	@tmux new-session -d -s scada-backend \
		"cd ${BACKEND_PATH} && \
		./gradlew bootrun --args='--missions.parallelized.configuration.path=${MISSIONS_CONFIG_PATH} \
		--configuration.path=${BACKEND_CONFIG_PATH}'"
	@echo "✔ SCADA backend started"
	@echo "  → Logs: tmux attach -t scada-backend"

gateway: grafana influxdb
	@echo "🔄 Restarting gateway service..."
	-@tmux kill-session -t gateway 2>/dev/null || \
		echo "ℹ️  Gateway session not running (nothing to stop)"
	@tmux new-session -d -s gateway \
		"cd ${GATEWAY_PATH} && \
		./gradlew bootrun --args='--configuration.path=${GATEWAY_CONFIG_PATH}'"
	@echo "✔ Gateway service started"
	@echo "  → Logs: tmux attach -t gateway"

grafana:
	@echo "🔄 Restarting Grafana service..."
	-@tmux kill-session -t grafana 2>/dev/null || \
		echo "ℹ️  Grafana session not running (nothing to stop)"
	@tmux new-session -d -s grafana \
		"cd ${GRAFANA_PATH} && \
		docker compose up grafana"
	@echo "✔ Grafana service started"
	@echo "  → Logs: tmux attach -t grafana"


influxdb:
	@echo "🔄 Restarting InfluxDB service..."
	-@tmux kill-session -t influxdb 2>/dev/null || \
		echo "ℹ️  InfluxDB session not running (nothing to stop)"
	@tmux new-session -d -s influxdb \
		"cd ${INFLUXDB_PATH} && \
		docker compose up influxdb2"
	@echo "✔ InfluxDB service started"
	@echo "  → Logs: tmux attach -t influxdb"

kafka:
	@echo "🔄 Restarting Kafka service..."
	-@tmux kill-session -t kafka 2>/dev/null || \
		echo "ℹ️  Kafka session not running (nothing to stop)"
	@tmux new-session -d -s kafka \
		"cd ${KAFKA_PATH} && \
		docker compose up"
	@echo "✔ Kafka service started"
	@echo "  → Logs: tmux attach -t kafka"

# ------------------------------------------------------------------------------
# Utilities
# ------------------------------------------------------------------------------

status:
	@echo ""
	@echo "📊 tmux session status:"
	@tmux ls 2>/dev/null || echo "ℹ️  No tmux server running"

attach:
ifndef SERVICE
	$(error Usage: make attach SERVICE=<mqtt|plc|scada-frontend|scada-backend|gateway|grafana|influxdb|kafka>)
endif
	tmux attach -t $(SERVICE)
