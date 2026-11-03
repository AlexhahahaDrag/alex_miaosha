---
name: style-update
description: >-
  Upgrades PC and mobile frontend pages, tables, filter bars, modals, and drawers using the
  combined taste-skill (high-craft Tailwind Soft UI aesthetic) and impact-cable (cognitive flow,
  zero-dropdown segmented controls, proportional table layout, and anti-fragile interaction)
  methodology, referencing the golden benchmark of the Finance Manager (记账流水) module.
  Activate this skill whenever the user says "style-update", "/style-update", or asks to upgrade
  a page/component's UI style using taste-skill, impact-cable, and Tailwind referencing 记账流水.
---

# `style-update` — Taste-Skill × Impact-Cable Tailwind Soft UI Upgrade Workflow

When triggered (e.g. user says `style-update`), analyze and upgrade the target page, table, filter bar, modal, or drawer by combining **Taste-Skill** (visual craft) and **Impact-Cable** (interaction ergonomics & structural resilience), strictly aligning with the **记账流水 (`src/views/selfFinance/financeManager`)** benchmark.

---

## 1. Golden Benchmark Reference (`记账流水`)

Always align visual tokens, spacing, and component patterns with these reference files in `alex_miaosha_front`:

- **Page Container & Layout**: `src/views/selfFinance/financeManager/index.vue`
- **Compact Single-Line + Soft Expandable Filter Bar**: `src/views/selfFinance/financeManager/finance-manager-filter/index.vue`
- **Dual KPI / Overview Soft Cards**: `src/views/selfFinance/financeManager/components/FinanceOverviewCard.vue`
- **Proportional 5-Column Table & Magnetic Action Pills**: `src/views/selfFinance/financeManager/components/FinanceManagerTable.vue`
- **3-Card Grouped Form & Readonly Voucher Drawer**: `src/views/selfFinance/financeManager/finance-manager-detail/index.vue`
- **Right-Side Config & Simulation Drawer**: `src/views/selfFinance/financeManager/finance-budget-modal/index.vue`
- **Shared Emoji Dictionary & Borderless Pill Palette**: `src/views/selfFinance/financeManager/config/index.ts`

---

## 2. Taste-Skill: Pure Tailwind Soft UI Visual System

1. **Color Palette (Zero Fluorescent AntD Legacy Colors)**:
   - **Ban** legacy `#ff4d4f`, `#52c41a`, `#1677ff`, `#8c8c8c` inline hexes and heavy `1px solid` nested borders.
   - **Primary Brand Accent**: Indigo (`#4F6AF6` / `bg-indigo-500`, hover `hover:bg-indigo-600`, soft tint `bg-indigo-500/10 text-indigo-600`, button glow `shadow-md shadow-indigo-500/25`).
   - **Positive / Income / Valid Semantic**: Emerald (`text-emerald-600`, soft badge `bg-emerald-500/10 text-emerald-600`).
   - **Negative / Expense / Danger Semantic**: Rose (`text-rose-600`, soft badge `bg-rose-500/10 text-rose-600`).
   - **Neutral Slate Hierarchy**:
     - Page canvas: `bg-slate-50`
     - Primary section cards: `bg-white rounded-2xl border-0 shadow-sm shadow-slate-200/60`
     - Inner form/drawer group cards & expanded filter tray: `bg-slate-50/80 rounded-2xl p-4 border-0` (or `rounded-xl p-3`)
     - Primary text: `text-slate-900 font-semibold`
     - Secondary label: `text-slate-600` / `text-slate-500`
     - Auxiliary caption: `text-slate-400 text-xs`
2. **Borderless Translucent Pills (`Soft Pill`)**:
   - Category, status, and user tags use **10% opacity background + deep semantic text + `rounded-full border-0`** (e.g. `px-2.5 py-0.5 rounded-full bg-indigo-500/10 text-indigo-600 text-xs font-medium`).
3. **Tabular Monospace Numerics**:
   - Every amount, percentage, count, ID, and timestamp must use `font-mono tabular-nums tracking-tight`.
