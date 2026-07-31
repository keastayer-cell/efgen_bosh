<script setup>
import { computed, onMounted, ref } from 'vue'
import { requestJson } from '../api/http'

const tokenKey = 'efgen_access_token'
const userKey = 'efgen_user'
const token = ref(localStorage.getItem(tokenKey))
const savedUser = JSON.parse(localStorage.getItem(userKey) || 'null')
const user = ref(savedUser)
const login = ref({ email: '', password: '' })
const authMode = ref('login')
const authError = ref('')
const authBusy = ref(false)
const activeFilter = ref('active')
const search = ref('')
const cars = ref([])
const insurers = ref([])
const suppliers = ref([])
const shifts = ref([])
const workCatalog = ref([])
const carsBusy = ref(false)
const carsError = ref('')
const modal = ref(null)
const toast = ref('')
const carFormVisible = ref(false)
const partFormVisible = ref(false)
const workOrderVisible = ref(false)
const editingCar = ref(null)
const editingPart = ref(null)
const selectedCar = ref(null)
const selectedWorkOrderCar = ref(null)
const workOrder = ref(null)
const workOrderBusy = ref(false)
const workOrderError = ref('')
const carForm = ref(emptyCarForm())
const partForm = ref(emptyPartForm())

function emptyCarForm() {
  return {
    vehicleName: '', vehicleNameLatin: '', registrationNumber: '', vin: '',
    insuredPerson: '', claimNumber: '', acceptedAt: '', startedAt: '',
    appointmentDate: '', comment: '', documentFolderUrl: '', insurerId: '', shiftId: '',
  }
}

function emptyPartForm() {
  return { name: '', article: '', supplierId: '', expectedDate: '', sortOrder: 0 }
}

const mappedCars = computed(() => cars.value.map((car) => ({
  ...car,
  number: car.accountingNumber,
  registration: car.registrationNumber || 'Госномер не указан',
  vehicle: car.vehicleName,
  insurer: insurers.value.find((item) => item.id === car.insurerId)?.name || (car.insurerId ? `Страховая #${car.insurerId}` : 'Страховая не выбрана'),
  start: car.startedAt || car.acceptedAt || '—',
  parts: car.parts.length ? `${car.parts.filter((part) => part.received).length} / ${car.parts.length}` : '—',
  comment: car.comment || '—',
  record: car.appointmentDate || '—',
  shift: shifts.value.find((item) => item.id === car.shiftId)?.name || (car.shiftId ? `Смена #${car.shiftId}` : '—'),
  status: car.status.toLowerCase(),
})))

const visibleCars = computed(() => mappedCars.value.filter((car) => {
  const matchesFilter = activeFilter.value === 'active'
    ? car.status !== 'delivered'
    : activeFilter.value === car.status
  const query = search.value.trim().toLowerCase()
  return matchesFilter && (!query || Object.values(car).some((value) => String(value).toLowerCase().includes(query)))
}))

const stats = computed(() => ({
  active: mappedCars.value.filter((car) => car.status !== 'delivered').length,
  waiting: mappedCars.value.filter((car) => car.status === 'waiting').length,
  ready: mappedCars.value.filter((car) => car.status === 'ready').length,
  delivered: mappedCars.value.filter((car) => car.status === 'delivered').length,
}))

async function loadCars() {
  carsBusy.value = true
  carsError.value = ''
  try {
    cars.value = await requestJson('/api/v1/cars')
  } catch (error) {
    carsError.value = error.message
  } finally {
    carsBusy.value = false
  }
}

async function loadDirectories() {
  try {
    const [insurerItems, supplierItems, shiftItems, workItems] = await Promise.all([
      requestJson('/api/v1/directories/insurers'),
      requestJson('/api/v1/directories/suppliers'),
      requestJson('/api/v1/directories/shifts'),
      requestJson('/api/v1/directories/works'),
    ])
    insurers.value = insurerItems
    suppliers.value = supplierItems
    shifts.value = shiftItems
    workCatalog.value = workItems
  } catch (error) {
    showToast(`Справочники не загружены: ${error.message}`)
  }
}

