# Подготовленное размещение final

Сервера пока нет. Эта инструкция и Compose подготовлены; публичного URL пока нет.
Ничего покупать автоматически не требуется. Нужны аккаунт VPS, SSH-доступ и домен.

## Что арендовать

Обычный Linux VPS: **Ubuntu 24.04, x86_64, 2 vCPU, 4 GB RAM, 30+ GB SSD** —
разумная отправная точка для учебного показа двух JVM и двух PostgreSQL.
Это оценка, не результат нагрузочного теста. При сборке на маленьком сервере
собирайте сервисы последовательно. Можно использовать, например,
[Hetzner Cloud](https://www.hetzner.com/cloud/) или доступного вам VPS-провайдера.
Цену и доступность проверяйте перед покупкой; заказ/оплату выполняет пользователь.

## Перед загрузкой проекта

1. Создайте VPS, добавьте свой SSH public key. Приватный ключ не присылайте в чат.
2. Разрешите входящий TCP 80/443; SSH 22 ограничьте своим адресом. Не открывайте
   PostgreSQL, accounts :8081 или items :8080 всему интернету.
3. Направьте DNS A домена на IPv4 сервера. Удалите неверный AAAA, если IPv6
   не настроен. Для Caddy публичный домен должен реально указывать на сервер.
4. Установите Git, Docker Engine и Compose по
   [официальной инструкции Ubuntu](https://docs.docker.com/engine/install/ubuntu/).
   Проверьте `sudo docker info` и `sudo docker compose version`.

## Запуск после публикации ветки на GitHub

```sh
git clone --branch final-microservices https://github.com/GumballPenny/-campus-lost-and-found.git
cd -- -campus-lost-and-found
sh scripts/setup-env.sh
```

Откройте `.env` локальным редактором и замените `CAMPUS_DOMAIN=localhost` на
реальный домен, например `CAMPUS_DOMAIN=lost.example.org`. Остальные случайные
секреты сохраните. Не пересоздавайте их при каждом запуске.

```sh
sudo docker compose build accounts
sudo docker compose build items
sudo docker compose --profile public up -d
sudo docker compose --profile public ps
sudo docker compose logs --tail=100 items accounts
```

Caddy подготовлен как reverse proxy. Для обычной автоматической выдачи TLS
домен/DNS и порты должны быть доступны: см.
[официальный HTTPS quick-start](https://caddyserver.com/docs/quick-starts/https).
Не объявляйте HTTPS работающим, пока браузер действительно не открыл ваш домен.

## Приёмка на сервере

```sh
curl --fail https://ВАШ_ДОМЕН/actuator/health
curl -i https://ВАШ_ДОМЕН/api/me
```

Ожидается `{"status":"UP"}` и 401 JSON у второго вызова. Затем в браузере
выполните сценарий регистрации → вещь 201 → junior 403 → senior FOUND.
Перезапустите accounts и убедитесь, что вход прежнего пользователя работает:

```sh
sudo docker compose restart accounts
```

Проверьте сохранность вещи после `sudo docker compose restart items`.
Перед показом убедитесь, что UI не раскрывает raw JWT и пароль не попадает в лог.

## Резервная копия и обновление

```sh
mkdir -p backups
chmod 700 backups
sudo docker compose exec -T items-db pg_dump -U campus -d campus_items > backups/items.sql
sudo docker compose exec -T accounts-db pg_dump -U campus -d campus_accounts > backups/accounts.sql
chmod 600 backups/*.sql
```

В accounts backup есть хеши паролей: храните закрыто. Для обновления сначала
backup, затем `git pull`, сборка и `docker compose --profile public up -d`.
Flyway применит только новые миграции. Не меняйте применённые V1/V2 и не
используйте `docker compose down -v`, если нужны данные.

Остановка: `sudo docker compose --profile public down` — тома сохраняются.
Публичная эксплуатация дополнительно требует ограничений частоты входа,
мониторинга и процесса управления senior-ролями; они не заявлены готовыми.
