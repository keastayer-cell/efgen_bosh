<script setup>
import { computed, onMounted, ref, watch } from 'vue'
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
const counterparties = ref([])
const vehicleAliases = ref([])
const contractors = ref([])
const carsBusy = ref(false)
const carsError = ref('')
const carHistory = ref([])
const modal = ref(null)
const toast = ref('')
const carFormVisible = ref(false)
const partFormVisible = ref(false)
const workOrderVisible = ref(false)
const directoriesVisible = ref(false)
const contractorVisible = ref(false)
const editingContractor = ref(null)
const contractorForm = ref({ code: '', shortName: '', fullName: '', signerName: '', inn: '', ogrnip: '', address: '', bankName: '', bankInn: '', bankKpp: '', bik: '', correspondentAccount: '', settlementAccount: '' })
const editingCar = ref(null)
const editingPart = ref(null)
const selectedCar = ref(null)
const selectedWorkOrderCar = ref(null)
const workOrder = ref(null)
const workOrderBusy = ref(false)
const workOrderError = ref('')
const generatedDocuments = ref([])
const workOrderDocumentType = ref('order')
const defectVisible = ref(false)
const defectBusy = ref(false)
const defectError = ref('')
const selectedDefectCar = ref(null)
const defect = ref({ id: null, status: 'DRAFT', findings: '', recommendations: '', photos: [] })
const directoryType = ref('insurers')
const editingDirectory = ref(null)
const directoryForm = ref({ name: '', code: '', categoryName: '', defaultUnit: 'н/ч', inn: '', address: '', phone: '', note: '' })
const carForm = ref(emptyCarForm())
const partForm = ref(emptyPartForm())

function emptyCarForm() {
  return {
    vehicleName: '', vehicleNameLatin: '', registrationNumber: '', vin: '',
    insuredPerson: '', claimNumber: '', acceptedAt: '', startedAt: '',
    appointmentDate: '', comment: '', documentFolderUrl: '', insurerId: '', shiftId: '', contractorId: '',
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
    cars.value = await requestJson(search.value.trim() ? `/api/v1/cars/search?q=${encodeURIComponent(search.value.trim())}` : '/api/v1/cars')
  } catch (error) {
    carsError.value = error.message
  } finally {
    carsBusy.value = false
  }
}

async function loadDirectories() {
  try {
    const [insurerItems, supplierItems, shiftItems, workItems, counterpartyItems, vehicleItems, contractorItems] = await Promise.all([
      requestJson('/api/v1/directories/insurers'),
      requestJson('/api/v1/directories/suppliers'),
      requestJson('/api/v1/directories/shifts'),
      requestJson('/api/v1/directories/works'),
      requestJson('/api/v1/counterparties'),
      requestJson('/api/v1/directories/vehicles'),
      requestJson('/api/v1/contractors'),
    ])
    insurers.value = insurerItems
    suppliers.value = supplierItems
    shifts.value = shiftItems
    workCatalog.value = workItems
    counterparties.value = counterpartyItems
    vehicleAliases.value = vehicleItems
    contractors.value = contractorItems
  } catch (error) {
    showToast(`Справочники не загружены: ${error.message}`)
  }
}

function applyVehicleAlias(id) {
  const item = vehicleAliases.value.find((entry) => String(entry.id) === String(id))
  if (!item) return
  carForm.value.vehicleName = item.sourceName
  carForm.value.vehicleNameLatin = item.normalizedLatinName
}

function applyContractor(id) { carForm.value.contractorId = id ? Number(id) : null }

function openContractorForm(item = null) {
  editingContractor.value = item
  contractorForm.value = { code: item?.code || '', shortName: item?.shortName || '', fullName: item?.fullName || '', signerName: item?.signerName || '', inn: item?.inn || '', ogrnip: item?.ogrnip || '', address: item?.address || '', bankName: item?.bankName || '', bankInn: item?.bankInn || '', bankKpp: item?.bankKpp || '', bik: item?.bik || '', correspondentAccount: item?.correspondentAccount || '', settlementAccount: item?.settlementAccount || '' }
  contractorVisible.value = true
}

async function saveContractor() {
  try { await requestJson(editingContractor.value ? `/api/v1/contractors/${editingContractor.value.id}` : '/api/v1/contractors', { method: editingContractor.value ? 'PUT' : 'POST', body: JSON.stringify(contractorForm.value) }); await loadDirectories(); contractorVisible.value = false; showToast('Исполнитель сохранён') } catch (error) { showToast(error.message) }
}

async function deleteContractor(item) {
  if (!window.confirm(`Удалить исполнителя «${item.shortName}»?`)) return
  try { await requestJson(`/api/v1/contractors/${item.id}`, { method: 'DELETE' }); await loadDirectories(); showToast('Исполнитель удалён') } catch (error) { showToast(error.message) }
}

