<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
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
const activeFilter = ref('all')
const insurerFilter = ref('')
const shiftFilter = ref('')
const contractorFilter = ref('')
const overduePartsOnly = ref(false)
const search = ref('')
const cars = ref([])
const activeSection = ref('cases')
const repairRegistry = ref([])
const caseSearch = ref('')
const caseStatusFilter = ref('ALL')
const caseContractorFilter = ref('')
const editingContractorCaseId = ref(null)
const savingContractorCaseId = ref(null)
const savingAppointmentDateCaseId = ref(null)
const caseStatusOptions = ref([{ code: 'ALL', label: 'Все' }])
const casePage = ref(0)
const casePageSize = ref(20)
const caseTotalItems = ref(0)
const caseTotalPagesFromApi = ref(0)
const insurers = ref([])
const insurersBusy = ref(false)
const insurersError = ref('')
const suppliers = ref([])
const shifts = ref([])
const workCatalog = ref([])
const counterparties = ref([])
const vehicleAliases = ref([])
const contractors = ref([])
const contractorsBusy = ref(false)
const contractorsError = ref('')
const vehicleMakes = ref([])
const vehicleModels = ref([])
const vehicleMakeSelected = ref(false)
const vehicleModelSelected = ref(false)
const carsBusy = ref(false)
const carsError = ref('')
const carPage = ref(0)
const carPageSize = ref(25)
const carTotalPages = ref(0)
const carTotalItems = ref(0)
const carHistory = ref([])
const expandedCars = ref(new Set())
const carPhotos = ref([])
const photosVisible = ref(false)
const photosCar = ref(null)
const photosCase = ref(null)
const photosBusy = ref(false)
const repairCases = ref([])
const repairCasesVisible = ref(false)
const repairMenuVisible = ref(false)
const insuranceVehiclePickerVisible = ref(false)
const insuranceCaseCreateVisible = ref(false)
const repairVinSearch = ref('')
const repairVehicleCandidates = ref([])
const repairVehicleSearchBusy = ref(false)
const repairVehicleSearchError = ref('')
let repairVehicleSearchRequestId = 0
const repairCasesBusy = ref(false)
const repairCaseCar = ref(null)
const editingRepairCase = ref(null)
const repairCaseForm = ref({ caseNumber: '', status: 'CREATED', insuredPerson: '', claimNumber: '', insurerId: '', contractorId: '', shiftId: '', acceptedAt: '' })
const repairCasePhotos = ref([])
const newInsuranceCaseForm = ref({ caseNumber: '', insurerId: '' })
const selectedCreationType = ref('INSURANCE')
const caseDetailVisible = ref(false)
const caseDetailTab = ref('main')
const selectedRegistryCase = ref(null)
const caseDetailRecord = ref(null)
const caseContractorId = ref('')
const caseHistory = ref([])
const caseParts = ref([])
const expandedCaseId = ref(null)
const expandedCaseParts = ref([])
const caseActionModal = ref(null)
const scheduleRepairModal = ref(false)
const caseActionForm = ref({ contractorId: '', appointmentDate: '', appointmentTime: '', receivedBy: '', comment: '' })
let caseActionObserver = null
const caseDocumentsVisible = ref(false)
const statusLabels = { CREATED: 'Создан', WAITING_PARTS: 'Ждем детали', PARTS_RECEIVED: 'Детали поступили', SCHEDULED: 'Запись на ремонт', DELIVERED: 'Машина выдана' }
const statusLabel = (status) => statusLabels[status] || status

function removeClosedCaseAction() {
  document.querySelectorAll('.case-action-bar button').forEach((button) => {
    if (button.textContent.trim() === 'Закрыть случай') button.remove()
    if (button.textContent.trim() === 'Начать ремонт') button.textContent = 'Выдать автомобиль'
    if (button.textContent.trim() === 'Завершить ремонт') button.remove()
  })
}
const modal = ref(null)
const toast = ref('')
let toastTimer = null
const carFormVisible = ref(false)
const carViewVisible = ref(false)
const carViewTab = ref('main')
const viewCar = ref(null)
const viewCarCases = ref([])
const viewCarCasesBusy = ref(false)
const partFormVisible = ref(false)
const workOrderVisible = ref(false)
const workOrderRegistryVisible = ref(false)
const workOrderRegistry = ref([])
const workOrderRegistryBusy = ref(false)
const workOrderRegistryError = ref('')
const directoriesVisible = ref(false)
const settingsTab = ref('directories')
const settingsNewInsurer = ref('')
const settingsNewSupplier = ref('')
const settingsNewWork = ref({ name: '', normHours: 1 })
const settingsNewMaster = ref({ code: '', shortName: '' })
const settingsNewCounterparty = ref({ name: '', inn: '', phone: '', address: '', note: '' })
const settingsDirectoryTab = ref('insurers')
const settingsEditor = ref(null)
const settingsEditorForm = ref({ name: '', legalDetails: '', normHours: 1, categoryName: 'Кузовные работы', fullName: '', phone: '', inn: '', address: '', note: '', makeId: '', makeName: '', modelName: '' })
const settingsVehicleRows = ref([])
const contractorVisible = ref(false)
const editingContractor = ref(null)
const contractorForm = ref({ code: '', shortName: '', fullName: '', phone: '', signerName: '', inn: '', ogrnip: '', address: '', bankName: '', bankInn: '', bankKpp: '', bik: '', correspondentAccount: '', settlementAccount: '' })
const editingCar = ref(null)
const editingPart = ref(null)
const casePartContext = ref(null)
const selectedCar = ref(null)
const selectedWorkOrderCar = ref(null)
const selectedWorkOrderCase = ref(null)
const workOrder = ref(null)
const workOrderBusy = ref(false)
const workOrderError = ref('')
const workCatalogPickerVisible = ref(false)
const workCatalogPickerId = ref('')
watch(workOrderVisible, (visible, previous) => {
  if (!visible && previous && selectedWorkOrderCase.value) {
    caseDetailTab.value = 'works'
    caseDetailVisible.value = true
  }
})
watch(partFormVisible, (visible, previous) => {
  if (!visible && previous && casePartContext.value) {
    caseDetailTab.value = 'parts'
    caseDetailVisible.value = true
  }
})
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

const DISPLAY_TIME_ZONE = 'Asia/Krasnoyarsk'
const displayDateFormatter = new Intl.DateTimeFormat('ru-RU', {
  timeZone: DISPLAY_TIME_ZONE,
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
})
const displayDateTimePartsFormatter = new Intl.DateTimeFormat('en-CA', {
  timeZone: DISPLAY_TIME_ZONE,
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  hourCycle: 'h23',
})

function formatDisplayDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? String(value).slice(0, 10) : displayDateFormatter.format(date)
}

function formatDisplayDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value).slice(0, 16).replace('T', ' ')
  const parts = Object.fromEntries(displayDateTimePartsFormatter.formatToParts(date).filter((part) => part.type !== 'literal').map((part) => [part.type, part.value]))
  return `${parts.year}-${parts.month}-${parts.day} ${parts.hour}:${parts.minute}`
}

function displayTodayIso() {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: DISPLAY_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(new Date())
  const values = Object.fromEntries(parts.filter((part) => part.type !== 'literal').map((part) => [part.type, part.value]))
  return `${values.year}-${values.month}-${values.day}`
}

function emptyCarForm() {
  return {
    vehicleMake: '', vehicleModel: '', registrationNumber: '', vin: '', ownerName: '', ownerPhone: '',
    comment: '', documentFolderUrl: '',
  }
}

function emptyPartForm() {
  return { name: '', article: '', catalogNumber: '', manufacturer: '', quantity: 1, supplierId: '', orderedAt: '', expectedDate: '', comment: '', received: false, sortOrder: 0 }
}

const mappedCars = computed(() => cars.value.map((car) => ({
  ...car,
  number: car.accountingNumber,
  registration: car.registrationNumber || 'Госномер не указан',
  vehicle: [car.vehicleMake, car.vehicleModel].filter(Boolean).join(' ') || car.vehicleName,
  insurer: insurers.value.find((item) => item.id === car.insurerId)?.name || (car.insurerId ? `Страховая #${car.insurerId}` : 'Страховая не выбрана'),
  start: formatDisplayDate(car.createdAt),
  parts: car.parts,
  partsSummary: car.parts.length ? `${car.parts.filter((part) => part.received).length} / ${car.parts.length}` : '—',
  comment: car.comment || '—',
  ownerPhone: car.ownerPhone || '',
  record: formatDisplayDate(car.createdAt),
  shift: shifts.value.find((item) => item.id === car.shiftId)?.name || (car.shiftId ? `Смена #${car.shiftId}` : '—'),
  status: car.status.toLowerCase(),
})))

const visibleCars = computed(() => mappedCars.value)
const displayedClientCars = visibleCars

const stats = ref({ active: 0, waiting: 0, ready: 0, delivered: 0 })

async function loadCars() {
  carsBusy.value = true
  carsError.value = ''
  try {
    const params = new URLSearchParams({ page: String(carPage.value), size: String(carPageSize.value), status: activeFilter.value, overdue: String(overduePartsOnly.value) })
    if (search.value.trim()) params.set('q', search.value.trim())
    if (insurerFilter.value) params.set('insurerId', insurerFilter.value)
    if (shiftFilter.value) params.set('shiftId', shiftFilter.value)
    if (contractorFilter.value) params.set('contractorId', contractorFilter.value)
    const result = await requestJson(`/api/v1/cars/search/page?${params.toString()}`)
    cars.value = result.items
    carTotalPages.value = result.totalPages
    carTotalItems.value = result.totalItems
    stats.value = result.summary || stats.value
  } catch (error) {
    carsError.value = error.message
  } finally {
    carsBusy.value = false
  }
}

async function loadRepairRegistry() {
  try {
    const params = new URLSearchParams({ page: String(casePage.value), size: String(casePageSize.value), status: caseStatusFilter.value === 'ALL' ? 'all' : caseStatusFilter.value })
    if (caseSearch.value.trim()) params.set('q', caseSearch.value.trim())
    if (caseContractorFilter.value) params.set('contractorId', caseContractorFilter.value)
    const result = await requestJson(`/api/v1/cars/search/page?${params.toString()}`)
    cars.value = result.items
    carTotalPages.value = result.totalPages
    carTotalItems.value = result.totalItems
    stats.value = result.summary || stats.value
    repairRegistry.value = result.items.flatMap((car) => (car.repairCases || []).filter((item) => (caseStatusFilter.value === 'ALL' || item.status === caseStatusFilter.value) && (!caseContractorFilter.value || String(item.contractorId || '') === String(caseContractorFilter.value))).map((item) => ({ ...item, carId: car.id }))).sort((left, right) => Number(right.id || 0) - Number(left.id || 0))
    caseTotalItems.value = result.totalItems
    caseTotalPagesFromApi.value = result.totalPages
    syncCaseRowMeta()
    if (caseSearch.value.trim() && repairRegistry.value.length) {
      const first = repairRegistry.value[0]
      expandedCaseId.value = first.id
      expandedCaseParts.value = await requestJson(`/api/v1/cars/${first.carId}/repair-cases/${first.id}/parts`).catch(() => [])
    }
  } catch (error) { showToast(`Страховые случаи не загружены: ${error.message}`) }
}

async function loadRepairCaseStatuses() {
  try {
    const result = await requestJson('/api/v1/repair-case-statuses')
    caseStatusOptions.value = [{ code: 'ALL', label: 'Все' }, ...result]
  } catch (error) {
    showToast(`Статусы страховых случаев не загружены: ${error.message}`)
  }
}

async function openCaseDetail(item) {
  await Promise.all([loadContractors(), loadInsurers()])
  selectedRegistryCase.value = item
  caseDetailRecord.value = null
  caseContractorId.value = item.contractorId ? String(item.contractorId) : ''
  casePartContext.value = item
  repairCaseCar.value = mappedCars.value.find((car) => car.id === item.carId) || { id: item.carId, number: item.accountingNumber, vehicle: `${item.vehicleMake} ${item.vehicleModel}`, vin: item.vin, registration: item.registrationNumber, ownerName: item.ownerName, ownerPhone: item.ownerPhone, parts: [] }
  caseParts.value = []; caseDetailTab.value = 'main'; caseDetailVisible.value = true
  nextTick(() => {
    const contractorPanel = document.querySelector('.case-detail-contractor')
    contractorPanel?.classList.remove('is-editing')
    contractorPanel?.classList.toggle('has-contractor', Boolean(item.contractorId))
  })
  const [history, parts, detailWorkOrder, detailRecord] = await Promise.all([
    requestJson(`/api/v1/cars/${item.carId}/repair-cases/${item.id}/history`).catch(() => []),
    requestJson(`/api/v1/cars/${item.carId}/repair-cases/${item.id}/parts`).catch(() => []),
    requestJson(`/api/v1/cars/${item.carId}/repair-cases/${item.id}/work-order`).catch(() => null),
    requestJson(`/api/v1/cars/${item.carId}/repair-cases/${item.id}`).catch(() => null),
  ])
  caseHistory.value = history; caseParts.value = parts; workOrder.value = detailWorkOrder; caseDetailRecord.value = detailRecord; if (detailRecord) selectedRegistryCase.value = { ...selectedRegistryCase.value, ...detailRecord, createdBy: detailRecord.createdByName || detailRecord.createdBy }; caseContractorId.value = detailRecord?.contractorId ? String(detailRecord.contractorId) : caseContractorId.value
  const assignedContractor = contractors.value.find((value) => String(value.id) === String(item.contractorId))?.shortName
  carHistory.value = caseHistory.value.map((event) => { const text = event.comment || `${statusLabel(event.previousStatus)} → ${statusLabel(event.newStatus)}`; const assignment = text.includes('записан на ремонт') && assignedContractor && !text.includes('Исполнитель') ? `. Исполнитель: ${assignedContractor}${item.appointmentDate ? `. Дата: ${item.appointmentDate}` : ''}` : ''; return { ...event, actor: event.createdByName || 'Система', details: `${event.createdByName || 'Система'} · ${text}${assignment}` } })
}

async function saveCaseContractor() {
  const item = selectedRegistryCase.value
  const detail = caseDetailRecord.value
  if (!item || !detail || !caseContractorId.value) return
  try {
    const saved = await requestJson(`/api/v1/cars/${item.carId}/repair-cases/${item.id}`, {
      method: 'PUT',
      body: JSON.stringify({
        caseNumber: detail.caseNumber || item.caseNumber,
        status: detail.status || item.status,
        repairType: detail.repairType || 'INSURANCE',
        insuredPerson: detail.insuredPerson || '',
        claimNumber: detail.claimNumber || item.claimNumber || '',
        insurerId: detail.insurerId ?? item.insurerId ?? null,
        contractorId: Number(caseContractorId.value),
        shiftId: detail.shiftId ?? null,
        acceptedAt: detail.acceptedAt || null,
        comment: detail.comment || '',
        appointmentDate: detail.appointmentDate || null,
        appointmentTime: detail.appointmentTime || null,
        receivedBy: detail.receivedBy || '',
      }),
    })
    caseDetailRecord.value = saved
    selectedRegistryCase.value = { ...selectedRegistryCase.value, contractorId: saved.contractorId }
    const history = await requestJson(`/api/v1/cars/${item.carId}/repair-cases/${item.id}/history`)
    carHistory.value = history.map((event) => ({ ...event, actor: event.createdByName || 'Система', details: `${event.createdByName || 'Система'} · ${event.comment || `${statusLabel(event.previousStatus)} → ${statusLabel(event.newStatus)}`}` }))
    await Promise.all([loadRepairRegistry(), loadRepairCaseStatuses()])
    document.querySelector('.case-detail-contractor')?.classList.remove('is-editing')
    showToast('Исполнитель заменён')
  } catch (error) { showToast(error.message) }
}

async function saveInlineCaseContractor(item, event) {
  const contractorId = event.target.value
  if (!contractorId || savingContractorCaseId.value === item.id) return
  savingContractorCaseId.value = item.id
  try {
    const saved = await requestJson(`/api/v1/cars/${item.carId}/repair-cases/${item.id}/contractor?contractorId=${encodeURIComponent(contractorId)}`, { method: 'PATCH' })
    item.contractorId = saved.contractorId
    editingContractorCaseId.value = null
    showToast('Исполнитель заменён')
  } catch (error) {
    showToast(error.message)
  } finally {
    savingContractorCaseId.value = null
  }
}

async function saveInlineAppointmentDate(item, event) {
  if (savingAppointmentDateCaseId.value === item.id) return
  savingAppointmentDateCaseId.value = item.id
  const appointmentDate = event.target.value || null
  try {
    const saved = await requestJson(`/api/v1/cars/${item.carId}/repair-cases/${item.id}`, {
      method: 'PUT',
      body: JSON.stringify({
        caseNumber: item.caseNumber,
        status: item.status,
        repairType: item.repairType || 'INSURANCE',
        insuredPerson: item.insuredPerson || '',
        claimNumber: item.claimNumber || '',
        insurerId: item.insurerId ?? null,
        contractorId: item.contractorId ?? null,
        shiftId: item.shiftId ?? null,
        acceptedAt: item.acceptedAt || null,
        comment: item.comment || '',
        appointmentDate,
        appointmentTime: item.appointmentTime || null,
        receivedBy: item.receivedBy || '',
      }),
    })
    Object.assign(item, saved)
    showToast('Дата записи на ремонт сохранена')
  } catch (error) {
    event.target.value = item.appointmentDate || ''
    showToast(error.message)
  } finally {
    savingAppointmentDateCaseId.value = null
  }
}

async function toggleCaseRow(item) {
  if (expandedCaseId.value === item.id) { expandedCaseId.value = null; expandedCaseParts.value = []; return }
  expandedCaseId.value = item.id
  expandedCaseParts.value = await requestJson(`/api/v1/cars/${item.carId}/repair-cases/${item.id}/parts`).catch(() => [])
}

async function toggleCasePartReceived(item, part) {
  try {
    const updated = await requestJson(`/api/v1/cars/${item.carId}/repair-cases/${item.id}/parts/${part.id}`, {
      method: 'PUT',
      body: JSON.stringify({ name: part.name, article: part.article || '', catalogNumber: part.catalogNumber || '', manufacturer: part.manufacturer || '', quantity: Number(part.quantity || 1), supplierId: part.supplierId || null, orderedAt: part.orderedAt || null, expectedDate: part.expectedDate || null, comment: part.comment || '', received: !part.received, sortOrder: part.sortOrder || 0 }),
    })
    expandedCaseParts.value = expandedCaseParts.value.map((value) => value.id === updated.id ? updated : value)
    await Promise.all([loadRepairRegistry(), loadRepairCaseStatuses(), loadContractors()])
    selectedRegistryCase.value = repairRegistry.value.find((value) => value.id === item.id) || selectedRegistryCase.value
    showToast(updated.received ? `Деталь «${updated.name}» получена` : `Поступление детали «${updated.name}» отменено`)
  } catch (error) { showToast(error.message) }
}

async function runCaseAction(action) {
  const item = selectedRegistryCase.value; if (!item) return
  if (action === 'CLOSE') return
  if (action === 'START_REPAIR' && item.status === 'SCHEDULED') action = 'DELIVER'
  if (action === 'SCHEDULE_REPAIR') { await loadContractors(); caseActionForm.value = { contractorId: item.contractorId ? String(item.contractorId) : '', appointmentDate: '', appointmentTime: '', receivedBy: '', comment: '' }; scheduleRepairModal.value = true; return }
  if (action === 'DELIVER') { caseActionModal.value = action; caseActionForm.value = { contractorId: '', appointmentDate: '', appointmentTime: '', receivedBy: 'Система', comment: '' }; return }
  try { const updated = await requestJson(`/api/v1/cars/${item.carId}/repair-cases/${item.id}/actions/${action}`, { method: 'POST' }); selectedRegistryCase.value = updated; await loadRepairRegistry(); showToast('Действие выполнено') } catch (error) { showToast(error.message) }
}

async function submitCaseAction() {
  const item = selectedRegistryCase.value; if (!item) return
  const action = caseActionModal.value
  try { const updated = await requestJson(`/api/v1/cars/${item.carId}/repair-cases/${item.id}/actions/${action}`, { method: 'POST', body: JSON.stringify(action === 'DELIVER' ? { ...caseActionForm.value, receivedBy: '' } : caseActionForm.value) }); selectedRegistryCase.value = updated; caseActionModal.value = null; await loadRepairRegistry(); showToast('Действие сохранено') } catch (error) { showToast(error.message) }
}

async function submitScheduleRepair() {
  const item = selectedRegistryCase.value
  if (!item || !caseActionForm.value.contractorId) { showToast('Выберите исполнителя для страхового случая.'); return }
  try {
    const updated = await requestJson(`/api/v1/cars/${item.carId}/repair-cases/${item.id}/actions/SCHEDULE_REPAIR`, { method: 'POST', body: JSON.stringify(caseActionForm.value) })
    selectedRegistryCase.value = updated
    scheduleRepairModal.value = false
    await loadRepairRegistry()
    showToast('Запись на ремонт сохранена')
  } catch (error) { showToast(error.message) }
}

function openCaseDocuments() { caseDocumentsVisible.value = true }

function syncContractorPanel() {
  nextTick(() => {
    const panel = document.querySelector('.case-detail-contractor')
    if (!panel) return
    panel.classList.remove('is-editing')
    panel.classList.toggle('has-contractor', Boolean(selectedRegistryCase.value?.contractorId))
  })
}

const filteredRepairCases = computed(() => repairRegistry.value)
const visibleRepairCases = computed(() => repairRegistry.value)
const paginatedRepairCases = visibleRepairCases
const caseTotalPages = computed(() => Math.max(1, caseTotalPagesFromApi.value))
const expandedCaseItem = computed(() => visibleRepairCases.value.find((item) => item.id === expandedCaseId.value) || null)

function syncCaseRowMeta() {
  nextTick(() => document.querySelectorAll('.case-row').forEach((row, index) => {
    const item = paginatedRepairCases.value[index]
    if (!item) return
    row.dataset.recordId = item.id
    const vehicleCell = row.querySelector('.cell:first-of-type')
    if (vehicleCell) vehicleCell.dataset.registration = item.registrationNumber || 'Госномер не указан'
  }))
}

