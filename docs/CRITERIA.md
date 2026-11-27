# Критерии и фактическая проверка

Проверка от 27 ноября 2026. Файлы Lab 4 и Lab 5 прочитаны; дополнительные
требования final взяты из сообщения преподавателя, переданного пользователем.
Отдельной официальной полной рубрики final в исходных материалах нет.

## Lab 5 — ветка lab5-security

| Критерий | Реализация / доказательство |
|---|---|
| Тот же продукт, Java 21, Boot 3.4.x | Campus Lost & Found; pom 3.4.13 |
| Две роли и таблица правил | FINDER, CUSTODIAN; README |
| BCrypt, два dev пользователя, без users table | DevUsers, @Profile(dev), DelegatingPasswordEncoder |
| Login и одинаковый плохой login | AuthController, LoginContractTest: имя/пароль → одинаковый 401 |
| HS256, issuer, 900 секунд, roles | TokenService, JwtConfiguration, TokenServiceTest |
| Ключ вне main Java, короткий ключ не стартует | application-dev/prod.yml, JwtProperties |
| 401/403 с общей формой и correlationId | SecurityErrors, SecurityContractTest |
| Узкое правило раньше общего, denyAll | SecurityConfiguration |
| Stateless, no form/Basic, CSRF off | SecurityConfiguration |
| Реальная смена статуса, actor из Authentication | LostItemController → LostItemService.markFound |
| Junior не дошёл до сервиса | Web slice проверяет verifyNoInteractions |
| Method security, domain обычная Java | @PreAuthorize и MethodSecurityTest; domain без framework imports |
| Запрет по политике не даёт 200 | Повторный FOUND → 409 INVALID_TRANSITION |
| /api/me, health/docs | MeController, ActuatorTest, OpenApiConfiguration |
| Старые 201/400/404/409 теперь с token | LostItemControllerTest |
| Своя auth page, logout, no raw JWT | static/index.html, app.css, app.js |
| Тесты и старая транзакционная семантика | verify: 35 tests, failures 0/errors 0/skipped 0; counts 0 и 1 |
| Без Docker/users service/real outbound в сдаче | Отдельная ветка Lab 5; final не подставлять вместо неё |

## Дополнительные требования final — ветка final-microservices

| Требование | Что сделано | Что ещё требует внешнего запуска |
|---|---|---|
| Микросервис, не один монолит | items + accounts, отдельные pom/jar/процессы; HTTP RestClient | Локальная связка реально запускалась |
| Регистрация и users, не ручные INSERT | UI → items → accounts → BCrypt users | Браузерная регистрация вернула 201 |
| Данные сохраняются | После перезапуска обоих приложений и локальной PostgreSQL прежний аккаунт вошёл, прежняя вещь найдена | Проверить то же на сервере |
| Flyway | Отдельная история миграций у каждой БД; root V1 не переписана | При размещении стартовать на чистых собственных базах |
| Логи действий | auth_event и lost_item_event; Flyway создаёт их структуру | Flyway history — история схемы, не журнал действий |
| Полноценный frontend | Вход/регистрация/роль/вещи/поиск/смена/выход, desktop/mobile | Реальные UI 201/403/200/401 проверены локально |
| Docker | Два образа собраны и запущены на GitHub Actions. Регистрация и сохранение вещи пережили перезапуск контейнеров | На ноутбуке Docker Engine недоступен |
| GitHub | Lab 5 PR #2, final PR #3. GitHub Actions успешно прошёл в обоих | — |
| Сервер | VPS-инструкция, Caddy, HTTPS, env и backup | Пользователь сообщил, что сервера пока нет; покупка/адрес/домен и deployment ещё нужны |
| Презентация | Подготовлена отдельная презентация на 10 слайдов | Не заменяет живую демонстрацию |
| Без Kafka | Kafka не добавлена | — |

## Что и как проверялось

- Основной сервис: `mvn -q verify`, 35 тестов, успешный код завершения.
- Accounts: собственный `mvn -q verify`, 2 интеграционных теста, успешное завершение.
- Тестовая PostgreSQL 18 изолирована от исходной css. Миграции применились
  автоматически; старые rollback/commit assertions сохранены.
- Реальные два Java-процесса: signup 201, login 200, junior create 201,
  junior locked 403 с REPORTED, senior locked 200 с FOUND, list 200, anonymous 401.
- Web slices используют работающие security filters, jwt() и mock service;
  настоящий token дополнительно проверен encoder → decoder и HTTP-тестом.
- [Lab 5 CI](https://github.com/GumballPenny/-campus-lost-and-found/actions/runs/46015527273)
  прошёл. [Final CI](https://github.com/GumballPenny/-campus-lost-and-found/actions/runs/46015534453)
  прошёл оба задания: Maven verify и реальный запуск Compose.
- Развёртывание на VPS и публичный HTTPS проверяются после появления сервера.

## Ограничения учебного final

Регистрация senior по приватному приглашению достаточна для демонстрации двух
ролей, но для публичной эксплуатации нужен управляемый процесс назначения роли.
JWT не отзывается мгновенно; logout удаляет token из памяти браузера, срок — 15
минут. Нет refresh/recovery/MFA/rate limit. Общий HMAC-ключ означает общую зону
доверия двух сервисов. Эти возможности не заявлены реализованными.
