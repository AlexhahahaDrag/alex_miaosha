---
name: vuejs-skills
description: >
  Curated Vue 3 Composition API, Composable, Pinia, and Vitest testing best practices derived from vuejs-ai/skills. Enforces reactivity safety, toValue/MaybeRefOrGetter composable patterns, storeToRefs, and BDD test suites, while strictly respecting alex_miaosha front-end engineering red lines (auto-import, stringified IDs, destructuring API responses). Use whenever designing, writing, reviewing, or testing Vue 3 components, composables, pinia stores, or tests in PC and mobile frontends.
---

# Vue.js Agent Skills & Best Practices (`alex_miaosha` Edition)

This skill provides expert-level guidelines for writing idiomatic, safe, and high-performance Vue 3 code, combining official community practices from `vuejs-ai/skills` with the project's strict engineering boundaries.

---

## 1. ⛔ Project Engineering Red Lines (Highest Priority)

Before writing any Vue 3 code in `alex_miaosha_front` or `alex_miaosha_mobile`, always follow these non-negotiable rules:

1. **Auto-Import Exclusivity**:
   - `unplugin-auto-import` and `unplugin-vue-components` are globally configured.
   - **NEVER manually write explicit imports** for common Vue APIs: `ref`, `computed`, `watch`, `onMounted`, `onUnmounted`, `useRoute`, `useRouter`, etc.
   - **NEVER manually write explicit imports** for Ant Design Vue components (in PC) or Vant components (in Mobile).
2. **Snowflake ID Safety**:
   - Primary/foreign IDs from the backend are 64-bit Longs. Frontend must **strictly maintain them as `string`**.
   - **NEVER use `Number(id)` or `parseInt(id)`**, as this causes precision truncation on large IDs.
3. **API Response Destructuring**:
   - Always destructure response objects directly:
     ```typescript
     const { code, data, message } = await api();
     ```
   - **NEVER chain reads like `res.code` or `res.data`**.
4. **Strict Path Aliases**:
   - Always use `@/` path alias (e.g. `@/views/finance/gift/config`, `@/composables/usePagination`).
   - **NEVER use relative paths like `../` or `./`**.
5. **Date Utilities**:
   - Use `@/utils/dayjs` helper functions (`formatDate`, `formatTime`, `getMonthRange`). Never import `dayjs` instance directly in pages.

---

## 2. ⚡ Reactivity Safety & Performance (Vue 3 Composition API)

### 2.1 Prevent Reactivity Loss on Destructuring
- **Rule**: Never destructure `props` or a `reactive` object directly. Direct destructuring breaks proxy binding.
- **Good (Vue 3.3+ Reactive Props Destructure or toRefs)**:
  ```typescript
  // If using props destructuring with macro support:
  const { title, count = 0 } = defineProps<{ title: string; count?: number }>();
  // Or using toRefs:
  const props = defineProps<{ title: string }>();
  const { title } = toRefs(props);
  ```
- **Bad**:
  ```typescript
  const props = defineProps<{ title: string }>();
  const { title } = props; // ❌ Reactivity lost!
  ```

### 2.2 Reassignment Safeguards
- Always use `ref` for array/object collections that need complete replacement:
  ```typescript
  const list = ref<ItemVo[]>([]);
  list.value = newList; // ✅ Safe and reactive
  ```
- Do not declare `let state = reactive([])` and reassign `state = newList;` (breaks reactivity).

### 2.3 ShallowRef & MarkRaw for Large Objects
- For heavy, immutable, or non-reactive third-party instances (e.g., ECharts instances, Leaflet/AMap maps, large read-only static dictionaries, complex rich text editors):
  ```typescript
  const chartInstance = shallowRef<echarts.ECharts | null>(null);
  // Or for registered components in router tables:
  const route = { path: '/home', component: markRaw(HomeView) };
  ```
  This eliminates Vue's deep dependency-tracking overhead and console warnings.

### 2.4 Computed over Watch + Reassign
- When deriving a value from other state, always prefer `computed`:
  ```typescript
  // ✅ Pure, self-updating, memoized
  const isOverBudget = computed(() => totalExpense.value > budgetAmount.value);
  ```
