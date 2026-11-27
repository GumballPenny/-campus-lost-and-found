# Защита Lab 5 и финального проекта

## Короткий текст для Lab 5

«Я продолжаю Campus Lost & Found. Добавил аутентификацию через JWT и две роли.
FINDER создаёт и читает вещи, CUSTODIAN дополнительно подтверждает находку.
При входе AuthenticationManager проверяет BCrypt-хеш. Сервер выдаёт подписанный
HS256-токен на 15 минут. На следующих запросах фильтр проверяет подпись, issuer
и срок, затем получает роли. Без токена ответ 401, с недостаточной ролью — 403.
Правило CUSTODIAN проверяется раньше общего правила API и повторяется на методе
сервиса. Автор действия берётся из Authentication, а не из JSON. Покажу создание,
запрет обычному пользователю и успешный переход REPORTED → FOUND хранителем».

## Короткий текст для финального проекта

«В финальной версии я выделил accounts в отдельную Spring-программу со своим
портом, сборкой и базой. Регистрация создаёт users через приложение, хранится
BCrypt-хеш. Основной items вызывает accounts по HTTP для регистрации и входа,
а JWT проверяет самостоятельно. Предметы и их изменения принадлежат items,
аккаунты и события входа — accounts. Flyway версионирует структуру каждой базы.
События записываются приложением в audit-таблицы. Docker Compose описывает оба
сервиса и две базы; Caddy подготовлен для HTTPS на сервере. Публичного сервера
пока нет, поэтому покажу проверенный локальный запуск двух процессов».

## Что открыть в коде по порядку

Все пути основного сервиса ниже начинаются с
`src/main/java/com/example/lostfound/`.

| Файл | Что показать и объяснить |
|---|---|
| `config/Application.java` | `main` вызывает `SpringApplication.run`. Spring собирает контекст. `@SpringBootApplication` включает настройки и поиск компонентов. Сканируем весь `com.example.lostfound`, потому что Application лежит внутри config. |
| `security/DevUsers.java` | `@Profile("dev")`: только учебный запуск. `InMemoryUserDetailsManager` содержит finder/custodian. `PasswordEncoder` создаёт BCrypt-хеши. `DaoAuthenticationProvider` сравнивает пароль с хешем через manager. |
| `handler/AuthController.java` | `@RestController`: JSON API. `@PostMapping`: путь и HTTP-метод. `@Valid` проверяет `@NotBlank` полей LoginRequest. Контроллер передаёт пароль manager, получает Authentication и вызывает TokenService. |
| `security/TokenService.java` | Берёт имя и authorities, убирает ROLE_ для claim, записывает iss/sub/iat/exp/roles. `now.plusSeconds(900)` задаёт 15 минут. `JwtEncoder` подписывает HS256. Пароля в JWT нет. |
| `security/JwtConfiguration.java` | `@Bean` регистрирует encoder/decoder. Общий ключ HMAC; проверяем алгоритм HS256. `JwtValidators.createDefaultWithIssuer` проверяет issuer и временные ограничения. |
| `security/JwtProperties.java` | `@ConfigurationProperties` связывает YAML и Java. Проверка длины ключа останавливает запуск с коротким ключом. В prod/final ключ приходит из окружения. |
| `security/SecurityConfiguration.java` | `SecurityFilterChain` работает до контроллера. Сначала открытые URL, затем узкий `/found` для CUSTODIAN, потом общий API. `anyRequest().denyAll()` закрывает остальное. Converter читает roles и добавляет ROLE_. `STATELESS` не хранит серверную сессию. |
| `security/SecurityErrors.java` | Ошибки фильтров не попадают в обычный controller advice. Поэтому entry point пишет наш 401, access denied handler — наш 403. Везде ApiError и correlationId. |
| `handler/MeController.java` | `/api/me` возвращает имя и authorities из проверенного Authentication. Клиент не назначает роль сам. |
| `handler/LostItemController.java` | Create/read сохранились. Метод `/found` принимает UUID и Authentication, передаёт `getName()` в сервис. Подставленный JSON actor/role не становится автором/ролью. |
| `config/LostItemService.java` | Конструктор принимает правила и repository. `@Service` делает компонент Spring. `@PreAuthorize` повторно защищает метод. `@Transactional` объединяет изменение статуса и аудит: либо оба, либо откат. |
| `domain/LostItemRule.java`, `TransitionRule.java`, `ReturnedItemIsFinalRule.java` | Интерфейс — общий контракт проверки. Реализации — таблица переходов и запрет менять возвращённую вещь. `CompositeLostItemRule` последовательно вызывает правила. Всё обычная Java без Spring. |
| `persistence/LostItemJpa.java` | Адаптер реализует интерфейс repository. Условный UPDATE проверяет прежний статус, чтобы два одновременных действия не прошли оба. Успешное изменение пишет audit-строку в той же транзакции. |
| `persistence/LostItemEntity.java` | `@Entity` — отображение строки; `@Table` и `@Column` связывают имена; `@Id` — ключ; `@Enumerated(STRING)` хранит текст статуса. Entity отделена от domain и HTTP records. |
| `handler/ApiExceptionHandler.java` | Превращает исключения в понятные HTTP-коды и ApiError: валидация 400, нет предмета 404, дубликат/запрещённый переход 409. Stack trace клиенту не отдаёт. |
| `src/main/resources/static/` | HTML — структура, CSS — вид, JS — реальные fetch-запросы. Token лежит в переменной, заголовок Bearer отправляется при запросах. Текст вставляется через textContent. Logout удаляет token. |

