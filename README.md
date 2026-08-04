# efgen_bosh

Внутренняя система кузовного автосервиса Bosh для работы с автомобилями,
страховыми случаями, деталями и исполнителями.

Стек проекта:

- Vue 3 + Vite;
- Java 21 + Spring Boot;
- PostgreSQL;
- Flyway;
- Spring Security;
- серверный адаптер для AI-дефектовки.

Актуальная версия и история релизов описаны в [RELEASE.md](RELEASE.md).

## Структура

```text
app/                  Spring Boot REST API
web/                  Vue 3 SPA
ops/                  шаблоны эксплуатационной конфигурации
scripts/              локальные и CI-проверки
.github/workflows/    GitHub Actions
```

Java-код размещается в `com.efgenbosh.backend` и раскладывается по слоям:

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

Реализованы основные рабочие модули:

- авторизация и роли пользователей;
- реестр автомобилей с поиском, фильтрацией, сортировкой и пагинацией;
- страховые случаи с единой статусной моделью;
- детали страхового случая, поставщики, плановые даты, поступление и отказ;
- назначение и замена исполнителей;
- история действий и изменений по случаю;
- фотографии повреждений, работы и документы;
- справочники страховых компаний, поставщиков, исполнителей и работ;
- CI-проверки и автоматический deploy ветки `dev`.

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
curl http://127.0.0.1:8081/api/health
```

Ожидаемый ответ:

```json
{"status":"UP","service":"efgen-bosh-app"}
```

## Запуск web

```bash
cd web
npm install
npm run dev -- --host 127.0.0.1 --port 5174
```

Web: `http://127.0.0.1:5174/`.

## Проверки

```bash
mvn -f app/pom.xml test
bash scripts/validate-migrations.sh
cd web
npm run lint
npm test
npm run build
```

## Релизы

Релизная информация хранится в [RELEASE.md](RELEASE.md). После существенных
изменений обновляются версия и история релизов.

## Dev deployment

Ветка `dev` является источником тестового деплоя. Push в `dev` запускает
`.github/workflows/deploy-dev.yml`: GitHub Actions проверяет backend и frontend,
собирает релиз и передаёт его на тестовый сервер. Сервер заменяет JAR только
после остановки Java-сервиса, сохраняет предыдущий runtime, запускает smoke-check
и при ошибке восстанавливает предыдущий JAR и frontend.

В GitHub Environment `test` должен быть настроен один secret:

- secret: `VPS_SSH_KEY`.

Хост, пользователь и публичный URL зафиксированы в workflow.

Пароли БД и JWT-секреты остаются только в `/etc/efgen-bosh/test` на сервере.
