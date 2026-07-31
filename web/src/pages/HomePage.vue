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
const insurerFilter = ref('')
const shiftFilter = ref('')
const contractorFilter = ref('')
const overduePartsOnly = ref(false)
const search = ref('')
const cars = ref([])
const insurers = ref([])
const suppliers = ref([])
const shifts = ref([])
const workCatalog = ref([])
const counterparties = ref([])
const vehicleAliases = ref([])
const contractors = ref([])
const vehicleMakes = ref([])
const vehicleModels = ref([])
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
const repairCasesBusy = ref(false)
const repairCaseCar = ref(null)
const editingRepairCase = ref(null)
const repairCaseForm = ref({ caseNumber: '', status: 'OPEN', insuredPerson: '', claimNumber: '', insurerId: '', contractorId: '', shiftId: '', acceptedAt: '' })
const modal = ref(null)
const toast = ref('')
const carFormVisible = ref(false)
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
    vehicleMake: '', vehicleModel: '', registrationNumber: '', vin: '', ownerName: '', ownerPhone: '',
    comment: '', documentFolderUrl: '',
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
  start: car.createdAt ? car.createdAt.slice(0, 10) : '—',
  parts: car.parts,
  partsSummary: car.parts.length ? `${car.parts.filter((part) => part.received).length} / ${car.parts.length}` : '—',
  comment: car.comment || '—',
  record: car.createdAt ? car.createdAt.slice(0, 10) : '—',
  shift: shifts.value.find((item) => item.id === car.shiftId)?.name || (car.shiftId ? `Смена #${car.shiftId}` : '—'),
  status: car.status.toLowerCase(),
})))

const visibleCars = computed(() => mappedCars.value.filter((car) => {
  const matchesFilter = activeFilter.value === 'active'
    ? car.status !== 'delivered'
    : activeFilter.value === car.status
  const matchesInsurer = !insurerFilter.value || String(car.insurerId || '') === insurerFilter.value
  const matchesShift = !shiftFilter.value || String(car.shiftId || '') === shiftFilter.value
  const matchesContractor = !contractorFilter.value || String(car.contractorId || '') === contractorFilter.value
  const matchesOverdue = !overduePartsOnly.value || car.parts?.some((part) => !part.received && part.expectedDate && part.expectedDate < new Date().toISOString().slice(0, 10))
  const query = search.value.trim().toLowerCase()
  return matchesFilter && matchesInsurer && matchesShift && matchesContractor && matchesOverdue && (!query || Object.values(car).some((value) => String(value).toLowerCase().includes(query)))
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
    if (search.value.trim()) {
      const result = await requestJson(`/api/v1/cars/search/page?q=${encodeURIComponent(search.value.trim())}&page=${carPage.value}&size=${carPageSize.value}`)
      cars.value = result.items
      carTotalPages.value = result.totalPages
      carTotalItems.value = result.totalItems
    } else {
      cars.value = await requestJson('/api/v1/cars')
      carPage.value = 0
      carTotalPages.value = 0
      carTotalItems.value = cars.value.length
    }
  } catch (error) {
    carsError.value = error.message
  } finally {
    carsBusy.value = false
  }
}

function changeCarPage(page) {
  carPage.value = Math.max(0, Math.min(page, Math.max(0, carTotalPages.value - 1)))
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
    contractors.value = contractorItems
    vehicleMakes.value = makeItems
  } catch (error) {
    showToast(`Справочники не загружены: ${error.message}`)
  }
}

async function loadVehicleModels() {
  const make = vehicleMakes.value.find((item) => item.name === carForm.value.vehicleMake)
  vehicleModels.value = make ? await requestJson(`/api/v1/directories/vehicle-catalog/models?makeId=${make.id}`).catch(() => []) : []
}

async function openWorkOrderRegistry() {
  workOrderRegistryVisible.value = true
  workOrderRegistryBusy.value = true
  workOrderRegistryError.value = ''
  try { workOrderRegistry.value = await requestJson('/api/v1/work-orders') } catch (error) { workOrderRegistryError.value = error.message } finally { workOrderRegistryBusy.value = false }
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
  carPhotos.value = []
  photosCar.value = null
  carFormVisible.value = true
}

async function openCarPhotos(car) {
  photosCar.value = car; photosCase.value = null
  photosVisible.value = true
  photosBusy.value = true
  try { carPhotos.value = await requestJson(`/api/v1/cars/${car.id}/photos`) } catch (error) { showToast(error.message) } finally { photosBusy.value = false }
}

async function openRepairCasePhotos(caseItem) {
  photosCar.value = repairCaseCar.value; photosCase.value = caseItem; photosVisible.value = true; photosBusy.value = true
  try { carPhotos.value = await requestJson(`/api/v1/cars/${repairCaseCar.value.id}/repair-cases/${caseItem.id}/photos`) } catch (error) { showToast(error.message) } finally { photosBusy.value = false }
}

async function openRepairCases(car) {
  repairCaseCar.value = car; repairCasesVisible.value = true; repairCasesBusy.value = true
  try { repairCases.value = await requestJson(`/api/v1/cars/${car.id}/repair-cases`) } catch (error) { showToast(error.message) } finally { repairCasesBusy.value = false }
}