const caseReceivedPartsCount = computed(() => caseParts.value.filter((part) => part.received).length)
const casePartsCost = computed(() => caseParts.value.reduce((sum, part) => sum + (Number(part.quantity) || 0) * (Number(part.price) || 0), 0))
const caseWorksCost = computed(() => (workOrder.value?.lines || []).reduce((sum, line) => sum + (Number(line.quantity) || 0) * (Number(line.price) || 0), 0))
const caseTotalCost = computed(() => casePartsCost.value + caseWorksCost.value)
const caseReadiness = computed(() => {
  const item = selectedRegistryCase.value
  if (!item) return { icon: '⚪', label: 'Нет данных', tone: 'neutral' }
  if (item.status === 'WAITING_PARTS') return { icon: '🟡', label: 'Ждём детали', tone: 'warning' }
  if (item.status === 'PARTS_RECEIVED') return { icon: '🟢', label: 'Можно записывать на ремонт', tone: 'success' }
  if (item.status === 'SCHEDULED') return { icon: '🔵', label: 'Записан на ремонт', tone: 'info' }
  return { icon: '⚪', label: statusLabel(item.status), tone: 'neutral' }
})
const caseRemainingTasks = computed(() => {
  const item = selectedRegistryCase.value
  if (!item) return []
  const tasks = []
  if (item.status === 'CREATED' && !caseParts.value.length) tasks.push('Добавьте хотя бы одну деталь и нажмите «Заказать детали».')
  if (item.status === 'WAITING_PARTS' && caseReceivedPartsCount.value < caseParts.value.length) tasks.push(`Не получены ${caseParts.value.length - caseReceivedPartsCount.value} детали.`)
  return tasks
})

function changeCarPage(page) {
  const totalPages = carTotalPages.value
  carPage.value = Math.max(0, Math.min(page, Math.max(0, totalPages - 1)))
  loadCars()
}

async function loadDirectories() {
  try {
    const [insurerItems, supplierItems, shiftItems, workItems, counterpartyItems, vehicleItems, contractorItems, makeItems] = await Promise.all([
      requestJson('/api/v1/directories/insurers'),
      requestJson('/api/v1/directories/suppliers'),
      requestJson('/api/v1/directories/shifts'),
      requestJson('/api/v1/directories/works'),
      requestJson('/api/v1/counterparties'),
      requestJson('/api/v1/directories/vehicles'),
      requestJson('/api/v1/contractors'),
      requestJson('/api/v1/directories/vehicle-catalog/makes'),
    ])
    insurers.value = insurerItems
    suppliers.value = supplierItems
    shifts.value = shiftItems
    workCatalog.value = workItems
    counterparties.value = counterpartyItems
    vehicleAliases.value = vehicleItems
    contractors.value = contractorItems.map((item) => ({ ...item, code: item.phone || item.code }))
    vehicleMakes.value = makeItems
  } catch (error) {
    showToast(`Справочники не загружены: ${error.message}`)
  }
}

async function loadWorkCatalog() {
  if (workCatalog.value.length) return
  try { workCatalog.value = await requestJson('/api/v1/directories/works') } catch (error) { showToast(`Работы не загружены: ${error.message}`) }
}

async function loadContractors() {
  if (contractors.value.length) return
  contractorsBusy.value = true
  contractorsError.value = ''
  try {
    const items = await requestJson('/api/v1/contractors')
    contractors.value = items.map((item) => ({ ...item, code: item.phone || item.code }))
  } catch (error) { contractorsError.value = error.message; showToast(`Исполнители не загружены: ${error.message}`) }
  finally { contractorsBusy.value = false }
}

async function loadInsurers() {
  if (insurers.value.length) return
  insurersBusy.value = true
  insurersError.value = ''
  try { insurers.value = await requestJson('/api/v1/directories/insurers') } catch (error) { insurersError.value = error.message; showToast(`Страховые компании не загружены: ${error.message}`) }
  finally { insurersBusy.value = false }
}

async function loadSuppliers() {
  if (suppliers.value.length) return
  try { suppliers.value = await requestJson('/api/v1/directories/suppliers') } catch (error) { showToast(`Поставщики не загружены: ${error.message}`) }
}

async function loadShifts() {
  if (shifts.value.length) return
  try { shifts.value = await requestJson('/api/v1/directories/shifts') } catch (error) { showToast(`Смены не загружены: ${error.message}`) }
}

async function loadVehicleMakes() {
  if (vehicleMakes.value.length) return
  try { vehicleMakes.value = await requestJson('/api/v1/directories/vehicle-catalog/makes') } catch (error) { showToast(`Марки автомобилей не загружены: ${error.message}`) }
}

async function loadVehicleModels() {
  await loadVehicleMakes()
  const make = vehicleMakes.value.find((item) => item.name === carForm.value.vehicleMake)
  vehicleMakeSelected.value = Boolean(make)
  vehicleModelSelected.value = false
  vehicleModels.value = make ? await requestJson(`/api/v1/directories/vehicle-catalog/models?makeId=${make.id}`).catch(() => []) : []
}

function downloadWorkOrderCsv() {
  const header = ['ID', 'Заказ-наряд', 'Статус', 'Дата', 'Заказчик', 'Автомобиль', 'Госномер', 'Итого', 'Счёт', 'Акт']
  const rows = workOrderRegistry.value.map((item) => [item.id, item.orderNumber, item.status, item.documentDate, item.customer, item.vehicleName, item.registrationNumber, item.total, item.invoiceNumber, item.actNumber])
  const csv = [header, ...rows].map((row) => row.map((value) => `"${String(value ?? '').replaceAll('"', '""')}"`).join(';')).join('\n')
  const link = document.createElement('a'); link.href = URL.createObjectURL(new Blob([`\ufeff${csv}`], { type: 'text/csv;charset=utf-8' })); link.download = 'реестр-заказ-нарядов.csv'; link.click(); URL.revokeObjectURL(link.href)
}

function applyVehicleAlias(id) {
  const item = vehicleAliases.value.find((entry) => String(entry.id) === String(id))
  if (!item) return
  carForm.value.vehicleName = item.sourceName
  carForm.value.vehicleNameLatin = item.normalizedLatinName
}

function openContractorForm(item = null) {
  editingContractor.value = item
  contractorForm.value = { code: item?.code || '', shortName: item?.shortName || item?.fullName || '', fullName: item?.fullName || item?.shortName || '', phone: item?.phone || '', signerName: item?.signerName || '', inn: item?.inn || '', ogrnip: item?.ogrnip || '', address: item?.address || '', bankName: item?.bankName || '', bankInn: item?.bankInn || '', bankKpp: item?.bankKpp || '', bik: item?.bik || '', correspondentAccount: item?.correspondentAccount || '', settlementAccount: item?.settlementAccount || '' }
  contractorVisible.value = true
}

async function saveContractor() {
  try { await requestJson(editingContractor.value ? `/api/v1/contractors/${editingContractor.value.id}` : '/api/v1/contractors', { method: editingContractor.value ? 'PUT' : 'POST', body: JSON.stringify({ ...contractorForm.value, phone: contractorForm.value.phone.trim() }) }); await loadDirectories(); contractorVisible.value = false; showToast('Исполнитель сохранён') } catch (error) { showToast(error.message) }
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
  workOrder.value = { id: null, carId: null, status: 'DRAFT', documentDate: displayTodayIso(), customer: '', orderNumber: '', invoiceNumber: '', actNumber: '', lines: [], partLines: [] }
  workOrderError.value = ''; workOrderVisible.value = true
  generatedDocuments.value = []
}

function openCarForm() {
  loadVehicleMakes()
  modal.value = null
  editingCar.value = null
  carForm.value = emptyCarForm()
  vehicleMakeSelected.value = false
  vehicleModelSelected.value = false
  carPhotos.value = []
  photosCar.value = null
  carFormVisible.value = true
}

async function openCarView(car) {
  viewCar.value = cars.value.find((item) => item.id === car.id) || car
  carViewTab.value = 'main'
  carViewVisible.value = true
  viewCarCasesBusy.value = true
  try { viewCarCases.value = await requestJson(`/api/v1/cars/${car.id}/repair-cases`) } catch (error) { showToast(error.message) } finally { viewCarCasesBusy.value = false }
}

function openViewedCase(item) {
  carViewVisible.value = false
  activeSection.value = 'cases'
  openCaseDetail({ ...item, carId: viewCar.value.id })
}

async function openRepairCasePhotos(caseItem) {
  photosCar.value = repairCaseCar.value; photosCase.value = caseItem; photosBusy.value = true
  // В карточке обращения фотографии показываются inline, без второго overlay.
  photosVisible.value = !caseDetailVisible.value
  try { carPhotos.value = await requestJson(`/api/v1/cars/${repairCaseCar.value.id}/repair-cases/${caseItem.id}/photos`) } catch (error) { showToast(error.message) } finally { photosBusy.value = false }
}

async function openRepairCases(car) {
  activeSection.value = 'cases'
  caseSearch.value = car?.vin || ''
  repairCasesVisible.value = false
}

function openRepairMenu() { repairMenuVisible.value = true }
function startPlaceholderRepair() {
  repairMenuVisible.value = false; selectedCreationType.value = 'REPAIR'
  repairVinSearch.value = ''; repairVehicleCandidates.value = []; repairVehicleSearchError.value = ''
  insuranceVehiclePickerVisible.value = true
}
function startInsuranceCaseFlow() {
  repairMenuVisible.value = false; selectedCreationType.value = 'INSURANCE'
  repairVinSearch.value = ''; repairVehicleCandidates.value = []; repairVehicleSearchError.value = ''
  insuranceVehiclePickerVisible.value = true
}
async function searchRepairVehicle() {
  const query = repairVinSearch.value.trim()
  repairVehicleCandidates.value = []; repairVehicleSearchError.value = ''
  if (!query) return
  const requestId = ++repairVehicleSearchRequestId
  repairVehicleSearchBusy.value = true
  try {
    const result = await requestJson(`/api/v1/cars/search?q=${encodeURIComponent(query)}`)
    if (requestId !== repairVehicleSearchRequestId) return
    repairVehicleCandidates.value = (Array.isArray(result) ? result : []).map((car) => ({
      ...car,
      number: car.accountingNumber,
      registration: car.registrationNumber || 'Госномер не указан',
      vehicle: [car.vehicleMake, car.vehicleModel].filter(Boolean).join(' ') || car.vehicleName,
      ownerPhone: car.ownerPhone || '',
    }))
  } catch (error) {
    if (requestId === repairVehicleSearchRequestId) repairVehicleSearchError.value = error.message
  } finally {
    if (requestId === repairVehicleSearchRequestId) repairVehicleSearchBusy.value = false
  }
}
async function chooseRepairVehicle(car) {
  if (selectedCreationType.value === 'INSURANCE') await loadInsurers()
  insuranceVehiclePickerVisible.value = false
  repairCaseCar.value = car
  newInsuranceCaseForm.value = { caseNumber: '', insurerId: '' }
  repairCasePhotos.value = []
  insuranceCaseCreateVisible.value = true
  nextTick(() => document.querySelector('.insurance-case-create-modal')?.setAttribute('novalidate', ''))
}

async function saveNewInsuranceCase() {
  if (!newInsuranceCaseForm.value.caseNumber.trim() || (selectedCreationType.value === 'INSURANCE' && !newInsuranceCaseForm.value.insurerId) || (selectedCreationType.value === 'INSURANCE' && !repairCasePhotos.value.length)) { showToast(selectedCreationType.value === 'INSURANCE' ? 'Заполните номер дела, выберите страховую и добавьте фото' : 'Заполните номер ремонта'); return }
  try { const saved = await requestJson(`/api/v1/cars/${repairCaseCar.value.id}/repair-cases`, { method: 'POST', body: JSON.stringify({ caseNumber: newInsuranceCaseForm.value.caseNumber, repairType: selectedCreationType.value, insurerId: selectedCreationType.value === 'INSURANCE' ? Number(newInsuranceCaseForm.value.insurerId) : null, contractorId: newInsuranceCaseForm.value.contractorId ? Number(newInsuranceCaseForm.value.contractorId) : null, shiftId: null, insuredPerson: '' }) }); for (const photo of repairCasePhotos.value) await requestJson(`/api/v1/cars/${repairCaseCar.value.id}/repair-cases/${saved.id}/photos`, { method: 'POST', body: JSON.stringify(photo) }); insuranceCaseCreateVisible.value = false; await loadRepairRegistry(); showToast(selectedCreationType.value === 'INSURANCE' ? 'Страховой случай создан' : 'Ремонт создан') } catch (error) { showToast(error.message) }
}

function startRepairCase(caseItem = null) {
  loadInsurers(); loadContractors(); loadShifts()
  editingRepairCase.value = caseItem
  repairCasePhotos.value = []
  repairCaseForm.value = caseItem ? { caseNumber: caseItem.caseNumber, status: caseItem.status, insuredPerson: caseItem.insuredPerson || '', claimNumber: caseItem.claimNumber || '', insurerId: caseItem.insurerId ? String(caseItem.insurerId) : '', contractorId: caseItem.contractorId ? String(caseItem.contractorId) : '', shiftId: caseItem.shiftId ? String(caseItem.shiftId) : '', acceptedAt: caseItem.acceptedAt || '' } : { caseNumber: '', status: 'CREATED', insuredPerson: '', claimNumber: '', insurerId: '', contractorId: '', shiftId: '', acceptedAt: '' }
}

async function saveRepairCase() {
  if (!editingRepairCase.value && !repairCasePhotos.value.length) { showToast('Добавьте хотя бы одно фото автомобиля'); return }
  try { const path = editingRepairCase.value ? `/api/v1/cars/${repairCaseCar.value.id}/repair-cases/${editingRepairCase.value.id}` : `/api/v1/cars/${repairCaseCar.value.id}/repair-cases`; const saved = await requestJson(path, { method: editingRepairCase.value ? 'PUT' : 'POST', body: JSON.stringify({ caseNumber: repairCaseForm.value.caseNumber, insuredPerson: repairCaseForm.value.insuredPerson, claimNumber: repairCaseForm.value.claimNumber, insurerId: repairCaseForm.value.insurerId ? Number(repairCaseForm.value.insurerId) : null, contractorId: repairCaseForm.value.contractorId ? Number(repairCaseForm.value.contractorId) : null, shiftId: repairCaseForm.value.shiftId ? Number(repairCaseForm.value.shiftId) : null, acceptedAt: repairCaseForm.value.acceptedAt || null }) }); if (!editingRepairCase.value) { for (const photo of repairCasePhotos.value) await requestJson(`/api/v1/cars/${repairCaseCar.value.id}/repair-cases/${saved.id}/photos`, { method: 'POST', body: JSON.stringify(photo) }) } repairCases.value = await requestJson(`/api/v1/cars/${repairCaseCar.value.id}/repair-cases`); await loadRepairRegistry(); editingRepairCase.value = null; repairCasePhotos.value = []; showToast('Страховой случай создан') } catch (error) { showToast(error.message) }
}

function readRepairCasePhotos(event) { const files = Array.from(event.target.files || []).slice(0, 20); Promise.all(files.map((file) => new Promise((resolve, reject) => { if (!file.type.startsWith('image/') || file.size > 8 * 1024 * 1024) { reject(new Error('Фото должно быть изображением до 8 МБ')); return } const reader = new FileReader(); reader.onload = () => resolve({ fileName: file.name, mimeType: file.type, dataUrl: reader.result }); reader.onerror = reject; reader.readAsDataURL(file) }))).then((photos) => { repairCasePhotos.value = photos }).catch((error) => showToast(error.message)); event.target.value = '' }

async function deleteRepairCase(item) {
  if (!window.confirm(`Удалить страховой случай №${item.caseNumber}?`)) return
  try { await requestJson(`/api/v1/cars/${repairCaseCar.value.id}/repair-cases/${item.id}`, { method: 'DELETE' }); repairCases.value = repairCases.value.filter((value) => value.id !== item.id); showToast('Страховой случай удалён') } catch (error) { showToast(error.message) }
}

function readCarPhotos(event) {
  const files = Array.from(event.target.files || []).slice(0, 20 - carPhotos.value.length)
  files.forEach((file) => {
    if (!file.type.startsWith('image/') || file.size > 8 * 1024 * 1024) { showToast('Фото должно быть изображением до 8 МБ'); return }
    const reader = new FileReader()
    reader.onload = async () => {
      const photo = { fileName: file.name, mimeType: file.type, dataUrl: reader.result, pending: true }
      carPhotos.value.push(photo)
      if ((photosVisible.value || caseDetailVisible.value) && photosCar.value?.id) {
        const path = photosCase.value ? `/api/v1/cars/${photosCar.value.id}/repair-cases/${photosCase.value.id}/photos` : `/api/v1/cars/${photosCar.value.id}/photos`
        try { const saved = await requestJson(path, { method: 'POST', body: JSON.stringify(photo) }); Object.assign(photo, saved); delete photo.pending; showToast('Фото сохранено в документах') } catch (error) { carPhotos.value = carPhotos.value.filter((item) => item !== photo); showToast(error.message) }
      }
    }
    reader.readAsDataURL(file)
  })
  event.target.value = ''
}

async function deleteCarPhoto(photo) {
  if (photo.pending) { carPhotos.value = carPhotos.value.filter((item) => item !== photo); return }
  const path = photosCase.value ? `/api/v1/cars/${photosCar.value.id}/repair-cases/${photosCase.value.id}/photos/${photo.id}` : `/api/v1/cars/${photosCar.value.id}/photos/${photo.id}`
  try { await requestJson(path, { method: 'DELETE' }); carPhotos.value = carPhotos.value.filter((item) => item.id !== photo.id); showToast('Фото удалено') } catch (error) { showToast(error.message) }
}

function openDirectories() {
  loadDirectories()
  modal.value = null
  directoryForm.value = { name: '', code: '', categoryName: '', defaultUnit: 'н/ч', inn: '', address: '', phone: '', note: '' }
  directoriesVisible.value = true
  settingsDirectoryTab.value = 'insurers'
  settingsEditor.value = null
  nextTick(() => {
    const fields = document.querySelectorAll('.settings-overlay-v2 .settings-directory-grid .settings-card:nth-child(4) .settings-add-row input')
    if (fields[0]) fields[0].placeholder = 'Номер телефона'
    if (fields[1]) fields[1].placeholder = 'ФИО мастера'
  })
}

function startSettingsCreate(tab = settingsDirectoryTab.value) {
  settingsDirectoryTab.value = tab
  settingsEditor.value = { mode: 'create', type: tab }
  settingsEditorForm.value = { name: '', legalDetails: '', normHours: 1, categoryName: 'Кузовные работы', fullName: '', phone: '', inn: '', address: '', note: '', makeId: vehicleMakes.value[0]?.id ? String(vehicleMakes.value[0].id) : '', makeName: '', modelName: '' }
}

function startSettingsEdit(type, item) {
  settingsDirectoryTab.value = type
  settingsEditor.value = { mode: 'edit', type, id: item.id }
  settingsEditorForm.value = { name: item.name || '', legalDetails: item.legalDetails || '', normHours: item.normHours || 1, categoryName: item.categoryName || 'Кузовные работы', fullName: item.fullName || item.shortName || '', phone: item.phone || '', inn: item.inn || '', address: item.address || '', note: item.note || '', makeId: item.makeId ? String(item.makeId) : '', makeName: item.makeName || '', modelName: item.modelName || '' }
}

async function saveSettingsEditor() {
  const type = settingsEditor.value?.type
  const editing = settingsEditor.value?.mode === 'edit'
  const form = settingsEditorForm.value
  try {
    if (type === 'insurers' || type === 'suppliers') {
      if (!form.name.trim()) return showToast('Введите название')
      await requestJson(`/api/v1/directories/${type}${editing ? `/${settingsEditor.value.id}` : ''}`, { method: editing ? 'PUT' : 'POST', body: JSON.stringify({ name: form.name.trim(), legalDetails: type === 'insurers' ? form.legalDetails.trim() : '' }) })
    } else if (type === 'works') {
      if (!form.name.trim()) return showToast('Введите название работы')
      await requestJson(`/api/v1/directories/works${editing ? `/${settingsEditor.value.id}` : ''}`, { method: editing ? 'PUT' : 'POST', body: JSON.stringify({ code: editing ? (workCatalog.value.find((item) => item.id === settingsEditor.value.id)?.code || `WORK-${Date.now()}`) : `WORK-${Date.now()}`, name: form.name.trim(), categoryName: form.categoryName.trim() || 'Кузовные работы', defaultUnit: 'н/ч', normHours: Number(form.normHours) || 0 }) })
    } else if (type === 'contractors') {
      if (!form.fullName.trim() || !form.phone.trim()) return showToast('Введите ФИО и номер телефона')
      const current = contractors.value.find((item) => item.id === settingsEditor.value.id)
      await requestJson(`/api/v1/contractors${editing ? `/${settingsEditor.value.id}` : ''}`, { method: editing ? 'PUT' : 'POST', body: JSON.stringify({ code: editing ? current?.code || '' : '', shortName: form.fullName.trim(), fullName: form.fullName.trim(), phone: form.phone.trim() }) })
    } else if (type === 'counterparties') {
      if (!form.name.trim()) return showToast('Введите наименование или ФИО')
      await requestJson(`/api/v1/counterparties${editing ? `/${settingsEditor.value.id}` : ''}`, { method: editing ? 'PUT' : 'POST', body: JSON.stringify({ name: form.name.trim(), inn: form.inn.trim(), phone: form.phone.trim(), address: form.address.trim(), note: form.note.trim() }) })
    } else if (type === 'vehicles') {
      if ((!form.makeId && !form.makeName.trim()) || !form.modelName.trim()) return showToast('Выберите или создайте марку и укажите модель')
      let makeId = form.makeId
      if (makeId === 'new') makeId = (await requestJson('/api/v1/directories/vehicle-catalog/makes', { method: 'POST', body: JSON.stringify({ name: form.makeName.trim() }) })).id
      await requestJson(`/api/v1/directories/vehicle-catalog/models${editing ? `/${settingsEditor.value.id}` : ''}`, { method: editing ? 'PUT' : 'POST', body: JSON.stringify({ makeId: Number(makeId), name: form.modelName.trim() }) })
    }
    await loadDirectories(); settingsEditor.value = null; showToast('Изменения сохранены')
  } catch (error) { showToast(error.message) }
}