## Что добавлено только для final

| Файл | Смысл |
|---|---|
| `handler/RemoteAuthController.java` | В final вместо локального login-контроллера пересылает login/register в accounts. Передаёт correlationId; недоступный сервис превращает в 503. |
| `client/AccountsConfiguration.java` | Создаёт RestClient, URL и таймаут берёт из конфигурации. Реальный HTTP, не заглушка. |
| `handler/FinalItemsController.java` | Возвращает до 100 последних вещей для страницы. |
| `services/accounts-service/.../AccountsApplication.java` | Вторая точка запуска; это отдельный сервис, не просто ещё один package. |
| `AccountController.java` | Два внутренних HTTP-метода: регистрация и вход. |
| `AccountService.java` | Регистрация: назначает FINDER или проверяет приглашение для CUSTODIAN, генерирует UUID, хеширует пароль, пишет users и auth_event, выдаёт token. Вход: manager проверяет пароль, пишется LOGIN. |
| `AccountUsers.java` | JDBC UserDetailsService загружает хеш и роль из users для AuthenticationManager. |
| `AccountSecurity.java` | Правила внутренних путей, PasswordEncoder, AuthenticationManager, JwtEncoder. В Docker внутренний порт не опубликован. |
| `services/accounts-service/src/main/resources/db/migration/V1__users_and_auth_events.sql` | Accounts создаёт собственные users и auth_event. |
| `compose.yml` | Сборка, адреса внутри сети, переменные, два PostgreSQL, проверки готовности, постоянные тома. |
| `Dockerfile` у каждого сервиса | Сначала компиляция Maven/JDK, затем небольшой runtime с JRE и непривилегированным пользователем. |

## Flyway, JPA, транзакции — что ответить

- **Миграция** — версионированное изменение структуры БД. V1 создаёт таблицу,
  V2 добавляет историю. Успешную V1 не переписываем: создаём новую версию.
- **flyway_schema_history** — техническая история применённых миграций с версиями
  и checksum. Это не журнал действий пользователя.
- **Аудит** — наши `lost_item_event` и `auth_event`: кто и что сделал. Их пишет
  приложение. Flyway только создаёт структуру этих таблиц.
- **Checksum** — контрольная сумма SQL. Изменённая уже применённая миграция
  вызывает ошибку проверки, а не молча меняет рабочую базу.
- **JPA** — спецификация отображения Java-объектов на таблицы. **Hibernate** —
  её реализация. **ORM** — этот способ отображения.
- **EntityManager** — управляет сущностями; `persist` добавляет новую,
  `flush` отправляет накопленный SQL и позволяет увидеть constraint-ошибку.
- **JPQL** — запрос по классам/полям entity, а не напрямую по именам SQL-таблиц.
- **ddl-auto: validate** — Hibernate проверяет соответствие, но не создаёт
  таблицы. Создание/изменение принадлежит Flyway.
- **open-in-view: false** — не держим persistence context на весь HTTP-запрос.
- **Транзакция** — группа действий как единое целое. **Commit** фиксирует,
  **rollback** отменяет. Ошибка второй вставки одной транзакции даёт count 0;
  ошибка нового запроса не отменяет ранее зафиксированное: count 1.
- **UNIQUE** — база не разрешит повтор business_key/username, даже при гонке.
- **PRIMARY KEY** — уникальный идентификатор строки. **FOREIGN KEY** — связь
  события с существующим предметом/пользователем. **CHECK** — допустимые статусы.