function openCarForm() {
  modal.value = null
  editingCar.value = null
  carForm.value = emptyCarForm()
  carFormVisible.value = true
}

function openCarEdit(car) {
  modal.value = null
  editingCar.value = car
  const source = cars.value.find((item) => item.id === car.id) || car
  carForm.value = {
    vehicleName: source.vehicleName || '',
    vehicleNameLatin: source.vehicleNameLatin || '',
    registrationNumber: source.registrationNumber || '',
    vin: source.vin || '',
    insuredPerson: source.insuredPerson || '',
    claimNumber: source.claimNumber || '',
    acceptedAt: source.acceptedAt || '',
    startedAt: source.startedAt || '',
    appointmentDate: source.appointmentDate || '',
    comment: source.comment || '',
    documentFolderUrl: source.documentFolderUrl || '',
    insurerId: source.insurerId ? String(source.insurerId) : '',
    shiftId: source.shiftId ? String(source.shiftId) : '',
  }
  carFormVisible.value = true
}

function openPartForm(car) {
  modal.value = null
  selectedCar.value = car
  editingPart.value = null
  partForm.value = emptyPartForm()
  partFormVisible.value = true
}

function emptyWorkOrderLine() {
  return { catalogId: '', categoryName: '', name: '', unit: 'шт.', quantity: 1, price: 0, sortOrder: 0 }
}

function applyCatalogLine(line) {
  const item = workCatalog.value.find((entry) => String(entry.id) === String(line.catalogId))
  if (!item) return
  line.categoryName = item.categoryName
  line.name = item.name
  line.unit = item.defaultUnit
}

async function openWorkOrder(car) {
  modal.value = null
  selectedWorkOrderCar.value = car
  workOrderBusy.value = true
  workOrderError.value = ''
  try {
    workOrder.value = await requestJson(`/api/v1/cars/${car.id}/work-order`)
    workOrder.value.lines = workOrder.value.lines.map((line) => ({ ...line, catalogId: '' }))
  } catch (error) {
    workOrderError.value = error.message
  } finally {
    workOrderBusy.value = false
  }
  workOrderVisible.value = true
}

function addWorkOrderLine() {
  workOrder.value.lines.push(emptyWorkOrderLine())
}

function removeWorkOrderLine(index) {
  workOrder.value.lines.splice(index, 1)
}

function workOrderTotal() {
  return (workOrder.value?.lines || []).reduce((sum, line) => sum + (Number(line.quantity) || 0) * (Number(line.price) || 0), 0)
}

async function saveWorkOrder() {
  if (!selectedWorkOrderCar.value || !workOrder.value) return
  try {
    const result = await requestJson(`/api/v1/cars/${selectedWorkOrderCar.value.id}/work-order`, {
      method: 'PUT',
      body: JSON.stringify({
        documentDate: workOrder.value.documentDate,
        customer: workOrder.value.customer,
        status: workOrder.value.status,
        lines: workOrder.value.lines.map((line, index) => ({
          categoryName: line.categoryName || '', name: line.name, unit: line.unit,
          quantity: Number(line.quantity), price: Number(line.price), sortOrder: index,
        })),
      }),
    })
    workOrder.value = result
    showToast('Заказ-наряд сохранён')
  } catch (error) {
    showToast(error.message)
  }
}

function openPartEdit(car, part) {
  modal.value = null
  selectedCar.value = car
  editingPart.value = part
  partForm.value = {
    name: part.name || '',
    article: part.article || '',
    supplierId: part.supplierId ? String(part.supplierId) : '',
    expectedDate: part.expectedDate || '',
    sortOrder: part.sortOrder || 0,
  }
  partFormVisible.value = true
}