async function removeSettingsEntity(type, item) {
  if (!window.confirm(`Удалить «${item.name || item.fullName || item.shortName || item.modelName}»?`)) return
  try {
    const path = type === 'vehicles' ? `/api/v1/directories/vehicle-catalog/models/${item.id}` : type === 'works' ? `/api/v1/directories/works/${item.id}` : type === 'contractors' ? `/api/v1/contractors/${item.id}` : type === 'counterparties' ? `/api/v1/counterparties/${item.id}` : `/api/v1/directories/${type}/${item.id}`
    await requestJson(path, { method: 'DELETE' }); await loadDirectories(); showToast('Запись удалена')
  } catch (error) { showToast(error.message) }
}

async function addSettingsItem(type, name) {
  if (!name.trim()) return
  try { await requestJson(`/api/v1/directories/${type}`, { method: 'POST', body: JSON.stringify({ name: name.trim(), legalDetails: type === 'insurers' ? directoryForm.value.note : '' }) }); await loadDirectories(); if (type === 'insurers') { settingsNewInsurer.value = ''; directoryForm.value.note = '' } else settingsNewSupplier.value = ''; showToast('Элемент справочника добавлен') } catch (error) { showToast(error.message) }
}

async function removeSettingsItem(type, item) {
  if (!window.confirm(`Удалить «${item.name}»?`)) return
  try { await requestJson(`/api/v1/directories/${type}/${item.id}`, { method: 'DELETE' }); await loadDirectories(); showToast('Элемент удалён') } catch (error) { showToast(error.message) }
}

async function saveSettingsCounterparty() {
  if (!settingsNewCounterparty.value.name.trim()) return
  try { await requestJson('/api/v1/counterparties', { method: 'POST', body: JSON.stringify(settingsNewCounterparty.value) }); await loadDirectories(); settingsNewCounterparty.value = { name: '', inn: '', phone: '', address: '', note: '' }; showToast('Контрагент сохранён') } catch (error) { showToast(error.message) }
}

async function saveSettingsWork() {
  if (!settingsNewWork.value.name.trim()) return
  try { await requestJson('/api/v1/directories/works', { method: 'POST', body: JSON.stringify({ code: `WORK-${Date.now()}`, name: settingsNewWork.value.name.trim(), categoryName: 'Кузовные работы', defaultUnit: 'н/ч', normHours: Number(settingsNewWork.value.normHours) || 1 }) }); await loadDirectories(); settingsNewWork.value = { name: '', normHours: 1 }; showToast('Работа добавлена') } catch (error) { showToast(error.message) }
}

async function saveSettingsMaster() {
  const fullName = settingsNewMaster.value.shortName.trim()
  const phone = settingsNewMaster.value.code.trim()
  if (!fullName || !phone) { showToast('Укажите ФИО и номер телефона мастера'); return }
  try { await requestJson('/api/v1/contractors', { method: 'POST', body: JSON.stringify({ shortName: fullName, fullName, phone }) }); await loadDirectories(); settingsNewMaster.value = { code: '', shortName: '' }; showToast('Мастер добавлен') } catch (error) { showToast(error.message) }
}

async function removeSettingsMaster(item) {
  if (!window.confirm(`Удалить «${item.shortName}»?`)) return
  try { await requestJson(`/api/v1/contractors/${item.id}`, { method: 'DELETE' }); await loadDirectories(); showToast('Мастер удалён') } catch (error) { showToast(error.message) }
}

async function removeSettingsWork(item) {
  if (!window.confirm(`Удалить «${item.name}»?`)) return
  try { await requestJson(`/api/v1/directories/works/${item.id}`, { method: 'DELETE' }); await loadDirectories(); showToast('Работа удалена') } catch (error) { showToast(error.message) }
}

async function removeSettingsCounterparty(item) {
  if (!window.confirm(`Удалить «${item.name}»?`)) return
  try { await requestJson(`/api/v1/counterparties/${item.id}`, { method: 'DELETE' }); await loadDirectories(); showToast('Контрагент удалён') } catch (error) { showToast(error.message) }
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
  photosCar.value = source
  carHistory.value = await requestJson(`/api/v1/cars/${source.id}/history`).catch(() => [])
  carPhotos.value = []
  carForm.value = {
    vehicleMake: source.vehicleMake || '',
    vehicleModel: source.vehicleModel || source.vehicleName || '',
    registrationNumber: source.registrationNumber || '',
    vin: source.vin || '',
    ownerName: source.ownerName || '',
    ownerPhone: source.ownerPhone || '',
    comment: source.comment || '',
    documentFolderUrl: source.documentFolderUrl || '',
  }
  vehicleMakeSelected.value = Boolean(carForm.value.vehicleMake)
  vehicleModelSelected.value = Boolean(carForm.value.vehicleModel)
  await loadVehicleModels()
  carFormVisible.value = true
}

function openPartForm(car, caseItem = caseDetailVisible.value ? selectedRegistryCase.value : null) {
  loadSuppliers()
  if (caseItem) caseDetailVisible.value = false
  modal.value = null
  selectedCar.value = car
  casePartContext.value = caseItem
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

function applyCatalogLine(line) {
  const item = workCatalog.value.find((entry) => String(entry.id) === String(line.catalogId))
  if (!item) return
  line.categoryName = item.categoryName
  line.name = item.name
  line.unit = item.defaultUnit
}

function catalogIdForWorkLine(line) {
  if (line.catalogId) return String(line.catalogId)
  const item = workCatalog.value.find((entry) => String(entry.name).trim().toLowerCase() === String(line.name || '').trim().toLowerCase())
  return item ? String(item.id) : ''
}

function attachWorkCatalogSuggestions() {
  nextTick(() => {
    let datalist = document.querySelector('#case-work-catalog-options')
    if (!datalist) {
      datalist = document.createElement('datalist')
      datalist.id = 'case-work-catalog-options'
      document.body.appendChild(datalist)
    }
    datalist.innerHTML = workCatalog.value.map((item) => `<option value="${String(item.name).replaceAll('&', '&amp;').replaceAll('"', '&quot;')}">${item.normHours || 0} н/ч</option>`).join('')
    document.querySelectorAll('.case-work-line input').forEach((input) => { input.setAttribute('list', 'case-work-catalog-options') })
  })
}

async function openWorkOrder(car, caseItem = null) {
  if (!caseItem && caseDetailVisible.value && selectedRegistryCase.value) caseItem = selectedRegistryCase.value
  if (caseItem) { caseDetailVisible.value = true; caseDetailTab.value = 'works' }
  await loadWorkCatalog()
  modal.value = null
  selectedWorkOrderCar.value = car
  selectedWorkOrderCase.value = caseItem
  workOrderDocumentType.value = 'order'
  workOrderBusy.value = true
  workOrderError.value = ''
  try {
    workOrder.value = await requestJson(caseItem ? `/api/v1/cars/${car.id}/repair-cases/${caseItem.id}/work-order` : `/api/v1/cars/${car.id}/work-order`)
    generatedDocuments.value = await requestJson(`/api/v1/work-orders/${workOrder.value.id}/documents`).catch(() => [])
    workOrder.value.lines = workOrder.value.lines.map((line) => ({ ...line, catalogId: catalogIdForWorkLine(line) }))
    attachWorkCatalogSuggestions()
    workOrder.value.partLines = (workOrder.value.partLines || []).map((line) => ({ ...line }))
  } catch (error) {
    workOrderError.value = error.message
  } finally {
    workOrderBusy.value = false
  }
  workOrderVisible.value = !caseItem
}

async function addWorkOrderLine() {
  await loadWorkCatalog()
  workCatalogPickerId.value = ''
  workCatalogPickerVisible.value = true
}

async function confirmAddWorkOrderLine() {
  const item = workCatalog.value.find((entry) => String(entry.id) === String(workCatalogPickerId.value))
  if (!item || !workOrder.value) return
  workOrder.value.lines.push({ catalogId: String(item.id), categoryName: item.categoryName || '', name: item.name, unit: item.defaultUnit || 'н/ч', quantity: 1, price: 0, contractorId: '', comment: '', sortOrder: workOrder.value.lines.length })
  workCatalogPickerVisible.value = false
  await saveWorkOrder()
}

async function removeWorkOrderLine(index) {
  workOrder.value.lines.splice(index, 1)
  await saveWorkOrder()
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

async function saveWorkOrder(silent = false) {
  if (!selectedWorkOrderCar.value || !workOrder.value) return false
  try {
    const standalone = !selectedWorkOrderCar.value.id
    const casePath = selectedWorkOrderCase.value ? `/api/v1/cars/${selectedWorkOrderCar.value.id}/repair-cases/${selectedWorkOrderCase.value.id}/work-order` : `/api/v1/cars/${selectedWorkOrderCar.value.id}/work-order`
    const result = await requestJson(standalone ? '/api/v1/work-orders/standalone' : casePath, {
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
          quantity: Number(line.quantity), price: Number(line.price), contractorId: line.contractorId ? Number(line.contractorId) : null, comment: line.comment || '', sortOrder: index,
        })),
        partLines: workOrder.value.partLines.map((line, index) => ({
          partId: line.partId, name: line.name, article: line.article || '',
          quantity: Number(line.quantity), price: Number(line.price), sortOrder: index,
        })),
      }),
    })
    workOrder.value = { ...result, lines: (result.lines || []).map((line) => ({ ...line, catalogId: catalogIdForWorkLine(line) })), partLines: result.partLines || [] }
    if (!silent) showToast('Данные сохранены')
    return true
  } catch (error) {
    showToast(error.message)
    return false
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

async function saveCar() {
  try {
    const path = editingCar.value ? `/api/v1/cars/${editingCar.value.id}` : '/api/v1/cars'
    const payload = {
      ...carForm.value,
    }
    await requestJson(path, {
      method: editingCar.value ? 'PUT' : 'POST',
      body: JSON.stringify(payload),
    })
    carFormVisible.value = false
    await loadCars()
    activeFilter.value = 'all'
    showToast(editingCar.value ? 'Автомобиль сохранён' : 'Автомобиль добавлен. Теперь выберите его в реестре и создайте страховой случай.')
    editingCar.value = null
  } catch (error) {
    showToast(error.message)
  }
}

async function updateCarInline(car, field, value) {
  try {
    const payload = {
      vehicleMake: car.vehicleMake || '', vehicleModel: car.vehicleModel || car.vehicleName || '', registrationNumber: car.registrationNumber || '', vin: car.vin || '', ownerName: car.ownerName || '', ownerPhone: car.ownerPhone || '', comment: car.comment === '—' ? '' : car.comment || '', documentFolderUrl: car.documentFolderUrl || '',
    }
    if (field === 'shiftId') payload.shiftId = value ? Number(value) : null; else payload[field] = value || null
    await requestJson(`/api/v1/cars/${car.id}`, { method: 'PUT', body: JSON.stringify(payload) })
    await loadCars()
    showToast('Изменение сохранено')
  } catch (error) { showToast(error.message) }
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
    const basePath = casePartContext.value ? `/api/v1/cars/${selectedCar.value.id}/repair-cases/${casePartContext.value.id}/parts` : `/api/v1/cars/${selectedCar.value.id}/parts`
    const path = editingPart.value ? `${basePath}/${editingPart.value.id}` : basePath
    const savedPart = await requestJson(path, {
      method: editingPart.value ? 'PUT' : 'POST',
      body: JSON.stringify({
        ...partForm.value,
        supplierId: partForm.value.supplierId ? Number(partForm.value.supplierId) : null,
        sortOrder: Number(partForm.value.sortOrder || 0),
      }),
    })
    if (casePartContext.value) {
      const orderPath = `/api/v1/cars/${selectedCar.value.id}/repair-cases/${casePartContext.value.id}/work-order`
      const order = await requestJson(orderPath)
      const lines = (order.partLines || []).filter((line) => line.partId !== savedPart.id)
      lines.push({ partId: savedPart.id, name: savedPart.name, article: savedPart.article || '', quantity: 1, price: 0, sortOrder: lines.length })
      await requestJson(orderPath, { method: 'PUT', body: JSON.stringify({ documentDate: order.documentDate, customer: order.customer, orderNumber: order.orderNumber, invoiceNumber: order.invoiceNumber, actNumber: order.actNumber, status: order.status, lines: (order.lines || []).map((line, index) => ({ categoryName: line.categoryName || '', name: line.name, unit: line.unit, quantity: Number(line.quantity), price: Number(line.price), sortOrder: index })), partLines: lines }) })
    }
    partFormVisible.value = false
    await loadCars()
    if (casePartContext.value) { caseParts.value = await requestJson(`/api/v1/cars/${selectedCar.value.id}/repair-cases/${casePartContext.value.id}/parts`); selectedRegistryCase.value = repairRegistry.value.find((item) => item.id === casePartContext.value.id) || selectedRegistryCase.value }
    showToast(editingPart.value ? 'Запчасть сохранена' : 'Запчасть добавлена')
    editingPart.value = null
  } catch (error) {
    showToast(error.message)
  }
}

async function deletePart(car, part) {
  if (!window.confirm(`Удалить запчасть «${part.name}»?`)) return
  try {
    const context = casePartContext.value || (caseDetailVisible.value ? selectedRegistryCase.value : null)
    const path = context ? `/api/v1/cars/${car.id}/repair-cases/${context.id}/parts/${part.id}` : `/api/v1/cars/${car.id}/parts/${part.id}`
    await requestJson(path, { method: 'DELETE' })
    if (context) caseParts.value = caseParts.value.filter((item) => item.id !== part.id)
    if (expandedCaseId.value === context?.id) expandedCaseParts.value = expandedCaseParts.value.filter((item) => item.id !== part.id)
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

async function updatePart(car, part, changes) {
  try {
    await requestJson(`/api/v1/cars/${car.id}/parts/${part.id}`, { method: 'PUT', body: JSON.stringify({ name: part.name, article: part.article || '', supplierId: changes.supplierId === undefined ? part.supplierId : (changes.supplierId ? Number(changes.supplierId) : null), expectedDate: changes.expectedDate === undefined ? part.expectedDate : (changes.expectedDate || null), sortOrder: part.sortOrder || 0 }) })
    await loadCars()
    showToast('Запчасть обновлена')
  } catch (error) { showToast(error.message) }
}

function updatePartSupplier(car, part, supplierId) { updatePart(car, part, { supplierId }) }
function updatePartExpectedDate(car, part, expectedDate) { updatePart(car, part, { expectedDate }) }

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
    await loadRepairRegistry()
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
  if (!message) return
  toast.value = message
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => { toast.value = '' }, 3600)
}

function handleApiError(event) {
  if (event.detail?.status === 401) {
    localStorage.removeItem(tokenKey)
    localStorage.removeItem(userKey)
    token.value = null
    user.value = null
    authError.value = event.detail.message || 'Сессия истекла. Войдите в систему снова.'
  }
  showToast(event.detail?.message || 'Не удалось выполнить запрос.')
}

function handleVehicleFieldChange(event) {
  const input = event.target
  if (!(input instanceof HTMLInputElement)) return
  if (input.getAttribute('list') === 'vehicle-makes') {
    const make = vehicleMakes.value.find((item) => item.name === input.value)
    if (make) {
      input.removeAttribute('list')
      vehicleMakeSelected.value = true
    }
  } else if (input.getAttribute('list') === 'vehicle-models' && input.value.trim()) {
    input.removeAttribute('list')
    vehicleModelSelected.value = true
  }
}

function handleWorkLineChange(event) {
  if (event.target.closest?.('.case-work-line')) saveWorkOrder()
}

function toggleCarDetails(car) {
  const next = new Set(expandedCars.value)
  if (next.has(car.id)) next.delete(car.id); else next.add(car.id)
  expandedCars.value = next
}

onMounted(() => {
  window.addEventListener('efgen-api-error', handleApiError)
  document.addEventListener('change', handleVehicleFieldChange)
  document.addEventListener('change', handleWorkLineChange)
  document.addEventListener('click', (event) => {
    const trigger = event.target.closest?.('.case-detail-contractor-card strong')
    if (!trigger) return
    trigger.closest('.case-detail-contractor')?.classList.toggle('is-editing')
  })
  document.addEventListener('click', (event) => { const row = event.target.closest('.case-row'); if (!row) return; const index = Array.from(document.querySelectorAll('.case-row')).indexOf(row); const item = paginatedRepairCases.value[index]; if (!item) return; if (event.target.closest('.case-row-details-button')) { event.stopPropagation(); toggleCaseRow(item) } else if (event.target.closest('.cell') === row.querySelector('.cell:nth-child(4)')) { event.stopPropagation(); openCaseDetail(item).then(() => { caseDetailTab.value = 'contractor' }) } else openCaseDetail(item) })
  caseActionObserver = new MutationObserver(removeClosedCaseAction)
  caseActionObserver.observe(document.body, { childList: true, subtree: true })
  removeClosedCaseAction()
  if (token.value) {
    loadRepairRegistry()
    loadRepairCaseStatuses()
    loadContractors()
    loadInsurers()
  }
  syncCaseRowMeta()
})

onUnmounted(() => {
  window.removeEventListener('efgen-api-error', handleApiError)
  document.removeEventListener('change', handleVehicleFieldChange)
  document.removeEventListener('change', handleWorkLineChange)
  caseActionObserver?.disconnect()
  window.clearTimeout(toastTimer)
})

