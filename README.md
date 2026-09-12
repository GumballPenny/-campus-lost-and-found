# Campus Lost & Found

## Product

Campus Lost & Found is a small Java project that describes the lifecycle of a lost item on campus.

## Core item

The core item is a lost item. It has a validated identifier and one of three statuses:
`REPORTED`, `FOUND`, or `RETURNED`.

## Status table

| From | To | Result |
|---|---|---|
| REPORTED | FOUND | Allowed |
| FOUND | RETURNED | Allowed |
| REPORTED | RETURNED | Forbidden |
| RETURNED | FOUND | Forbidden |

## Forbidden — why

`REPORTED → RETURNED` is forbidden because an item must be found before it can be returned.
`RETURNED → FOUND` is forbidden because a returned item has already completed its lifecycle.

## Run tests

```bash
mvn test
```
