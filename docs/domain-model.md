# Domain Model

## Order
Aggregate root for the order lifecycle. Owned by `order-service`.

Fields:
- `id: Long` — internal DB id (never exposed externally)
- `orderId: UUID` — public identifier, used in all API responses and inter-service communication
- `customerId: UUID` — references the customer who placed the order
- `status: OrderStatus` — current lifecycle status
- `totalAmount: BigDecimal` — sum of all OrderItems
- `currency: String` — ISO 4217 (e.g. "PLN", "EUR")
- `paymentIntentId: String` — Stripe PaymentIntent ID, set after payment initiation
- `deliveryAddress: DeliveryAddress` — embedded value object
- `items: List<OrderItem>` — 1..n items, min 1 required
- `createdAt: Instant` — set on creation
- `updatedAt: Instant` — updated on every status change
- `expiresAt: Instant` — deadline for PENDING orders; scheduler auto-cancels after this
- `version: Long` — optimistic locking (@Version)

Status machine:
```
PENDING → CONFIRMED → SHIPPED → DELIVERED
       ↘ CANCELLED (from PENDING or CONFIRMED only)
```

Invalid transitions throw `OrderStatusException`. Status changes are always done
through a dedicated method on the aggregate (e.g. `order.confirm()`, `order.cancel(reason)`),
never by setting the field directly.

## OrderItem
Value object owned by Order. No independent lifecycle.

Fields:
- `id: Long` — internal DB id
- `productId: UUID` — references external product catalog (not managed here)
- `productName: String` — snapshot at order time (product name may change later)
- `quantity: int` — min 1
- `unitPrice: BigDecimal` — snapshot at order time

## DeliveryAddress
Embedded value object inside Order. No separate table.

Fields:
- `street: String`
- `city: String`
- `postalCode: String`
- `country: String` — ISO 3166-1 alpha-2 (e.g. "PL", "DE")

## Tracking
Separate aggregate. Linked to Order by `orderId` (UUID reference only — no FK across services).
Owned by `tracking-service`.

Fields (TrackingEntry per status change):
- `id: Long`
- `trackingId: UUID` — public identifier
- `orderId: UUID` — reference to Order in order-service
- `status: String` — mirrors OrderStatus at the time of the entry
- `location: String` — optional, human-readable (e.g. "Warsaw sorting center")
- `note: String` — optional, additional info
- `recordedAt: Instant` — when this entry was created

A full tracking history is the list of all TrackingEntries for a given orderId, ordered by recordedAt.

## Notification
No persistent domain model — stateless event processing.
Owned by `notification-service`.

The only persisted data is the idempotency table:

**ProcessedEvent:**
- `id: Long`
- `eventId: UUID` — unique identifier of the Kafka event
- `processedAt: Instant` — when it was handled

Before processing any event, the consumer checks if `eventId` already exists in this table.
If yes — skip silently. If no — process and insert within the same transaction.

## Product
Static product catalog. Owned by `order-service` (no separate service).
Managed via Flyway seed data — not editable via API by regular users.
Admins can manage products via `/api/v1/admin/products`.

Fields:
- `id: Long` — internal DB id
- `productId: UUID` — public identifier
- `name: String` — display name
- `description: String` — optional
- `price: BigDecimal` — current price
- `currency: String` — ISO 4217
- `available: boolean` — if false, cannot be ordered

Note: `productName` and `unitPrice` in OrderItem are snapshots taken at order creation time.
They do not change if the product price or name changes later.