watch([search, activeFilter, insurerFilter, shiftFilter, contractorFilter, overduePartsOnly], () => { carPage.value = 0; window.clearTimeout(window.__efgenSearchTimer); window.__efgenSearchTimer = window.setTimeout(loadCars, 250) })
watch(activeSection, (section) => {
  if (section === 'clients') loadCars()
  if (section === 'settings') loadDirectories()
})
watch([caseSearch, caseStatusFilter, caseContractorFilter], () => {
  casePage.value = 0
  expandedCaseId.value = null
  window.clearTimeout(window.__efgenCaseTimer)
  window.__efgenCaseTimer = window.setTimeout(loadRepairRegistry, 250)
})
watch(paginatedRepairCases, syncCaseRowMeta)
watch([caseDetailVisible, caseDetailTab, selectedRegistryCase], syncContractorPanel)
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
    <div v-if="toast" class="toast" role="alert">{{ toast }}</div>
  </section>

  <template v-else>
    <div v-if="directoriesVisible" class="settings-overlay-v3" @click.self="directoriesVisible = false">
      <section class="settings-screen-v3">
        <button class="icon-button" aria-label="Закрыть" @click="directoriesVisible = false">×</button>
        <p class="eyebrow">Настройки программы</p>
        <h2>Настройки</h2>
        <p class="settings-subtitle">Управление справочниками и данными, которые используются в работе сервиса.</p>
        <nav class="settings-tabs-v3">
          <button v-for="tab in [['insurers','Страховые компании'],['suppliers','Поставщики'],['works','Работы'],['contractors','Исполнители'],['counterparties','Контрагенты']]" :key="tab[0]" type="button" :class="{ 'is-active': settingsDirectoryTab === tab[0] }" @click="settingsDirectoryTab = tab[0]; settingsEditor = null">{{ tab[1] }}</button>
        </nav>

        <section v-if="!settingsEditor" class="settings-tab-content-v3">
          <div class="settings-section-head-v3">
            <div>
              <h3>{{ ({ insurers: 'Страховые компании', suppliers: 'Поставщики', works: 'Работы', contractors: 'Исполнители / мастера', counterparties: 'Контрагенты' })[settingsDirectoryTab] }}</h3>
              <p v-if="settingsDirectoryTab === 'insurers'">Название и адрес или реквизиты страховой компании.</p>
              <p v-else-if="settingsDirectoryTab === 'suppliers'">Поставщики, доступные при добавлении запчастей.</p>
              <p v-else-if="settingsDirectoryTab === 'works'">Работы и нормативная трудоёмкость в нормо-часах.</p>
              <p v-else-if="settingsDirectoryTab === 'contractors'">ФИО мастера и номер телефона.</p>
              <p v-else-if="settingsDirectoryTab === 'counterparties'">Организации и физические лица для документов.</p>
              <p v-else>Марки и модели автомобилей, доступные в карточке автомобиля.</p>
            </div>
            <button type="button" class="button button-primary" @click="startSettingsCreate()">＋ Добавить</button>
          </div>

          <div v-if="settingsDirectoryTab === 'insurers'" class="settings-record-list-v3"><article v-for="item in insurers" :key="item.id" class="settings-record-v3"><div><strong>{{ item.name }}</strong><small>{{ item.legalDetails || 'Адрес и реквизиты не указаны' }}</small></div><div><button type="button" class="settings-edit" @click="startSettingsEdit('insurers', item)">Просмотр / редактировать</button><button type="button" class="settings-delete-text" @click="removeSettingsEntity('insurers', item)">Удалить</button></div></article><p v-if="!insurers.length" class="empty-state">Страховые компании пока не добавлены.</p></div>
          <div v-else-if="settingsDirectoryTab === 'suppliers'" class="settings-record-list-v3"><article v-for="item in suppliers" :key="item.id" class="settings-record-v3"><div><strong>{{ item.name }}</strong><small>Поставщик запчастей</small></div><div><button type="button" class="settings-edit" @click="startSettingsEdit('suppliers', item)">Просмотр / редактировать</button><button type="button" class="settings-delete-text" @click="removeSettingsEntity('suppliers', item)">Удалить</button></div></article><p v-if="!suppliers.length" class="empty-state">Поставщики пока не добавлены.</p></div>
          <div v-else-if="settingsDirectoryTab === 'works'" class="settings-record-list-v3"><article v-for="item in workCatalog" :key="item.id" class="settings-record-v3"><div><strong>{{ item.name }}</strong><small>{{ item.categoryName }} · {{ item.normHours || 0 }} н/ч</small></div><div><button type="button" class="settings-edit" @click="startSettingsEdit('works', item)">Просмотр / редактировать</button><button type="button" class="settings-delete-text" @click="removeSettingsEntity('works', item)">Удалить</button></div></article><p v-if="!workCatalog.length" class="empty-state">Работы пока не добавлены.</p></div>
          <div v-else-if="settingsDirectoryTab === 'contractors'" class="settings-record-list-v3"><article v-for="item in contractors" :key="item.id" class="settings-record-v3"><div><strong>{{ item.shortName || item.fullName }}</strong><small>{{ item.phone || item.code || 'Телефон не указан' }}</small></div><div><button type="button" class="settings-edit" @click="startSettingsEdit('contractors', item)">Просмотр / редактировать</button><button type="button" class="settings-delete-text" @click="removeSettingsEntity('contractors', item)">Удалить</button></div></article><p v-if="!contractors.length" class="empty-state">Исполнители пока не добавлены.</p></div>
          <div v-else-if="settingsDirectoryTab === 'counterparties'" class="settings-record-list-v3"><article v-for="item in counterparties" :key="item.id" class="settings-record-v3"><div><strong>{{ item.name }}</strong><small>ИНН: {{ item.inn || 'не указан' }} · {{ item.phone || 'Телефон не указан' }}</small><small>{{ item.address || 'Адрес не указан' }}</small></div><div><button type="button" class="settings-edit" @click="startSettingsEdit('counterparties', item)">Просмотр / редактировать</button><button type="button" class="settings-delete-text" @click="removeSettingsEntity('counterparties', item)">Удалить</button></div></article><p v-if="!counterparties.length" class="empty-state">Контрагенты пока не добавлены.</p></div>
          <div v-else class="settings-record-list-v3"><article v-for="item in settingsVehicleRows" :key="item.id" class="settings-record-v3"><div><strong>{{ item.makeName }} · {{ item.modelName }}</strong><small>Марка и модель автомобиля</small></div><div><button type="button" class="settings-edit" @click="startSettingsEdit('vehicles', item)">Просмотр / редактировать</button><button type="button" class="settings-delete-text" @click="removeSettingsEntity('vehicles', item)">Удалить</button></div></article><p v-if="!settingsVehicleRows.length" class="empty-state">Автомобили пока не добавлены.</p></div>
        </section>

        <form v-else class="settings-editor-v3" @submit.prevent="saveSettingsEditor">
          <div class="settings-section-head-v3"><div><h3>{{ settingsEditor.mode === 'edit' ? 'Редактирование' : 'Создание' }}</h3><p>Заполните данные и сохраните запись.</p></div><button type="button" class="button button-cloud dark-button" @click="settingsEditor = null">Назад к списку</button></div>
          <template v-if="settingsEditor.type === 'insurers'"><label><span>Название страховой компании *</span><input v-model="settingsEditorForm.name" required /></label><label><span>Адрес и реквизиты</span><input v-model="settingsEditorForm.legalDetails" /></label></template>
          <template v-else-if="settingsEditor.type === 'suppliers'"><label><span>Название поставщика *</span><input v-model="settingsEditorForm.name" required /></label></template>
          <template v-else-if="settingsEditor.type === 'works'"><label><span>Название работы *</span><input v-model="settingsEditorForm.name" required /></label><label><span>Нормо-часы *</span><input v-model.number="settingsEditorForm.normHours" type="number" min="0" step="0.01" required /></label><label><span>Категория</span><input v-model="settingsEditorForm.categoryName" /></label></template>
          <template v-else-if="settingsEditor.type === 'contractors'"><label><span>ФИО мастера *</span><input v-model="settingsEditorForm.fullName" required /></label><label><span>Номер телефона *</span><input v-model="settingsEditorForm.phone" type="tel" required /></label></template>
          <template v-else-if="settingsEditor.type === 'counterparties'"><label><span>Наименование или ФИО *</span><input v-model="settingsEditorForm.name" required /></label><label><span>ИНН</span><input v-model="settingsEditorForm.inn" /></label><label><span>Телефон</span><input v-model="settingsEditorForm.phone" type="tel" /></label><label><span>Адрес</span><input v-model="settingsEditorForm.address" /></label><label><span>Комментарий</span><textarea v-model="settingsEditorForm.note" rows="3"></textarea></label></template>
          <template v-else><label><span>Марка *</span><select v-model="settingsEditorForm.makeId" required><option value="" disabled>Выберите марку</option><option value="new">＋ Новая марка</option><option v-for="make in vehicleMakes" :key="make.id" :value="String(make.id)">{{ make.name }}</option></select></label><label v-if="settingsEditorForm.makeId === 'new'"><span>Название новой марки *</span><input v-model="settingsEditorForm.makeName" /></label><label><span>Модель *</span><input v-model="settingsEditorForm.modelName" required placeholder="Например, H3" /></label></template>
          <div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="settingsEditor = null">Отмена</button><button type="submit" class="button button-primary">Сохранить</button></div>
        </form>
      </section>
    </div>
    <div v-if="toast" class="toast" role="alert">{{ toast }}</div>
    <header class="topbar">
      <div class="brand"><span class="brand-mark">B</span><span><strong>Bosh: кузовной ремонт</strong><small>Автомобили и запчасти</small></span></div>
      <div class="topbar-actions">
        <nav class="topbar-nav-group topbar-work-nav" aria-label="Рабочие разделы">
          <button class="button button-cloud" :class="{ 'is-current': activeSection === 'cases' }" type="button" @click="activeSection = 'cases'">Страховые случаи</button>
          <button class="button button-cloud" :class="{ 'is-current': activeSection === 'clients' }" type="button" @click="activeSection = 'clients'">Клиенты сервиса</button>
        </nav>
        <div class="topbar-nav-divider" aria-hidden="true"></div>
        <nav class="topbar-nav-group topbar-system-nav" aria-label="Системные разделы">
          <button class="button button-cloud" type="button" @click="openStub('Настройки')">Настройки</button>
        </nav>
        <button class="button button-primary" @click="openCarForm">＋ Добавить автомобиль</button>
        <button class="user-chip" title="Выйти" @click="logout">{{ user?.name || user?.email || 'Пользователь' }} · Выйти</button>
      </div>
    </header>

    <main class="page-shell">
      <section v-if="activeSection === 'cases'" class="stats-grid" aria-label="Сводка">
        <article class="stat-card stat-card-main"><span class="stat-icon">А</span><div><small>Автомобилей в работе</small><strong>{{ stats.active }}</strong></div></article>
        <article class="stat-card"><span class="stat-icon stat-icon-amber">!</span><div><small>Ждём запчасти</small><strong>{{ stats.waiting }}</strong></div></article>
        <article class="stat-card"><span class="stat-icon stat-icon-green">✓</span><div><small>Все детали поступили</small><strong>{{ stats.ready }}</strong></div></article>
        <article class="stat-card"><span class="stat-icon stat-icon-gray">В</span><div><small>Автомобилей выдано</small><strong>{{ stats.delivered }}</strong></div></article>
      </section>

      <section v-if="activeSection === 'cases'" class="workspace cases-workspace">
        <div class="toolbar"><label class="search-field"><span>⌕</span><input v-model="caseSearch" type="search" placeholder="Поиск по VIN, номеру авто, названию авто, номеру убытка или артикулу детали" /></label><div class="case-status-filters"><button v-for="filter in caseStatusOptions" :key="filter.code" type="button" class="filter" :class="{ 'is-active': caseStatusFilter === filter.code }" @click="caseStatusFilter = filter.code">{{ filter.label }}</button></div><button class="button button-primary" type="button" @click="openRepairMenu">＋ Создать страховой случай</button></div>
        <div class="case-contractor-filter-row"><label>Исполнитель<select v-model="caseContractorFilter" aria-label="Фильтр по исполнителю"><option value="">Все исполнители</option><option v-for="item in contractors" :key="item.id" :value="String(item.id)">{{ item.shortName || item.fullName }}</option></select></label></div>
        <div class="list-head case-list-head" style="grid-template-columns: .55fr 1.35fr 1.2fr 1.2fr 1.05fr 1.15fr 1fr .9fr;"><span>Автомобиль</span><span>Страховой случай</span><span>Страховая</span><span>Исполнитель</span><span>Запись</span><span>Детали</span><span>Статус</span></div>
        <div class="cars-list"><template v-for="item in visibleRepairCases" :key="item.id"><article class="car-row case-row" :class="{ 'is-selected': expandedCaseId === item.id }" style="grid-template-columns: .55fr 1.35fr 1.2fr 1.2fr 1.05fr 1.15fr 1fr .9fr;"><div class="cell"><strong>{{ item.vehicleMake }} {{ item.vehicleModel }}</strong><small>VIN {{ item.vin }}</small><small>{{ item.registrationNumber }} · {{ item.ownerName }} · {{ item.ownerPhone }}</small></div><div class="cell"><strong>№{{ item.caseNumber }}</strong></div><div class="cell"><span class="insurance-pill">{{ insurers.find((insurer) => insurer.id === item.insurerId)?.name || 'Страховая не указана' }}</span></div><div class="cell inline-contractor-cell"><select v-if="editingContractorCaseId === item.id" class="inline-contractor-select" :value="String(item.contractorId || '')" :disabled="savingContractorCaseId === item.id" @click.stop @change.stop="saveInlineCaseContractor(item, $event)"><option value="" disabled>Выберите исполнителя</option><option v-for="contractor in contractors" :key="contractor.id" :value="String(contractor.id)">{{ contractor.shortName || contractor.fullName }}</option></select><button v-else type="button" class="inline-contractor-button" @click.stop="editingContractorCaseId = item.id; loadContractors()">{{ contractors.find((contractor) => contractor.id === item.contractorId)?.shortName || 'Не назначен' }}</button></div><div class="cell inline-appointment-date-cell"><input v-if="item.appointmentDate" type="date" class="inline-appointment-date-input" :value="item.appointmentDate" :disabled="item.status === 'DELIVERED' || item.status === 'CLOSED' || savingAppointmentDateCaseId === item.id" @click.stop @change.stop="saveInlineAppointmentDate(item, $event)" /><span v-else class="inline-appointment-date-hint" title="Чтобы выставить дату, переведите статус случая «Запись на ремонт»" aria-label="Чтобы выставить дату, переведите статус случая «Запись на ремонт»">—</span></div><div class="cell case-parts-progress-cell"><div class="parts-progress-ring" :style="{ '--parts-progress': item.partsTotal ? (item.partsReceived / item.partsTotal) * 100 + '%' : '0%' }"><span>{{ item.partsReceived || 0 }}/{{ item.partsTotal || 0 }}</span></div><div><strong>{{ item.partsReceived || 0 }} из {{ item.partsTotal || 0 }}</strong><small>{{ item.partsTotal ? (item.partsReceived === item.partsTotal ? 'Поступление по графику' : 'Ожидание деталей') : 'Детали не добавлены' }}</small></div></div><div class="cell"><span class="case-status">{{ statusLabel(item.status) }}</span></div><div class="cell muted-cell"><button type="button" class="case-row-details-button" @click.stop="toggleCaseRow(item)">{{ expandedCaseId === item.id ? 'Скрыть детали' : 'Открыть' }}</button></div></article><section v-if="expandedCaseId === item.id" class="case-row-details"><div class="case-row-details-head"><strong>Детали страхового случая №{{ item.caseNumber }}</strong><span>{{ expandedCaseParts.length }} деталей · {{ expandedCaseParts.filter((part) => part.received).length }} получено</span></div><div v-if="expandedCaseParts.length" class="case-parts-table"><div class="case-parts-table-head"><span>Деталь</span><span>Артикул</span><span>Поставщик</span><span>Плановая поставка</span><span>Статус</span><span>Поступила</span></div><div v-for="part in expandedCaseParts" :key="part.id" class="case-parts-table-row"><span><strong>{{ part.name }}</strong><small v-if="part.comment">{{ part.comment }}</small></span><span>{{ part.article || '—' }}</span><span>{{ suppliers.find((supplier) => supplier.id === part.supplierId)?.name || '—' }}</span><span>{{ part.expectedDate || '—' }}</span><span class="case-part-status-cell"><div class="case-part-status-actions"><b :class="part.received ? 'part-received' : 'part-waiting'">{{ part.received ? 'Получена' : 'Ожидается' }}</b><button type="button" class="part-receipt-button" :class="{ 'is-received': part.received }" @click.stop="toggleCasePartReceived(item, part)">{{ part.received ? 'Отменить' : 'Отметить поступление' }}</button></div></span><span class="case-received-date">{{ part.received ? part.receivedAt : '—' }}</span></div></div><p v-else class="empty-state">У этого страхового случая деталей пока нет.</p></section></template><p v-if="!visibleRepairCases.length" class="empty-state">Страховых случаев пока нет. Создайте первый через «＋ Ремонт».</p></div>
        <section v-if="expandedCaseItem" class="case-receipt-panel"><div class="case-receipt-head"><div><strong>Детали страхового случая №{{ expandedCaseItem.caseNumber }}</strong><span>{{ statusLabel(expandedCaseItem.status) }}</span></div><small>Отметьте поступление — дата поступления сохранится автоматически.</small></div><div v-if="expandedCaseParts.length" class="case-receipt-list"><div v-for="part in expandedCaseParts" :key="part.id" class="case-receipt-row"><div><strong>{{ part.name }}</strong><small>Артикул: {{ part.article || '—' }} · Плановая дата: {{ part.expectedDate || '—' }}</small><small v-if="part.received" class="part-received-date">Поступила: {{ part.receivedAt || 'дата не указана' }}</small></div><button type="button" class="part-receipt-button" :class="part.received ? 'is-received' : ''" @click="toggleCasePartReceived(expandedCaseItem, part)">{{ part.received ? 'Получена · отменить' : 'Отметить как полученную' }}</button></div></div><p v-else class="empty-state">Деталей пока нет.</p></section>
      </section>
      <div v-if="activeSection === 'cases' && filteredRepairCases.length" class="pagination-toolbar case-pagination" aria-label="Пагинация страховых случаев"><span>Найдено: {{ caseTotalItems }}</span><button type="button" class="link-button" :disabled="casePage === 0" @click="casePage--; loadRepairRegistry()">← Назад</button><strong>Страница {{ casePage + 1 }} из {{ caseTotalPages }}</strong><button type="button" class="link-button" :disabled="casePage >= caseTotalPages - 1" @click="casePage++; loadRepairRegistry()">Вперёд →</button></div>
      <section v-if="activeSection === 'clients'" class="workspace clients-workspace">
        <div class="toolbar"><label class="search-field"><span>⌕</span><input v-model="search" type="search" placeholder="Поиск по VIN, госномеру, ФИО или телефону" /></label><button class="button button-primary" type="button" @click="openCarForm">＋ Добавить автомобиль</button></div>
        <div class="list-head clients-list-head"><span>ID записи</span><span>Автомобиль</span><span>VIN</span><span>Госномер</span><span>Владелец</span><span>Телефон</span><span>Создан</span></div>
        <div class="cars-list"><article v-for="car in displayedClientCars" :key="car.id" class="car-row client-row"><div class="cell client-id-cell"><strong>{{ car.id }}</strong><button class="row-chevron client-row-chevron" type="button" aria-label="Открыть карточку автомобиля" @click="openCarView(car)">⌄</button></div><div class="cell"><strong>{{ car.vehicle }}</strong></div><div class="cell client-vin">{{ car.vin }}</div><div class="cell">{{ car.registration }}</div><div class="cell">{{ car.ownerName || '—' }}</div><div class="cell">{{ car.ownerPhone || '—' }}</div><div class="cell muted-cell">{{ car.record }}</div></article><p v-if="!displayedClientCars.length" class="empty-state">Клиенты сервиса пока не созданы.</p></div>
        <div v-if="carTotalPages > 1" class="pagination-toolbar" aria-label="Пагинация клиентов"><span>Найдено: {{ carTotalItems }}</span><button type="button" class="link-button" :disabled="carPage === 0" @click="changeCarPage(carPage - 1)">← Назад</button><strong>Страница {{ carPage + 1 }} из {{ carTotalPages }}</strong><button type="button" class="link-button" :disabled="carPage >= carTotalPages - 1" @click="changeCarPage(carPage + 1)">Вперёд →</button></div>
      </section>
      <section v-if="false && activeSection === 'clients'" class="workspace">
        <div class="toolbar">
          <label class="search-field"><span>⌕</span><input v-model="search" type="search" placeholder="Поиск по марке, госномеру, VIN или телефону владельца" /></label>
          <div class="filters" role="group" aria-label="Фильтр автомобилей">
            <button v-for="filter in [['all','Все автомобили'], ['active','В работе'], ['waiting','Ждём детали'], ['ready','Всё поступило'], ['delivered','Выданы']]" :key="filter[0]" class="filter" :class="{ 'is-active': activeFilter === filter[0] }" @click="activeFilter = filter[0]; carPage = 0">{{ filter[1] }}</button>
          </div>
          <div class="extended-filters">
            <select v-model="insurerFilter" aria-label="Фильтр по страховой"><option value="">Все страховые</option><option v-for="item in insurers" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select>
            <select v-model="shiftFilter" aria-label="Фильтр по смене"><option value="">Все смены</option><option v-for="item in shifts" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select>
            <select v-model="contractorFilter" aria-label="Фильтр по исполнителю"><option value="">Все исполнители</option><option v-for="item in contractors" :key="item.id" :value="String(item.id)">{{ item.shortName }}</option></select>
            <label class="overdue-filter"><input v-model="overduePartsOnly" type="checkbox" /> Просроченные детали</label>
          </div>
        </div>
        <div class="list-head"><span>Автомобиль</span><span>Страховая / Создана</span><span>Запчасти</span><span>Комментарий</span><span>Документы</span><span>Создана</span><span>Смена</span><span>Выдача</span></div>
        <div class="cars-list" aria-live="polite">
          <div v-if="carsBusy" class="empty-state">Загружаем реестр автомобилей…</div>
          <div v-else-if="carsError" class="empty-state">{{ carsError }} <button class="link-button" @click="loadCars">Повторить</button></div>
          <article v-for="car in displayedClientCars" :key="car.number" class="car-row" :class="[`is-${car.status}`, { 'is-open': expandedCars.has(car.id) }]">
            <div class="car-summary">
              <div class="cell car-identity"><div class="car-title"><button class="row-chevron" type="button" :aria-expanded="expandedCars.has(car.id)" @click="toggleCarDetails(car)">{{ expandedCars.has(car.id) ? '⌄' : '›' }}</button><b class="car-sequence">{{ car.number }}</b><button class="row-toggle" @click="openCarEdit(car)"><strong>{{ car.vehicle }}</strong><small>{{ car.registration }} · VIN {{ car.vin }}</small><small>{{ car.status === 'delivered' ? 'Выдан' : car.status === 'ready' ? 'Всё поступило' : car.status === 'waiting' ? 'Ожидаются детали' : 'В работе' }}</small></button></div></div>
              <div class="cell"><span class="insurance-pill">{{ car.insurer }}</span><small>Начало: {{ car.start }}</small></div>
              <div class="cell parts-glance"><span class="progress-ring" :style="{ '--progress': `${car.parts.length ? Math.round((car.parts.filter((part) => part.received).length / car.parts.length) * 100) : 0}%` }" :data-label="`${car.parts.filter((part) => part.received).length}/${car.parts.length}`"></span><span><strong>{{ car.parts.length ? `${car.parts.filter((part) => part.received).length} из ${car.parts.length} поступили` : 'Нет деталей' }}</strong><small>{{ car.parts.some((part) => !part.received && part.expectedDate && part.expectedDate < displayTodayIso()) ? 'Есть просроченные детали' : 'Поступление по графику' }}</small></span></div>
              <div class="cell"><input class="comment-input" type="text" :value="car.comment === '—' ? '' : car.comment" placeholder="Комментарий..." @change="updateCarInline(car, 'comment', $event.target.value)" /></div>
              <div class="cell"><button class="link-button" type="button" @click="openRepairCases(car)">Страховые случаи</button><a v-if="car.documentFolderUrl" class="link-button" :href="car.documentFolderUrl" target="_blank" rel="noreferrer">Открыть папку</a></div>
              <div class="cell muted-cell">{{ car.record }}</div><div class="cell"><select class="shift-select" :value="car.shiftId || ''" @change="updateCarInline(car, 'shiftId', $event.target.value)"><option value="">Не назначена</option><option v-for="item in shifts" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select></div>
              <div class="cell"><label class="delivered-check"><input type="checkbox" :checked="car.status === 'delivered'" @change="toggleDelivered(car)" /><span>Выдан</span></label><small v-if="car.deliveredAt">{{ car.deliveredAt }}</small></div>
            </div>
            <div v-if="expandedCars.has(car.id)" class="car-details"><div class="details-panel"><div v-if="car.parts.length" class="details-head"><span>Поступление</span><span>Деталь</span><span>Артикул</span><span>Поставщик</span><span>Дата поступления</span><span></span></div><div v-for="part in car.parts" :key="part.id" class="part-row" :class="{ 'is-received': part.received }"><label class="received-control"><input type="checkbox" :checked="part.received" @change="togglePartReceived(car, part)" /><span>{{ part.received ? 'Поступила' : 'Ожидается' }}</span></label><div><div class="part-name">{{ part.name }}</div><small v-if="part.receivedAt">Фактически: {{ part.receivedAt }}</small></div><span class="article">{{ part.article || '—' }}</span><select :value="part.supplierId || ''" @change="updatePartSupplier(car, part, $event.target.value)"><option value="">Не указан</option><option v-for="item in suppliers" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select><input class="part-date-input" type="date" :value="part.expectedDate || ''" @change="updatePartExpectedDate(car, part, $event.target.value)" /><button class="part-delete" type="button" title="Удалить деталь" @click="deletePart(car, part)">×</button></div><div class="details-actions"><button class="link-button" type="button" @click="openRepairCases(car)">＋ Создать / открыть страховой случай</button><button class="link-button" type="button" @click="openPartForm(car)">＋ Добавить деталь</button><button class="link-button" type="button" @click="openStub('Дефектовка')">Дефектовка</button><button class="link-button" type="button" @click="openWorkOrder(car)">ЗН+Счёт</button><button class="link-button" type="button" @click="toggleAccepted(car)">{{ car.acceptedAt ? 'Отменить приёмку' : 'Принять автомобиль' }}</button></div></div></div>
          </article>
          <div v-if="!carsBusy && !carsError && !visibleCars.length" class="empty-state">По выбранному фильтру автомобили не найдены.</div>
        </div>
        <div v-if="carTotalPages > 1" class="pagination-toolbar" aria-label="Пагинация клиентов">
          <span>Найдено: {{ carTotalItems }}</span>
          <button type="button" class="link-button" :disabled="carPage === 0" @click="changeCarPage(carPage - 1)">← Назад</button>
          <strong>Страница {{ carPage + 1 }} из {{ carTotalPages }}</strong>
          <button type="button" class="link-button" :disabled="carPage >= carTotalPages - 1" @click="changeCarPage(carPage + 1)">Вперёд →</button>
        </div>
      </section>
    </main>
    <div v-if="scheduleRepairModal" class="stub-overlay schedule-repair-overlay" @click.self="scheduleRepairModal = false"><form class="data-modal compact-modal schedule-repair-modal" @submit.prevent="submitScheduleRepair"><button type="button" class="icon-button" aria-label="Закрыть" @click="scheduleRepairModal = false">×</button><p class="eyebrow">Запись на ремонт</p><h2>Выберите исполнителя</h2><p class="modal-subtitle">{{ selectedRegistryCase?.vehicleMake }} {{ selectedRegistryCase?.vehicleModel }} · дело {{ selectedRegistryCase?.caseNumber }}</p><div class="data-form-grid"><label class="form-wide"><span>Исполнитель по страховому случаю *</span><select v-model="caseActionForm.contractorId" required :disabled="contractorsBusy || !contractors.length"><option value="" disabled>Выберите исполнителя</option><option v-for="contractor in contractors" :key="contractor.id" :value="String(contractor.id)">{{ contractor.shortName || contractor.fullName }}</option></select><small v-if="contractorsBusy" class="field-hint">Загружаем исполнителей…</small><small v-else-if="!contractors.length" class="field-hint">Исполнители пока не добавлены.</small></label><label><span>Дата ремонта *</span><input v-model="caseActionForm.appointmentDate" type="date" required /></label><label><span>Время ремонта *</span><input v-model="caseActionForm.appointmentTime" type="time" required /></label><label class="form-wide"><span>Комментарий</span><textarea v-model="caseActionForm.comment" rows="2" placeholder="Комментарий к записи"></textarea></label></div><p class="field-hint">Работы и исполнители по работам добавляются позже в разделе «Работы».</p><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="scheduleRepairModal = false">Отмена</button><button class="button button-primary" type="submit" :disabled="!caseActionForm.contractorId || contractorsBusy">Записать на ремонт</button></div></form></div>

    <footer class="global-footer"><span>Efgen Bosh · рабочий интерфейс</span><span>Данные разделов подключаются поэтапно</span></footer>
    <button v-if="false" type="button" class="standalone-order-button button button-primary" @click="openStandaloneWorkOrder">＋ Новый заказ-наряд</button>
    <button v-if="directoriesVisible" type="button" class="contractors-button button button-cloud" @click="openContractorForm()">Исполнители</button>
    <button v-if="defectVisible && defect.recommendations" type="button" class="defect-transfer-button button button-primary" @click="transferDefectRecommendations">Перенести рекомендации в заказ-наряд</button>
    <div v-if="workOrderVisible && generatedDocuments.length" class="generated-documents-toolbar"><strong>Документы:</strong><span v-for="doc in generatedDocuments.slice(0, 6)" :key="doc.id">{{ doc.documentType }}{{ doc.documentNumber ? ` №${doc.documentNumber}` : '' }}</span></div>
    <button v-if="workOrderVisible && workOrder" type="button" class="bundle-print-button button button-cloud" @click="printDocument('bundle')">Печатный комплект</button>

    <div v-if="carFormVisible && editingCar" class="car-delete-toolbar"><span>Карточка автомобиля №{{ editingCar.accountingNumber }}</span><button type="button" class="link-button" @click="printCarDocument('acceptance')">Акт приёма</button><button type="button" class="link-button" @click="printCarDocument('delivery')">Акт выдачи</button><button type="button" class="link-button danger-link" @click="deleteCar">Удалить автомобиль</button></div>
    <div v-if="carFormVisible && editingCar && carHistory.length" class="car-history-toolbar"><strong>История:</strong><span v-for="event in carHistory.slice(0, 4)" :key="event.id">{{ event.details }}</span></div>
    <div v-if="carFormVisible && vehicleAliases.length" class="vehicle-alias-toolbar"><span>Модель из справочника:</span><select @change="applyVehicleAlias($event.target.value)"><option value="">Выбрать модель</option><option v-for="item in vehicleAliases" :key="item.id" :value="item.id">{{ item.sourceName }}<template v-if="item.normalizedLatinName"> · {{ item.normalizedLatinName }}</template></option></select></div>

    <div v-if="workOrderVisible && workOrder" class="document-toolbar"><span>Номера документов:</span><input v-model="workOrder.orderNumber" placeholder="Заказ-наряд №" /><input v-model="workOrder.invoiceNumber" placeholder="Счёт №" /><input v-model="workOrder.actNumber" placeholder="Акт №" /><span>Печать:</span><button type="button" class="link-button" @click="printDocument('order')">Заказ-наряд</button><button type="button" class="link-button" @click="printDocument('invoice')">Счёт</button><button type="button" class="link-button" @click="printDocument('act')">Акт</button></div>

    <div v-if="defectVisible" class="stub-overlay" @click.self="defectVisible = false"><section class="data-modal defect-modal"><button class="icon-button" aria-label="Закрыть" @click="defectVisible = false">×</button><p class="eyebrow">Осмотр автомобиля</p><h2>Дефектовка</h2><p class="modal-subtitle">{{ selectedDefectCar?.number }} · {{ selectedDefectCar?.vehicle }} · {{ selectedDefectCar?.registration }}</p><div v-if="defectBusy" class="empty-state">Загружаем дефектовку…</div><div v-else-if="defectError" class="empty-state">{{ defectError }}</div><form v-else class="data-form-grid" @submit.prevent="saveDefect"><label><span>Статус</span><select v-model="defect.status"><option value="DRAFT">Черновик</option><option value="CONFIRMED">Подтверждено</option></select></label><label class="form-wide"><span>Повреждения и замечания</span><textarea v-model="defect.findings" rows="5" placeholder="Передний бампер, левая дверь…"></textarea></label><label class="form-wide"><span>Рекомендованные работы и запчасти</span><textarea v-model="defect.recommendations" rows="5" placeholder="Замена бампера, окраска двери…"></textarea></label><label class="form-wide"><span>Фотографии осмотра (до 8)</span><input type="file" accept="image/*" multiple @change="readDefectPhotos" /></label><div v-if="defect.photos.length" class="defect-photo-grid form-wide"><img v-for="(photo, index) in defect.photos" :key="`${photo.slice(0, 24)}-${index}`" :src="photo" alt="Фото повреждения" /></div><div class="modal-actions form-wide"><button type="button" class="button button-cloud dark-button" @click="defectVisible = false">Отмена</button><button class="button button-primary" type="submit">Сохранить дефектовку</button></div></form></section></div>
    <div v-if="contractorVisible" class="stub-overlay" @click.self="contractorVisible = false"><form class="data-modal contractor-modal" @submit.prevent="saveContractor"><button type="button" class="icon-button" aria-label="Закрыть" @click="contractorVisible = false">×</button><p class="eyebrow">Реквизиты</p><h2>{{ editingContractor ? 'Изменить исполнителя' : 'Новый исполнитель' }}</h2><div class="data-form-grid"><label><span>Код</span><input v-model="contractorForm.code" required /></label><label><span>Краткое название</span><input v-model="contractorForm.shortName" required /></label><label class="form-wide"><span>Полное название</span><input v-model="contractorForm.fullName" required /></label><label><span>Подписант</span><input v-model="contractorForm.signerName" /></label><label><span>ИНН</span><input v-model="contractorForm.inn" /></label><label><span>ОГРНИП</span><input v-model="contractorForm.ogrnip" /></label><label class="form-wide"><span>Адрес</span><input v-model="contractorForm.address" /></label><label class="form-wide"><span>Банк</span><input v-model="contractorForm.bankName" /></label><label><span>БИК</span><input v-model="contractorForm.bik" /></label><label><span>Расчётный счёт</span><input v-model="contractorForm.settlementAccount" /></label></div><div class="contractor-list"><div v-for="item in contractors" :key="item.id" class="directory-item"><span>{{ item.shortName }}<small>{{ item.code }} · {{ item.inn || 'ИНН не указан' }}</small></span><button type="button" class="link-button" @click="openContractorForm(item)">Изменить</button><button type="button" class="link-button danger-link" @click="deleteContractor(item)">Удалить</button></div></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="contractorVisible = false">Закрыть</button><button class="button button-primary" type="submit">Сохранить</button></div></form></div>

    <div v-if="carFormVisible" class="stub-overlay" @click.self="carFormVisible = false"><form class="data-modal" @submit.prevent="saveCar"><button type="button" class="icon-button" aria-label="Закрыть" @click="carFormVisible = false">×</button><p class="eyebrow">Карточка автомобиля</p><h2>{{ editingCar ? `Автомобиль №${editingCar.accountingNumber}` : 'Новый автомобиль' }}</h2><div class="data-form-grid"><label><span>Марка *</span><input v-model="carForm.vehicleMake" list="vehicle-makes" required placeholder="Начните вводить марку" @change="loadVehicleModels" /></label><label><span>Модель *</span><input v-model="carForm.vehicleModel" list="vehicle-models" required placeholder="Начните вводить модель" /></label><datalist id="vehicle-makes"><option v-for="item in vehicleMakes" :key="item.id" :value="item.name" /></datalist><datalist id="vehicle-models"><option v-for="item in vehicleModels" :key="item.id" :value="item.name" /></datalist><label><span>Госномер *</span><input v-model="carForm.registrationNumber" required placeholder="А123ВС124" /></label><label><span>VIN *</span><input v-model="carForm.vin" required placeholder="VIN автомобиля" /></label><label><span>ФИО владельца *</span><input v-model="carForm.ownerName" required placeholder="ФИО владельца" /></label><label><span>Телефон владельца *</span><input v-model="carForm.ownerPhone" required placeholder="+7 999 000-00-00" /></label><label class="form-wide"><span>Комментарий</span><textarea v-model="carForm.comment" rows="3" placeholder="Необязательный комментарий"></textarea></label></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="carFormVisible = false">Отмена</button><button class="button button-primary" type="submit">{{ editingCar ? 'Сохранить изменения' : 'Сохранить автомобиль' }}</button></div></form></div>

    <div v-if="partFormVisible" class="stub-overlay" @click.self="partFormVisible = false"><form class="data-modal compact-modal part-form-modal" @submit.prevent="savePart"><button type="button" class="icon-button" aria-label="Закрыть" @click="partFormVisible = false">×</button><p class="eyebrow">Заказ запчасти</p><h2>{{ editingPart ? 'Изменить деталь' : 'Добавить деталь' }}</h2><p class="modal-subtitle">{{ selectedCar?.number }} · {{ selectedCar?.vehicle }}</p><div class="data-form-grid"><label><span>Деталь *</span><input v-model="partForm.name" required placeholder="Бампер передний" /></label><label><span>Артикул</span><input v-model="partForm.article" placeholder="604A124500" /></label><label><span>Каталожный номер</span><input v-model="partForm.catalogNumber" /></label><label><span>Производитель</span><input v-model="partForm.manufacturer" /></label><label><span>Количество</span><input v-model.number="partForm.quantity" type="number" min="0.001" step="0.001" /></label><label><span>Поставщик</span><select v-model="partForm.supplierId"><option value="">Не выбран</option><option v-for="item in suppliers" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select></label><label><span>Дата заказа</span><input v-model="partForm.orderedAt" type="date" /></label><label><span>Ожидаемая дата</span><input v-model="partForm.expectedDate" type="date" /></label><label class="form-wide"><span>Комментарий</span><textarea v-model="partForm.comment" rows="2"></textarea></label><label v-if="editingPart" class="checkbox-field"><input v-model="partForm.received" type="checkbox" /><span>Деталь получена</span></label></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="partFormVisible = false">Отмена</button><button class="button button-primary" type="submit">{{ editingPart ? 'Сохранить изменения' : 'Добавить деталь' }}</button></div></form></div>

    <div v-if="directoriesVisible" class="stub-overlay" @click.self="directoriesVisible = false"><section class="data-modal directory-modal"><button class="icon-button" aria-label="Закрыть" @click="directoriesVisible = false">×</button><p class="eyebrow">Настройки</p><h2>Справочники</h2><div class="directory-tabs"><button type="button" :class="{ 'is-active': directoryType === 'insurers' }" @click="directoryType = 'insurers'">Страховые</button><button type="button" :class="{ 'is-active': directoryType === 'suppliers' }" @click="directoryType = 'suppliers'">Поставщики</button><button type="button" :class="{ 'is-active': directoryType === 'shifts' }" @click="directoryType = 'shifts'">Смены</button><button type="button" :class="{ 'is-active': directoryType === 'works' }" @click="directoryType = 'works'">Работы</button><button type="button" :class="{ 'is-active': directoryType === 'counterparties' }" @click="directoryType = 'counterparties'">Контрагенты</button></div><form class="data-form-grid" @submit.prevent="createDirectoryItem"><label v-if="directoryType === 'works'"><span>Код работы</span><input v-model="directoryForm.code" required placeholder="BODY-001" /></label><label><span>{{ directoryType === 'works' ? 'Название работы' : 'Название' }}</span><input v-model="directoryForm.name" required placeholder="Название элемента" /></label><label v-if="directoryType === 'works'"><span>Категория</span><input v-model="directoryForm.categoryName" required placeholder="Кузовные работы" /></label><label v-if="directoryType === 'works'"><span>Единица</span><input v-model="directoryForm.defaultUnit" required placeholder="н/ч" /></label><label v-if="directoryType === 'counterparties'"><span>ИНН</span><input v-model="directoryForm.inn" placeholder="ИНН" /></label><label v-if="directoryType === 'counterparties'"><span>Телефон</span><input v-model="directoryForm.phone" placeholder="+7..." /></label><label v-if="directoryType === 'counterparties'" class="form-wide"><span>Адрес</span><input v-model="directoryForm.address" placeholder="Адрес" /></label><label v-if="directoryType === 'counterparties'" class="form-wide"><span>Примечание</span><textarea v-model="directoryForm.note" rows="2"></textarea></label><div class="modal-actions form-wide"><button class="button button-primary" type="submit">{{ editingDirectory ? 'Сохранить изменения' : 'Добавить в справочник' }}</button></div></form><div class="directory-list"><div v-for="item in (directoryType === 'insurers' ? insurers : directoryType === 'suppliers' ? suppliers : directoryType === 'shifts' ? shifts : directoryType === 'counterparties' ? counterparties : workCatalog)" :key="item.id" class="directory-item"><span>{{ item.name }}<small v-if="directoryType === 'works'">{{ item.categoryName }} · {{ item.defaultUnit }}</small><small v-if="directoryType === 'counterparties'">{{ item.inn }} · {{ item.phone }}</small></span><code v-if="directoryType === 'works'">{{ item.code }}</code><button type="button" class="link-button" @click="openDirectoryEdit(item)">Изменить</button><button type="button" class="link-button danger-link" @click="deleteDirectoryItem(item)">Удалить</button></div><p v-if="!(directoryType === 'insurers' ? insurers : directoryType === 'suppliers' ? suppliers : directoryType === 'shifts' ? shifts : directoryType === 'counterparties' ? counterparties : workCatalog).length" class="empty-state">Справочник пока пуст.</p></div></section></div>

    <div v-if="workOrderVisible" class="stub-overlay" @click.self="workOrderVisible = false"><section class="data-modal work-order-modal print-target"><button class="icon-button" aria-label="Закрыть" @click="workOrderVisible = false">×</button><p class="eyebrow">Рабочие данные</p><h2>Заказ-наряд · №{{ selectedWorkOrderCar?.number }}</h2><p class="modal-subtitle">{{ selectedWorkOrderCar?.vehicle }} · {{ selectedWorkOrderCar?.registration }}</p><div v-if="workOrderBusy" class="empty-state">Загружаем заказ-наряд…</div><div v-else-if="workOrderError" class="empty-state">{{ workOrderError }}</div><template v-else-if="workOrder"><div class="data-form-grid work-order-meta"><label><span>Дата документа</span><input v-model="workOrder.documentDate" type="date" /></label><label><span>Заказчик из справочника</span><select v-model="workOrder.customer"><option value="">Произвольный заказчик</option><option v-for="item in counterparties" :key="item.id" :value="item.name">{{ item.name }} · {{ item.inn || "без ИНН" }}</option></select></label><label><span>Заказчик</span><input v-model="workOrder.customer" placeholder="ФИО или организация" /></label></div><div class="work-order-lines"><div class="work-order-line work-order-line-head"><span>Категория</span><span>Работа</span><span>Ед.</span><span>Кол-во</span><span>Цена</span><span>Сумма</span><span></span></div><div v-for="(line, index) in workOrder.lines" :key="line.id || `new-${index}`" class="work-order-line"><select v-model="line.catalogId" @change="applyCatalogLine(line)"><option value="">Своя работа</option><option v-for="item in workCatalog" :key="item.id" :value="String(item.id)">{{ item.categoryName }} · {{ item.name }}</option></select><input v-model="line.name" required placeholder="Ремонт двери" /><input v-model="line.unit" placeholder="шт." /><input v-model.number="line.quantity" type="number" min="0.001" step="0.001" /><input v-model.number="line.price" type="number" min="0" step="0.01" /><strong>{{ ((Number(line.quantity) || 0) * (Number(line.price) || 0)).toFixed(2) }}</strong><button type="button" class="icon-button small-icon" aria-label="Удалить строку" @click="removeWorkOrderLine(index)">×</button></div><button type="button" class="link-button" @click="addWorkOrderLine">＋ Добавить работу</button></div><div class="work-order-parts"><div class="work-order-line work-order-line-head"><span>Запчасть</span><span>Артикул</span><span>Кол-во</span><span>Цена</span><span>Сумма</span><span></span></div><div v-for="(line, index) in workOrder.partLines" :key="line.id || `part-new-${index}`" class="work-order-part-line"><strong>{{ line.name }}</strong><span>{{ line.article || "—" }}</span><input v-model.number="line.quantity" type="number" min="0.001" step="0.001" /><input v-model.number="line.price" type="number" min="0" step="0.01" /><strong>{{ ((Number(line.quantity) || 0) * (Number(line.price) || 0)).toFixed(2) }}</strong><button type="button" class="icon-button small-icon" aria-label="Удалить строку запчасти" @click="removeWorkOrderPartLine(index)">×</button></div><div class="work-order-part-picker"><span>Добавить запчасть:</span><button v-for="part in selectedWorkOrderCar.parts" :key="part.id" type="button" class="link-button" :disabled="workOrder.partLines.some((line) => line.partId === part.id)" @click="addWorkOrderPartLine(part)">{{ part.name }}</button></div></div><div class="work-order-total">Итого: <strong>{{ workOrderTotal().toFixed(2) }}</strong></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="workOrderVisible = false">Закрыть</button><button class="button button-cloud dark-button" @click="printWorkOrder">Печать</button><button class="button button-primary" @click="saveWorkOrder">Сохранить заказ-наряд</button></div></template></section></div>

    <div v-if="modal" class="stub-overlay" @click.self="modal = null"><section class="stub-modal"><button class="icon-button" aria-label="Закрыть" @click="modal = null">×</button><p class="eyebrow">Заглушка раздела</p><h2>{{ modal }}</h2><p>Внешний вид и место действия уже подготовлены. Реальная загрузка и сохранение данных будут подключены к backend следующим этапом.</p><button class="button button-primary" @click="modal = null">Понятно</button></section></div>
    <div v-if="photosVisible" class="stub-overlay" @click.self="photosVisible = false"><section class="data-modal photos-modal"><button class="icon-button" aria-label="Закрыть" @click="photosVisible = false">×</button><p class="eyebrow">Документы автомобиля</p><h2>Фото автомобиля</h2><p class="modal-subtitle">{{ photosCar?.number }} · {{ photosCar?.vehicle }}</p><div v-if="photosBusy" class="empty-state">Загружаем фотографии…</div><div v-else class="car-photo-grid"><div v-for="photo in carPhotos" :key="photo.id || photo.dataUrl" class="car-photo-card"><img :src="photo.dataUrl" :alt="photo.fileName || 'Фото автомобиля'" /><div><small>{{ photo.fileName }}</small><button type="button" class="link-button danger-link" @click="deleteCarPhoto(photo)">Удалить</button></div></div><p v-if="!carPhotos.length" class="empty-state">Фотографии пока не добавлены.</p></div></section></div>
    <div v-if="directoriesVisible" class="settings-overlay" @click.self="directoriesVisible = false"><section class="settings-screen"><button class="icon-button" aria-label="Закрыть" @click="directoriesVisible = false">×</button><p class="eyebrow">Управление программой</p><h2>Настройки</h2><p class="settings-subtitle">Справочники, контрагенты и рабочие параметры.</p><nav class="settings-tabs"><button type="button" :class="{ 'is-active': settingsTab === 'directories' }" @click="settingsTab = 'directories'">Справочники</button><button type="button" :class="{ 'is-active': settingsTab === 'counterparties' }" @click="settingsTab = 'counterparties'">Контрагенты</button><button type="button" disabled>Резервные копии</button><button type="button" disabled>История</button><button type="button" disabled>Безопасность</button></nav><template v-if="settingsTab === 'directories'"><div class="settings-section-head"><div><h3>Справочники</h3><p>Эти значения используются в карточках автомобилей, запчастях и документах.</p></div></div><div class="settings-directory-grid"><section class="settings-card"><div class="settings-card-head"><div><h3>Страховые компании</h3><p>Название для выбора в карточке автомобиля.</p></div><button type="button" class="settings-add" @click="addSettingsItem('insurers', settingsNewInsurer)">＋ Добавить</button></div><div class="settings-add-row"><input v-model="settingsNewInsurer" placeholder="Название страховой" @keyup.enter="addSettingsItem('insurers', settingsNewInsurer)" /></div><div class="settings-list"><div v-for="item in insurers" :key="item.id" class="settings-list-row"><input :value="item.name" @change="openDirectoryEdit(item); directoryType = 'insurers'; createDirectoryItem()" /><button type="button" class="settings-delete" @click="removeSettingsItem('insurers', item)">×</button></div></div></section><section class="settings-card"><div class="settings-card-head"><div><h3>Поставщики</h3><p>Выбор в строках запчастей.</p></div><button type="button" class="settings-add" @click="addSettingsItem('suppliers', settingsNewSupplier)">＋ Добавить</button></div><div class="settings-add-row"><input v-model="settingsNewSupplier" placeholder="Название поставщика" @keyup.enter="addSettingsItem('suppliers', settingsNewSupplier)" /></div><div class="settings-list"><div v-for="item in suppliers" :key="item.id" class="settings-list-row"><input :value="item.name" @change="openDirectoryEdit(item); directoryType = 'suppliers'; createDirectoryItem()" /><button type="button" class="settings-delete" @click="removeSettingsItem('suppliers', item)">×</button></div></div></section></div></template><template v-else><div class="settings-section-head"><div><h3>Контрагенты</h3><p>Контрагенты используются в генераторе документов для автомобилей вне реестра.</p></div></div><div class="settings-counterparty-grid"><form class="settings-card settings-counterparty-form" @submit.prevent="saveSettingsCounterparty"><h3>Новый контрагент</h3><label><span>Наименование или ФИО *</span><input v-model="settingsNewCounterparty.name" required placeholder="Например, ООО «Автотранс»" /></label><div class="settings-two-fields"><label><span>ИНН</span><input v-model="settingsNewCounterparty.inn" placeholder="ИНН организации или ИП" /></label><label><span>Телефон</span><input v-model="settingsNewCounterparty.phone" placeholder="+7 999 000-00-00" /></label></div><label><span>Адрес</span><input v-model="settingsNewCounterparty.address" placeholder="Город, улица, дом" /></label><label><span>Комментарий</span><input v-model="settingsNewCounterparty.note" placeholder="Необязательная внутренняя заметка" /></label><button class="button button-primary" type="submit">Сохранить контрагента</button></form><section class="settings-card"><h3>Сохранённые контрагенты</h3><p>{{ counterparties.length }} записей</p><div class="settings-list"><div v-for="item in counterparties" :key="item.id" class="counterparty-row"><div><strong>{{ item.name }}</strong><small>ИНН {{ item.inn || 'не указан' }}</small><small>{{ item.address || 'Адрес не указан' }}</small></div><div><button type="button" class="settings-edit" @click="openDirectoryEdit(item); directoryType = 'counterparties'">Изменить</button><button type="button" class="settings-delete-text" @click="removeSettingsCounterparty(item)">Удалить</button></div></div></div></section></div></template></section></div>
    <div v-if="directoriesVisible" class="settings-overlay-v2" @click.self="directoriesVisible = false"><section class="settings-screen"><button class="icon-button" aria-label="Закрыть" @click="directoriesVisible = false">×</button><p class="eyebrow">Управление программой</p><h2>Настройки</h2><p class="settings-subtitle">Справочники, контрагенты, мастера и рабочие параметры.</p><nav class="settings-tabs"><button type="button" :class="{ 'is-active': settingsTab === 'directories' }" @click="settingsTab = 'directories'">Справочники</button><button type="button" :class="{ 'is-active': settingsTab === 'counterparties' }" @click="settingsTab = 'counterparties'">Контрагенты</button><button type="button" disabled>Резервные копии</button><button type="button" disabled>История</button><button type="button" disabled>Безопасность</button></nav><template v-if="settingsTab === 'directories'"><div class="settings-section-head"><div><h3>Справочники</h3><p>Значения используются в карточках автомобилей, запчастях и документах.</p></div></div><div class="settings-directory-grid"><section class="settings-card"><div class="settings-card-head"><div><h3>Страховые компании</h3><p>Название и адрес/реквизиты одной строкой.</p></div></div><div class="settings-add-row settings-insurer-add"><input v-model="settingsNewInsurer" placeholder="Название страховой" /><input v-model="directoryForm.note" placeholder="Адрес и реквизиты" /><button type="button" class="settings-add" @click="addSettingsItem('insurers', settingsNewInsurer)">＋ Добавить</button></div><div class="settings-list"><div v-for="item in insurers" :key="item.id" class="settings-list-row"><input :value="item.name" /><input :value="item.legalDetails || ''" placeholder="Адрес и реквизиты" /><button type="button" class="settings-delete" @click="removeSettingsItem('insurers', item)">×</button></div></div></section><section class="settings-card"><div class="settings-card-head"><div><h3>Поставщики</h3><p>Только названия для строк запчастей.</p></div></div><div class="settings-add-row"><input v-model="settingsNewSupplier" placeholder="Название поставщика" /><button type="button" class="settings-add" @click="addSettingsItem('suppliers', settingsNewSupplier)">＋ Добавить</button></div><div class="settings-list"><div v-for="item in suppliers" :key="item.id" class="settings-list-row"><input :value="item.name" /><button type="button" class="settings-delete" @click="removeSettingsItem('suppliers', item)">×</button></div></div></section><section class="settings-card"><div class="settings-card-head"><div><h3>Работы</h3><p>Название и расшифровка в нормо-часах.</p></div></div><div class="settings-add-row"><input v-model="settingsNewWork.name" placeholder="Название работы" /><input v-model.number="settingsNewWork.normHours" type="number" min="0" step="0.01" placeholder="Нормо-часы" /><button type="button" class="settings-add" @click="saveSettingsWork">＋ Добавить</button></div><div class="settings-list"><div v-for="item in workCatalog" :key="item.id" class="settings-list-row"><span>{{ item.name }}</span><strong>{{ item.normHours || 0 }} н/ч</strong><button type="button" class="settings-delete" @click="removeSettingsWork(item)">×</button></div></div></section><section class="settings-card"><div class="settings-card-head"><div><h3>Исполнители / мастера</h3><p>Мастера, доступные в карточке автомобиля.</p></div></div><div class="settings-add-row"><input v-model="settingsNewMaster.code" placeholder="Код" /><input v-model="settingsNewMaster.shortName" placeholder="Имя мастера" /><button type="button" class="settings-add" @click="saveSettingsMaster">＋ Добавить</button></div><div class="settings-list"><div v-for="item in contractors" :key="item.id" class="settings-list-row"><span>{{ item.shortName }}</span><code>{{ item.code }}</code><button type="button" class="settings-delete" @click="removeSettingsMaster(item)">×</button></div></div></section></div></template><template v-else><div class="settings-section-head"><div><h3>Контрагенты</h3><p>Контрагенты используются в генераторе документов для автомобилей вне реестра.</p></div></div><div class="settings-counterparty-grid"><form class="settings-card settings-counterparty-form" @submit.prevent="saveSettingsCounterparty"><h3>Новый контрагент</h3><p>Наименование или ФИО *</p><input v-model="settingsNewCounterparty.name" required placeholder="Например, ООО «Автотранс»" /><div class="settings-two-fields"><label><span>ИНН</span><input v-model="settingsNewCounterparty.inn" placeholder="ИНН организации или ИП" /></label><label><span>Телефон</span><input v-model="settingsNewCounterparty.phone" placeholder="+7 999 000-00-00" /></label></div><label><span>Адрес</span><input v-model="settingsNewCounterparty.address" placeholder="Город, улица, дом" /></label><label><span>Комментарий</span><input v-model="settingsNewCounterparty.note" placeholder="Необязательная внутренняя заметка" /></label><button class="button button-primary" type="submit">Сохранить контрагента</button></form><section class="settings-card"><h3>Сохранённые контрагенты</h3><p>{{ counterparties.length }} записей</p><div class="settings-list"><div v-for="item in counterparties" :key="item.id" class="counterparty-row"><div><strong>{{ item.name }}</strong><small>ИНН {{ item.inn || 'не указан' }}</small><small>{{ item.address || 'Адрес не указан' }}</small></div><button type="button" class="settings-delete-text" @click="removeSettingsCounterparty(item)">Удалить</button></div></div></section></div></template></section></div>
    <button v-if="carFormVisible && editingCar" type="button" class="repair-cases-button button button-primary" @click="openRepairCases(editingCar)">Страховые случаи</button>
    <div v-if="caseDetailVisible && selectedRegistryCase" class="stub-overlay" @click.self="caseDetailVisible = false"><section class="data-modal case-detail-modal"><button class="icon-button" aria-label="Закрыть" @click="caseDetailVisible = false">×</button><p class="eyebrow">Обращение · {{ statusLabel(selectedRegistryCase.status) }}</p><h2>{{ selectedRegistryCase.caseNumber }}</h2><p class="modal-subtitle">{{ selectedRegistryCase.vehicleMake }} {{ selectedRegistryCase.vehicleModel }} · VIN {{ selectedRegistryCase.vin }} · {{ selectedRegistryCase.registrationNumber }}</p><div class="case-action-bar"><span class="case-status-large">{{ statusLabel(selectedRegistryCase.status) }}</span><button v-if="selectedRegistryCase.status === 'CREATED' && caseParts.length" type="button" class="button button-cloud" @click="runCaseAction('ORDER_PARTS')">Заказать детали</button><button v-if="selectedRegistryCase.status === 'PARTS_RECEIVED'" type="button" class="button button-cloud" @click="runCaseAction('SCHEDULE_REPAIR')">Записать на ремонт</button><button v-if="selectedRegistryCase.status === 'SCHEDULED'" type="button" class="button button-cloud" @click="runCaseAction('START_REPAIR')">Начать ремонт</button><button v-if="selectedRegistryCase.status === 'IN_REPAIR'" type="button" class="button button-cloud" @click="runCaseAction('FINISH_REPAIR')">Завершить ремонт</button><button v-if="selectedRegistryCase.status === 'READY'" type="button" class="button button-cloud" @click="runCaseAction('DELIVER')">Выдать автомобиль</button><button v-if="selectedRegistryCase.status === 'DELIVERED'" type="button" class="button button-cloud" @click="runCaseAction('CLOSE')">Закрыть случай</button></div><nav class="case-detail-tabs"><button type="button" :class="{ 'is-active': caseDetailTab === 'main' }" @click="caseDetailTab = 'main'">Основное</button><button type="button" :class="{ 'is-active': caseDetailTab === 'works' }" @click="caseDetailTab = 'works'; openWorkOrder(repairCaseCar)">Работы</button><button type="button" :class="{ 'is-active': caseDetailTab === 'parts' }" @click="caseDetailTab = 'parts'">Запчасти</button><button type="button" :class="{ 'is-active': caseDetailTab === 'photos' }" @click="caseDetailTab = 'photos'; openRepairCasePhotos(selectedRegistryCase)">Фотографии</button><button type="button" :class="{ 'is-active': caseDetailTab === 'history' }" @click="caseDetailTab = 'history'">История</button><button type="button" :class="{ 'is-active': caseDetailTab === 'contractor' }" @click="caseDetailTab = 'contractor'">Исполнитель</button></nav><div v-if="caseDetailTab === 'contractor'" class="case-detail-contractor"><div class="case-detail-contractor-card"><span>Назначенный исполнитель</span><strong>{{ contractors.find((item) => item.id === selectedRegistryCase.contractorId)?.shortName || 'Не назначен' }}</strong></div><label><span>Заменить исполнителя</span><select v-model="caseContractorId" :disabled="selectedRegistryCase.status === 'CLOSED'"><option value="" disabled>Выберите исполнителя</option><option v-for="item in contractors" :key="item.id" :value="String(item.id)">{{ item.shortName }}</option></select></label><button type="button" class="button button-primary" :disabled="selectedRegistryCase.status === 'CLOSED' || !caseContractorId || caseContractorId === String(selectedRegistryCase.contractorId || '')" @click="saveCaseContractor">Сохранить исполнителя</button></div><div v-if="caseDetailTab === 'main'" class="case-detail-main"><div><span>Автомобиль</span><strong>{{ selectedRegistryCase.vehicleMake }} {{ selectedRegistryCase.vehicleModel }}</strong></div><div><span>Клиент</span><strong>{{ selectedRegistryCase.ownerName || '—' }} · {{ selectedRegistryCase.ownerPhone || '—' }}</strong></div><div><span>Страховая</span><strong>{{ insurers.find((item) => item.id === selectedRegistryCase.insurerId)?.name || '—' }}</strong></div><div><span>Исполнитель</span><strong>{{ contractors.find((item) => item.id === selectedRegistryCase.contractorId)?.shortName || '—' }}</strong></div><div><span>Создано</span><strong>{{ formatDisplayDateTime(selectedRegistryCase.createdAt) }} · пользователь #{{ selectedRegistryCase.createdBy || '—' }}</strong></div></div><div v-else-if="caseDetailTab === 'works'" class="case-inline-work-order"><div v-if="workOrderBusy" class="empty-state">Загружаем работы…</div><template v-else-if="workOrder"><div class="case-detail-actions"><p>Работы обращения · {{ workOrder.lines?.length || 0 }}</p><button type="button" class="button button-primary" @click="addWorkOrderLine">＋ Добавить работу</button></div><div v-for="(line, index) in workOrder.lines" :key="line.id || `case-line-${index}`" class="case-work-line"><input v-model="line.name" placeholder="Название работы" /><select v-model="line.unit"><option value="н/ч">н/ч</option><option value="шт.">шт.</option></select><button type="button" class="link-button danger-link" @click="removeWorkOrderLine(index)">Удалить</button></div><p v-if="!workOrder.lines?.length" class="empty-state">Работы пока не добавлены.</p><button type="button" class="button button-primary" @click="saveWorkOrder">Сохранить работы</button></template></div><div v-else-if="caseDetailTab === 'parts'" class="case-detail-parts"><div class="case-detail-actions"><p>Запчасти обращения · {{ caseParts.length }}</p><button type="button" class="button button-primary" @click="openPartForm(repairCaseCar)">＋ Добавить запчасть</button></div><div class="case-parts-list"><div v-for="part in caseParts" :key="part.id" class="case-part-item"><div><strong>{{ part.name }}</strong><small>Артикул: {{ part.article || '—' }} · Поставщик: {{ suppliers.find((item) => item.id === part.supplierId)?.name || '—' }}</small><small>Ожидаемая дата: {{ part.expectedDate || '—' }}</small><small v-if="part.received" class="part-received-date">Поступила: {{ part.receivedAt || 'дата не указана' }}</small></div><div><button type="button" class="link-button" @click="openPartForm(repairCaseCar, selectedRegistryCase); editingPart = part; partForm = { name: part.name || '', article: part.article || '', supplierId: part.supplierId ? String(part.supplierId) : '', expectedDate: part.expectedDate || '', received: !!part.received, receivedAt: part.receivedAt || '', sortOrder: part.sortOrder || 0 }">Изменить</button><button type="button" class="link-button danger-link" @click="deletePart(repairCaseCar, part)">Удалить</button></div></div><div v-if="caseParts.length" class="case-receipt-inline-list"><div v-for="part in caseParts" :key="part.id" class="case-receipt-inline-row"><span><strong>{{ part.name }}</strong><small>{{ part.received ? 'Поступила: ' + (part.receivedAt || 'дата не указана') : 'Ожидается' }}</small></span><button type="button" class="part-receipt-button" :class="{ 'is-received': part.received }" :disabled="selectedRegistryCase.status === 'CLOSED'" @click="toggleCasePartReceived(selectedRegistryCase, part)">{{ part.received ? 'Получена · отменить' : 'Отметить как полученную' }}</button></div></div><p v-if="!caseParts.length" class="empty-state">Запчасти по обращению пока не добавлены.</p></div></div><div v-else-if="caseDetailTab === 'photos'" class="case-detail-photos"><div v-if="photosBusy" class="empty-state">Загружаем фотографии…</div><template v-else><div class="case-photo-grid"><div v-for="photo in carPhotos" :key="photo.id || photo.dataUrl" class="case-photo-card"><img :src="photo.dataUrl" :alt="photo.fileName || 'Фото повреждения'" /><div><small>{{ photo.fileName }}</small><button type="button" class="link-button danger-link" @click="deleteCarPhoto(photo)">Удалить</button></div></div></div><p v-if="!carPhotos.length" class="empty-state">Фотографии по обращению пока не добавлены.</p><label class="case-photo-upload"><span>Добавить фото повреждения</span><input type="file" accept="image/*" multiple @change="readCarPhotos" /></label></template></div><div v-else-if="caseDetailTab === 'history'" class="case-history-list"><div v-for="event in carHistory" :key="event.id"><strong>{{ formatDisplayDateTime(event.createdAt) }}</strong><span>{{ event.details }}</span></div><p v-if="!carHistory.length" class="empty-state">История пока пуста.</p></div></section></div>
    <div v-if="repairMenuVisible" class="stub-overlay" @click.self="repairMenuVisible = false"><section class="data-modal compact-modal repair-menu-modal"><button class="icon-button" aria-label="Закрыть" @click="repairMenuVisible = false">×</button><p class="eyebrow">Создание ремонта</p><h2>Что создаём?</h2><p class="modal-subtitle">Сначала выберите тип ремонта. Страховой случай будет связан с выбранным клиентом сервиса.</p><div class="repair-type-actions"><button type="button" class="button button-primary" @click="startInsuranceCaseFlow">Страховой случай</button><button type="button" class="button button-cloud dark-button" @click="startPlaceholderRepair">Ремонт</button></div></section></div>
    <div v-if="insuranceVehiclePickerVisible" class="stub-overlay" @click.self="insuranceVehiclePickerVisible = false"><section class="data-modal repair-picker-modal"><button class="icon-button" aria-label="Закрыть" @click="insuranceVehiclePickerVisible = false">×</button><p class="eyebrow">{{ selectedCreationType === 'INSURANCE' ? 'Страховой случай' : 'Ремонт' }}</p><h2>Найдите автомобиль по VIN</h2><p class="modal-subtitle">Введите VIN — поиск выполняется через API по всем автомобилям. Список автомобилей заранее не загружается.</p><label class="search-field repair-vin-search"><span>⌕</span><input v-model="repairVinSearch" type="search" placeholder="Введите VIN автомобиля" autocomplete="off" @input="searchRepairVehicle" /></label><div class="repair-vehicle-results"><p v-if="repairVehicleSearchBusy" class="empty-state">Ищем автомобиль…</p><p v-else-if="repairVehicleSearchError" class="empty-state">Не удалось выполнить поиск: {{ repairVehicleSearchError }}</p><button v-for="car in repairVehicleCandidates" :key="car.id" type="button" class="repair-vehicle-option" @click="chooseRepairVehicle(car)"><strong>{{ car.vehicle }}</strong><span>VIN: {{ car.vin }} · {{ car.registration }}</span><small>{{ car.ownerName }} · {{ car.ownerPhone }}</small></button><p v-if="!repairVehicleSearchBusy && !repairVehicleSearchError && repairVinSearch && !repairVehicleCandidates.length" class="empty-state">Автомобиль с таким VIN не найден.</p><p v-if="!repairVinSearch" class="empty-state">Введите VIN, чтобы найти автомобиль.</p></div></section></div>
    <div v-if="insuranceCaseCreateVisible" class="stub-overlay" @click.self="insuranceCaseCreateVisible = false"><form class="data-modal insurance-case-create-modal" @submit.prevent="saveNewInsuranceCase"><button type="button" class="icon-button" aria-label="Закрыть" @click="insuranceCaseCreateVisible = false">×</button><p class="eyebrow">Автомобиль №{{ repairCaseCar?.number }} · VIN {{ repairCaseCar?.vin }}</p><h2>{{ selectedCreationType === 'INSURANCE' ? 'Новый страховой случай' : 'Новое обращение на ремонт' }}</h2><p class="modal-subtitle">{{ repairCaseCar?.vehicle }} · {{ repairCaseCar?.registration }}</p><div class="data-form-grid"><label><span>Номер дела / направления *</span><input v-model="newInsuranceCaseForm.caseNumber" required /></label><label v-if="selectedCreationType === 'INSURANCE'"><span>Страховая компания *</span><select v-model="newInsuranceCaseForm.insurerId" required :disabled="insurersBusy || !insurers.length"><option value="" disabled>Выберите страховую</option><option v-for="item in insurers" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select><small v-if="insurersBusy" class="field-hint">Загружаем список страховых компаний…</small><small v-else-if="insurersError" class="field-hint field-hint-error">Не удалось загрузить страховые компании.</small><small v-else-if="!insurers.length" class="field-hint">Страховые компании пока не добавлены в справочник.</small></label><label v-if="selectedCreationType === 'INSURANCE'"><span>Исполнитель *</span><select v-model="newInsuranceCaseForm.contractorId" required :disabled="contractorsBusy || !contractors.length"><option value="" disabled>Выберите исполнителя</option><option v-for="item in contractors" :key="item.id" :value="String(item.id)">{{ item.shortName }}</option></select><small v-if="contractorsBusy" class="field-hint">Загружаем список исполнителей…</small><small v-else-if="contractorsError" class="field-hint field-hint-error">Не удалось загрузить исполнителей.</small><small v-else-if="!contractors.length" class="field-hint">Исполнители пока не добавлены в справочник.</small></label><label class="form-wide"><span>Фото автомобиля{{ selectedCreationType === 'INSURANCE' ? ' *' : '' }}</span><input type="file" accept="image/*" multiple @change="readRepairCasePhotos" /><small>{{ selectedCreationType === 'INSURANCE' ? 'Добавьте фотографии повреждений. Максимум 20 файлов по 8 МБ.' : 'Фотографии можно добавить позже в карточке обращения.' }}</small><div v-if="repairCasePhotos.length" class="form-photo-preview"><div v-for="photo in repairCasePhotos" :key="photo.fileName"><img :src="photo.dataUrl" :alt="photo.fileName" /><span>{{ photo.fileName }}</span></div></div></label></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="insuranceCaseCreateVisible = false">Отмена</button><button class="button button-primary" type="submit">{{ selectedCreationType === 'INSURANCE' ? 'Создать страховой случай' : 'Создать ремонт' }}</button></div></form></div>
    <div v-if="repairCasesVisible" class="stub-overlay" @click.self="repairCasesVisible = false"><section class="data-modal repair-cases-modal"><button class="icon-button" aria-label="Закрыть" @click="repairCasesVisible = false">×</button><p class="eyebrow">Автомобиль №{{ repairCaseCar?.number }} · VIN {{ repairCaseCar?.vin }}</p><h2>Страховые случаи</h2><div v-if="repairCasesBusy" class="empty-state">Загружаем случаи…</div><template v-else><div class="repair-case-list"><div v-for="item in repairCases" :key="item.id" class="repair-case-card"><div><strong>Случай №{{ item.caseNumber }}</strong><small>{{ item.claimNumber || 'Номер дела не указан' }} · {{ statusLabel(item.status) }}</small><small>{{ item.insuredPerson || 'Страхователь не указан' }}</small></div><div><button type="button" class="settings-edit" @click="openRepairCasePhotos(item)">Фото</button><button type="button" class="settings-edit" @click="startRepairCase(item)">Изменить</button><button type="button" class="settings-delete-text" @click="deleteRepairCase(item)">Удалить</button></div></div></div><button type="button" class="button button-primary" @click="startRepairCase()">＋ Новый страховой случай</button><form v-if="editingRepairCase || repairCaseForm.caseNumber" class="data-form-grid repair-case-form" @submit.prevent="saveRepairCase"><label><span>Номер дела / направления *</span><input v-model="repairCaseForm.caseNumber" required /></label><label><span>Статус *</span><select v-model="repairCaseForm.status" required><option v-for="item in caseStatusOptions.filter((value) => value.code !== 'ALL')" :key="item.code" :value="item.code">{{ item.label }}</option></select></label><label><span>Страхователь</span><input v-model="repairCaseForm.insuredPerson" /></label><label><span>Страховая компания *</span><select v-model="repairCaseForm.insurerId" required><option value="" disabled>Выберите страховую</option><option v-for="item in insurers" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select></label><label><span>Исполнитель *</span><select v-model="repairCaseForm.contractorId" required><option value="" disabled>Выберите исполнителя</option><option v-for="item in contractors" :key="item.id" :value="String(item.id)">{{ item.shortName }}</option></select></label><label><span>Номер дела (дополнительно)</span><input v-model="repairCaseForm.claimNumber" /></label><label><span>Смена</span><select v-model="repairCaseForm.shiftId"><option value="">Не выбрана</option><option v-for="item in shifts" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select></label><label><span>Дата приёмки</span><input v-model="repairCaseForm.acceptedAt" type="date" /></label><div class="modal-actions form-wide"><button class="button button-primary" type="submit">Сохранить страховой случай</button></div></form></template></section></div>
    <div v-if="caseActionModal" class="stub-overlay" @click.self="caseActionModal = null"><form class="data-modal compact-modal" @submit.prevent="submitCaseAction"><button type="button" class="icon-button" aria-label="Закрыть" @click="caseActionModal = null">×</button><p class="eyebrow">Изменение страхового случая</p><h2>{{ caseActionModal === 'SCHEDULE_REPAIR' ? 'Записать на ремонт' : 'Выдать автомобиль' }}</h2><p class="modal-subtitle">{{ selectedRegistryCase?.vehicleMake }} {{ selectedRegistryCase?.vehicleModel }} · дело {{ selectedRegistryCase?.caseNumber }}</p><div class="data-form-grid"><label v-if="caseActionModal === 'SCHEDULE_REPAIR'"><span>Исполнитель *</span><select v-model="caseActionForm.contractorId" required><option value="" disabled>Выберите исполнителя</option><option v-for="item in contractors" :key="item.id" :value="String(item.id)">{{ item.shortName }}</option></select></label><label v-if="caseActionModal === 'SCHEDULE_REPAIR'"><span>Дата ремонта *</span><input v-model="caseActionForm.appointmentDate" type="date" required /></label><label v-if="caseActionModal === 'SCHEDULE_REPAIR'"><span>Время ремонта *</span><input v-model="caseActionForm.appointmentTime" type="time" required /></label><label v-if="caseActionModal === 'DELIVER'"><span>Кому выдан автомобиль *</span><input v-model="caseActionForm.receivedBy" required placeholder="ФИО получателя" /></label><label class="form-wide"><span>Комментарий</span><textarea v-model="caseActionForm.comment" rows="3" placeholder="Комментарий к действию"></textarea></label></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="caseActionModal = null">Отмена</button><button class="button button-primary" type="submit">{{ caseActionModal === 'SCHEDULE_REPAIR' ? 'Записать на ремонт' : 'Выдать автомобиль' }}</button></div></form></div>
    <div v-if="caseDocumentsVisible" class="stub-overlay" @click.self="caseDocumentsVisible = false"><section class="data-modal compact-modal"><button type="button" class="icon-button" aria-label="Закрыть" @click="caseDocumentsVisible = false">×</button><p class="eyebrow">Документы обращения</p><h2>{{ selectedRegistryCase?.caseNumber }}</h2><p class="modal-subtitle">Документы формируются из рабочего заказа и доступны после его сохранения.</p><div v-if="generatedDocuments.length" class="generated-documents-list"><div v-for="doc in generatedDocuments" :key="doc.id" class="generated-document-row"><strong>{{ doc.documentType }}</strong><span>{{ doc.documentNumber ? `№${doc.documentNumber}` : 'Без номера' }}</span></div></div><p v-else class="empty-state">Документы пока не сформированы.</p><div class="modal-actions"><button type="button" class="button button-primary" @click="caseDocumentsVisible = false">Закрыть</button></div></section></div>
    <section v-if="caseDetailVisible && selectedRegistryCase" class="case-live-summary"><div class="case-readiness" :class="`case-readiness-${caseReadiness.tone}`"><span>{{ caseReadiness.icon }}</span><strong>{{ caseReadiness.label }}</strong></div><div class="case-live-metrics"><span><b>{{ caseParts.length }}</b> деталей</span><span><b>{{ caseReceivedPartsCount }}</b> получено</span><span><b>{{ caseWorksCost.toFixed(2) }}</b> работы</span><span><b>{{ casePartsCost.toFixed(2) }}</b> детали</span><span><b>{{ caseTotalCost.toFixed(2) }}</b> всего</span></div><div v-if="caseRemainingTasks.length" class="case-remaining"><strong>Что осталось сделать</strong><span v-for="task in caseRemainingTasks" :key="task">❌ {{ task }}</span></div><button type="button" class="button button-cloud case-documents-floating" @click="openCaseDocuments">Документы</button></section>
    <section v-if="caseDetailVisible && selectedRegistryCase && caseDetailTab === 'works' && workOrder" class="case-work-detail-dock"><strong>Исполнитель и комментарий по работам</strong><div v-for="(line, index) in workOrder.lines" :key="line.id || `dock-${index}`" class="case-work-detail-row"><span>{{ line.name || `Работа ${index + 1}` }}</span><select v-model="line.contractorId"><option value="">Исполнитель</option><option v-for="item in contractors" :key="item.id" :value="String(item.id)">{{ item.shortName }}</option></select><input v-model="line.comment" placeholder="Комментарий к работе" /></div><button type="button" class="button button-primary" @click="saveWorkOrder">Сохранить работы</button></section>
    <button v-if="caseDetailVisible && selectedRegistryCase" type="button" class="button button-primary case-documents-inline" @click="openCaseDocuments">Документы</button>
    <div v-if="carViewVisible && viewCar" class="stub-overlay" @click.self="carViewVisible = false"><section class="data-modal car-view-modal"><button type="button" class="icon-button" aria-label="Закрыть" @click="carViewVisible = false">×</button><p class="eyebrow">Клиент сервиса · просмотр</p><h2>{{ viewCar.vehicleMake }} {{ viewCar.vehicleModel }}</h2><p class="modal-subtitle">Автомобиль №{{ viewCar.accountingNumber }} · VIN {{ viewCar.vin }} · {{ viewCar.registrationNumber }}</p><nav class="car-view-tabs"><button type="button" :class="{ 'is-active': carViewTab === 'main' }" @click="carViewTab = 'main'">Основное</button><button type="button" :class="{ 'is-active': carViewTab === 'cases' }" @click="carViewTab = 'cases'">Страховые случаи</button></nav><div v-if="carViewTab === 'main'" class="car-view-grid"><div><span>Владелец</span><strong>{{ viewCar.ownerName || '—' }}</strong></div><div><span>Телефон</span><strong>{{ viewCar.ownerPhone || '—' }}</strong></div><div><span>Госномер</span><strong>{{ viewCar.registrationNumber || '—' }}</strong></div><div><span>VIN</span><strong>{{ viewCar.vin || '—' }}</strong></div><div class="form-wide"><span>Комментарий</span><strong>{{ viewCar.comment || '—' }}</strong></div></div><div v-else class="car-view-cases"><div v-if="viewCarCasesBusy" class="empty-state">Загружаем страховые случаи…</div><button v-for="item in viewCarCases" :key="item.id" type="button" class="car-view-case-row" @click="openViewedCase(item)"><span><strong>Случай №{{ item.caseNumber }}</strong><small>{{ statusLabel(item.status) }} · {{ formatDisplayDate(item.createdAt) }}</small></span><span>{{ insurers.find((value) => value.id === item.insurerId)?.name || 'Страховая не указана' }}<small>Открыть →</small></span></button><p v-if="!viewCarCasesBusy && !viewCarCases.length" class="empty-state">Страховых случаев пока нет.</p></div><div class="modal-actions"><button type="button" class="button button-primary" @click="carViewVisible = false; openCarEdit(viewCar)">Редактировать карточку</button></div></section></div>
  </template>
    <div v-if="workOrderRegistryVisible" class="stub-overlay" @click.self="workOrderRegistryVisible = false"><section class="data-modal registry-modal"><button class="icon-button" aria-label="Закрыть" @click="workOrderRegistryVisible = false">×</button><p class="eyebrow">Реестр документов</p><h2>Заказ-наряды</h2><div class="registry-actions"><button class="button button-cloud dark-button" type="button" @click="downloadWorkOrderCsv" :disabled="!workOrderRegistry.length">Скачать CSV</button></div><div v-if="workOrderRegistryBusy" class="empty-state">Загружаем реестр…</div><div v-else-if="workOrderRegistryError" class="empty-state">{{ workOrderRegistryError }}</div><div v-else class="registry-table"><div class="registry-row registry-head"><span>№</span><span>Автомобиль</span><span>Заказчик</span><span>Дата</span><span>Статус</span><span>Итого</span><span>Документы</span></div><div v-for="item in workOrderRegistry" :key="item.id" class="registry-row"><span>{{ item.orderNumber || `#${item.id}` }}</span><span>{{ item.vehicleName || 'Без автомобиля' }}<small>{{ item.registrationNumber || '—' }}</small></span><span>{{ item.customer || '—' }}</span><span>{{ item.documentDate || '—' }}</span><span>{{ item.status }}</span><strong>{{ Number(item.total || 0).toFixed(2) }}</strong><span>{{ item.invoiceNumber || '—' }} · {{ item.actNumber || '—' }}</span></div><p v-if="!workOrderRegistry.length" class="empty-state">Заказ-нарядов пока нет.</p></div></section></div>
    <div v-if="workCatalogPickerVisible" class="stub-overlay" @click.self="workCatalogPickerVisible = false"><form class="data-modal compact-modal work-catalog-picker-modal" @submit.prevent="confirmAddWorkOrderLine"><button type="button" class="icon-button" aria-label="Закрыть" @click="workCatalogPickerVisible = false">×</button><p class="eyebrow">Работы обращения</p><h2>Добавить работу</h2><p class="modal-subtitle">Выберите работу из справочника. Нормо-часы подставятся автоматически.</p><label class="work-catalog-picker-field"><span>Работа *</span><select v-model="workCatalogPickerId" required><option value="" disabled>Выберите работу</option><option v-for="item in workCatalog" :key="item.id" :value="String(item.id)">{{ item.name }} · {{ item.normHours || 0 }} н/ч</option></select></label><p v-if="!workCatalog.length" class="empty-state">В справочнике пока нет работ.</p><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="workCatalogPickerVisible = false">Отмена</button><button class="button button-primary" type="submit" :disabled="!workCatalog.length">Добавить работу</button></div></form></div>