- Do not use `watch` to manually sync state unless performing asynchronous side effects.

---

## 3. 🧩 High-Quality Composables Pattern (`create-adaptable-composable`)

When creating reusable business hooks in `src/composables/`:

### 3.1 Accept Flexible Arguments (`MaybeRefOrGetter`)
- Support primitive values, refs, or getters seamlessly using Vue 3.3+ `MaybeRefOrGetter<T>` and `toValue()`:
  ```typescript
  import type { MaybeRefOrGetter } from 'vue';
  
  export function useFormattedMoney(amount: MaybeRefOrGetter<number | string>) {
    const formatted = computed(() => {
      const raw = toValue(amount);
      if (!raw) return '¥0.00';
      return `¥${Number(raw).toFixed(2)}`;
    });
    return { formatted };
  }
  ```

### 3.2 Return Flat Ref Objects
- Always return a plain JavaScript object containing `ref` or `computed` properties. This allows callers to safely destructure returned values without losing reactivity:
  ```typescript
  export function useCounter() {
    const count = ref(0);
    const doubleCount = computed(() => count.value * 2);
    const increment = () => count.value++;
    return { count, doubleCount, increment };
  }
  ```

### 3.3 Automatic Resource Cleanup (`onScopeDispose`)
- Any timer, WebSocket, SSE connection, EventListener, or ResizeObserver created inside a composable must be safely released when the calling component unmounts:
  ```typescript
  import { onScopeDispose } from 'vue';

  export function useAutoPolling(task: () => void, interval = 5000) {
    const timer = setInterval(task, interval);
    onScopeDispose(() => {
      clearInterval(timer);
    });
  }
  ```

### 3.4 Race-Condition Protection
- For composables that trigger async searches or data fetching based on fast-changing user input, always guard against stale responses:
  ```typescript
  let currentRequestId = 0;
  async function fetchData(query: string) {
    const reqId = ++currentRequestId;
    const { data } = await apiSearch(query);
    if (reqId === currentRequestId) {
      list.value = data;
    }
  }
  ```

---

## 4. 🍍 Pinia State Management Safety (`vue-pinia-best-practices`)

### 4.1 Destructuring State with `storeToRefs`
- Always use `storeToRefs(store)` when destructuring reactive state or getters from Pinia.
- Functions (actions) can be destructured directly from the store instance:
  ```typescript
  import { storeToRefs } from 'pinia';
  
  const userStore = useUserStore();
  // ✅ State and getters wrapped in refs, actions destructured directly
  const { userInfo, hasMenu } = storeToRefs(userStore);
  const { resetAuth, changeRouteStatus } = userStore;
  ```
- **Bad**:
  ```typescript
  const { userInfo, hasMenu } = useUserStore(); // ❌ Reactivity lost!
  ```

### 4.2 Shared Cache Integrity
- When reading global structures (such as `menuAllTree` or `permissionContext`), **never mutate `node.children` directly**. Use `structuredClone(tree)` before applying in-memory filtering or sorting.

---

## 5. 🧪 Vitest Testing Standard (`vue-testing-best-practices`)

In alignment with `TESTING_STANDARD.md` (PC 60% / Mobile 55% unit test coverage):

### 5.1 Test Behavior, Not Internal Details
- Focus assertions on user-visible DOM changes, emitted events, and output states rather than internal private variables.

### 5.2 Advancing Async State (`flushPromises` & `nextTick`)
- Never use arbitrary `setTimeout` delays in unit tests.
- Always use `flushPromises()` from `@vue/test-utils` to drain the microtask queue:
  ```typescript
  import { mount, flushPromises } from '@vue/test-utils';
  
  test('submits form on button click', async () => {
    const wrapper = mount(MyComponent);
    await wrapper.find('[data-testid="submit-btn"]').trigger('click');
    await flushPromises();
    expect(wrapper.text()).toContain('Success');
  });
  ```

### 5.3 Snowflake ID Contract in Tests
- Always include test cases verifying that IDs > `Number.MAX_SAFE_INTEGER` (e.g. `"1810856881968091138"`) are preserved exactly as strings across state transitions.