async function saveCar() {
  try {
    const path = editingCar.value ? `/api/v1/cars/${editingCar.value.id}` : '/api/v1/cars'
    const payload = {
      ...carForm.value,
      insurerId: carForm.value.insurerId ? Number(carForm.value.insurerId) : null,
      shiftId: carForm.value.shiftId ? Number(carForm.value.shiftId) : null,
      contractorId: null,
    }
    await requestJson(path, {
      method: editingCar.value ? 'PUT' : 'POST',
      body: JSON.stringify(payload),
    })
    carFormVisible.value = false
    await loadCars()
    showToast(editingCar.value ? 'Автомобиль сохранён' : 'Автомобиль добавлен')
    editingCar.value = null
  } catch (error) {
    showToast(error.message)
  }
}

async function savePart() {
  if (!selectedCar.value) return
  try {
    const path = editingPart.value
      ? `/api/v1/cars/${selectedCar.value.id}/parts/${editingPart.value.id}`
      : `/api/v1/cars/${selectedCar.value.id}/parts`
    await requestJson(path, {
      method: editingPart.value ? 'PUT' : 'POST',
      body: JSON.stringify({
        ...partForm.value,
        supplierId: partForm.value.supplierId ? Number(partForm.value.supplierId) : null,
        sortOrder: Number(partForm.value.sortOrder || 0),
      }),
    })
    partFormVisible.value = false
    await loadCars()
    showToast(editingPart.value ? 'Запчасть сохранена' : 'Запчасть добавлена')
    editingPart.value = null
  } catch (error) {
    showToast(error.message)
  }
}

async function deletePart(car, part) {
  if (!window.confirm(`Удалить запчасть «${part.name}»?`)) return
  try {
    await requestJson(`/api/v1/cars/${car.id}/parts/${part.id}`, { method: 'DELETE' })
    await loadCars()
    showToast('Запчасть удалена')
  } catch (error) {
    showToast(error.message)
  }
}

async function toggleDelivered(car) {
  try {
    await requestJson(`/api/v1/cars/${car.id}/delivery?delivered=${!car.delivered}`, { method: 'PATCH' })
    await loadCars()
    showToast(car.delivered ? 'Выдача отменена' : 'Автомобиль выдан')
  } catch (error) {
    showToast(error.message)
  }
}

async function togglePartReceived(car, part) {
  try {
    await requestJson(`/api/v1/cars/${car.id}/parts/${part.id}/receipt?received=${!part.received}`, { method: 'PATCH' })
    await loadCars()
    showToast(part.received ? 'Поступление отменено' : 'Запчасть отмечена как поступившая')
  } catch (error) {
    showToast(error.message)
  }
}

async function submitLogin() {
  authBusy.value = true
  authError.value = ''
  try {
    const result = await requestJson(authMode.value === 'login' ? '/api/auth/login' : '/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(login.value),
    })
    localStorage.setItem(tokenKey, result.token)
    localStorage.setItem(userKey, JSON.stringify(result))
    token.value = result.token
    user.value = result
    await Promise.all([loadCars(), loadDirectories()])
  } catch (error) {
    authError.value = error.message
  } finally {
    authBusy.value = false
  }
}

async function logout() {
  try { await requestJson('/api/auth/logout', { method: 'POST' }) } catch { /* session cleanup remains local */ }
  localStorage.removeItem(tokenKey)
  localStorage.removeItem(userKey)
  token.value = null
  user.value = null
}

function openStub(name) {
  modal.value = name
}

function showToast(message) {
  toast.value = message
  window.setTimeout(() => { toast.value = '' }, 2600)
}

onMounted(() => {
  if (token.value) {
    loadCars()
    loadDirectories()
  }
})
</script>

