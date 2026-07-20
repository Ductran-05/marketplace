.PHONY: help up down logs ps run dev build test clean reset-db db mail swagger health frontend

help:            ## Liệt kê các lệnh
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-12s\033[0m %s\n", $$1, $$2}'

up:              ## Bật infrastructure (Postgres, Redis, Kafka, Mailhog)
	docker compose up -d

down:            ## Tắt infrastructure
	docker compose down

logs:            ## Xem logs infrastructure
	docker compose logs -f

ps:              ## Trạng thái các container
	docker compose ps

run:           ## Bật infra + chạy app
	./mvnw spring-boot:run

dev: up          ## Chạy app với profile dev
	./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

build:           ## Build jar (bỏ qua tests)
	./mvnw clean package -DskipTests

test:            ## Chạy toàn bộ tests
	./mvnw test

clean:           ## Xóa build artifacts
	./mvnw clean

reset-db:        ## Xóa sạch DB và data, tạo lại
	docker compose down -v
	docker compose up -d

mail:            ## Mở Mailhog UI
	open http://localhost:8025

dbgate:          ## Mở DbGate UI (xem DB bằng trình duyệt)
	open http://localhost:3100

redis:           ## Mở redis-cli console
	docker exec -it marketplace-redis redis-cli

minio:           ## Mở MinIO Console (user/pass: minioadmin)
	open http://localhost:9001

kafka-ui:        ## Mở Kafka UI (xem topic, message, consumer group)
	open http://localhost:8090

swagger:         ## Mở Swagger UI
	open http://localhost:8080/swagger-ui.html

health:          ## Kiểm tra app còn sống không
	@curl -s localhost:8080/actuator/health | python3 -m json.tool

frontend:        ## Cài đặt (nếu cần) và chạy dev server React (http://localhost:5173)
	@test -d frontend/node_modules || (cd frontend && npm install)
	cd frontend && npm run dev
