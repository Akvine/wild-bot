# Метрики wild-bot

Приложение собирает метрики через Micrometer и отдаёт их по адресу `/wild-bot/actuator/prometheus`.
Prometheus забирает их раз в 15 секунд и хранит историю, Grafana показывает графики.

## Запуск

Приложение должно быть запущено. Из корня проекта:

```
docker compose -f monitoring/docker-compose.yml up -d
```

| Что | Адрес | Зачем |
|---|---|---|
| Grafana | http://localhost:3000, вход `admin` / `admin` | панель `wild-bot` открывается первой |
| Prometheus | http://localhost:9090/targets | видно, удаётся ли забрать метрики: у цели `wild-bot` должно быть `UP` |
| Метрики приложения | http://localhost:8080/wild-bot/actuator/prometheus | то, что видит Prometheus |

Источник данных и панель подключаются сами. Остановить: `docker compose -f monitoring/docker-compose.yml down`,
накопленные данные при этом остаются; чтобы удалить и их, добавьте `-v`.

Prometheus ищет приложение на порту 8080 - это `server.port` профиля `local`. Без профиля приложение слушает 8106
(`application.properties`): тогда, а также если процессов несколько (`server.app.role=web` и `worker`), поправьте
список `targets` в `prometheus/prometheus.yml` и перезапустите Prometheus.

## Как выключить

В `application.properties`:

```
monitoring.metrics.enabled=false
```

Метрики перестают собираться, адрес `/actuator/prometheus` перестаёт существовать (приложение отвечает на него так
же, как на любой неизвестный адрес), фильтр метрик не встаёт в цепочку фильтров бота, сервисы интеграции отдаются
без обёрток, база не опрашивается. Остальной мониторинг (`monitoring.api.metrics.*`, `db.metrics.*` и прочее) от
этой настройки не зависит.

## Что собирается

Метрики приложения:

| Метрика в Prometheus | Что показывает | Метки |
|---|---|---|
| `bot_updates_seconds_*` | обработанные обновления бота и время обработки | `bot`, `type`, `outcome`, `exception` |
| `integration_requests_seconds_*` | обращения к Wildberries, Telegram и Max | `system`, `operation`, `outcome`, `exception` |
| `bot_clients` | клиенты без удалённых | `bot` |
| `bot_subscriptions_active` | подписки, срок которых не истёк | |
| `bot_adverts` | рекламные кампании без удалённых | `status` |
| `outbox_messages` | недоставленные сообщения outbox | `status`: pending, processing, failed |
| `executor_*` | пулы потоков из `AsyncConfig`: очередь, активные потоки | `name` |
| `resilience4j_circuitbreaker_*` | состояние circuit breaker'ов внешних систем | `name`, `state` |

Стандартные метрики Spring Boot: `http_server_requests_seconds_*` (вебхуки и админский REST),
`tasks_scheduled_execution_seconds_*` (задачи с `@Scheduled`), `spring_data_repository_invocations_seconds_*`,
`jdbc_connections_*`, `logback_events_total`, `jvm_*`, `process_*`, `system_*`.

У всех метрик есть метки `application="wild-bot"` и `role` (значение `server.app.role`).

## Что стоит знать

- **`outcome="error"` у обновлений** означает, что обработка закончилась исключением. Сюда попадают и ожидаемые
  отказы, которые `BotExceptionHandler` превращает в сообщение пользователю: клиента нет в белом списке, нет
  подписки. Что именно произошло, видно по метке `exception`.
- **Время обращения к внешней системе** считается так, как его видит приложение: HTTP-обмен вместе с ожиданием
  ограничителей и повторами.
- **Показатели из базы** (клиенты, подписки, кампании, outbox) пересчитываются, когда метрики читают, но не чаще
  раза в минуту: `monitoring.metrics.state.refresh.interval.milliseconds`. Пока метрики никто не собирает,
  запросов в базу нет.
- **Метрик `hikaricp_*` нет**, пока включён `monitoring.connection.pool.enabled`: он занимает у HikariCP место,
  куда встал бы Micrometer. Занятые соединения и размер пула видны в `jdbc_connections_*`.
- **Перцентили** (p95, p99) требуют гистограмм, а те в десятки раз увеличивают число рядов. По умолчанию они
  выключены, панель показывает среднее и максимум. Включаются по одной метрике - закомментированные строки
  `management.metrics.distribution.percentiles-histogram.*` в `application.properties`.
- **Адрес с метриками открыт без входа**: в `SecurityConfig` разрешено всё под `/actuator/**`. Из actuator наружу
  отдаётся только `prometheus`, но за пределы своей сети его выпускать не стоит: закройте путь на обратном прокси.
- **В тестах** Spring Boot реестр Prometheus не создаёт. Тесты, которым он нужен, помечены
  `@AutoConfigureObservability` (см. `MetricsEndpointTest`).
- **Панель** описана файлом `grafana/dashboards/wild-bot.json`. Grafana перечитывает его раз в 30 секунд,
  изменения из интерфейса в файл не сохраняются.