4. **Clean Up Legacy Scoped CSS**:
   - Strip redundant `<style scoped lang="scss">` blocks in favor of Tailwind utility classes, keeping only minimal `:deep(.ant-*)` overrides (`border-radius: 12px !important; border-color: rgba(226, 232, 240, 0.9) !important;`).

---

## 3. Impact-Cable: Ergonomics, Cognitive Flow & Layout Resilience

1. **Table Layout — Eliminate Wide-Screen Blank Gaps**:
   - **Compound Primary Column**: Merge sparse metadata (e.g. status dot + title on line 1; owner badge + timestamp on line 2) into the first column (`minWidth: 240`, **no fixed `%` width** so it elastically absorbs remaining width without overflowing `<colgroup>` when `rowSelection` is present).
   - **Proportional Middle Columns**: Assign percentage widths (`width: '16%' ~ '20%'`, `align: 'center'` for pills/tags, `align: 'right'` for amounts).
   - **Rigid Right Action Column**: Pin `operation` (`fixed: 'right'`, `width: 190 ~ 230`, `minWidth >= 185`), using borderless magnetic micro-buttons (`hover:bg-indigo-500/10`, `hover:bg-rose-500/10`, `whitespace-nowrap shrink-0`).
2. **Form & Drawer Ergonomics**:
   - **Right-Side `<BaseRbacDrawer>` over Tall `<a-modal>`**: Convert tall or expandable modals into right-side `<BaseRbacDrawer>` (`width="560px"` or `"640px"`).
   - **3-Card Cognitive Grouping**: Group form fields into 2–3 `bg-slate-50/80 rounded-2xl p-4` cards by mental flow (e.g. Card 1: Core Type/Status & Amount/Title; Card 2: Category Chips & Channel; Card 3: Attribution & Time).
   - **Zero-Dropdown Segmented Controls**: Replace binary/ternary `<a-select>` dropdowns (e.g. 收支类型, 有效状态, 统计范畴) with 1-click Tailwind Segmented pills (`grid grid-cols-2 gap-1 p-1 bg-slate-200/60 rounded-xl`), explicitly calling `void formRef.value?.validateFields(['fieldName'])` on click.
   - **Rich Icons in `<a-select>` & Chips**: Always render brand SVG icons (`iconComponentMap`) or Emoji (`getCategoryEmoji`) inside `<a-select-option>` items and quick-select chip pools.
   - **Anti-Flash Loading Guard**: Compute `isEdit` from `Boolean(modelInfo.value?.id || formState.value?.id)` and render `<a-skeleton>` while `detailLoading` is true.

---

## 4. ⛔ Tailwind `preflight: false` Red Lines (`alex_miaosha_front`)

Because `alex_miaosha_front` disables Tailwind's `preflight` reset (`corePlugins: { preflight: false }`):

1. **Buttons**: Every custom `<button>` MUST include `border-0` (or explicit `border border-solid border-...`), otherwise the browser renders a native beveled button border.
2. **Single-Side Borders**: Never write `border-t border-solid` or `border-b border-solid` alone! Always prefix with `border-0` (e.g. `border-0 border-t border-solid border-slate-100`), otherwise `border-solid` activates the browser's default `2px` border on the other three sides.

---

## 5. Execution Checklist on `style-update`

1. **Inspect Target Component(s)**: Identify all legacy SCSS rules, AntD default inputs/modals/tables, binary dropdowns, and icon/emoji gaps.
2. **Present or Apply Upgrade**:
   - If the user asks for analysis/suggestions first, present a concise **Taste-Skill + Impact-Cable** diagnostic table and wait for confirmation (`确认`).
   - If the user asks to execute `style-update` directly, refactor the template, script, and styles to match the `financeManager` benchmark.
3. **Verify & Sync**:
   - Run relevant Vitest unit tests (`npx vitest run ...`).
   - Synchronize any updated UI/UX contracts to `F:\workplace\project\myself\my_alex_brain`.