</template>

<style scoped>
.auth-tabs { display: flex; gap: 4px; margin: 22px 0 4px; padding: 4px; border-radius: 10px; background: var(--soft); }
.auth-tabs button { flex: 1; padding: 9px; border: 0; border-radius: 7px; color: var(--muted); background: transparent; font-size: 12px; font-weight: 750; }
.auth-tabs button.is-active { color: var(--brand); background: white; box-shadow: 0 2px 8px rgb(24 52 47 / 10%); }
.data-modal { position: relative; width: min(720px, 100%); max-height: 90vh; overflow: auto; padding: 34px; border-radius: 18px; background: white; box-shadow: 0 25px 90px rgb(8 43 37 / 27%); }
.registry-modal { width: min(1180px, 100%); }
.registry-actions { display: flex; justify-content: flex-end; margin-bottom: 14px; }
.registry-table { overflow: auto; }
.registry-row { display: grid; grid-template-columns: .55fr 1.5fr 1.4fr .8fr .7fr .9fr 1.2fr; gap: 10px; align-items: center; min-width: 900px; padding: 10px 8px; border-bottom: 1px solid var(--line); font-size: 12px; }
.registry-row small { display: block; margin-top: 3px; color: var(--muted); }
.registry-head { color: var(--muted); font-size: 11px; font-weight: 750; }
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
.pagination-toolbar { display: flex; justify-content: flex-end; align-items: center; gap: 16px; padding: 14px 4px 2px; color: var(--muted); font-size: 12px; }
.pagination-toolbar button:disabled { cursor: not-allowed; opacity: .45; }
.bundle-print-button { position: fixed; right: 24px; bottom: 76px; z-index: 21; }
.directory-modal { width: min(760px, 100%); }
.directory-tabs { display: flex; gap: 6px; margin: 6px 0 20px; border-bottom: 1px solid var(--line); }
.directory-tabs button { padding: 9px 12px; border: 0; border-bottom: 2px solid transparent; color: var(--muted); background: transparent; font-size: 12px; font-weight: 750; }
.directory-tabs button.is-active { color: var(--brand); border-bottom-color: var(--accent); }
.extended-filters { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin-left: auto; }
.extended-filters select { height: 34px; padding: 0 8px; border: 1px solid var(--line); border-radius: 7px; color: var(--ink); background: var(--soft); font-size: 11px; }
.overdue-filter { display: flex; gap: 5px; align-items: center; color: var(--muted); font-size: 11px; white-space: nowrap; }
.settings-overlay { display: none; }
.settings-overlay-v2 { display: none !important; }
.settings-overlay-v3 { position: fixed; inset: 0; z-index: 90; overflow: auto; padding: 36px; background: #eef4f2; }
.settings-screen-v3 { position: relative; width: min(1500px, 100%); min-height: calc(100vh - 72px); margin: 0 auto; padding: 16px 0 60px; }
.settings-screen-v3 h2 { margin: 0; color: var(--ink); font-size: 34px; }.settings-screen-v3 h3 { margin: 0; font-size: 25px; }.settings-screen-v3 .settings-subtitle { margin: 8px 0 28px; }
.settings-tabs-v3 { display: flex; flex-wrap: wrap; gap: 6px; margin: 0 -36px 30px; padding: 8px 36px; border-top: 1px solid var(--line); border-bottom: 1px solid var(--line); background: #f5f9f7; }.settings-tabs-v3 button { padding: 14px 18px; border: 0; border-radius: 12px; color: var(--muted); background: transparent; font-weight: 800; cursor: pointer; }.settings-tabs-v3 button.is-active { color: var(--brand); background: white; box-shadow: 0 3px 14px rgb(20 63 56 / 10%); }
.settings-section-head-v3 { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; margin-bottom: 20px; }.settings-section-head-v3 p { margin: 7px 0 0; color: var(--muted); }.settings-record-list-v3 { display: grid; gap: 10px; }.settings-record-v3 { display: flex; align-items: center; justify-content: space-between; gap: 18px; padding: 17px 19px; border: 1px solid var(--line); border-radius: 13px; background: white; }.settings-record-v3 > div:first-child { display: grid; gap: 6px; min-width: 0; }.settings-record-v3 strong { font-size: 16px; }.settings-record-v3 small { color: var(--muted); }.settings-record-v3 > div:last-child { display: flex; flex-wrap: wrap; justify-content: flex-end; gap: 8px; }.settings-editor-v3 { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; max-width: 900px; padding: 24px; border: 1px solid var(--line); border-radius: 16px; background: white; }.settings-editor-v3 .settings-section-head-v3, .settings-editor-v3 .modal-actions { grid-column: 1 / -1; }.settings-editor-v3 label { display: grid; gap: 7px; }.settings-editor-v3 label span { color: var(--muted); font-size: 12px; font-weight: 750; }.settings-editor-v3 input, .settings-editor-v3 select, .settings-editor-v3 textarea { width: 100%; min-height: 44px; padding: 0 12px; border: 1px solid var(--line); border-radius: 9px; outline: 0; background: var(--soft); }.settings-editor-v3 textarea { padding-top: 10px; }.settings-editor-v3 input:focus, .settings-editor-v3 select:focus, .settings-editor-v3 textarea:focus { border-color: var(--accent); box-shadow: 0 0 0 3px #1cc9b221; }
.settings-screen { position: relative; width: min(1800px, 100%); min-height: calc(100vh - 72px); margin: 0 auto; padding: 16px 0 60px; }
.settings-screen h2 { margin: 0; color: var(--ink); font-size: 34px; }
.settings-subtitle, .settings-section-head p, .settings-card p { color: var(--muted); }
.settings-tabs { display: flex; gap: 6px; margin: 34px -36px 30px; padding: 8px 36px; border-top: 1px solid var(--line); border-bottom: 1px solid var(--line); background: #f5f9f7; }
.settings-tabs button { padding: 14px 18px; border: 0; border-radius: 12px; color: var(--muted); background: transparent; font-weight: 800; }
.settings-tabs button.is-active { color: var(--brand); background: white; box-shadow: 0 3px 14px rgb(20 63 56 / 10%); }
.settings-tabs button:disabled { opacity: .48; cursor: not-allowed; }
.settings-section-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.settings-section-head h3, .settings-card h3 { margin: 0 0 6px; font-size: 23px; }
.settings-directory-grid, .settings-counterparty-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 24px; }
.settings-card { min-width: 0; padding: 24px; border: 1px solid var(--line); border-radius: 18px; background: rgb(255 255 255 / 88%); box-shadow: 0 8px 25px rgb(20 63 56 / 5%); }
.settings-card-head { display: flex; justify-content: space-between; gap: 18px; align-items: flex-start; margin-bottom: 15px; }
.settings-card-head p { margin: 0; font-size: 12px; }
.settings-add, .settings-edit { padding: 10px 14px; border: 0; border-radius: 10px; color: var(--brand); background: var(--soft); font-weight: 800; white-space: nowrap; }
.settings-add-row { display: flex; gap: 8px; margin-bottom: 12px; }
.settings-add-row input, .settings-list-row input, .settings-card label input, .settings-counterparty-form > input { min-width: 0; width: 100%; height: 42px; padding: 0 12px; border: 1px solid var(--line); border-radius: 10px; background: white; }
.settings-add-row button { flex: 0 0 auto; }
.settings-list { display: grid; gap: 8px; max-height: 420px; overflow: auto; }
.settings-list-row { display: flex; gap: 8px; align-items: center; padding: 8px; border: 1px solid var(--line); border-radius: 10px; background: white; }
.settings-list-row > span { flex: 1; font-weight: 700; }
.settings-list-row > strong { color: var(--muted); white-space: nowrap; }
.settings-list-row code { color: var(--muted); }
.settings-delete, .settings-delete-text { flex: 0 0 auto; border: 0; border-radius: 9px; color: #c44b43; background: #fff0ef; font-size: 20px; }
.settings-delete-text { padding: 10px 12px; font-size: 12px; font-weight: 800; }
.settings-two-fields { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.settings-counterparty-form { display: grid; gap: 14px; }
.settings-counterparty-form label { display: grid; gap: 6px; color: var(--muted); font-weight: 750; }
.settings-counterparty-form .button { justify-self: end; margin-top: 12px; }
.counterparty-row { display: flex; justify-content: space-between; gap: 15px; align-items: center; padding: 16px; border: 1px solid var(--line); border-radius: 12px; background: white; }
.counterparty-row strong, .counterparty-row small { display: block; }
.counterparty-row small { margin-top: 4px; color: var(--muted); font-size: 11px; }
.photos-modal { width: min(960px, 100%); }
.photo-upload-button, .new-car-photo-toolbar .button { display: inline-flex; position: relative; align-items: center; gap: 8px; }
.photo-file-input { position: absolute; inset: 0; width: 100%; height: 100%; cursor: pointer; opacity: 0; }
.car-photo-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin-top: 18px; }
.car-photo-card { overflow: hidden; border: 1px solid var(--line); border-radius: 10px; background: var(--soft); }
.car-photo-card img { display: block; width: 100%; height: 150px; object-fit: cover; background: #e7efec; }
.car-photo-card > div { display: flex; justify-content: space-between; gap: 8px; align-items: center; padding: 8px; }
.car-photo-card small { overflow: hidden; color: var(--muted); font-size: 10px; text-overflow: ellipsis; white-space: nowrap; }
.new-car-photo-toolbar { position: fixed; left: 24px; bottom: 24px; z-index: 21; display: flex; align-items: center; gap: 10px; padding: 10px 14px; border: 1px solid var(--line); border-radius: 10px; background: white; box-shadow: 0 8px 30px rgb(8 43 37 / 12%); color: var(--muted); font-size: 11px; }
.photo-form-field small { color: var(--muted); font-size: 10px; font-weight: 400; }
.data-form-grid label:has(input[type='url']) { display: none; }
.form-photo-preview { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
.form-photo-preview > div { position: relative; width: 86px; overflow: hidden; border: 1px solid var(--line); border-radius: 8px; background: var(--soft); }
.form-photo-preview img { display: block; width: 86px; height: 64px; object-fit: cover; }
.form-photo-preview .link-button { display: block; width: 100%; padding: 4px; font-size: 9px; }
.row-chevron { width: 26px; height: 26px; padding: 0; border: 0; border-radius: 7px; color: var(--muted); background: var(--soft); font-size: 20px; line-height: 1; }
.car-title .car-sequence { order: 1; }
.car-title .row-chevron { order: 2; font-size: 0; }
.car-title .row-toggle { order: 3; }
.car-title .row-chevron::before { content: '⌄'; font-size: 19px; }
.car-title .row-chevron[aria-expanded='true']::before { content: '⌃'; }
.case-row > .cell:first-child { padding-left: 44px; }
.case-row-details-button { position: absolute !important; left: 70px !important; top: 50%; z-index: 2; display: grid !important; width: 28px; height: 28px; padding: 0; transform: translateY(-50%); place-items: center; border: 0; border-radius: 8px; color: transparent !important; background: #edf3f1 !important; font-size: 0 !important; line-height: 1; text-indent: -9999px; }
.case-row-details-button::before { content: '⌄'; color: #596b66; font-size: 20px; text-indent: 0; }
.case-row.is-selected .case-row-details-button::before { content: '⌃'; }
.case-row-details-button:hover { color: var(--brand); background: var(--accent-soft); }
.insurance-pill { display: inline-block; padding: 7px 11px; border-radius: 999px; color: #3e4a47; background: #edf1f0; font-size: 11px; font-weight: 750; }
.comment-input, .appointment-input, .shift-select { width: 100%; min-height: 34px; padding: 6px 8px; border: 1px solid transparent; border-radius: 8px; outline: 0; color: inherit; background: transparent; font-size: 11px; }
.comment-input:hover, .appointment-input:hover, .shift-select:hover { border-color: var(--line); background: white; }
.comment-input:focus, .appointment-input:focus, .shift-select:focus { border-color: var(--accent); background: white; box-shadow: 0 0 0 3px rgb(28 201 178 / 10%); }
.delivered-check { display: inline-flex; align-items: center; gap: 8px; min-height: 34px; padding: 0 10px; border: 1px solid var(--line); border-radius: 10px; color: var(--muted); background: white; font-size: 11px; font-weight: 750; white-space: nowrap; }
.delivered-check input { width: 18px; height: 18px; accent-color: var(--green); }
.parts-glance { display: flex; align-items: center; gap: 10px; }
.progress-ring { width: 48px; height: 48px; flex: 0 0 auto; display: grid; place-items: center; border-radius: 50%; background: conic-gradient(var(--green) var(--progress), #dce8e3 0); position: relative; }
.progress-ring::after { content: ''; position: absolute; width: 36px; height: 36px; border-radius: 50%; background: white; }
.progress-ring::before { content: attr(data-label); position: relative; z-index: 1; color: var(--brand); font-size: 11px; font-weight: 800; }
.car-details { padding: 0 21px 16px 69px; }
.details-panel { overflow: hidden; border: 1px solid var(--line); border-radius: 12px; background: #fbfdfc; }
.details-head, .part-row { display: grid; grid-template-columns: 1fr 2fr 1.5fr 1.4fr 1.25fr 30px; gap: 12px; align-items: center; min-width: 900px; }
.details-head { padding: 11px 14px; color: var(--muted); background: #f2f7f5; font-size: 10px; font-weight: 800; letter-spacing: .06em; text-transform: uppercase; }
.part-row { padding: 12px 14px; border-top: 1px solid var(--line); font-size: 12px; }
.part-row select, .part-date-input { width: 100%; height: 34px; padding: 0 8px; border: 1px solid var(--line); border-radius: 7px; color: var(--ink); background: white; font-size: 11px; }
.received-control { display: flex; align-items: center; gap: 8px; color: var(--ink); font-weight: 750; }
.received-control input { width: 21px; height: 21px; accent-color: var(--green); }
.part-name { font-weight: 750; }
.part-row small { display: block; margin-top: 4px; color: var(--muted); font-size: 10px; }
.article { color: var(--muted); font-family: Consolas, monospace; }
.part-delete { border: 0; color: var(--muted); background: transparent; font-size: 18px; }
.details-actions { display: flex; gap: 16px; padding: 13px 14px; border-top: 1px solid var(--line); }
.repair-type-actions { display: flex; gap: 12px; margin-top: 24px; }
.repair-picker-modal { width: min(680px, 94vw); }
.repair-vin-search { display: flex; margin: 18px 0; }
.repair-vehicle-results { display: grid; gap: 8px; max-height: 360px; overflow: auto; }
.repair-vehicle-option { display: grid; gap: 4px; padding: 14px; border: 1px solid var(--line); border-radius: 12px; text-align: left; color: var(--ink); background: #fbfdfc; cursor: pointer; }
.repair-vehicle-option:hover { border-color: var(--accent); background: #effbf8; }
.repair-vehicle-option span, .repair-vehicle-option small { color: var(--muted); font-size: 11px; }
.clients-list-head { min-width: 1160px; grid-template-columns: .7fr 1.5fr 1.6fr 1fr 1.4fr 1.2fr .9fr; }
.client-row { display: grid; grid-template-columns: .7fr 1.5fr 1.6fr 1fr 1.4fr 1.2fr .9fr; align-items: center; min-height: 86px; padding: 14px 21px; }
.case-list-head { min-width: 1320px; grid-template-columns: .55fr 1.5fr 1.35fr 1.2fr 1fr 1.25fr .8fr .9fr; }
.case-list-head::before { content: 'ID записи'; }
.case-row { display: grid; grid-template-columns: .55fr 1.5fr 1.35fr 1.2fr 1fr 1.25fr .8fr .9fr; align-items: center; min-width: 1320px; min-height: 86px; padding: 14px 21px; }
.case-row::before { content: attr(data-record-id); color: var(--muted); font-size: 12px; font-weight: 750; }
.case-row .cell:first-of-type small { display: none; }
.case-row .cell:first-of-type::after { content: attr(data-registration); display: block; margin-top: 5px; color: var(--muted); font-size: 10px; }
.case-parts-progress-cell { display: flex; align-items: center; gap: 7px; }.case-parts-progress-cell > div:last-child { display: grid; gap: 3px; }.case-parts-progress-cell strong { font-size: 12px; }.case-parts-progress-cell small { margin: 0; color: var(--muted); font-size: 10px; }.parts-progress-ring { width: 40px; height: 40px; display: grid; place-items: center; border-radius: 50%; background: conic-gradient(var(--green) var(--parts-progress), #dfe9e5 0); position: relative; font-size: 10px; font-weight: 850; }.parts-progress-ring::after { content: ''; position: absolute; inset: 5px; border-radius: 50%; background: white; }.parts-progress-ring span { position: relative; z-index: 1; }
.case-row.is-selected { background: #e7f7f0; box-shadow: inset 4px 0 0 var(--brand); }
.case-row { cursor: pointer; }.case-row:hover { background: #f0faf6; }.case-status-filters { display: flex; gap: 3px; padding: 4px; overflow-x: auto; border-radius: 10px; background: var(--soft); }.case-status-filters .filter { white-space: nowrap; }
.client-row:hover { background: #f0faf6; }.client-id-cell { display: flex; align-items: center; gap: 14px; }.client-id-cell .client-row-chevron { display: grid; flex: 0 0 28px; width: 28px; height: 28px; margin-left: 2px; place-items: center; border: 0; border-radius: 8px; color: #596b66; background: #edf3f1; font-size: 19px; line-height: 1; cursor: pointer; }.client-id-cell .client-row-chevron:hover { color: var(--brand); background: var(--accent-soft); }
.case-row-details-button { margin-top: 0; cursor: pointer; }
.case-row-details { margin: -1px 0 10px; padding: 16px 20px 20px; border: 1px solid var(--line); border-top: 0; background: #fbfdfc; }
.case-row-details-head { display: flex; justify-content: space-between; gap: 16px; margin-bottom: 12px; color: var(--muted); }
.case-parts-table { overflow: hidden; border: 1px solid var(--line); border-radius: 10px; }
.case-parts-table-head, .case-parts-table-row { display: grid; grid-template-columns: 1.5fr 1fr 1.2fr 1fr 1.25fr .8fr; gap: 12px; align-items: center; padding: 11px 13px; }
.case-parts-table-head { color: var(--muted); background: var(--soft); font-size: 11px; font-weight: 800; text-transform: uppercase; }
.case-parts-table-row { border-top: 1px solid var(--line); }
.case-parts-table-row span:first-child { display: grid; gap: 3px; }
.case-parts-table-row small { color: var(--muted); }
.part-received, .part-waiting { display: inline-block; width: fit-content; padding: 5px 8px; border-radius: 999px; font-size: 11px; }
.part-received { color: #087d59; background: #dff7eb; }
.part-waiting { color: #9a6a00; background: #fff1c7; }
.case-status { display: inline-block; padding: 7px 11px; border-radius: 999px; color: #18744e; background: #e8f8f0; font-size: 11px; font-weight: 800; }
.case-detail-modal { width: min(980px, 94vw); max-height: 84vh; }
.case-detail-tabs { display: flex; gap: 6px; margin: 20px 0; padding: 5px; border-radius: 10px; background: var(--soft); }
.case-detail-tabs button { padding: 10px 14px; border: 0; border-radius: 8px; color: var(--muted); background: transparent; font-weight: 750; }
.case-detail-tabs button.is-active { color: var(--brand); background: white; box-shadow: 0 2px 8px rgb(24 52 47 / 10%); }
.case-detail-contractor { width: min(620px, 100%); display: grid; gap: 20px; padding: 4px 0 10px; }
.case-detail-contractor::before { content: 'Назначение исполнителя'; color: var(--brand); font-size: 17px; font-weight: 850; letter-spacing: -.02em; }
.case-detail-contractor-card { position: relative; display: grid; gap: 8px; padding: 20px 22px; border: 1px solid #cfe0da; border-radius: 16px; background: linear-gradient(135deg, #f4faf7 0%, #eef6f3 100%); box-shadow: 0 8px 22px rgb(21 63 56 / 6%); }
.case-detail-contractor-card::after { content: 'ТЕКУЩИЙ'; position: absolute; top: 18px; right: 20px; padding: 5px 8px; border-radius: 999px; color: var(--green); background: var(--green-soft); font-size: 9px; font-weight: 850; letter-spacing: .08em; }
.case-detail-contractor:not(.has-contractor) .case-detail-contractor-card::after { content: 'НЕ НАЗНАЧЕН'; color: var(--muted); background: #e9efed; }
.case-detail-contractor-card span, .case-detail-contractor label span { color: var(--muted); font-size: 12px; font-weight: 650; }
.case-detail-contractor-card strong { width: fit-content; color: var(--brand-2); font-size: 25px; letter-spacing: -.03em; cursor: pointer; text-decoration: underline; text-decoration-color: #9ecfc4; text-underline-offset: 5px; }
.case-detail-contractor:not(.has-contractor) .case-detail-contractor-card strong { color: var(--muted); }
.case-detail-contractor label { display: grid; gap: 9px; }
.case-detail-contractor select { width: 100%; min-height: 50px; padding: 0 15px; border: 1px solid #cbdad6; border-radius: 12px; outline: 0; color: var(--ink); background: #fff; box-shadow: 0 3px 10px rgb(21 63 56 / 4%); }
.case-detail-contractor select:focus { border-color: var(--accent); box-shadow: 0 0 0 3px rgb(28 201 178 / 15%); }
.case-detail-contractor:not(.is-editing) > label, .case-detail-contractor:not(.is-editing) > .button { display: none; }
.case-detail-contractor > .button { justify-self: start; min-width: 245px; min-height: 46px; }
.case-detail-main { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
.case-detail-main > div, .case-detail-main > label { display: grid; gap: 5px; padding: 14px; border: 1px solid var(--line); border-radius: 10px; background: #fbfdfc; }
.case-detail-main span, .case-detail-actions p { color: var(--muted); font-size: 11px; }
.case-detail-main select { height: 36px; padding: 0 8px; border: 1px solid var(--line); border-radius: 8px; background: white; }
.case-detail-photos { display: grid; gap: 16px; }
.case-detail-parts { display: grid; gap: 14px; }
.case-action-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; margin: 18px 0; padding: 12px; border-radius: 12px; background: var(--soft); }
.case-action-bar .button-cloud { color: var(--brand); border-color: var(--accent); background: var(--accent); }
.case-action-bar .button-cloud:hover { color: var(--brand); background: #32ddc7; }
.case-status-large { margin-right: auto; padding: 9px 13px; border-radius: 999px; color: var(--brand); background: white; font-weight: 850; }
.case-documents-inline { display: none !important; }
.case-live-summary { display: none; }
.case-work-detail-dock { display: none; }
.case-readiness { display: flex; align-items: center; gap: 8px; padding-bottom: 10px; font-size: 15px; }
.case-readiness-warning { color: #986b16; }.case-readiness-success { color: #19815d; }.case-readiness-info { color: #27789b; }.case-readiness-neutral { color: var(--muted); }
.case-live-metrics { display: grid; grid-template-columns: repeat(2, 1fr); gap: 7px; color: var(--muted); font-size: 12px; }.case-live-metrics span { padding: 8px; border-radius: 8px; background: var(--soft); }.case-live-metrics b { display: block; color: var(--ink); font-size: 14px; }
.case-remaining { display: grid; gap: 5px; margin-top: 10px; padding-top: 10px; border-top: 1px solid var(--line); color: var(--muted); font-size: 12px; }.case-remaining strong { color: var(--ink); font-size: 13px; }
.case-work-detail-dock { position: fixed; left: 34px; right: 420px; bottom: 24px; z-index: 118; display: grid; gap: 8px; padding: 14px; border: 1px solid var(--line); border-radius: 14px; background: rgb(255 255 255 / 97%); box-shadow: 0 12px 32px rgb(8 43 37 / 16%); }.case-work-detail-row { display: grid; grid-template-columns: minmax(120px, .8fr) minmax(150px, .7fr) minmax(180px, 1fr); gap: 8px; align-items: center; }.case-work-detail-row input,.case-work-detail-row select { min-width: 0; padding: 8px 10px; border: 1px solid var(--line); border-radius: 8px; background: white; }
.insurance-case-create-modal .data-form-grid > label:nth-child(3) { display: none; }
.generated-documents-list { display: grid; gap: 8px; margin: 20px 0; }
.generated-document-row { display: flex; justify-content: space-between; gap: 16px; padding: 14px 16px; border: 1px solid var(--line); border-radius: 12px; background: var(--soft); }
.part-form-modal .data-form-grid > label:nth-child(3), .part-form-modal .data-form-grid > label:nth-child(4) { display: none; }
.car-view-modal { width: min(900px, 94vw); }.car-view-tabs { display: flex; gap: 6px; margin: 20px 0; padding: 5px; border-radius: 10px; background: var(--soft); }.car-view-tabs button { padding: 11px 15px; border: 0; border-radius: 8px; color: var(--muted); background: transparent; font-weight: 750; }.car-view-tabs button.is-active { color: var(--brand); background: white; box-shadow: 0 2px 8px rgb(24 52 47 / 10%); }.car-view-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }.car-view-grid > div { display: grid; gap: 5px; padding: 14px; border: 1px solid var(--line); border-radius: 10px; background: #fbfdfc; }.car-view-grid span, .car-view-case-row small { color: var(--muted); font-size: 11px; }.car-view-cases { display: grid; gap: 9px; }.car-view-case-row { display: flex; justify-content: space-between; gap: 16px; width: 100%; padding: 14px 16px; border: 1px solid var(--line); border-radius: 10px; color: var(--ink); background: #fbfdfc; text-align: left; cursor: pointer; }.car-view-case-row:hover { border-color: var(--accent); background: var(--accent-soft); }.car-view-case-row > span { display: grid; gap: 5px; }.car-view-case-row > span:last-child { text-align: right; }
.case-work-detail-dock { display: none !important; }
.case-receipt-panel { margin: 14px 0 0; padding: 18px 20px 22px; border: 1px solid var(--line); border-radius: 14px; background: #fbfdfc; }
.case-receipt-panel, .case-receipt-inline-list { display: none !important; }
.case-receipt-inline-list { display: none !important; gap: 8px; margin-top: 14px; padding-top: 14px; border-top: 1px solid var(--line); }.case-receipt-inline-row { display: flex; justify-content: space-between; align-items: center; gap: 12px; padding: 10px 12px; border-radius: 9px; background: var(--soft); }.case-receipt-inline-row span { display: grid; gap: 4px; }.case-receipt-inline-row small { color: var(--muted); font-size: 11px; }
.case-part-status-cell { display: grid; justify-items: start; gap: 4px; }.case-part-status-cell .part-receipt-button { padding: 4px 7px; min-height: 26px; font-size: 10px; }
.case-part-status-actions { display: flex; align-items: center; gap: 8px; white-space: nowrap; }.case-part-status-actions .part-receipt-button { margin: 0; }.case-received-date { color: var(--muted); font-size: 12px; }
.case-receipt-head { display: flex; justify-content: space-between; gap: 20px; align-items: flex-start; margin-bottom: 14px; }.case-receipt-head div { display: grid; gap: 6px; }.case-receipt-head span { width: fit-content; padding: 5px 9px; border-radius: 999px; color: var(--brand); background: var(--accent-soft); font-size: 11px; font-weight: 800; }.case-receipt-head small { color: var(--muted); }
.case-receipt-list { display: grid; gap: 8px; }.case-receipt-row { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 12px 14px; border: 1px solid var(--line); border-radius: 10px; background: white; }.case-receipt-row > div { display: grid; gap: 4px; }.case-receipt-row small { color: var(--muted); font-size: 11px; }.part-received-date { color: #16815c !important; font-weight: 750; }.part-receipt-button { min-height: 36px; padding: 0 12px; border: 1px solid var(--accent); border-radius: 8px; color: var(--brand); background: var(--accent-soft); font-size: 12px; font-weight: 800; }.part-receipt-button.is-received { border-color: var(--line); color: var(--muted); background: var(--soft); }
.case-inline-work-order { display: grid; align-content: start; gap: 12px; min-height: 0; }
.case-work-line { display: grid; grid-template-columns: minmax(0, 1fr) 130px auto; gap: 12px; align-items: center; padding: 15px 16px; border: 1px solid var(--line); border-radius: 12px; background: #fbfdfc; }
.case-work-line input, .case-work-line select { min-height: 40px; padding: 0 10px; border: 1px solid var(--line); border-radius: 9px; background: white; }
.case-inline-work-order > .button-primary { width: fit-content; }
.case-inline-work-order > .button-primary { display: none; }
.case-inline-work-order .empty-state { padding: 24px 12px; border: 1px dashed var(--line); border-radius: 11px; background: var(--soft); }
.case-work-line input { min-width: 0; }
.work-catalog-picker-modal { width: min(560px, 94vw); }
.work-catalog-picker-field { display: grid; gap: 8px; color: var(--ink); font-size: 12px; font-weight: 750; }
.work-catalog-picker-field select { width: 100%; min-height: 44px; padding: 0 12px; border: 1px solid var(--line); border-radius: 9px; background: var(--soft); }
.case-parts-list { display: grid; gap: 8px; }
.case-part-item { display: flex; align-items: center; justify-content: space-between; gap: 14px; padding: 13px 15px; border: 1px solid var(--line); border-radius: 11px; background: #fbfdfc; }
.case-part-item > div:first-child { display: grid; gap: 4px; }
.case-part-item small { color: var(--muted); }
.case-part-item > div:last-child { display: flex; gap: 10px; flex-shrink: 0; }
.case-photo-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
.case-photo-card { overflow: hidden; border: 1px solid var(--line); border-radius: 12px; background: #fbfdfc; }
.case-photo-card img { display: block; width: 100%; aspect-ratio: 4 / 3; object-fit: cover; }
.case-photo-card > div { display: flex; align-items: center; justify-content: space-between; gap: 8px; padding: 8px; }
.case-photo-card small { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.case-photo-upload { display: inline-flex; width: fit-content; align-items: center; gap: 10px; padding: 11px 15px; border-radius: 10px; color: var(--brand); background: var(--soft); font-weight: 750; cursor: pointer; }
.case-photo-upload input { display: none; }
.case-history-list { display: grid; gap: 8px; }
.case-history-list div { display: flex; gap: 16px; padding: 12px; border-bottom: 1px solid var(--line); }
.case-history-list strong { min-width: 150px; color: var(--muted); font-size: 11px; }
.compact-modal label:has(input[placeholder="ФИО получателя"]) { display: none; }
.client-vin { color: var(--muted); font-family: Consolas, monospace; font-size: 11px; }
.insurance-case-create-modal .form-photo-preview { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 12px; }
.insurance-case-create-modal .form-photo-preview div { display: grid; gap: 4px; width: 92px; color: var(--muted); font-size: 10px; }
.insurance-case-create-modal .form-photo-preview img { width: 92px; height: 72px; object-fit: cover; border-radius: 8px; border: 1px solid var(--line); }
.schedule-work-list { display: grid; gap: 8px; margin: 6px 0 16px; }
.schedule-repair-overlay { z-index: 30; }
.schedule-work-row { display: grid; grid-template-columns: minmax(0, 1fr) minmax(220px, .8fr); align-items: center; gap: 14px; padding: 11px 12px; border: 1px solid var(--line); border-radius: 10px; background: var(--soft); }
.schedule-work-row strong, .schedule-work-row small { display: block; }
.schedule-work-row small { margin-top: 4px; color: var(--muted); font-size: 11px; }
.case-row .cell:nth-child(4) { cursor: pointer; }
.case-row .cell:nth-child(4):hover { color: var(--brand-2); text-decoration: underline; text-underline-offset: 3px; }
.inline-contractor-button { max-width: 100%; padding: 0; border: 0; color: var(--brand-2); background: transparent; font-size: 12px; text-align: left; text-decoration: underline; text-underline-offset: 3px; }
.inline-contractor-button:disabled { color: var(--muted); cursor: default; text-decoration: none; }
.inline-contractor-select { width: 100%; min-height: 34px; padding: 0 8px; border: 1px solid var(--accent); border-radius: 8px; color: var(--ink); background: var(--paper); font-size: 12px; }
.inline-appointment-date-cell { display: grid; align-items: center; }
.inline-appointment-date-input { width: 142px; max-width: 100%; min-height: 34px; padding: 0 7px; border: 1px solid var(--line); border-radius: 8px; color: var(--ink); background: var(--soft); font-size: 11px; }
.inline-appointment-date-input:focus { border-color: var(--accent); outline: 0; box-shadow: 0 0 0 3px #1cc9b221; }
.inline-appointment-date-input:disabled { color: var(--muted); background: #eef3f1; cursor: not-allowed; }
.inline-appointment-date-hint { display: inline-flex; width: 142px; min-height: 34px; align-items: center; padding: 0 7px; border: 1px dashed var(--line); border-radius: 8px; color: var(--muted); background: var(--soft); font-size: 11px; cursor: help; }
.field-hint { display: block; margin-top: 6px; color: var(--muted); font-size: 11px; font-weight: 500; }
.field-hint-error { color: var(--red); }
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