## Остальные термины простыми словами

| Термин | Значение и место |
|---|---|
| Класс / объект | Класс описывает поведение, объект — конкретный экземпляр. LostItemService — класс, созданный Spring service — объект. |
| Конструктор | Метод с именем класса без возвращаемого типа. Заполняет final-поля зависимостями. Spring передаёт их сам. |
| Интерфейс | Контракт методов без привязки к конкретному хранилищу: LostItemRepository. |
| Record | Короткий тип данных: LoginRequest, ApiError, LostItemResponse. Это не entity. |
| Enum | Закрытый набор значений: REPORTED/FOUND/RETURNED. |
| Bean / DI / IoC | Bean — объект под управлением Spring. DI — передача зависимостей, здесь через конструктор. IoC — созданием и связыванием управляет контейнер. |
| Аутентификация | Кто вы; проверка пароля при login или JWT в API. |
| Авторизация / RBAC | Что вам можно; правила по ролям FINDER/CUSTODIAN. |
| JWT | Подписанные claims. Это не шифрование: payload можно прочитать. Поэтому там нет паролей. |
| HS256 / HMAC | Подпись с общим секретом SHA-256. Оба сервиса знают ключ; утечка ключа позволяет подделывать token. |
| iss / sub / iat / exp | Издатель / пользователь / время выдачи / срок окончания. |
| BCrypt / salt | Медленное одностороннее хеширование пароля со случайной солью. Пароль не расшифровывается; проверяется matches. |
| Bearer | Обладатель токена может им пользоваться. Передаётся в Authorization, при публичном запуске нужен HTTPS. |
| Stateless | Сервер не хранит HTTP-сессию пользователя. Logout в странице удаляет копию токена; уже украденный token действует до exp. |
| CSRF | Атака с автоматическими cookie-учётными данными. Здесь нет cookie-login; Bearer добавляется JS, поэтому CSRF выключен в цепочке. XSS при этом остаётся отдельным риском. |
| CORS / same-origin | Контроль запросов из другого origin. Страница и API здесь на одном адресе, wildcard CORS не нужен. |
| 200 / 201 / 400 / 401 / 403 / 404 / 409 / 503 | Успех / создано / неверные данные / нет корректной аутентификации / нет прав / не найдено / конфликт / недоступен зависимый сервис. |
| DTO | Данные границы API: request/response, отдельно от entity. |
| UUID / бизнес-ключ | UUID — внутренний id пути; LF-DEMO-01 — понятный человеку уникальный ключ. |
| Correlation ID | Метка одного запроса для поиска связанных ошибок. Не роль и не токен. |
| REST / JSON / API | HTTP-интерфейс продукта и формат передаваемых данных. |
| Unit / web slice / integration test | Изолированная логика / HTTP+security с mock сервиса / Spring и реальная PostgreSQL. |
| Mock | Подмена зависимости, чтобы проверить вызов или его отсутствие. Junior-тест проверяет, что сервис вообще не вызывался. |
| Microservice | Независимо запускаемый сервис с собственной ответственностью/данными. Accounts — отдельный pom, jar, процесс и база. |
| Docker image / container / volume | Шаблон приложения / запущенный экземпляр / постоянные данные за пределами экземпляра. |
| Compose / healthcheck | Описание нескольких контейнеров / проверка готовности. Наличие YAML ещё не доказывает успешный запуск. |
| Reverse proxy / TLS / HTTPS | Caddy принимает публичный запрос, шифрует соединение и передаёт запрос items. |
| CI / Git / branch / PR | Автопроверки / история кода / отдельная линия изменений / запрос на проверку и слияние ветки. |
| push / pull | Отправить свои commits в remote / получить чужие commits и интегрировать их локально. |
| pom.xml | Версия Java, библиотеки и правила сборки Maven. Не предметные данные. |
| target / .idea / .m2-cache | Результаты сборки / настройки IntelliJ / кэш библиотек. Не пишем туда исходники, не сдаём их в Git. |

## Границы готовности

Проверены тесты и локальные процессы/страница. Docker runtime, внешний HTTPS
и работа на арендованном сервере требуют отдельного реального запуска. Не
называйте их проверенными до этого. Нет refresh-токенов, восстановления пароля,
публичного управления ролями, MFA, антибот-защиты и rate limiting: это учебный
проект, а не готовая эксплуатационная система университета.
