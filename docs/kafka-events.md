# Kafka Events

Topic: `order.events`
Partition key: `orderId` (ensures ordering of events per order)
Retention: 7 days
Partitions: 3
Replication factor: 1 (local), 2 (AWS MSK)

All events are immutable. Names are past tense.
Every event must include: `eventId`, `timestamp`, `aggregateId`, `version`.

---

## Base fields (present in every event)

| Field | Type | Description |
|---|---|---|
| `eventId` | UUID | Unique event identifier — used for idempotency checks |
| `aggregateId` | UUID | The orderId this event belongs to |
| `timestamp` | ISO-8601 | When the event occurred |
| `eventType` | String | Discriminator field (mirrors class name) |
| `version` | int | Event schema version — start at 1, increment on breaking changes |

---

## OrderConfirmed

Published when: order transitions from PENDING → CONFIRMED (payment succeeded via Stripe webhook).

| Field | Type | Description |
|---|---|---|
| `eventId` | UUID | |
| `aggregateId` | UUID | orderId |
| `timestamp` | ISO-8601 | |
| `eventType` | String | `"OrderConfirmed"` |
| `version` | int | `1` |
| `customerId` | UUID | Who placed the order |
| `totalAmount` | BigDecimal | Order total |
| `currency` | String | ISO 4217 (e.g. "PLN") |
| `deliveryAddress` | Object | Snapshot of delivery address at confirmation time |

---

## OrderCancelled

Published when: order transitions to CANCELLED (from PENDING or CONFIRMED).

| Field | Type | Description |
|---|---|---|
| `eventId` | UUID | |
| `aggregateId` | UUID | orderId |
| `timestamp` | ISO-8601 | |
| `eventType` | String | `"OrderCancelled"` |
| `version` | int | `1` |
| `customerId` | UUID | |
| `reason` | String | `CUSTOMER_REQUEST` \| `PAYMENT_FAILED` \| `EXPIRED` |

---

## OrderShipped

Published when: order transitions from CONFIRMED → SHIPPED.

| Field | Type | Description |
|---|---|---|
| `eventId` | UUID | |
| `aggregateId` | UUID | orderId |
| `timestamp` | ISO-8601 | |
| `eventType` | String | `"OrderShipped"` |
| `version` | int | `1` |
| `customerId` | UUID | |
| `estimatedDelivery` | ISO-8601 date | Optional estimated delivery date |

---

## OrderDelivered

Published when: order transitions from SHIPPED → DELIVERED.

| Field | Type | Description |
|---|---|---|
| `eventId` | UUID | |
| `aggregateId` | UUID | orderId |
| `timestamp` | ISO-8601 | |
| `eventType` | String | `"OrderDelivered"` |
| `version` | int | `1` |
| `customerId` | UUID | |

---

## Consumer groups

| Consumer | Group ID | Handles |
|---|---|---|
| notification-service | `notification-group` | All events |

---

## Notes

- Consumers must be idempotent — check `eventId` in `processed_events` table before processing.
- Dead Letter Topic: `order.events.DLT` — unrecoverable consumer errors land here.
- Do not add new required fields to existing event versions — create a new version instead.
- `productName` and `totalAmount` in events are snapshots — do not recalculate from current state.