function printCarDocument(type) {
  const target = document.querySelector('.data-modal')
  const heading = target?.querySelector('h2')
  const original = heading?.textContent
  if (heading) heading.textContent = type === 'acceptance' ? 'Акт приёма автомобиля' : 'Акт выдачи автомобиля'
  document.body.classList.add('printing-car-document')
  window.setTimeout(() => window.print(), 0)
  window.setTimeout(() => { if (heading && original) heading.textContent = original; document.body.classList.remove('printing-car-document') }, 1000)
}

function openStandaloneWorkOrder() {
  selectedWorkOrderCar.value = { id: null, number: 'новый', vehicle: 'Без автомобиля', registration: '', parts: [] }
  workOrder.value = { id: null, carId: null, status: 'DRAFT', documentDate: new Date().toISOString().slice(0, 10), customer: '', orderNumber: '', invoiceNumber: '', actNumber: '', lines: [], partLines: [] }
  workOrderError.value = ''; workOrderVisible.value = true
  generatedDocuments.value = []
}

function openCarForm() {
  modal.value = null
  editingCar.value = null
  carForm.value = emptyCarForm()
  carFormVisible.value = true
}

function openDirectories() {
  modal.value = null
  directoryForm.value = { name: '', code: '', categoryName: '', defaultUnit: 'н/ч', inn: '', address: '', phone: '', note: '' }
  directoriesVisible.value = true
}

function openDirectoryEdit(item) {
  editingDirectory.value = item
  directoryForm.value = {
    name: item.name || '', code: item.code || '', categoryName: item.categoryName || '', defaultUnit: item.defaultUnit || 'н/ч',
    inn: item.inn || '', address: item.address || '', phone: item.phone || '', note: item.note || '',
  }
}

async function createDirectoryItem() {
  try {
    if (directoryType.value === 'works') {
      await requestJson(editingDirectory.value ? `/api/v1/directories/works/${editingDirectory.value.id}` : '/api/v1/directories/works', {
        method: editingDirectory.value ? 'PUT' : 'POST', body: JSON.stringify(directoryForm.value),
      })
    } else if (directoryType.value === 'counterparties') {
      await requestJson(editingDirectory.value ? `/api/v1/counterparties/${editingDirectory.value.id}` : '/api/v1/counterparties', {
        method: editingDirectory.value ? 'PUT' : 'POST', body: JSON.stringify(directoryForm.value),
      })
    } else {
      await requestJson(editingDirectory.value ? `/api/v1/directories/${directoryType.value}/${editingDirectory.value.id}` : `/api/v1/directories/${directoryType.value}`, {
        method: editingDirectory.value ? 'PUT' : 'POST', body: JSON.stringify({ name: directoryForm.value.name }),
      })
    }
    await loadDirectories()
    editingDirectory.value = null
    directoryForm.value = { name: '', code: '', categoryName: '', defaultUnit: 'н/ч', inn: '', address: '', phone: '', note: '' }
    showToast('Элемент справочника сохранён')
  } catch (error) {
    showToast(error.message)
  }
}

async function deleteDirectoryItem(item) {
  if (!window.confirm(`Удалить «${item.name}» из справочника?`)) return
  const path = directoryType.value === 'counterparties'
    ? `/api/v1/counterparties/${item.id}`
    : `/api/v1/directories/${directoryType.value}/${item.id}`
  try {
    await requestJson(path, { method: 'DELETE' })
    await loadDirectories()
    showToast('Элемент справочника удалён')
  } catch (error) {
    showToast(error.message)
  }
}