<template>
  <section v-if="!token" class="auth-gate" aria-label="Вход в программу">
    <form class="auth-card" @submit.prevent="submitLogin">
      <span class="auth-logo" aria-hidden="true">B</span>
      <h1>Bosh: кузовной ремонт</h1>
      <p>{{ authMode === 'login' ? 'Войдите, чтобы открыть внутреннюю систему автосервиса.' : 'Создайте учетную запись для работы в системе.' }}</p>
      <div class="auth-tabs" role="tablist" aria-label="Авторизация">
        <button type="button" :class="{ 'is-active': authMode === 'login' }" @click="authMode = 'login'; authError = ''">Войти</button>
        <button type="button" :class="{ 'is-active': authMode === 'register' }" @click="authMode = 'register'; authError = ''">Регистрация</button>
      </div>
      <label v-if="authMode === 'register'"><span>Имя</span><input v-model="login.name" type="text" autocomplete="name" required placeholder="Ваше имя" /></label>
      <label><span>Email</span><input v-model="login.email" type="email" autocomplete="username" required placeholder="operator@example.com" /></label>
      <label><span>Пароль</span><input v-model="login.password" type="password" autocomplete="current-password" required placeholder="Введите пароль" /></label>
      <p v-if="authError" class="auth-error" role="alert">{{ authError }}</p>
      <button class="auth-button" type="submit" :disabled="authBusy">{{ authBusy ? 'Подождите…' : authMode === 'login' ? 'Войти' : 'Создать аккаунт' }}</button>
      <small class="auth-hint">Новая учетная запись получает базовую роль VIEWER.</small>
    </form>
  </section>

  <template v-else>
    <header class="topbar">
      <div class="brand"><span class="brand-mark">B</span><span><strong>Bosh: кузовной ремонт</strong><small>Автомобили и запчасти</small></span></div>
      <div class="topbar-actions">
        <button class="button button-cloud" @click="openStub('Облачное хранилище')">☁ Облако</button>
        <button class="button button-cloud" @click="openStub('Заказ-наряд')">Заказ-наряд</button>
        <button class="button button-cloud" @click="openStub('Настройки')">Настройки</button>
        <button class="button button-primary" @click="openCarForm">＋ Добавить автомобиль</button>
        <button class="user-chip" title="Выйти" @click="logout">{{ user?.name || user?.email || 'Пользователь' }} · Выйти</button>
      </div>
    </header>

    <main class="page-shell">
      <section class="stats-grid" aria-label="Сводка">
        <article class="stat-card stat-card-main"><span class="stat-icon">А</span><div><small>Автомобилей в работе</small><strong>{{ stats.active }}</strong></div></article>
        <article class="stat-card"><span class="stat-icon stat-icon-amber">!</span><div><small>Ждём запчасти</small><strong>{{ stats.waiting }}</strong></div></article>
        <article class="stat-card"><span class="stat-icon stat-icon-green">✓</span><div><small>Все детали поступили</small><strong>{{ stats.ready }}</strong></div></article>
        <article class="stat-card"><span class="stat-icon stat-icon-gray">В</span><div><small>Автомобилей выдано</small><strong>{{ stats.delivered }}</strong></div></article>
      </section>

      <section class="workspace">
        <div class="toolbar">
          <label class="search-field"><span>⌕</span><input v-model="search" type="search" placeholder="Поиск по автомобилю, номеру, детали или артикулу" /></label>
          <div class="filters" role="group" aria-label="Фильтр автомобилей">
            <button v-for="filter in [['active','В работе'], ['waiting','Ждём детали'], ['ready','Всё поступило'], ['delivered','Выданы']]" :key="filter[0]" class="filter" :class="{ 'is-active': activeFilter === filter[0] }" @click="activeFilter = filter[0]">{{ filter[1] }}</button>
          </div>
        </div>
        <div class="list-head"><span>Автомобиль</span><span>Страховая / Начало</span><span>Запчасти</span><span>Комментарий</span><span>Документы</span><span>Запись</span><span>Смена</span><span>Выдача</span></div>
        <div class="cars-list" aria-live="polite">
          <div v-if="carsBusy" class="empty-state">Загружаем реестр автомобилей…</div>
          <div v-else-if="carsError" class="empty-state">{{ carsError }} <button class="link-button" @click="loadCars">Повторить</button></div>
          <article v-for="car in visibleCars" :key="car.number" class="car-row" :class="`is-${car.status}`">
            <div class="car-summary">
              <div class="cell car-identity"><div class="car-title"><b class="car-sequence">{{ car.number }}</b><button class="row-toggle" @click="openCarEdit(car)"><strong>{{ car.vehicle }}</strong><small>{{ car.registration }} · VIN {{ car.vin }}</small></button></div></div>
              <div class="cell"><strong>{{ car.insurer }}</strong><small>Начало: {{ car.start }}</small></div>
              <div class="cell"><strong>{{ car.parts }}</strong><small>{{ car.status === 'waiting' ? 'ожидаются детали' : 'все детали на месте' }}</small></div>
              <div class="cell muted-cell">{{ car.comment }}</div>
              <div class="cell"><a v-if="car.documentFolderUrl" class="link-button" :href="car.documentFolderUrl" target="_blank" rel="noreferrer">Открыть папку</a><button v-else class="link-button" @click="openStub('Документы')">Папка не указана</button></div>
              <div class="cell muted-cell">{{ car.record }}</div><div class="cell">{{ car.shift }}</div>
              <div class="cell"><button v-if="car.status !== 'delivered'" class="link-button" @click="toggleDelivered(car)">Выдать</button><button v-else class="link-button" @click="toggleDelivered(car)">Отменить</button></div>
            </div>
            <div class="row-actions"><button class="link-button" @click="openStub('Дефектовка')">Дефектовка</button><button class="link-button" @click="openWorkOrder(car)">Заказ-наряд</button><button class="link-button" @click="openPartForm(car)">＋ Запчасть</button><template v-for="part in car.parts" :key="part.id"><button class="link-button" @click="togglePartReceived(car, part)">{{ part.received ? `Отменить: ${part.name}` : `Поступила: ${part.name}` }}</button><button class="link-button" @click="openPartEdit(car, part)">Изменить: {{ part.name }}</button><button class="link-button danger-link" @click="deletePart(car, part)">Удалить</button></template></div>
          </article>
          <div v-if="!carsBusy && !carsError && !visibleCars.length" class="empty-state">По выбранному фильтру автомобили не найдены.</div>
        </div>
      </section>
    </main>

    <footer class="global-footer"><span>Efgen Bosh · рабочий интерфейс</span><span>Данные разделов подключаются поэтапно</span></footer>

    <div v-if="carFormVisible" class="stub-overlay" @click.self="carFormVisible = false"><form class="data-modal" @submit.prevent="saveCar"><button type="button" class="icon-button" aria-label="Закрыть" @click="carFormVisible = false">×</button><p class="eyebrow">Карточка автомобиля</p><h2>{{ editingCar ? `Автомобиль №${editingCar.accountingNumber}` : 'Новый автомобиль' }}</h2><div class="data-form-grid"><label><span>Госномер</span><input v-model="carForm.registrationNumber" required placeholder="А123ВС124" /></label><label><span>Автомобиль</span><input v-model="carForm.vehicleName" required placeholder="Джили Окаванго" /></label><label class="form-wide"><span>Марка / модель латиницей</span><input v-model="carForm.vehicleNameLatin" placeholder="HYUNDAI CRETA" /></label><label><span>VIN</span><input v-model="carForm.vin" placeholder="VIN автомобиля" /></label><label><span>Страхователь</span><input v-model="carForm.insuredPerson" placeholder="ФИО или организация" /></label><label><span>Страховая</span><select v-model="carForm.insurerId"><option value="">Не выбрана</option><option v-for="item in insurers" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select></label><label><span>Номер дела</span><input v-model="carForm.claimNumber" placeholder="108148/26" /></label><label><span>Смена</span><select v-model="carForm.shiftId"><option value="">Не выбрана</option><option v-for="item in shifts" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select></label><label><span>Дата начала</span><input v-model="carForm.startedAt" type="date" /></label><label><span>Дата приёмки</span><input v-model="carForm.acceptedAt" type="date" /></label><label><span>Дата записи</span><input v-model="carForm.appointmentDate" type="date" /></label><label class="form-wide"><span>Папка документов</span><input v-model="carForm.documentFolderUrl" type="url" placeholder="https://..." /></label><label class="form-wide"><span>Комментарий</span><textarea v-model="carForm.comment" rows="3"></textarea></label></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="carFormVisible = false">Отмена</button><button class="button button-primary" type="submit">{{ editingCar ? 'Сохранить изменения' : 'Сохранить автомобиль' }}</button></div></form></div>

    <div v-if="partFormVisible" class="stub-overlay" @click.self="partFormVisible = false"><form class="data-modal compact-modal" @submit.prevent="savePart"><button type="button" class="icon-button" aria-label="Закрыть" @click="partFormVisible = false">×</button><p class="eyebrow">Заказ запчасти</p><h2>{{ editingPart ? 'Изменить деталь' : 'Добавить деталь' }}</h2><p class="modal-subtitle">{{ selectedCar?.number }} · {{ selectedCar?.vehicle }}</p><div class="data-form-grid"><label><span>Деталь</span><input v-model="partForm.name" required placeholder="Бампер передний" /></label><label><span>Артикул</span><input v-model="partForm.article" placeholder="604A124500" /></label><label><span>Поставщик</span><select v-model="partForm.supplierId"><option value="">Не выбран</option><option v-for="item in suppliers" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select></label><label><span>Ожидаемая дата</span><input v-model="partForm.expectedDate" type="date" /></label></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="partFormVisible = false">Отмена</button><button class="button button-primary" type="submit">{{ editingPart ? 'Сохранить изменения' : 'Добавить деталь' }}</button></div></form></div>

    <div v-if="workOrderVisible" class="stub-overlay" @click.self="workOrderVisible = false"><section class="data-modal work-order-modal"><button class="icon-button" aria-label="Закрыть" @click="workOrderVisible = false">×</button><p class="eyebrow">Рабочие данные</p><h2>Заказ-наряд · №{{ selectedWorkOrderCar?.number }}</h2><p class="modal-subtitle">{{ selectedWorkOrderCar?.vehicle }} · {{ selectedWorkOrderCar?.registration }}</p><div v-if="workOrderBusy" class="empty-state">Загружаем заказ-наряд…</div><div v-else-if="workOrderError" class="empty-state">{{ workOrderError }}</div><template v-else-if="workOrder"><div class="data-form-grid work-order-meta"><label><span>Дата документа</span><input v-model="workOrder.documentDate" type="date" /></label><label><span>Заказчик</span><input v-model="workOrder.customer" placeholder="ФИО или организация" /></label></div><div class="work-order-lines"><div class="work-order-line work-order-line-head"><span>Категория</span><span>Работа</span><span>Ед.</span><span>Кол-во</span><span>Цена</span><span>Сумма</span><span></span></div><div v-for="(line, index) in workOrder.lines" :key="line.id || `new-${index}`" class="work-order-line"><select v-model="line.catalogId" @change="applyCatalogLine(line)"><option value="">Своя работа</option><option v-for="item in workCatalog" :key="item.id" :value="String(item.id)">{{ item.categoryName }} · {{ item.name }}</option></select><input v-model="line.name" required placeholder="Ремонт двери" /><input v-model="line.unit" placeholder="шт." /><input v-model.number="line.quantity" type="number" min="0.001" step="0.001" /><input v-model.number="line.price" type="number" min="0" step="0.01" /><strong>{{ ((Number(line.quantity) || 0) * (Number(line.price) || 0)).toFixed(2) }}</strong><button type="button" class="icon-button small-icon" aria-label="Удалить строку" @click="removeWorkOrderLine(index)">×</button></div><button type="button" class="link-button" @click="addWorkOrderLine">＋ Добавить работу</button></div><div class="work-order-total">Итого: <strong>{{ workOrderTotal().toFixed(2) }}</strong></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="workOrderVisible = false">Закрыть</button><button class="button button-primary" @click="saveWorkOrder">Сохранить заказ-наряд</button></div></template></section></div>

    <div v-if="modal" class="stub-overlay" @click.self="modal = null"><section class="stub-modal"><button class="icon-button" aria-label="Закрыть" @click="modal = null">×</button><p class="eyebrow">Заглушка раздела</p><h2>{{ modal }}</h2><p>Внешний вид и место действия уже подготовлены. Реальная загрузка и сохранение данных будут подключены к backend следующим этапом.</p><button class="button button-primary" @click="modal = null">Понятно</button></section></div>
    <div v-if="toast" class="toast">{{ toast }}</div>
  </template>
