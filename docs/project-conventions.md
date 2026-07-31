# Соглашения проекта

`bg_foot_project` является инженерным эталоном для `efgen_bosh`.

## Что сохраняем одинаковым

- корневые каталоги `app`, `web`, `docs`, `ops`, `scripts`;
- Java 21, Maven и Spring Boot;
- Java-пакет приложения с суффиксом `.backend`;
- слои `config`, `controller`, `domain`, `dto`, `health`, `repository`,
  `security`, `service`;
- Vue 3 на JavaScript;
- каталоги web-приложения `api`, `components`, `composables`, `data`, `pages`,
  `router`, `store`, `utils`;
- команды `dev`, `build`, `lint`, `test`, `test:e2e`;
- Flyway-миграции `V<номер>__<описание>.sql`;
- CI-проверки backend, web и PostgreSQL migrations;
- локальный PostgreSQL как системный сервис, без Docker Compose;
- секреты только через переменные окружения.

## Что не копируем

- футбольные сущности и правила;
- роли футбольной лиги;
- UI и бренд футбольного проекта;
- deployment-адреса и секреты;
- код, который не относится к процессам автосервиса.

## Правило для новых изменений

Перед созданием нового каталога, библиотеки или отдельного способа запуска
проверяем, как эта задача решена в `bg_foot_project`. Если для Bosh требуется
отклонение, оно фиксируется отдельным ADR.
