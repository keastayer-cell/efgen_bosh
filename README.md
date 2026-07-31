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
- PostgreSQL 15+;
- Node.js `^22.18.0 || >=24.12.0`;
- npm.

## Локальная база

Создайте пользователя и базу данных:

```sql
CREATE ROLE efgen_bosh LOGIN PASSWORD 'local-password';
CREATE DATABASE efgen_bosh OWNER efgen_bosh;
```

Flyway создаёт и обновляет таблицы при запуске backend. Миграции вручную
применять не нужно.

Убедитесь, что PostgreSQL запущен:

```bash
pg_isready -h 127.0.0.1 -p 5432
```

Для PostgreSQL из Homebrew:

```bash
brew services start postgresql@17
```

Создайте локальный env-файл и замените пароль:

```bash
cp .env.example .env
```

## Запуск backend

```bash
EFGEN_BOSH_ENV_FILE="$PWD/.env" mvn -f app/pom.xml spring-boot:run
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
- [Этап 1.1: база пользователей и ролей](docs/stage-1-auth-database.md)
- [Архитектурное решение](docs/adr/0001-modular-monolith.md)
- [Правила структуры](docs/project-conventions.md)
- [Зафиксированные бизнес-правила](docs/business-rules.md)