</template>

<style scoped>
.auth-tabs { display: flex; gap: 4px; margin: 22px 0 4px; padding: 4px; border-radius: 10px; background: var(--soft); }
.auth-tabs button { flex: 1; padding: 9px; border: 0; border-radius: 7px; color: var(--muted); background: transparent; font-size: 12px; font-weight: 750; }
.auth-tabs button.is-active { color: var(--brand); background: white; box-shadow: 0 2px 8px rgb(24 52 47 / 10%); }
.data-modal { position: relative; width: min(720px, 100%); max-height: 90vh; overflow: auto; padding: 34px; border-radius: 18px; background: white; box-shadow: 0 25px 90px rgb(8 43 37 / 27%); }
.compact-modal { width: min(560px, 100%); }
.data-modal h2 { margin: 0 0 18px; }
.modal-subtitle { margin: -8px 0 18px; color: var(--muted); font-size: 13px; }
.data-form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 15px; }
.data-form-grid label { display: grid; gap: 7px; color: var(--ink); font-size: 12px; font-weight: 750; }
.data-form-grid input, .data-form-grid textarea, .data-form-grid select { width: 100%; padding: 10px 12px; border: 1px solid var(--line); border-radius: 9px; outline: 0; background: var(--soft); font-weight: 400; }
.data-form-grid input { height: 42px; }
.data-form-grid textarea { resize: vertical; }
.data-form-grid input:focus, .data-form-grid textarea:focus { border-color: var(--accent); box-shadow: 0 0 0 3px rgb(28 201 178 / 13%); }
.data-form-grid select { height: 42px; }
.form-wide { grid-column: 1 / -1; }
.modal-actions { display: flex; justify-content: flex-end; gap: 9px; margin-top: 24px; }
.dark-button { color: var(--brand); border-color: var(--line); background: var(--soft); }
.danger-link { color: #b44848; }
.work-order-modal { width: min(1120px, 100%); }
.work-order-meta { margin-bottom: 20px; }
.work-order-lines { overflow: auto; }
.work-order-line { display: grid; grid-template-columns: 1.1fr 2fr .65fr .8fr .95fr .95fr 34px; gap: 8px; align-items: center; min-width: 920px; margin-bottom: 8px; }
.work-order-line input, .work-order-line select { width: 100%; height: 38px; padding: 8px 10px; border: 1px solid var(--line); border-radius: 8px; background: var(--soft); }
.work-order-line-head { color: var(--muted); font-size: 11px; font-weight: 750; }
.work-order-line strong { text-align: right; font-size: 13px; }
.small-icon { width: 30px; height: 30px; }
.work-order-total { margin-top: 18px; text-align: right; font-size: 16px; }
</style>
