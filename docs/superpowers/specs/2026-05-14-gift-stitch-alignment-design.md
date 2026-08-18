# Gift Stitch Alignment Design

## Goal

Bring the gift management admin module back in line with the Stitch prototype for the five desktop pages: dashboard, person, event, record, and analysis. The implementation must keep the existing user, RBAC, organization, backend, and admin frontend frameworks, and must not add new foundation systems.

## Source Of Truth

The Stitch project `Gift Management Module UI` is the UI acceptance source. The desktop order is:

1. 数据概览 - 礼尚往来管理
2. 亲友管理 - 礼尚往来管理
3. 事由管理 - 礼尚往来管理
4. 礼金记录 - 礼尚往来管理
5. 统计报表 - 礼尚往来管理

The current admin implementation has the routes and basic CRUD shape, but misses the prototype's business statistics, aggregate table columns, and detail-side information density.

## Backend Design

No table split is required. The existing tables remain the source:

- `gift_person_info_t`
- `gift_event_info_t`
- `gift_record_info_t`
- `gift_relation_info_t`

New API responses are aggregate DTO/VO objects. Controllers still return `Result<VO>` and never return entity objects. Queries keep `org_id` isolation through the existing `@DataPermission` pattern and service ownership rules.

### Person Aggregates

Add person page aggregate APIs:

- `GET /gift-person-info-t/summary`
- `POST /gift-person-info-t/business-page`
- `GET /gift-person-info-t/profile?id={id}`

The business page extends base person fields with:

- `totalGiveAmount`
- `totalReceiveAmount`
- `netAmount`
- `latestRecordTime`
- `latestEventName`
- `latestDirection`
- `pendingReturnAmount`

The profile view returns the selected person's base info, totals, and recent gift record history for the detail drawer.

### Event Aggregates

Add event page aggregate APIs:

- `GET /gift-event-info-t/summary`
- `POST /gift-event-info-t/business-page`

The business page extends base event fields with:

- `participantCount`
- `totalAmount`
- `receiveAmount`
- `giveAmount`
- `eventStatus`
- `locationText`

`locationText` is derived from `remark` until a dedicated location column is approved.

### Record Aggregates

Add record page aggregate API:

- `POST /gift-record-info-t/summary`

The summary follows the current filters and returns:

- `receiveAmount`
- `giveAmount`
- `returnAmount`
- `netAmount`
- `recordCount`

Record list VO should also expose display names and compatibility fields:

- `eventName`
- `giverPersonName`
- `receiverPersonName`
- `personName`
- `paymentMethod`
- `handlerName`

`paymentMethod` and `handlerName` are display placeholders for now and should default to `-` unless represented in existing data.

### Analysis Aggregates

Add analysis APIs:

- `GET /gift-analysis/overview`
- `GET /gift-analysis/trend`
- `GET /gift-analysis/relation-distribution`
- `GET /gift-analysis/event-ranking`
- `GET /gift-analysis/person-ranking`

These endpoints aggregate by current organization and support the Stitch cards, trend blocks, relationship distribution, event frequency ranking, and core person ranking.

## Frontend Design

The admin pages should keep Ant Design Vue, existing route structure, and existing `usePagination`. The UI should align with the Stitch layout while respecting current project style.

### Dashboard

Add the missing sections:

- summary cards for total give, total receive, person count, pending return
- trend visualization area
- top person amount list
- recent gift records table with display names

### Person

Replace the plain CRUD table with the Stitch business table:

- search and relation filters
- summary cards
- table columns: name, relation, phone, total give, total receive, latest interaction, actions
- detail drawer with base info, totals, and recent interaction history

### Event

Add summary cards and business columns:

- monthly pending events
- total gift amount
- active person count
- table columns: event name, type, date, location, status, participant count, actions

### Record

Add filter summary cards and table display columns:

- receive amount
- give amount
- net amount
- record count
- table columns: date, person, relation, event, amount, direction, payment method, handler, remark, actions

### Analysis

Add Stitch report structure:

- period and event type filters
- export and print actions
- overview cards
- trend area
- relation distribution
- event ranking
- person ranking

## Testing

Use TDD for backend aggregate behavior first:

- aggregate person totals from gift records
- aggregate event counts and amounts
- aggregate record summary from filtered records
- expose controller route methods for the new endpoints

Frontend verification should include deterministic route rendering and field presence checks. Midscene can remain skipped unless explicitly re-enabled, but the final pass must compare rendered pages against the Stitch prototype checklist.

## Acceptance Checklist

- The five admin pages expose the same major sections as Stitch.
- Person, event, and record lists show aggregate business columns, not only base CRUD fields.
- Detail drawers show business context where the Stitch prototype shows it.
- Backend returns DTO/VO objects only.
- All aggregate data is isolated by `org_id`.
- Existing CRUD behavior remains intact.