async function openCarEdit(car) {
  modal.value = null
  editingCar.value = car
  const source = cars.value.find((item) => item.id === car.id) || car
  selectedCar.value = source
  carHistory.value = await requestJson(`/api/v1/cars/${source.id}/history`).catch(() => [])
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
    contractorId: source.contractorId ? String(source.contractorId) : '',
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

async function openDefect(car) {
  modal.value = null; selectedDefectCar.value = car; defectBusy.value = true; defectError.value = ''
  try { defect.value = await requestJson(`/api/v1/cars/${car.id}/defect-analysis`) }
  catch (error) { defectError.value = error.message }
  finally { defectBusy.value = false; defectVisible.value = true }
}

function readDefectPhotos(event) {
  const files = Array.from(event.target.files || []).slice(0, 8)
  Promise.all(files.map((file) => new Promise((resolve, reject) => {
    const reader = new FileReader(); reader.onload = () => resolve(String(reader.result)); reader.onerror = reject; reader.readAsDataURL(file)
  }))).then((photos) => { defect.value.photos = photos }).catch((error) => { defectError.value = error.message })
}

async function saveDefect() {
  if (!selectedDefectCar.value || !defect.value) return
  defectBusy.value = true; defectError.value = ''
  try {
    defect.value = await requestJson(`/api/v1/cars/${selectedDefectCar.value.id}/defect-analysis`, {
      method: 'PUT', body: JSON.stringify({ status: defect.value.status, findings: defect.value.findings, recommendations: defect.value.recommendations, photos: defect.value.photos }),
    })
    showToast('Дефектовка сохранена'); defectVisible.value = false
  } catch (error) { defectError.value = error.message }
  finally { defectBusy.value = false }
}

async function transferDefectRecommendations() {
  const source = mappedCars.value.find((item) => item.id === selectedDefectCar.value?.id) || selectedDefectCar.value
  if (!source) return
  await openWorkOrder(source)
  const names = String(defect.value.recommendations || '').split(/[,;\n]+/).map((value) => value.trim()).filter(Boolean)
  if (workOrder.value && names.length) workOrder.value.lines.push(...names.map((name, index) => ({ catalogId: '', categoryName: 'Дефектовка', name, unit: 'шт.', quantity: 1, price: 0, sortOrder: index })))
  defectVisible.value = false
  showToast('Рекомендации перенесены в заказ-наряд')
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
  workOrderDocumentType.value = 'order'
  workOrderBusy.value = true
  workOrderError.value = ''
  try {
    workOrder.value = await requestJson(`/api/v1/cars/${car.id}/work-order`)
    generatedDocuments.value = await requestJson(`/api/v1/work-orders/${workOrder.value.id}/documents`).catch(() => [])
    workOrder.value.lines = workOrder.value.lines.map((line) => ({ ...line, catalogId: '' }))
    workOrder.value.partLines = (workOrder.value.partLines || []).map((line) => ({ ...line }))
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

function addWorkOrderPartLine(part) {
  if (workOrder.value.partLines.some((line) => line.partId === part.id)) return
  workOrder.value.partLines.push({
    partId: part.id, name: part.name, article: part.article || '', quantity: 1, price: 0,
    sortOrder: workOrder.value.partLines.length,
  })
}

function removeWorkOrderPartLine(index) {
  workOrder.value.partLines.splice(index, 1)
}

function workOrderTotal() {
  const linesTotal = (workOrder.value?.lines || []).reduce((sum, line) => sum + (Number(line.quantity) || 0) * (Number(line.price) || 0), 0)
  const partsTotal = (workOrder.value?.partLines || []).reduce((sum, line) => sum + (Number(line.quantity) || 0) * (Number(line.price) || 0), 0)
  return linesTotal + partsTotal
}

async function saveWorkOrder() {
  if (!selectedWorkOrderCar.value || !workOrder.value) return
  try {
    const standalone = !selectedWorkOrderCar.value.id
    const result = await requestJson(standalone ? '/api/v1/work-orders/standalone' : `/api/v1/cars/${selectedWorkOrderCar.value.id}/work-order`, {
      method: standalone ? 'POST' : 'PUT',
      body: JSON.stringify({
        documentDate: workOrder.value.documentDate,
        customer: workOrder.value.customer,
        orderNumber: workOrder.value.orderNumber,
        invoiceNumber: workOrder.value.invoiceNumber,
        actNumber: workOrder.value.actNumber,
        status: workOrder.value.status,
        lines: workOrder.value.lines.map((line, index) => ({
          categoryName: line.categoryName || '', name: line.name, unit: line.unit,
          quantity: Number(line.quantity), price: Number(line.price), sortOrder: index,
        })),
        partLines: workOrder.value.partLines.map((line, index) => ({
          partId: line.partId, name: line.name, article: line.article || '',
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

function documentTitle() {
  if (workOrderDocumentType.value === 'bundle') return 'Печатный комплект'
  if (workOrderDocumentType.value === 'invoice') return 'Счёт на оплату'
  if (workOrderDocumentType.value === 'act') return 'Акт выполненных работ'
  return 'Заказ-наряд'
}

function printWorkOrder() {
  printDocument('order')
}

function printDocument(type) {
  workOrderDocumentType.value = type
  if (workOrder.value?.id) {
    requestJson(`/api/v1/work-orders/${workOrder.value.id}/documents/${type}?number=${encodeURIComponent(type === 'invoice' ? workOrder.value.invoiceNumber || '' : type === 'act' ? workOrder.value.actNumber || '' : workOrder.value.orderNumber || '')}`, { method: 'POST' }).catch((error) => showToast(`Документ не зарегистрирован: ${error.message}`))
  }
  documentTitle()
  const heading = document.querySelector('.print-target h2')
  const originalHeading = heading?.textContent
  const number = type === 'invoice' ? workOrder.value?.invoiceNumber : type === 'act' ? workOrder.value?.actNumber : workOrder.value?.orderNumber
  if (heading) heading.textContent = `${documentTitle()}${number ? ` · №${number}` : ''}`
  document.body.classList.add('printing-work-order')
  document.body.classList.add('printing-document', `printing-${type}`)
  window.setTimeout(() => window.print(), 0)
  window.setTimeout(() => {
    if (heading && originalHeading) heading.textContent = originalHeading
    document.body.classList.remove('printing-work-order', 'printing-document', `printing-${type}`)
  }, 1000)
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
      contractorId: carForm.value.contractorId ? Number(carForm.value.contractorId) : null,
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

async function deleteCar() {
  if (!editingCar.value || !window.confirm(`Удалить автомобиль №${editingCar.value.accountingNumber} вместе с запчастями и документами?`)) return
  try {
    await requestJson(`/api/v1/cars/${editingCar.value.id}`, { method: 'DELETE' })
    carFormVisible.value = false; editingCar.value = null; selectedCar.value = null
    await loadCars(); showToast('Автомобиль удалён')
  } catch (error) { showToast(error.message) }
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

async function toggleAccepted(car) {
  try {
    await requestJson(`/api/v1/cars/${car.id}/acceptance?accepted=${!car.acceptedAt}`, { method: 'PATCH' })
    await loadCars()
    showToast(car.acceptedAt ? 'Приёмка отменена' : 'Автомобиль принят')
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
  if (name === 'Настройки') {
    openDirectories()
    return
  }
  if (name === 'Дефектовка') {
    window.setTimeout(() => {
      const source = selectedCar.value || visibleCars.value[0]
      if (source) openDefect(mappedCars.value.find((item) => item.id === source.id) || source)
    }, 0)
    return
  }
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

watch(search, () => { window.clearTimeout(window.__efgenSearchTimer); window.__efgenSearchTimer = window.setTimeout(loadCars, 250) })
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
            <div class="row-actions"><button class="link-button" @click="openStub('Дефектовка')">Дефектовка</button><button class="link-button" @click="openWorkOrder(car)">Заказ-наряд</button><button class="link-button" @click="toggleAccepted(car)">{{ car.acceptedAt ? 'Отменить приёмку' : 'Принять автомобиль' }}</button><button class="link-button" @click="openPartForm(car)">＋ Запчасть</button><template v-for="part in car.parts" :key="part.id"><button class="link-button" @click="togglePartReceived(car, part)">{{ part.received ? `Отменить: ${part.name}` : `Поступила: ${part.name}` }}</button><button class="link-button" @click="openPartEdit(car, part)">Изменить: {{ part.name }}</button><button class="link-button danger-link" @click="deletePart(car, part)">Удалить</button></template></div>
          </article>
          <div v-if="!carsBusy && !carsError && !visibleCars.length" class="empty-state">По выбранному фильтру автомобили не найдены.</div>
        </div>
      </section>
    </main>

    <footer class="global-footer"><span>Efgen Bosh · рабочий интерфейс</span><span>Данные разделов подключаются поэтапно</span></footer>
    <button type="button" class="standalone-order-button button button-primary" @click="openStandaloneWorkOrder">＋ Новый заказ-наряд</button>
    <button v-if="directoriesVisible" type="button" class="contractors-button button button-cloud" @click="openContractorForm()">Исполнители</button>
    <button v-if="defectVisible && defect.recommendations" type="button" class="defect-transfer-button button button-primary" @click="transferDefectRecommendations">Перенести рекомендации в заказ-наряд</button>
    <div v-if="workOrderVisible && generatedDocuments.length" class="generated-documents-toolbar"><strong>Документы:</strong><span v-for="doc in generatedDocuments.slice(0, 6)" :key="doc.id">{{ doc.documentType }}{{ doc.documentNumber ? ` №${doc.documentNumber}` : '' }}</span></div>
    <button v-if="workOrderVisible && workOrder" type="button" class="bundle-print-button button button-cloud" @click="printDocument('bundle')">Печатный комплект</button>

    <div v-if="carFormVisible && editingCar" class="car-delete-toolbar"><span>Карточка автомобиля №{{ editingCar.accountingNumber }}</span><button type="button" class="link-button" @click="printCarDocument('acceptance')">Акт приёма</button><button type="button" class="link-button" @click="printCarDocument('delivery')">Акт выдачи</button><button type="button" class="link-button danger-link" @click="deleteCar">Удалить автомобиль</button></div>
    <div v-if="carFormVisible && editingCar && carHistory.length" class="car-history-toolbar"><strong>История:</strong><span v-for="event in carHistory.slice(0, 4)" :key="event.id">{{ event.details }}</span></div>
    <div v-if="carFormVisible && vehicleAliases.length" class="vehicle-alias-toolbar"><span>Модель из справочника:</span><select @change="applyVehicleAlias($event.target.value)"><option value="">Выбрать модель</option><option v-for="item in vehicleAliases" :key="item.id" :value="item.id">{{ item.sourceName }}<template v-if="item.normalizedLatinName"> · {{ item.normalizedLatinName }}</template></option></select></div>
    <div v-if="carFormVisible && contractors.length" class="contractor-toolbar"><span>Исполнитель:</span><select :value="carForm.contractorId" @change="applyContractor($event.target.value)"><option value="">Не выбран</option><option v-for="item in contractors" :key="item.id" :value="item.id">{{ item.shortName }} · {{ item.code }}</option></select></div>

    <div v-if="workOrderVisible && workOrder" class="document-toolbar"><span>Номера документов:</span><input v-model="workOrder.orderNumber" placeholder="Заказ-наряд №" /><input v-model="workOrder.invoiceNumber" placeholder="Счёт №" /><input v-model="workOrder.actNumber" placeholder="Акт №" /><span>Печать:</span><button type="button" class="link-button" @click="printDocument('order')">Заказ-наряд</button><button type="button" class="link-button" @click="printDocument('invoice')">Счёт</button><button type="button" class="link-button" @click="printDocument('act')">Акт</button></div>

    <div v-if="defectVisible" class="stub-overlay" @click.self="defectVisible = false"><section class="data-modal defect-modal"><button class="icon-button" aria-label="Закрыть" @click="defectVisible = false">×</button><p class="eyebrow">Осмотр автомобиля</p><h2>Дефектовка</h2><p class="modal-subtitle">{{ selectedDefectCar?.number }} · {{ selectedDefectCar?.vehicle }} · {{ selectedDefectCar?.registration }}</p><div v-if="defectBusy" class="empty-state">Загружаем дефектовку…</div><div v-else-if="defectError" class="empty-state">{{ defectError }}</div><form v-else class="data-form-grid" @submit.prevent="saveDefect"><label><span>Статус</span><select v-model="defect.status"><option value="DRAFT">Черновик</option><option value="CONFIRMED">Подтверждено</option></select></label><label class="form-wide"><span>Повреждения и замечания</span><textarea v-model="defect.findings" rows="5" placeholder="Передний бампер, левая дверь…"></textarea></label><label class="form-wide"><span>Рекомендованные работы и запчасти</span><textarea v-model="defect.recommendations" rows="5" placeholder="Замена бампера, окраска двери…"></textarea></label><label class="form-wide"><span>Фотографии осмотра (до 8)</span><input type="file" accept="image/*" multiple @change="readDefectPhotos" /></label><div v-if="defect.photos.length" class="defect-photo-grid form-wide"><img v-for="(photo, index) in defect.photos" :key="`${photo.slice(0, 24)}-${index}`" :src="photo" alt="Фото повреждения" /></div><div class="modal-actions form-wide"><button type="button" class="button button-cloud dark-button" @click="defectVisible = false">Отмена</button><button class="button button-primary" type="submit">Сохранить дефектовку</button></div></form></section></div>
    <div v-if="contractorVisible" class="stub-overlay" @click.self="contractorVisible = false"><form class="data-modal contractor-modal" @submit.prevent="saveContractor"><button type="button" class="icon-button" aria-label="Закрыть" @click="contractorVisible = false">×</button><p class="eyebrow">Реквизиты</p><h2>{{ editingContractor ? 'Изменить исполнителя' : 'Новый исполнитель' }}</h2><div class="data-form-grid"><label><span>Код</span><input v-model="contractorForm.code" required /></label><label><span>Краткое название</span><input v-model="contractorForm.shortName" required /></label><label class="form-wide"><span>Полное название</span><input v-model="contractorForm.fullName" required /></label><label><span>Подписант</span><input v-model="contractorForm.signerName" /></label><label><span>ИНН</span><input v-model="contractorForm.inn" /></label><label><span>ОГРНИП</span><input v-model="contractorForm.ogrnip" /></label><label class="form-wide"><span>Адрес</span><input v-model="contractorForm.address" /></label><label class="form-wide"><span>Банк</span><input v-model="contractorForm.bankName" /></label><label><span>БИК</span><input v-model="contractorForm.bik" /></label><label><span>Расчётный счёт</span><input v-model="contractorForm.settlementAccount" /></label></div><div class="contractor-list"><div v-for="item in contractors" :key="item.id" class="directory-item"><span>{{ item.shortName }}<small>{{ item.code }} · {{ item.inn || 'ИНН не указан' }}</small></span><button type="button" class="link-button" @click="openContractorForm(item)">Изменить</button><button type="button" class="link-button danger-link" @click="deleteContractor(item)">Удалить</button></div></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="contractorVisible = false">Закрыть</button><button class="button button-primary" type="submit">Сохранить</button></div></form></div>

    <div v-if="carFormVisible" class="stub-overlay" @click.self="carFormVisible = false"><form class="data-modal" @submit.prevent="saveCar"><button type="button" class="icon-button" aria-label="Закрыть" @click="carFormVisible = false">×</button><p class="eyebrow">Карточка автомобиля</p><h2>{{ editingCar ? `Автомобиль №${editingCar.accountingNumber}` : 'Новый автомобиль' }}</h2><div class="data-form-grid"><label><span>Госномер</span><input v-model="carForm.registrationNumber" required placeholder="А123ВС124" /></label><label><span>Автомобиль</span><input v-model="carForm.vehicleName" required placeholder="Джили Окаванго" /></label><label class="form-wide"><span>Марка / модель латиницей</span><input v-model="carForm.vehicleNameLatin" placeholder="HYUNDAI CRETA" /></label><label><span>VIN</span><input v-model="carForm.vin" placeholder="VIN автомобиля" /></label><label><span>Страхователь</span><input v-model="carForm.insuredPerson" placeholder="ФИО или организация" /></label><label><span>Страховая</span><select v-model="carForm.insurerId"><option value="">Не выбрана</option><option v-for="item in insurers" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select></label><label><span>Номер дела</span><input v-model="carForm.claimNumber" placeholder="108148/26" /></label><label><span>Смена</span><select v-model="carForm.shiftId"><option value="">Не выбрана</option><option v-for="item in shifts" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select></label><label><span>Дата начала</span><input v-model="carForm.startedAt" type="date" /></label><label><span>Дата приёмки</span><input v-model="carForm.acceptedAt" type="date" /></label><label><span>Дата записи</span><input v-model="carForm.appointmentDate" type="date" /></label><label class="form-wide"><span>Папка документов</span><input v-model="carForm.documentFolderUrl" type="url" placeholder="https://..." /></label><label class="form-wide"><span>Комментарий</span><textarea v-model="carForm.comment" rows="3"></textarea></label></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="carFormVisible = false">Отмена</button><button class="button button-primary" type="submit">{{ editingCar ? 'Сохранить изменения' : 'Сохранить автомобиль' }}</button></div></form></div>

    <div v-if="partFormVisible" class="stub-overlay" @click.self="partFormVisible = false"><form class="data-modal compact-modal" @submit.prevent="savePart"><button type="button" class="icon-button" aria-label="Закрыть" @click="partFormVisible = false">×</button><p class="eyebrow">Заказ запчасти</p><h2>{{ editingPart ? 'Изменить деталь' : 'Добавить деталь' }}</h2><p class="modal-subtitle">{{ selectedCar?.number }} · {{ selectedCar?.vehicle }}</p><div class="data-form-grid"><label><span>Деталь</span><input v-model="partForm.name" required placeholder="Бампер передний" /></label><label><span>Артикул</span><input v-model="partForm.article" placeholder="604A124500" /></label><label><span>Поставщик</span><select v-model="partForm.supplierId"><option value="">Не выбран</option><option v-for="item in suppliers" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select></label><label><span>Ожидаемая дата</span><input v-model="partForm.expectedDate" type="date" /></label></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="partFormVisible = false">Отмена</button><button class="button button-primary" type="submit">{{ editingPart ? 'Сохранить изменения' : 'Добавить деталь' }}</button></div></form></div>

    <div v-if="directoriesVisible" class="stub-overlay" @click.self="directoriesVisible = false"><section class="data-modal directory-modal"><button class="icon-button" aria-label="Закрыть" @click="directoriesVisible = false">×</button><p class="eyebrow">Настройки</p><h2>Справочники</h2><div class="directory-tabs"><button type="button" :class="{ 'is-active': directoryType === 'insurers' }" @click="directoryType = 'insurers'">Страховые</button><button type="button" :class="{ 'is-active': directoryType === 'suppliers' }" @click="directoryType = 'suppliers'">Поставщики</button><button type="button" :class="{ 'is-active': directoryType === 'shifts' }" @click="directoryType = 'shifts'">Смены</button><button type="button" :class="{ 'is-active': directoryType === 'works' }" @click="directoryType = 'works'">Работы</button><button type="button" :class="{ 'is-active': directoryType === 'counterparties' }" @click="directoryType = 'counterparties'">Контрагенты</button></div><form class="data-form-grid" @submit.prevent="createDirectoryItem"><label v-if="directoryType === 'works'"><span>Код работы</span><input v-model="directoryForm.code" required placeholder="BODY-001" /></label><label><span>{{ directoryType === 'works' ? 'Название работы' : 'Название' }}</span><input v-model="directoryForm.name" required placeholder="Название элемента" /></label><label v-if="directoryType === 'works'"><span>Категория</span><input v-model="directoryForm.categoryName" required placeholder="Кузовные работы" /></label><label v-if="directoryType === 'works'"><span>Единица</span><input v-model="directoryForm.defaultUnit" required placeholder="н/ч" /></label><label v-if="directoryType === 'counterparties'"><span>ИНН</span><input v-model="directoryForm.inn" placeholder="ИНН" /></label><label v-if="directoryType === 'counterparties'"><span>Телефон</span><input v-model="directoryForm.phone" placeholder="+7..." /></label><label v-if="directoryType === 'counterparties'" class="form-wide"><span>Адрес</span><input v-model="directoryForm.address" placeholder="Адрес" /></label><label v-if="directoryType === 'counterparties'" class="form-wide"><span>Примечание</span><textarea v-model="directoryForm.note" rows="2"></textarea></label><div class="modal-actions form-wide"><button class="button button-primary" type="submit">{{ editingDirectory ? 'Сохранить изменения' : 'Добавить в справочник' }}</button></div></form><div class="directory-list"><div v-for="item in (directoryType === 'insurers' ? insurers : directoryType === 'suppliers' ? suppliers : directoryType === 'shifts' ? shifts : directoryType === 'counterparties' ? counterparties : workCatalog)" :key="item.id" class="directory-item"><span>{{ item.name }}<small v-if="directoryType === 'works'">{{ item.categoryName }} · {{ item.defaultUnit }}</small><small v-if="directoryType === 'counterparties'">{{ item.inn }} · {{ item.phone }}</small></span><code v-if="directoryType === 'works'">{{ item.code }}</code><button type="button" class="link-button" @click="openDirectoryEdit(item)">Изменить</button><button type="button" class="link-button danger-link" @click="deleteDirectoryItem(item)">Удалить</button></div><p v-if="!(directoryType === 'insurers' ? insurers : directoryType === 'suppliers' ? suppliers : directoryType === 'shifts' ? shifts : directoryType === 'counterparties' ? counterparties : workCatalog).length" class="empty-state">Справочник пока пуст.</p></div></section></div>

    <div v-if="workOrderVisible" class="stub-overlay" @click.self="workOrderVisible = false"><section class="data-modal work-order-modal print-target"><button class="icon-button" aria-label="Закрыть" @click="workOrderVisible = false">×</button><p class="eyebrow">Рабочие данные</p><h2>Заказ-наряд · №{{ selectedWorkOrderCar?.number }}</h2><p class="modal-subtitle">{{ selectedWorkOrderCar?.vehicle }} · {{ selectedWorkOrderCar?.registration }}</p><div v-if="workOrderBusy" class="empty-state">Загружаем заказ-наряд…</div><div v-else-if="workOrderError" class="empty-state">{{ workOrderError }}</div><template v-else-if="workOrder"><div class="data-form-grid work-order-meta"><label><span>Дата документа</span><input v-model="workOrder.documentDate" type="date" /></label><label><span>Заказчик из справочника</span><select v-model="workOrder.customer"><option value="">Произвольный заказчик</option><option v-for="item in counterparties" :key="item.id" :value="item.name">{{ item.name }} · {{ item.inn || "без ИНН" }}</option></select></label><label><span>Заказчик</span><input v-model="workOrder.customer" placeholder="ФИО или организация" /></label></div><div class="work-order-lines"><div class="work-order-line work-order-line-head"><span>Категория</span><span>Работа</span><span>Ед.</span><span>Кол-во</span><span>Цена</span><span>Сумма</span><span></span></div><div v-for="(line, index) in workOrder.lines" :key="line.id || `new-${index}`" class="work-order-line"><select v-model="line.catalogId" @change="applyCatalogLine(line)"><option value="">Своя работа</option><option v-for="item in workCatalog" :key="item.id" :value="String(item.id)">{{ item.categoryName }} · {{ item.name }}</option></select><input v-model="line.name" required placeholder="Ремонт двери" /><input v-model="line.unit" placeholder="шт." /><input v-model.number="line.quantity" type="number" min="0.001" step="0.001" /><input v-model.number="line.price" type="number" min="0" step="0.01" /><strong>{{ ((Number(line.quantity) || 0) * (Number(line.price) || 0)).toFixed(2) }}</strong><button type="button" class="icon-button small-icon" aria-label="Удалить строку" @click="removeWorkOrderLine(index)">×</button></div><button type="button" class="link-button" @click="addWorkOrderLine">＋ Добавить работу</button></div><div class="work-order-parts"><div class="work-order-line work-order-line-head"><span>Запчасть</span><span>Артикул</span><span>Кол-во</span><span>Цена</span><span>Сумма</span><span></span></div><div v-for="(line, index) in workOrder.partLines" :key="line.id || `part-new-${index}`" class="work-order-part-line"><strong>{{ line.name }}</strong><span>{{ line.article || "—" }}</span><input v-model.number="line.quantity" type="number" min="0.001" step="0.001" /><input v-model.number="line.price" type="number" min="0" step="0.01" /><strong>{{ ((Number(line.quantity) || 0) * (Number(line.price) || 0)).toFixed(2) }}</strong><button type="button" class="icon-button small-icon" aria-label="Удалить строку запчасти" @click="removeWorkOrderPartLine(index)">×</button></div><div class="work-order-part-picker"><span>Добавить запчасть:</span><button v-for="part in selectedWorkOrderCar.parts" :key="part.id" type="button" class="link-button" :disabled="workOrder.partLines.some((line) => line.partId === part.id)" @click="addWorkOrderPartLine(part)">{{ part.name }}</button></div></div><div class="work-order-total">Итого: <strong>{{ workOrderTotal().toFixed(2) }}</strong></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="workOrderVisible = false">Закрыть</button><button class="button button-cloud dark-button" @click="printWorkOrder">Печать</button><button class="button button-primary" @click="saveWorkOrder">Сохранить заказ-наряд</button></div></template></section></div>

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
.work-order-part-line { display: grid; grid-template-columns: 2fr 1.2fr .8fr .95fr .95fr 34px; gap: 8px; align-items: center; min-width: 760px; margin-bottom: 8px; }
.work-order-part-line input { width: 100%; height: 38px; padding: 8px 10px; border: 1px solid var(--line); border-radius: 8px; background: var(--soft); }
.work-order-part-picker { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin-top: 12px; color: var(--muted); font-size: 12px; }
.small-icon { width: 30px; height: 30px; }
.work-order-total { margin-top: 18px; text-align: right; font-size: 16px; }
.defect-modal { width: min(760px, 100%); }
.defect-photo-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
.defect-photo-grid img { width: 100%; height: 120px; object-fit: cover; border-radius: 9px; border: 1px solid var(--line); }
.car-delete-toolbar { position: fixed; right: 24px; bottom: 24px; z-index: 20; display: flex; gap: 12px; align-items: center; padding: 10px 14px; border: 1px solid #ead1d1; border-radius: 10px; background: #fff7f7; box-shadow: 0 8px 30px rgb(8 43 37 / 12%); font-size: 12px; }
.vehicle-alias-toolbar { position: fixed; left: 24px; bottom: 24px; z-index: 20; display: flex; gap: 10px; align-items: center; padding: 10px 14px; border: 1px solid var(--line); border-radius: 10px; background: white; box-shadow: 0 8px 30px rgb(8 43 37 / 12%); color: var(--muted); font-size: 12px; }
.vehicle-alias-toolbar select { height: 32px; border: 1px solid var(--line); border-radius: 7px; background: var(--soft); }
.contractor-toolbar { position: fixed; left: 24px; bottom: 76px; z-index: 20; display: flex; gap: 10px; align-items: center; padding: 10px 14px; border: 1px solid var(--line); border-radius: 10px; background: white; box-shadow: 0 8px 30px rgb(8 43 37 / 12%); color: var(--muted); font-size: 12px; }
.contractor-toolbar select { height: 32px; border: 1px solid var(--line); border-radius: 7px; background: var(--soft); }
.standalone-order-button { position: fixed; right: 24px; top: 88px; z-index: 10; }
.contractors-button { position: fixed; right: 190px; top: 88px; z-index: 10; }
.contractor-modal { width: min(900px, 100%); }
.defect-transfer-button { position: fixed; right: 24px; bottom: 76px; z-index: 21; }
.car-history-toolbar { position: fixed; left: 24px; top: 88px; z-index: 20; display: flex; gap: 9px; align-items: center; max-width: 560px; padding: 9px 12px; border: 1px solid var(--line); border-radius: 9px; background: white; box-shadow: 0 8px 30px rgb(8 43 37 / 12%); color: var(--muted); font-size: 11px; }
.generated-documents-toolbar { position: fixed; left: 24px; bottom: 76px; z-index: 20; display: flex; gap: 10px; padding: 9px 12px; border: 1px solid var(--line); border-radius: 9px; background: white; box-shadow: 0 8px 30px rgb(8 43 37 / 12%); color: var(--muted); font-size: 11px; }
.bundle-print-button { position: fixed; right: 24px; bottom: 76px; z-index: 21; }
.directory-modal { width: min(760px, 100%); }
.directory-tabs { display: flex; gap: 6px; margin: 6px 0 20px; border-bottom: 1px solid var(--line); }
.directory-tabs button { padding: 9px 12px; border: 0; border-bottom: 2px solid transparent; color: var(--muted); background: transparent; font-size: 12px; font-weight: 750; }
.directory-tabs button.is-active { color: var(--brand); border-bottom-color: var(--accent); }
.directory-list { display: grid; gap: 7px; max-height: 260px; overflow: auto; margin-top: 22px; }
.directory-item { display: flex; justify-content: space-between; gap: 12px; padding: 10px 12px; border: 1px solid var(--line); border-radius: 8px; color: var(--ink); font-size: 13px; }
.directory-item small { display: block; margin-top: 3px; color: var(--muted); font-size: 11px; }
.directory-item code { color: var(--muted); }
:global(body.printing-work-order *) { visibility: hidden !important; }
:global(body.printing-work-order .print-target), :global(body.printing-work-order .print-target *) { visibility: visible !important; }
:global(body.printing-work-order .print-target) { position: absolute; inset: 0; width: 100%; max-height: none; overflow: visible; margin: 0; padding: 24px; border-radius: 0; box-shadow: none; }
:global(body.printing-work-order .modal-actions), :global(body.printing-work-order .icon-button), :global(body.printing-work-order .work-order-part-picker) { display: none !important; }
:global(body.printing-invoice .print-target h2), :global(body.printing-act .print-target h2) { font-size: 0 !important; }
:global(body.printing-invoice .print-target h2::after) { content: 'Счёт на оплату'; font-size: 28px; }
:global(body.printing-act .print-target h2::after) { content: 'Акт выполненных работ'; font-size: 28px; }
:global(body.printing-document .document-toolbar) { display: none !important; }
:global(body.printing-car-document *) { visibility: hidden !important; }
:global(body.printing-car-document .data-modal), :global(body.printing-car-document .data-modal *) { visibility: visible !important; }
:global(body.printing-car-document .data-modal) { position: absolute; inset: 0; width: 100%; max-height: none; margin: 0; padding: 30px; border-radius: 0; box-shadow: none; }
:global(body.printing-car-document .modal-actions), :global(body.printing-car-document .icon-button), :global(body.printing-car-document .car-delete-toolbar) { display: none !important; }
</style>
