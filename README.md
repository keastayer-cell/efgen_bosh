# efgen_bosh

Новая реализация внутренней системы кузовного автосервиса Bosh.

Проект переносится со смешанной Firebase/Firestore/Cloudflare/localStorage
архитектуры на стандартный стек:

- Vue 3 + Vite;
- Java 21 + Spring Boot;
- PostgreSQL;
- Flyway;
- Spring Security;
- серверный адаптер для AI-дефектовки.

Инженерные соглашения, структура репозитория и команды разработки повторяют
проект `bg_foot_project`. Предметная область при этом остаётся отдельной.

## Структура

```text
app/                  Spring Boot REST API
web/                  Vue 3 SPA
docs/                 архитектура и правила переноса
ops/                  шаблоны эксплуатационной конфигурации
scripts/              локальные и CI-проверки
.github/workflows/    GitHub Actions
```

Java-код размещается в `com.efgenbosh.backend` и раскладывается по тем же слоям,
что в футбольном проекте:

```text
config/
controller/
domain/
dto/
health/
repository/
security/
service/
```

Vue-код использует каталоги `api`, `components`, `composables`, `data`, `pages`,
`router`, `store` и `utils`.

## Текущий статус

Реализован инфраструктурный фундамент:

- Spring Boot приложение;
- публичный health endpoint;
- закрытая по умолчанию конфигурация Spring Security;
- PostgreSQL для локальной разработки;
- первая Flyway-миграция пользователей, ролей и аудита;
- Vue-каркас и API-клиент;
- backend, frontend и migration проверки в CI.

Бизнес-модули пока не реализованы. Перед переносом их поведение фиксируется
characterization-тестами старой системы.

## Требования

- Java 21;
- Maven 3.6.3+;
- PostgreSQL 17 либо Docker-совместимый container runtime;
- Node.js `^22.18.0 || >=24.12.0`;
- npm.

## Локальная база

```bash
cp .env.example .env
# Замените BOSH_DB_PASSWORD в .env.
docker compose up -d postgres
```

PostgreSQL доступен на `127.0.0.1:5434`, чтобы не конфликтовать с другими
локальными проектами.

## Запуск backend

```bash
set -a
source .env
set +a
mvn -f app/pom.xml spring-boot:run
```

Проверка:

```bash
curl http://127.0.0.1:8080/api/health
```

Ожидаемый ответ:

```json
{"status":"UP","service":"efgen-bosh-app"}
```

## Запуск web

```bash
cd web
npm install
npm run dev -- --host 127.0.0.1 --port 5173
```

Web: `http://127.0.0.1:5173/`.

## Проверки

```bash
mvn -f app/pom.xml test
bash scripts/validate-migrations.sh
cd web
npm run lint
npm test
npm run build
```

## Документация

- [Этап 0](docs/stage-0-foundation.md)
- [Архитектурное решение](docs/adr/0001-modular-monolith.md)
- [Правила структуры](docs/project-conventions.md)
- [Зафиксированные бизнес-правила](docs/business-rules.md)
