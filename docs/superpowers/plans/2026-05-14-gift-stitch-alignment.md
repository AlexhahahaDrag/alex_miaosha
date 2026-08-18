# Gift Stitch Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Align the gift management admin pages and backend aggregate APIs with the Stitch prototype.

**Architecture:** Add aggregate VO/query/service methods beside the existing CRUD layer, keeping existing entity tables and `org_id` isolation. Update the admin pages to consume the aggregate APIs and render the prototype sections.

**Tech Stack:** Spring Boot, MyBatis Plus, Maven, Vue 3, TypeScript, Ant Design Vue.

---

### Task 1: Backend Aggregate Contract Tests

**Files:**
- Modify: `alex_miaosha_finance/finance_boot/src/test/java/com/alex/finance/gift/GiftStructureTest.java`
- Create: `alex_miaosha_finance/finance_boot/src/test/java/com/alex/finance/gift/GiftAggregateBusinessRuleTest.java`

- [ ] **Step 1: Add failing tests for new route contracts**

Add assertions that `GiftPersonInfoTController` exposes `/summary`, `/business-page`, and `/profile`; `GiftEventInfoTController` exposes `/summary` and `/business-page`; `GiftRecordInfoTController` exposes `/summary`; and a new `GiftAnalysisController` exposes the five report endpoints.

- [ ] **Step 2: Add failing aggregate business tests**

Create test subclasses of the services that return in-memory people, events, and records. Assert person totals, event totals, record filtered totals, and ranking outputs.

- [ ] **Step 3: Run RED**

Run: `mvn -pl alex_miaosha_finance/finance_boot -Dtest=GiftStructureTest,GiftAggregateBusinessRuleTest test`

Expected: fails because aggregate VO classes, methods, and controllers do not exist.

### Task 2: Backend Aggregate API Implementation

**Files:**
- Create: `alex_miaosha_finance/finance_api/src/main/java/com/alex/api/finance/gift/summary/vo/GiftDashboardSummaryVo.java`
- Create: `alex_miaosha_finance/finance_api/src/main/java/com/alex/api/finance/gift/summary/vo/GiftAmountTrendVo.java`
- Create: `alex_miaosha_finance/finance_api/src/main/java/com/alex/api/finance/gift/summary/vo/GiftRankingItemVo.java`
- Create: `alex_miaosha_finance/finance_api/src/main/java/com/alex/api/finance/gift/summary/vo/GiftRelationDistributionVo.java`
- Create: `alex_miaosha_finance/finance_api/src/main/java/com/alex/api/finance/gift/person/vo/GiftPersonBusinessVo.java`
- Create: `alex_miaosha_finance/finance_api/src/main/java/com/alex/api/finance/gift/person/vo/GiftPersonProfileVo.java`
- Create: `alex_miaosha_finance/finance_api/src/main/java/com/alex/api/finance/gift/event/vo/GiftEventBusinessVo.java`
- Create: `alex_miaosha_finance/finance_api/src/main/java/com/alex/api/finance/gift/event/vo/GiftEventSummaryVo.java`
- Create: `alex_miaosha_finance/finance_api/src/main/java/com/alex/api/finance/gift/record/vo/GiftRecordSummaryVo.java`
- Modify: existing gift service interfaces, service implementations, mappers, and controllers.

- [ ] **Step 1: Implement minimal VO classes**

Add typed BigDecimal, Long, String, and LocalDateTime fields needed by tests and frontend.

- [ ] **Step 2: Implement service methods using MyBatis Plus wrappers**

Use existing list/page methods where possible, and aggregate in service code for the first iteration to avoid fragile XML SQL. Keep `LambdaQueryWrapper` filters and current organization data permission behavior.

- [ ] **Step 3: Add controller methods**

Expose the new endpoints using existing route style and `Result.success(...)`.

- [ ] **Step 4: Run GREEN**

Run: `mvn -pl alex_miaosha_finance/finance_boot -Dtest=GiftStructureTest,GiftAggregateBusinessRuleTest test`

Expected: pass.

### Task 3: Frontend API Types And Helpers

**Files:**
- Modify: `src/views/finance/gift/config.ts`
- Modify: `src/views/finance/gift/api/index.ts`

- [ ] **Step 1: Add TypeScript interfaces**

Add interfaces matching the new aggregate VO objects.

- [ ] **Step 2: Add API functions**

Add functions for dashboard, person summary/business-page/profile, event summary/business-page, record summary, and analysis endpoints.

- [ ] **Step 3: Run type check**

Run: `npm run type-check`

Expected: existing project type status or new gift type errors only; fix new gift errors.

### Task 4: Admin Page Alignment

**Files:**
- Modify: `src/views/finance/gift/dashboard/index.vue`
- Modify: `src/views/finance/gift/person/index.vue`
- Modify: `src/views/finance/gift/event/index.vue`
- Modify: `src/views/finance/gift/record/index.vue`
- Modify: `src/views/finance/gift/analysis/index.vue`

- [ ] **Step 1: Update dashboard**

Render Stitch summary cards, trend placeholder/table section, ranking list, and recent records.

- [ ] **Step 2: Update person page**

Render summary cards, business columns, and profile drawer.

- [ ] **Step 3: Update event page**

Render summary cards and business columns.

- [ ] **Step 4: Update record page**

Render filtered summary cards and Stitch business columns.

- [ ] **Step 5: Update analysis page**

Render filters, action buttons, overview cards, trend, distribution, and ranking sections.

### Task 5: Verification Against Stitch

**Files:**
- No production file changes unless verification finds defects.

- [ ] **Step 1: Backend test suite**

Run: `mvn -pl alex_miaosha_finance/finance_boot -Dtest=GiftStructureTest,GiftRecordBusinessRuleTest,GiftOwnershipTest,GiftAggregateBusinessRuleTest test`

- [ ] **Step 2: Frontend smoke**

Run the local app and navigate the five gift routes. Confirm the page text includes the Stitch sections and aggregate columns.

- [ ] **Step 3: Stitch checklist**

Compare rendered pages with the acceptance checklist in `docs/superpowers/specs/2026-05-14-gift-stitch-alignment-design.md`.