function startRepairCase(caseItem = null) {
  editingRepairCase.value = caseItem
  repairCaseForm.value = caseItem ? { caseNumber: caseItem.caseNumber, status: caseItem.status, insuredPerson: caseItem.insuredPerson || '', claimNumber: caseItem.claimNumber || '', insurerId: caseItem.insurerId ? String(caseItem.insurerId) : '', contractorId: caseItem.contractorId ? String(caseItem.contractorId) : '', shiftId: caseItem.shiftId ? String(caseItem.shiftId) : '', acceptedAt: caseItem.acceptedAt || '' } : { caseNumber: String(repairCases.value.length + 1), status: 'OPEN', insuredPerson: '', claimNumber: '', insurerId: '', contractorId: '', shiftId: '', acceptedAt: '' }
}

async function saveRepairCase() {
  try { const path = editingRepairCase.value ? `/api/v1/cars/${repairCaseCar.value.id}/repair-cases/${editingRepairCase.value.id}` : `/api/v1/cars/${repairCaseCar.value.id}/repair-cases`; await requestJson(path, { method: editingRepairCase.value ? 'PUT' : 'POST', body: JSON.stringify({ ...repairCaseForm.value, insurerId: repairCaseForm.value.insurerId ? Number(repairCaseForm.value.insurerId) : null, contractorId: repairCaseForm.value.contractorId ? Number(repairCaseForm.value.contractorId) : null, shiftId: repairCaseForm.value.shiftId ? Number(repairCaseForm.value.shiftId) : null }) }); repairCases.value = await requestJson(`/api/v1/cars/${repairCaseCar.value.id}/repair-cases`); editingRepairCase.value = null; showToast('Страховой случай сохранён') } catch (error) { showToast(error.message) }
}

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
      if (photosVisible.value && photosCar.value?.id) {
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
  modal.value = null
  directoryForm.value = { name: '', code: '', categoryName: '', defaultUnit: 'н/ч', inn: '', address: '', phone: '', note: '' }
  directoriesVisible.value = true
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
  if (!settingsNewMaster.value.shortName.trim() || !settingsNewMaster.value.code.trim()) return
  try { await requestJson('/api/v1/contractors', { method: 'POST', body: JSON.stringify({ code: settingsNewMaster.value.code.trim(), shortName: settingsNewMaster.value.shortName.trim(), fullName: settingsNewMaster.value.shortName.trim() }) }); await loadDirectories(); settingsNewMaster.value = { code: '', shortName: '' }; showToast('Мастер добавлен') } catch (error) { showToast(error.message) }
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
  await loadVehicleModels()
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

async function saveCar() {
  try {
    const path = editingCar.value ? `/api/v1/cars/${editingCar.value.id}` : '/api/v1/cars'
    const payload = {
      ...carForm.value,
    }
    const savedCar = await requestJson(path, {
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

function toggleCarDetails(car) {
  const next = new Set(expandedCars.value)
  if (next.has(car.id)) next.delete(car.id); else next.add(car.id)
  expandedCars.value = next
}

onMounted(() => {
  if (token.value) {
    loadCars()
    loadDirectories()
  }
})

watch(search, () => { carPage.value = 0; window.clearTimeout(window.__efgenSearchTimer); window.__efgenSearchTimer = window.setTimeout(loadCars, 250) })
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
        <button class="button button-cloud" @click="openWorkOrderRegistry">Заказ-наряды</button>
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
          <article v-for="car in visibleCars" :key="car.number" class="car-row" :class="[`is-${car.status}`, { 'is-open': expandedCars.has(car.id) }]">
            <div class="car-summary">
              <div class="cell car-identity"><div class="car-title"><button class="row-chevron" type="button" :aria-expanded="expandedCars.has(car.id)" @click="toggleCarDetails(car)">{{ expandedCars.has(car.id) ? '⌄' : '›' }}</button><b class="car-sequence">{{ car.number }}</b><button class="row-toggle" @click="openCarEdit(car)"><strong>{{ car.vehicle }}</strong><small>{{ car.registration }} · VIN {{ car.vin }}</small><small>{{ car.status === 'delivered' ? 'Выдан' : car.status === 'ready' ? 'Всё поступило' : car.status === 'waiting' ? 'Ожидаются детали' : 'В работе' }}</small></button></div></div>
              <div class="cell"><span class="insurance-pill">{{ car.insurer }}</span><small>Начало: {{ car.start }}</small></div>
              <div class="cell parts-glance"><span class="progress-ring" :style="{ '--progress': `${car.parts.length ? Math.round((car.parts.filter((part) => part.received).length / car.parts.length) * 100) : 0}%` }" :data-label="`${car.parts.filter((part) => part.received).length}/${car.parts.length}`"></span><span><strong>{{ car.parts.length ? `${car.parts.filter((part) => part.received).length} из ${car.parts.length} поступили` : 'Нет деталей' }}</strong><small>{{ car.parts.some((part) => !part.received && part.expectedDate && part.expectedDate < new Date().toISOString().slice(0, 10)) ? 'Есть просроченные детали' : 'Поступление по графику' }}</small></span></div>
              <div class="cell"><input class="comment-input" type="text" :value="car.comment === '—' ? '' : car.comment" placeholder="Комментарий..." @change="updateCarInline(car, 'comment', $event.target.value)" /></div>
              <div class="cell"><button class="link-button" @click="openCarPhotos(car)">Документы / фото</button><a v-if="car.documentFolderUrl" class="link-button" :href="car.documentFolderUrl" target="_blank" rel="noreferrer">Открыть папку</a></div>
              <div class="cell muted-cell">{{ car.record }}</div><div class="cell"><select class="shift-select" :value="car.shiftId || ''" @change="updateCarInline(car, 'shiftId', $event.target.value)"><option value="">Не назначена</option><option v-for="item in shifts" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select></div>
              <div class="cell"><label class="delivered-check"><input type="checkbox" :checked="car.status === 'delivered'" @change="toggleDelivered(car)" /><span>Выдан</span></label><small v-if="car.deliveredAt">{{ car.deliveredAt }}</small></div>
            </div>
            <div v-if="expandedCars.has(car.id)" class="car-details"><div class="details-panel"><div v-if="car.parts.length" class="details-head"><span>Поступление</span><span>Деталь</span><span>Артикул</span><span>Поставщик</span><span>Дата поступления</span><span></span></div><div v-for="part in car.parts" :key="part.id" class="part-row" :class="{ 'is-received': part.received }"><label class="received-control"><input type="checkbox" :checked="part.received" @change="togglePartReceived(car, part)" /><span>{{ part.received ? 'Поступила' : 'Ожидается' }}</span></label><div><div class="part-name">{{ part.name }}</div><small v-if="part.receivedAt">Фактически: {{ part.receivedAt }}</small></div><span class="article">{{ part.article || '—' }}</span><select :value="part.supplierId || ''" @change="updatePartSupplier(car, part, $event.target.value)"><option value="">Не указан</option><option v-for="item in suppliers" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select><input class="part-date-input" type="date" :value="part.expectedDate || ''" @change="updatePartExpectedDate(car, part, $event.target.value)" /><button class="part-delete" type="button" title="Удалить деталь" @click="deletePart(car, part)">×</button></div><div class="details-actions"><button class="link-button" type="button" @click="openPartForm(car)">＋ Добавить деталь</button><button class="link-button" type="button" @click="openStub('Дефектовка')">Дефектовка</button><button class="link-button" type="button" @click="openWorkOrder(car)">ЗН+Счёт</button><button class="link-button" type="button" @click="toggleAccepted(car)">{{ car.acceptedAt ? 'Отменить приёмку' : 'Принять автомобиль' }}</button></div></div></div>
          </article>
          <div v-if="!carsBusy && !carsError && !visibleCars.length" class="empty-state">По выбранному фильтру автомобили не найдены.</div>
        </div>
        <div v-if="search.trim() && carTotalPages > 1" class="pagination-toolbar" aria-label="Пагинация поиска">
          <span>Найдено: {{ carTotalItems }}</span>
          <button type="button" class="link-button" :disabled="carPage === 0" @click="changeCarPage(carPage - 1)">← Назад</button>
          <strong>Страница {{ carPage + 1 }} из {{ carTotalPages }}</strong>
          <button type="button" class="link-button" :disabled="carPage >= carTotalPages - 1" @click="changeCarPage(carPage + 1)">Вперёд →</button>
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

    <div v-if="workOrderVisible && workOrder" class="document-toolbar"><span>Номера документов:</span><input v-model="workOrder.orderNumber" placeholder="Заказ-наряд №" /><input v-model="workOrder.invoiceNumber" placeholder="Счёт №" /><input v-model="workOrder.actNumber" placeholder="Акт №" /><span>Печать:</span><button type="button" class="link-button" @click="printDocument('order')">Заказ-наряд</button><button type="button" class="link-button" @click="printDocument('invoice')">Счёт</button><button type="button" class="link-button" @click="printDocument('act')">Акт</button></div>

    <div v-if="defectVisible" class="stub-overlay" @click.self="defectVisible = false"><section class="data-modal defect-modal"><button class="icon-button" aria-label="Закрыть" @click="defectVisible = false">×</button><p class="eyebrow">Осмотр автомобиля</p><h2>Дефектовка</h2><p class="modal-subtitle">{{ selectedDefectCar?.number }} · {{ selectedDefectCar?.vehicle }} · {{ selectedDefectCar?.registration }}</p><div v-if="defectBusy" class="empty-state">Загружаем дефектовку…</div><div v-else-if="defectError" class="empty-state">{{ defectError }}</div><form v-else class="data-form-grid" @submit.prevent="saveDefect"><label><span>Статус</span><select v-model="defect.status"><option value="DRAFT">Черновик</option><option value="CONFIRMED">Подтверждено</option></select></label><label class="form-wide"><span>Повреждения и замечания</span><textarea v-model="defect.findings" rows="5" placeholder="Передний бампер, левая дверь…"></textarea></label><label class="form-wide"><span>Рекомендованные работы и запчасти</span><textarea v-model="defect.recommendations" rows="5" placeholder="Замена бампера, окраска двери…"></textarea></label><label class="form-wide"><span>Фотографии осмотра (до 8)</span><input type="file" accept="image/*" multiple @change="readDefectPhotos" /></label><div v-if="defect.photos.length" class="defect-photo-grid form-wide"><img v-for="(photo, index) in defect.photos" :key="`${photo.slice(0, 24)}-${index}`" :src="photo" alt="Фото повреждения" /></div><div class="modal-actions form-wide"><button type="button" class="button button-cloud dark-button" @click="defectVisible = false">Отмена</button><button class="button button-primary" type="submit">Сохранить дефектовку</button></div></form></section></div>
    <div v-if="contractorVisible" class="stub-overlay" @click.self="contractorVisible = false"><form class="data-modal contractor-modal" @submit.prevent="saveContractor"><button type="button" class="icon-button" aria-label="Закрыть" @click="contractorVisible = false">×</button><p class="eyebrow">Реквизиты</p><h2>{{ editingContractor ? 'Изменить исполнителя' : 'Новый исполнитель' }}</h2><div class="data-form-grid"><label><span>Код</span><input v-model="contractorForm.code" required /></label><label><span>Краткое название</span><input v-model="contractorForm.shortName" required /></label><label class="form-wide"><span>Полное название</span><input v-model="contractorForm.fullName" required /></label><label><span>Подписант</span><input v-model="contractorForm.signerName" /></label><label><span>ИНН</span><input v-model="contractorForm.inn" /></label><label><span>ОГРНИП</span><input v-model="contractorForm.ogrnip" /></label><label class="form-wide"><span>Адрес</span><input v-model="contractorForm.address" /></label><label class="form-wide"><span>Банк</span><input v-model="contractorForm.bankName" /></label><label><span>БИК</span><input v-model="contractorForm.bik" /></label><label><span>Расчётный счёт</span><input v-model="contractorForm.settlementAccount" /></label></div><div class="contractor-list"><div v-for="item in contractors" :key="item.id" class="directory-item"><span>{{ item.shortName }}<small>{{ item.code }} · {{ item.inn || 'ИНН не указан' }}</small></span><button type="button" class="link-button" @click="openContractorForm(item)">Изменить</button><button type="button" class="link-button danger-link" @click="deleteContractor(item)">Удалить</button></div></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="contractorVisible = false">Закрыть</button><button class="button button-primary" type="submit">Сохранить</button></div></form></div>

    <div v-if="carFormVisible" class="stub-overlay" @click.self="carFormVisible = false"><form class="data-modal" @submit.prevent="saveCar"><button type="button" class="icon-button" aria-label="Закрыть" @click="carFormVisible = false">×</button><p class="eyebrow">Карточка автомобиля</p><h2>{{ editingCar ? `Автомобиль №${editingCar.accountingNumber}` : 'Новый автомобиль' }}</h2><div class="data-form-grid"><label><span>Марка *</span><input v-model="carForm.vehicleMake" list="vehicle-makes" required placeholder="Начните вводить марку" @change="loadVehicleModels" /></label><label><span>Модель *</span><input v-model="carForm.vehicleModel" list="vehicle-models" required placeholder="Начните вводить модель" /></label><datalist id="vehicle-makes"><option v-for="item in vehicleMakes" :key="item.id" :value="item.name" /></datalist><datalist id="vehicle-models"><option v-for="item in vehicleModels" :key="item.id" :value="item.name" /></datalist><label><span>Госномер *</span><input v-model="carForm.registrationNumber" required placeholder="А123ВС124" /></label><label><span>VIN *</span><input v-model="carForm.vin" required placeholder="VIN автомобиля" /></label><label><span>ФИО владельца *</span><input v-model="carForm.ownerName" required placeholder="ФИО владельца" /></label><label><span>Телефон владельца *</span><input v-model="carForm.ownerPhone" required placeholder="+7 999 000-00-00" /></label><label class="form-wide"><span>Комментарий</span><textarea v-model="carForm.comment" rows="3" placeholder="Необязательный комментарий"></textarea></label></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="carFormVisible = false">Отмена</button><button class="button button-primary" type="submit">{{ editingCar ? 'Сохранить изменения' : 'Сохранить автомобиль' }}</button></div></form></div>

    <div v-if="partFormVisible" class="stub-overlay" @click.self="partFormVisible = false"><form class="data-modal compact-modal" @submit.prevent="savePart"><button type="button" class="icon-button" aria-label="Закрыть" @click="partFormVisible = false">×</button><p class="eyebrow">Заказ запчасти</p><h2>{{ editingPart ? 'Изменить деталь' : 'Добавить деталь' }}</h2><p class="modal-subtitle">{{ selectedCar?.number }} · {{ selectedCar?.vehicle }}</p><div class="data-form-grid"><label><span>Деталь</span><input v-model="partForm.name" required placeholder="Бампер передний" /></label><label><span>Артикул</span><input v-model="partForm.article" placeholder="604A124500" /></label><label><span>Поставщик</span><select v-model="partForm.supplierId"><option value="">Не выбран</option><option v-for="item in suppliers" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select></label><label><span>Ожидаемая дата</span><input v-model="partForm.expectedDate" type="date" /></label></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="partFormVisible = false">Отмена</button><button class="button button-primary" type="submit">{{ editingPart ? 'Сохранить изменения' : 'Добавить деталь' }}</button></div></form></div>

    <div v-if="directoriesVisible" class="stub-overlay" @click.self="directoriesVisible = false"><section class="data-modal directory-modal"><button class="icon-button" aria-label="Закрыть" @click="directoriesVisible = false">×</button><p class="eyebrow">Настройки</p><h2>Справочники</h2><div class="directory-tabs"><button type="button" :class="{ 'is-active': directoryType === 'insurers' }" @click="directoryType = 'insurers'">Страховые</button><button type="button" :class="{ 'is-active': directoryType === 'suppliers' }" @click="directoryType = 'suppliers'">Поставщики</button><button type="button" :class="{ 'is-active': directoryType === 'shifts' }" @click="directoryType = 'shifts'">Смены</button><button type="button" :class="{ 'is-active': directoryType === 'works' }" @click="directoryType = 'works'">Работы</button><button type="button" :class="{ 'is-active': directoryType === 'counterparties' }" @click="directoryType = 'counterparties'">Контрагенты</button></div><form class="data-form-grid" @submit.prevent="createDirectoryItem"><label v-if="directoryType === 'works'"><span>Код работы</span><input v-model="directoryForm.code" required placeholder="BODY-001" /></label><label><span>{{ directoryType === 'works' ? 'Название работы' : 'Название' }}</span><input v-model="directoryForm.name" required placeholder="Название элемента" /></label><label v-if="directoryType === 'works'"><span>Категория</span><input v-model="directoryForm.categoryName" required placeholder="Кузовные работы" /></label><label v-if="directoryType === 'works'"><span>Единица</span><input v-model="directoryForm.defaultUnit" required placeholder="н/ч" /></label><label v-if="directoryType === 'counterparties'"><span>ИНН</span><input v-model="directoryForm.inn" placeholder="ИНН" /></label><label v-if="directoryType === 'counterparties'"><span>Телефон</span><input v-model="directoryForm.phone" placeholder="+7..." /></label><label v-if="directoryType === 'counterparties'" class="form-wide"><span>Адрес</span><input v-model="directoryForm.address" placeholder="Адрес" /></label><label v-if="directoryType === 'counterparties'" class="form-wide"><span>Примечание</span><textarea v-model="directoryForm.note" rows="2"></textarea></label><div class="modal-actions form-wide"><button class="button button-primary" type="submit">{{ editingDirectory ? 'Сохранить изменения' : 'Добавить в справочник' }}</button></div></form><div class="directory-list"><div v-for="item in (directoryType === 'insurers' ? insurers : directoryType === 'suppliers' ? suppliers : directoryType === 'shifts' ? shifts : directoryType === 'counterparties' ? counterparties : workCatalog)" :key="item.id" class="directory-item"><span>{{ item.name }}<small v-if="directoryType === 'works'">{{ item.categoryName }} · {{ item.defaultUnit }}</small><small v-if="directoryType === 'counterparties'">{{ item.inn }} · {{ item.phone }}</small></span><code v-if="directoryType === 'works'">{{ item.code }}</code><button type="button" class="link-button" @click="openDirectoryEdit(item)">Изменить</button><button type="button" class="link-button danger-link" @click="deleteDirectoryItem(item)">Удалить</button></div><p v-if="!(directoryType === 'insurers' ? insurers : directoryType === 'suppliers' ? suppliers : directoryType === 'shifts' ? shifts : directoryType === 'counterparties' ? counterparties : workCatalog).length" class="empty-state">Справочник пока пуст.</p></div></section></div>

    <div v-if="workOrderVisible" class="stub-overlay" @click.self="workOrderVisible = false"><section class="data-modal work-order-modal print-target"><button class="icon-button" aria-label="Закрыть" @click="workOrderVisible = false">×</button><p class="eyebrow">Рабочие данные</p><h2>Заказ-наряд · №{{ selectedWorkOrderCar?.number }}</h2><p class="modal-subtitle">{{ selectedWorkOrderCar?.vehicle }} · {{ selectedWorkOrderCar?.registration }}</p><div v-if="workOrderBusy" class="empty-state">Загружаем заказ-наряд…</div><div v-else-if="workOrderError" class="empty-state">{{ workOrderError }}</div><template v-else-if="workOrder"><div class="data-form-grid work-order-meta"><label><span>Дата документа</span><input v-model="workOrder.documentDate" type="date" /></label><label><span>Заказчик из справочника</span><select v-model="workOrder.customer"><option value="">Произвольный заказчик</option><option v-for="item in counterparties" :key="item.id" :value="item.name">{{ item.name }} · {{ item.inn || "без ИНН" }}</option></select></label><label><span>Заказчик</span><input v-model="workOrder.customer" placeholder="ФИО или организация" /></label></div><div class="work-order-lines"><div class="work-order-line work-order-line-head"><span>Категория</span><span>Работа</span><span>Ед.</span><span>Кол-во</span><span>Цена</span><span>Сумма</span><span></span></div><div v-for="(line, index) in workOrder.lines" :key="line.id || `new-${index}`" class="work-order-line"><select v-model="line.catalogId" @change="applyCatalogLine(line)"><option value="">Своя работа</option><option v-for="item in workCatalog" :key="item.id" :value="String(item.id)">{{ item.categoryName }} · {{ item.name }}</option></select><input v-model="line.name" required placeholder="Ремонт двери" /><input v-model="line.unit" placeholder="шт." /><input v-model.number="line.quantity" type="number" min="0.001" step="0.001" /><input v-model.number="line.price" type="number" min="0" step="0.01" /><strong>{{ ((Number(line.quantity) || 0) * (Number(line.price) || 0)).toFixed(2) }}</strong><button type="button" class="icon-button small-icon" aria-label="Удалить строку" @click="removeWorkOrderLine(index)">×</button></div><button type="button" class="link-button" @click="addWorkOrderLine">＋ Добавить работу</button></div><div class="work-order-parts"><div class="work-order-line work-order-line-head"><span>Запчасть</span><span>Артикул</span><span>Кол-во</span><span>Цена</span><span>Сумма</span><span></span></div><div v-for="(line, index) in workOrder.partLines" :key="line.id || `part-new-${index}`" class="work-order-part-line"><strong>{{ line.name }}</strong><span>{{ line.article || "—" }}</span><input v-model.number="line.quantity" type="number" min="0.001" step="0.001" /><input v-model.number="line.price" type="number" min="0" step="0.01" /><strong>{{ ((Number(line.quantity) || 0) * (Number(line.price) || 0)).toFixed(2) }}</strong><button type="button" class="icon-button small-icon" aria-label="Удалить строку запчасти" @click="removeWorkOrderPartLine(index)">×</button></div><div class="work-order-part-picker"><span>Добавить запчасть:</span><button v-for="part in selectedWorkOrderCar.parts" :key="part.id" type="button" class="link-button" :disabled="workOrder.partLines.some((line) => line.partId === part.id)" @click="addWorkOrderPartLine(part)">{{ part.name }}</button></div></div><div class="work-order-total">Итого: <strong>{{ workOrderTotal().toFixed(2) }}</strong></div><div class="modal-actions"><button type="button" class="button button-cloud dark-button" @click="workOrderVisible = false">Закрыть</button><button class="button button-cloud dark-button" @click="printWorkOrder">Печать</button><button class="button button-primary" @click="saveWorkOrder">Сохранить заказ-наряд</button></div></template></section></div>

    <div v-if="modal" class="stub-overlay" @click.self="modal = null"><section class="stub-modal"><button class="icon-button" aria-label="Закрыть" @click="modal = null">×</button><p class="eyebrow">Заглушка раздела</p><h2>{{ modal }}</h2><p>Внешний вид и место действия уже подготовлены. Реальная загрузка и сохранение данных будут подключены к backend следующим этапом.</p><button class="button button-primary" @click="modal = null">Понятно</button></section></div>
    <div v-if="photosVisible" class="stub-overlay" @click.self="photosVisible = false"><section class="data-modal photos-modal"><button class="icon-button" aria-label="Закрыть" @click="photosVisible = false">×</button><p class="eyebrow">Документы автомобиля</p><h2>Фото автомобиля</h2><p class="modal-subtitle">{{ photosCar?.number }} · {{ photosCar?.vehicle }}</p><div v-if="photosBusy" class="empty-state">Загружаем фотографии…</div><div v-else class="car-photo-grid"><div v-for="photo in carPhotos" :key="photo.id || photo.dataUrl" class="car-photo-card"><img :src="photo.dataUrl" :alt="photo.fileName || 'Фото автомобиля'" /><div><small>{{ photo.fileName }}</small><button type="button" class="link-button danger-link" @click="deleteCarPhoto(photo)">Удалить</button></div></div><p v-if="!carPhotos.length" class="empty-state">Фотографии пока не добавлены.</p></div></section></div>
    <div v-if="directoriesVisible" class="settings-overlay" @click.self="directoriesVisible = false"><section class="settings-screen"><button class="icon-button" aria-label="Закрыть" @click="directoriesVisible = false">×</button><p class="eyebrow">Управление программой</p><h2>Настройки</h2><p class="settings-subtitle">Справочники, контрагенты и рабочие параметры.</p><nav class="settings-tabs"><button type="button" :class="{ 'is-active': settingsTab === 'directories' }" @click="settingsTab = 'directories'">Справочники</button><button type="button" :class="{ 'is-active': settingsTab === 'counterparties' }" @click="settingsTab = 'counterparties'">Контрагенты</button><button type="button" disabled>Резервные копии</button><button type="button" disabled>История</button><button type="button" disabled>Безопасность</button></nav><template v-if="settingsTab === 'directories'"><div class="settings-section-head"><div><h3>Справочники</h3><p>Эти значения используются в карточках автомобилей, запчастях и документах.</p></div></div><div class="settings-directory-grid"><section class="settings-card"><div class="settings-card-head"><div><h3>Страховые компании</h3><p>Название для выбора в карточке автомобиля.</p></div><button type="button" class="settings-add" @click="addSettingsItem('insurers', settingsNewInsurer)">＋ Добавить</button></div><div class="settings-add-row"><input v-model="settingsNewInsurer" placeholder="Название страховой" @keyup.enter="addSettingsItem('insurers', settingsNewInsurer)" /></div><div class="settings-list"><div v-for="item in insurers" :key="item.id" class="settings-list-row"><input :value="item.name" @change="openDirectoryEdit(item); directoryType = 'insurers'; createDirectoryItem()" /><button type="button" class="settings-delete" @click="removeSettingsItem('insurers', item)">×</button></div></div></section><section class="settings-card"><div class="settings-card-head"><div><h3>Поставщики</h3><p>Выбор в строках запчастей.</p></div><button type="button" class="settings-add" @click="addSettingsItem('suppliers', settingsNewSupplier)">＋ Добавить</button></div><div class="settings-add-row"><input v-model="settingsNewSupplier" placeholder="Название поставщика" @keyup.enter="addSettingsItem('suppliers', settingsNewSupplier)" /></div><div class="settings-list"><div v-for="item in suppliers" :key="item.id" class="settings-list-row"><input :value="item.name" @change="openDirectoryEdit(item); directoryType = 'suppliers'; createDirectoryItem()" /><button type="button" class="settings-delete" @click="removeSettingsItem('suppliers', item)">×</button></div></div></section></div></template><template v-else><div class="settings-section-head"><div><h3>Контрагенты</h3><p>Контрагенты используются в генераторе документов для автомобилей вне реестра.</p></div></div><div class="settings-counterparty-grid"><form class="settings-card settings-counterparty-form" @submit.prevent="saveSettingsCounterparty"><h3>Новый контрагент</h3><label><span>Наименование или ФИО *</span><input v-model="settingsNewCounterparty.name" required placeholder="Например, ООО «Автотранс»" /></label><div class="settings-two-fields"><label><span>ИНН</span><input v-model="settingsNewCounterparty.inn" placeholder="ИНН организации или ИП" /></label><label><span>Телефон</span><input v-model="settingsNewCounterparty.phone" placeholder="+7 999 000-00-00" /></label></div><label><span>Адрес</span><input v-model="settingsNewCounterparty.address" placeholder="Город, улица, дом" /></label><label><span>Комментарий</span><input v-model="settingsNewCounterparty.note" placeholder="Необязательная внутренняя заметка" /></label><button class="button button-primary" type="submit">Сохранить контрагента</button></form><section class="settings-card"><h3>Сохранённые контрагенты</h3><p>{{ counterparties.length }} записей</p><div class="settings-list"><div v-for="item in counterparties" :key="item.id" class="counterparty-row"><div><strong>{{ item.name }}</strong><small>ИНН {{ item.inn || 'не указан' }}</small><small>{{ item.address || 'Адрес не указан' }}</small></div><div><button type="button" class="settings-edit" @click="openDirectoryEdit(item); directoryType = 'counterparties'">Изменить</button><button type="button" class="settings-delete-text" @click="removeSettingsCounterparty(item)">Удалить</button></div></div></div></section></div></template></section></div>
    <div v-if="directoriesVisible" class="settings-overlay-v2" @click.self="directoriesVisible = false"><section class="settings-screen"><button class="icon-button" aria-label="Закрыть" @click="directoriesVisible = false">×</button><p class="eyebrow">Управление программой</p><h2>Настройки</h2><p class="settings-subtitle">Справочники, контрагенты, мастера и рабочие параметры.</p><nav class="settings-tabs"><button type="button" :class="{ 'is-active': settingsTab === 'directories' }" @click="settingsTab = 'directories'">Справочники</button><button type="button" :class="{ 'is-active': settingsTab === 'counterparties' }" @click="settingsTab = 'counterparties'">Контрагенты</button><button type="button" disabled>Резервные копии</button><button type="button" disabled>История</button><button type="button" disabled>Безопасность</button></nav><template v-if="settingsTab === 'directories'"><div class="settings-section-head"><div><h3>Справочники</h3><p>Значения используются в карточках автомобилей, запчастях и документах.</p></div></div><div class="settings-directory-grid"><section class="settings-card"><div class="settings-card-head"><div><h3>Страховые компании</h3><p>Название и адрес/реквизиты одной строкой.</p></div></div><div class="settings-add-row settings-insurer-add"><input v-model="settingsNewInsurer" placeholder="Название страховой" /><input v-model="directoryForm.note" placeholder="Адрес и реквизиты" /><button type="button" class="settings-add" @click="addSettingsItem('insurers', settingsNewInsurer)">＋ Добавить</button></div><div class="settings-list"><div v-for="item in insurers" :key="item.id" class="settings-list-row"><input :value="item.name" /><input :value="item.legalDetails || ''" placeholder="Адрес и реквизиты" /><button type="button" class="settings-delete" @click="removeSettingsItem('insurers', item)">×</button></div></div></section><section class="settings-card"><div class="settings-card-head"><div><h3>Поставщики</h3><p>Только названия для строк запчастей.</p></div></div><div class="settings-add-row"><input v-model="settingsNewSupplier" placeholder="Название поставщика" /><button type="button" class="settings-add" @click="addSettingsItem('suppliers', settingsNewSupplier)">＋ Добавить</button></div><div class="settings-list"><div v-for="item in suppliers" :key="item.id" class="settings-list-row"><input :value="item.name" /><button type="button" class="settings-delete" @click="removeSettingsItem('suppliers', item)">×</button></div></div></section><section class="settings-card"><div class="settings-card-head"><div><h3>Работы</h3><p>Название и расшифровка в нормо-часах.</p></div></div><div class="settings-add-row"><input v-model="settingsNewWork.name" placeholder="Название работы" /><input v-model.number="settingsNewWork.normHours" type="number" min="0" step="0.01" placeholder="Нормо-часы" /><button type="button" class="settings-add" @click="saveSettingsWork">＋ Добавить</button></div><div class="settings-list"><div v-for="item in workCatalog" :key="item.id" class="settings-list-row"><span>{{ item.name }}</span><strong>{{ item.normHours || 0 }} н/ч</strong><button type="button" class="settings-delete" @click="removeSettingsWork(item)">×</button></div></div></section><section class="settings-card"><div class="settings-card-head"><div><h3>Исполнители / мастера</h3><p>Мастера, доступные в карточке автомобиля.</p></div></div><div class="settings-add-row"><input v-model="settingsNewMaster.code" placeholder="Код" /><input v-model="settingsNewMaster.shortName" placeholder="Имя мастера" /><button type="button" class="settings-add" @click="saveSettingsMaster">＋ Добавить</button></div><div class="settings-list"><div v-for="item in contractors" :key="item.id" class="settings-list-row"><span>{{ item.shortName }}</span><code>{{ item.code }}</code><button type="button" class="settings-delete" @click="removeSettingsMaster(item)">×</button></div></div></section></div></template><template v-else><div class="settings-section-head"><div><h3>Контрагенты</h3><p>Контрагенты используются в генераторе документов для автомобилей вне реестра.</p></div></div><div class="settings-counterparty-grid"><form class="settings-card settings-counterparty-form" @submit.prevent="saveSettingsCounterparty"><h3>Новый контрагент</h3><p>Наименование или ФИО *</p><input v-model="settingsNewCounterparty.name" required placeholder="Например, ООО «Автотранс»" /><div class="settings-two-fields"><label><span>ИНН</span><input v-model="settingsNewCounterparty.inn" placeholder="ИНН организации или ИП" /></label><label><span>Телефон</span><input v-model="settingsNewCounterparty.phone" placeholder="+7 999 000-00-00" /></label></div><label><span>Адрес</span><input v-model="settingsNewCounterparty.address" placeholder="Город, улица, дом" /></label><label><span>Комментарий</span><input v-model="settingsNewCounterparty.note" placeholder="Необязательная внутренняя заметка" /></label><button class="button button-primary" type="submit">Сохранить контрагента</button></form><section class="settings-card"><h3>Сохранённые контрагенты</h3><p>{{ counterparties.length }} записей</p><div class="settings-list"><div v-for="item in counterparties" :key="item.id" class="counterparty-row"><div><strong>{{ item.name }}</strong><small>ИНН {{ item.inn || 'не указан' }}</small><small>{{ item.address || 'Адрес не указан' }}</small></div><button type="button" class="settings-delete-text" @click="removeSettingsCounterparty(item)">Удалить</button></div></div></section></div></template></section></div>
    <button v-if="carFormVisible && editingCar" type="button" class="repair-cases-button button button-primary" @click="openRepairCases(editingCar)">Страховые случаи</button>
    <div v-if="repairCasesVisible" class="stub-overlay" @click.self="repairCasesVisible = false"><section class="data-modal repair-cases-modal"><button class="icon-button" aria-label="Закрыть" @click="repairCasesVisible = false">×</button><p class="eyebrow">Автомобиль №{{ repairCaseCar?.number }}</p><h2>Страховые случаи</h2><div v-if="repairCasesBusy" class="empty-state">Загружаем случаи…</div><template v-else><div class="repair-case-list"><div v-for="item in repairCases" :key="item.id" class="repair-case-card"><div><strong>Случай №{{ item.caseNumber }}</strong><small>{{ item.claimNumber || 'Номер дела не указан' }} · {{ item.status }}</small><small>{{ item.insuredPerson || 'Страхователь не указан' }}</small></div><div><button type="button" class="settings-edit" @click="openRepairCasePhotos(item)">Фото</button><button type="button" class="settings-edit" @click="startRepairCase(item)">Изменить</button><button type="button" class="settings-delete-text" @click="deleteRepairCase(item)">Удалить</button></div></div></div><button type="button" class="button button-primary" @click="startRepairCase()">＋ Новый страховой случай</button><form v-if="editingRepairCase || repairCaseForm.caseNumber" class="data-form-grid repair-case-form" @submit.prevent="saveRepairCase"><label><span>Номер случая *</span><input v-model="repairCaseForm.caseNumber" required /></label><label><span>Статус *</span><select v-model="repairCaseForm.status" required><option value="OPEN">Открыт</option><option value="IN_REPAIR">В ремонте</option><option value="READY">Готов</option><option value="CLOSED">Закрыт</option></select></label><label><span>Страхователь</span><input v-model="repairCaseForm.insuredPerson" /></label><label><span>Номер дела</span><input v-model="repairCaseForm.claimNumber" /></label><label><span>Страховая</span><select v-model="repairCaseForm.insurerId"><option value="">Не выбрана</option><option v-for="item in insurers" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select></label><label><span>Мастер</span><select v-model="repairCaseForm.contractorId"><option value="">Не выбран</option><option v-for="item in contractors" :key="item.id" :value="String(item.id)">{{ item.shortName }}</option></select></label><label><span>Смена</span><select v-model="repairCaseForm.shiftId"><option value="">Не выбрана</option><option v-for="item in shifts" :key="item.id" :value="String(item.id)">{{ item.name }}</option></select></label><label><span>Дата приёмки</span><input v-model="repairCaseForm.acceptedAt" type="date" /></label><div class="modal-actions form-wide"><button class="button button-primary" type="submit">Сохранить случай</button></div></form></template></section></div>
    <div v-if="toast" class="toast">{{ toast }}</div>
  </template>
    <div v-if="workOrderRegistryVisible" class="stub-overlay" @click.self="workOrderRegistryVisible = false"><section class="data-modal registry-modal"><button class="icon-button" aria-label="Закрыть" @click="workOrderRegistryVisible = false">×</button><p class="eyebrow">Реестр документов</p><h2>Заказ-наряды</h2><div class="registry-actions"><button class="button button-cloud dark-button" type="button" @click="downloadWorkOrderCsv" :disabled="!workOrderRegistry.length">Скачать CSV</button></div><div v-if="workOrderRegistryBusy" class="empty-state">Загружаем реестр…</div><div v-else-if="workOrderRegistryError" class="empty-state">{{ workOrderRegistryError }}</div><div v-else class="registry-table"><div class="registry-row registry-head"><span>№</span><span>Автомобиль</span><span>Заказчик</span><span>Дата</span><span>Статус</span><span>Итого</span><span>Документы</span></div><div v-for="item in workOrderRegistry" :key="item.id" class="registry-row"><span>{{ item.orderNumber || `#${item.id}` }}</span><span>{{ item.vehicleName || 'Без автомобиля' }}<small>{{ item.registrationNumber || '—' }}</small></span><span>{{ item.customer || '—' }}</span><span>{{ item.documentDate || '—' }}</span><span>{{ item.status }}</span><strong>{{ Number(item.total || 0).toFixed(2) }}</strong><span>{{ item.invoiceNumber || '—' }} · {{ item.actNumber || '—' }}</span></div><p v-if="!workOrderRegistry.length" class="empty-state">Заказ-нарядов пока нет.</p></div></section></div>
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
.settings-overlay-v2 { position: fixed; inset: 0; z-index: 40; overflow: auto; padding: 36px; background: #eef4f2; }
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
